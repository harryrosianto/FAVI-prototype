package com.thelatenightstudio.favi.core.data.source.remote

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

class RemoteDataSource(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
) {

    companion object {
        const val USERS = "users"
        const val BALANCE = "balance"
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
            val transac = firebaseFirestore.runTransaction { transaction ->
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

}