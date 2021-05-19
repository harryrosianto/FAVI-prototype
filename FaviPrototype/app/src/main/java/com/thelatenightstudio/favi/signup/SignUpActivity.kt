package com.thelatenightstudio.favi.signup

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding2.widget.RxTextView
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.utils.InternetHelper
import com.thelatenightstudio.favi.databinding.ActivitySignUpBinding
import io.reactivex.Observable
import org.koin.android.ext.android.inject

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private val viewModel: SignUpViewModel by inject()

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val emailStream = getEmailStream()
        emailStream.subscribe { showEmailExistAlert(it) }

        val passwordStream = getPasswordStream()
        passwordStream.subscribe { showPasswordMinimalAlert(it) }

        val passwordConfirmationStream = getPasswordConfirmationStream()
        passwordConfirmationStream.subscribe { showPasswordConfirmationAlert(it) }

        val invalidFieldsStream =
            getInvalidFieldsStream(emailStream, passwordStream, passwordConfirmationStream)
        invalidFieldsStream.subscribe { isValid -> binding.btnSignUp.isEnabled = isValid }

        binding.btnSignUp.setOnClickListener {
            if (InternetHelper.isConnected()) {
                binding.progressBar.visibility = View.VISIBLE

                val email = binding.edEmail.text.toString()
                val password = binding.edPassword.text.toString()

                viewModel.createUser(email, password).observe(this, { response ->
                    val toastText = when (response) {
                        is ApiResponse.Success -> {
                            "SANJOU!"
                        }
                        is ApiResponse.Error -> {
                            response.errorMessage
                                ?: resources.getString(R.string.error)
                        }
                        is ApiResponse.Empty -> {
                            resources.getString(R.string.empty)
                        }
                    }
                    Toast.makeText(
                        this,
                        toastText,
                        Toast.LENGTH_SHORT
                    ).show()

                    binding.progressBar.visibility = View.GONE
                })
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.no_internet),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun getInvalidFieldsStream(
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

    private fun getEmailStream(): Observable<Boolean> =
        RxTextView.textChanges(binding.edEmail)
            .skipInitialValue()
            .map { email ->
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }

    private fun showEmailExistAlert(isNotValid: Boolean) {
        binding.edEmail.error = if (isNotValid) getString(R.string.email_not_valid) else null
    }

    private fun getPasswordStream(): Observable<Boolean> =
        RxTextView.textChanges(binding.edPassword)
            .skipInitialValue()
            .map { password ->
                password.length < 8
            }

    private fun showPasswordMinimalAlert(isNotValid: Boolean) {
        binding.edPassword.error = if (isNotValid) getString(R.string.password_not_valid) else null
    }

    private fun getPasswordConfirmationStream(): Observable<Boolean> =
        Observable.merge(
            RxTextView.textChanges(binding.edPassword)
                .map { password ->
                    password.toString() != binding.edConfirmPassword.text.toString()
                },
            RxTextView.textChanges(binding.edConfirmPassword)
                .map { confirmPassword ->
                    confirmPassword.toString() != binding.edPassword.text.toString()
                }
        )

    private fun showPasswordConfirmationAlert(isNotValid: Boolean) {
        binding.edConfirmPassword.error =
            if (isNotValid) getString(R.string.password_not_same) else null
    }

}