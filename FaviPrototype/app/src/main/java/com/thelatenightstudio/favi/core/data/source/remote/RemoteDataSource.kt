package com.thelatenightstudio.favi.core.data.source.remote

import android.net.Uri
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class RemoteDataSource(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val userVoiceInputBucket: StorageReference
) {

    companion object {
        const val USERS = "users"
        const val BALANCE = "balance"
        const val PREDICTION = "prediction"

        const val BALANCE_NOT_ENOUGH = "You don't have enough balance to transfer!"
    }

    @ExperimentalCoroutinesApi
    fun createUser(email: String, password: String): Flow<ApiResponse<Boolean>> =
        callbackFlow {
            val user = User()

            val firestoreCallback =
                OnCompleteListener<Void> { task ->
                    if (task.isSuccessful)
                        trySend(ApiResponse.Success(task.isSuccessful))
                    else
                        trySend(ApiResponse.Error(task.exception?.message))
                }

            val authCallback =
                OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        val uid = task.result?.user?.uid
                        user.uid = uid ?: ""
                        user.email = email

                        firebaseFirestore.collection(USERS).document(email).set(user)
                            .addOnCompleteListener(firestoreCallback)
                    } else trySend(ApiResponse.Error(task.exception?.message))
                }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(authCallback)

            awaitClose { }
        }.flowOn(Dispatchers.IO)

    @ExperimentalCoroutinesApi
    fun signIn(email: String, password: String): Flow<ApiResponse<Boolean>> =
        callbackFlow {
            val callback =
                OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful)
                        trySend(ApiResponse.Success(task.isSuccessful))
                    else trySend(ApiResponse.Error(task.exception?.message))
                }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(callback)

            awaitClose { }
        }.flowOn(Dispatchers.IO)

    fun signOut() {
        firebaseAuth.signOut()
    }

    @ExperimentalCoroutinesApi
    fun getIdToken(): Flow<ApiResponse<String>> =
        callbackFlow {
            val user = firebaseAuth.currentUser
            if (user != null) {
                val callback =
                    OnCompleteListener<GetTokenResult> { task ->
                        if (task.isSuccessful)
                            trySend(ApiResponse.Success(task.result?.token ?: ""))
                        else trySend(ApiResponse.Error(task.exception?.message))
                    }

                user.getIdToken(false)
                    .addOnCompleteListener(callback)
            } else {
                send(ApiResponse.Error(null))
            }
            awaitClose { }
        }.flowOn(Dispatchers.IO)

    @ExperimentalCoroutinesApi
    fun signInWithCustomToken(token: String): Flow<ApiResponse<Boolean>> =
        callbackFlow {
            val callback =
                OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful)
                        trySend(ApiResponse.Success(task.isSuccessful))
                    else trySend(ApiResponse.Error(task.exception?.message))
                }

            firebaseAuth.signInWithCustomToken(token)
                .addOnCompleteListener(callback)

            awaitClose { }
        }.flowOn(Dispatchers.IO)

    @ExperimentalCoroutinesApi
    fun getDataOfCurrentUser(): Flow<ApiResponse<User>> =
        callbackFlow {
            val email = firebaseAuth.currentUser?.email ?: ""
            val callback =
                OnCompleteListener<DocumentSnapshot> { task ->
                    val docSnap = task.result
                    val user = docSnap?.toObject<User>()
                    if (task.isSuccessful && user != null) {
                        trySend(ApiResponse.Success(user))
                    } else trySend(ApiResponse.Error(task.exception?.message))
                }
            val docRef = firebaseFirestore.collection(USERS).document(email)
            docRef.get().addOnCompleteListener(callback)

            awaitClose { }
        }.flowOn(Dispatchers.IO)

    @ExperimentalCoroutinesApi
    fun increaseBalanceOfCurrentUser(requestAmount: Double): Flow<ApiResponse<Boolean>> =
        callbackFlow {
            val email = firebaseAuth.currentUser?.email ?: ""
            val callback =
                OnCompleteListener<Nothing> { task ->
                    if (task.isSuccessful)
                        trySend(ApiResponse.Success(task.isSuccessful))
                    else
                        trySend(ApiResponse.Error(task.exception?.message))
                }

            val docRef = firebaseFirestore.collection(USERS).document(email)
            firebaseFirestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val balance = snapshot.getDouble(BALANCE)
                val totalBalance = balance?.plus(requestAmount)
                transaction.update(docRef, BALANCE, totalBalance)

                null
            }.addOnCompleteListener(callback)

            awaitClose { }
        }.flowOn(Dispatchers.IO)

    @ExperimentalCoroutinesApi
    fun getRealtimeUpdatesOfCurrentUser(): Flow<ApiResponse<User>> =
        callbackFlow {
            val email = firebaseAuth.currentUser?.email ?: ""
            val docRef = firebaseFirestore.collection(USERS).document(email)
            val listener = docRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(ApiResponse.Error(e.message))
                    return@addSnapshotListener
                }

                val user = snapshot?.toObject<User>()
                if (snapshot != null && snapshot.exists() && user != null) {
                    trySend(ApiResponse.Success(user))
                } else {
                    trySend(ApiResponse.Empty)
                }
            }

            awaitClose { listener.remove() }
        }.flowOn(Dispatchers.IO)

    @ExperimentalCoroutinesApi
    fun transferBalanceToAnotherUser(
        targetEmail: String,
        requestAmount: Double
    ): Flow<ApiResponse<Boolean>> =
        callbackFlow {
            val email = firebaseAuth.currentUser?.email ?: ""
            val callback =
                OnCompleteListener<Nothing> { task ->
                    if (task.isSuccessful)
                        trySend(ApiResponse.Success(task.isSuccessful))
                    else
                        trySend(ApiResponse.Error(task.exception?.message))
                }

            val userDocRef = firebaseFirestore.collection(USERS).document(email)
            userDocRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val balance = task.result?.getDouble(BALANCE)
                    val balanceAfterTransfer = balance?.minus(requestAmount)
                    if (balanceAfterTransfer != null) {
                        val isBalanceEnough = balanceAfterTransfer > 0
                        if (isBalanceEnough) {
                            executeTransferBalanceTransaction(
                                targetEmail,
                                userDocRef,
                                requestAmount,
                                callback
                            )
                        } else trySend(ApiResponse.Error(BALANCE_NOT_ENOUGH))
                    }
                } else
                    trySend(ApiResponse.Error(task.exception?.message))
            }

            awaitClose { }
        }.flowOn(Dispatchers.IO)

    private fun executeTransferBalanceTransaction(
        targetEmail: String,
        userDocRef: DocumentReference,
        requestAmount: Double,
        callback: OnCompleteListener<Nothing>
    ) {
        val targetDocRef =
            firebaseFirestore.collection(USERS).document(targetEmail)

        firebaseFirestore.runTransaction { transaction ->
            val userSnapshot = transaction.get(userDocRef)
            val userBalance = userSnapshot.getDouble(BALANCE)
            val userTotalBalance = userBalance?.minus(requestAmount)
            val targetSnapshot = transaction.get(targetDocRef)
            val targetBalance = targetSnapshot.getDouble(BALANCE)
            val targetTotalBalance = targetBalance?.plus(requestAmount)

            transaction.update(userDocRef, BALANCE, userTotalBalance)
            transaction.update(targetDocRef, BALANCE, targetTotalBalance)
            null
        }.addOnCompleteListener(callback)
    }

    @ExperimentalCoroutinesApi
    fun uploadFile(filePath: String): Flow<ApiResponse<Boolean>> =
        callbackFlow {
            val fileName = "${firebaseAuth.currentUser?.email}.wav"
            val uri = Uri.fromFile(File(filePath))
            val voiceRecordingRef = userVoiceInputBucket.child(fileName)

            val callback =
                OnCompleteListener<UploadTask.TaskSnapshot> { task ->
                    if (task.isSuccessful)
                        trySend(ApiResponse.Success(task.isSuccessful))
                    else
                        trySend(ApiResponse.Error(task.exception?.message))
                }
            voiceRecordingRef.putFile(uri).addOnCompleteListener(callback)

            awaitClose { }
        }.flowOn(Dispatchers.IO)

    fun resetPredictionFieldOfCurrentUser() {
        val email = firebaseAuth.currentUser?.email ?: ""

        val docRef = firebaseFirestore.collection(USERS).document(email)
        firebaseFirestore.runTransaction { transaction ->
            val emptyString = ""
            transaction.update(docRef, PREDICTION, emptyString)

            null
        }
    }

}