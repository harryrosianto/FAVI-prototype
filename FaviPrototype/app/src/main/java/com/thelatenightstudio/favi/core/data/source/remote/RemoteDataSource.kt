package com.thelatenightstudio.favi.core.data.source.remote

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
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

}