package com.thelatenightstudio.favi.core.utils

import android.util.Patterns
import android.widget.EditText
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable

object ObservableHelper {

    fun getEmailStream(edEmail: EditText): Observable<Boolean> =
        RxTextView.textChanges(edEmail)
            .skipInitialValue()
            .map { email ->
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }

    fun getPasswordStream(edPassword: EditText): Observable<Boolean> =
        RxTextView.textChanges(edPassword)
            .skipInitialValue()
            .map { password ->
                password.length < 8
            }

    fun getPasswordConfirmationStream(
        edPassword: EditText,
        edConfirmPassword: EditText
    ): Observable<Boolean> =
        Observable.merge(
            RxTextView.textChanges(edPassword)
                .map { password ->
                    password.toString() != edConfirmPassword.text.toString()
                },
            RxTextView.textChanges(edConfirmPassword)
                .map { confirmPassword ->
                    confirmPassword.toString() != edPassword.text.toString()
                }
        )

    fun getInvalidFieldsStream(
        emailStream: Observable<Boolean>,
        passwordStream: Observable<Boolean>,
        passwordConfirmationStream: Observable<Boolean>
    ) =
        Observable.combineLatest(
            emailStream,
            passwordStream,
            passwordConfirmationStream,
            { emailInvalid: Boolean, passwordInvalid: Boolean, passwordConfirmationInvalid: Boolean ->
                !emailInvalid && !passwordInvalid && !passwordConfirmationInvalid
            })

    fun getInvalidFieldsStream(
        emailStream: Observable<Boolean>,
        passwordStream: Observable<Boolean>
    ) =
        Observable.combineLatest(
            emailStream,
            passwordStream,
            { emailInvalid: Boolean, passwordInvalid: Boolean ->
                !emailInvalid && !passwordInvalid
            })
}