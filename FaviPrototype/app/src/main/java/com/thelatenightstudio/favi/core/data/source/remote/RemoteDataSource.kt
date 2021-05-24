package com.thelatenightstudio.favi.core.data.source.remote

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

class RemoteDataSource(private val firebaseAuth: FirebaseAuth) {

    @ExperimentalCoroutinesApi
    fun createUser(email: String, password: String): Flow<ApiResponse<Boolean>> =
        callbackFlow {
            val callback =
                OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful)
                        trySend(ApiResponse.Success(task.isSuccessful))
                    else trySend(ApiResponse.Error(task.exception?.message))
                }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(callback)

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

}