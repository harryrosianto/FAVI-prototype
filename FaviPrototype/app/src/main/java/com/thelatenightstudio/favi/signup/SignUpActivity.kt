package com.thelatenightstudio.favi.signup

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.utils.AppCoroutineScopes
import com.thelatenightstudio.favi.core.utils.EditTextHelper
import com.thelatenightstudio.favi.core.utils.InternetHelper
import com.thelatenightstudio.favi.core.utils.ObservableHelper
import com.thelatenightstudio.favi.databinding.ActivitySignUpBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private val viewModel: SignUpViewModel by viewModel()
    private val scopes: AppCoroutineScopes by inject()

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val emailStream =
            ObservableHelper.getEmailStream(binding.edEmail)
        emailStream.subscribe {
            EditTextHelper.showEditTextExistAlert(
                binding.edEmail,
                it,
                getString(R.string.email_not_valid)
            )
        }

        val passwordStream =
            ObservableHelper.getPasswordStream(binding.edPassword)
        passwordStream.subscribe {
            EditTextHelper.showEditTextExistAlert(
                binding.edPassword,
                it,
                getString(R.string.password_not_valid)
            )
        }

        val passwordConfirmationStream =
            ObservableHelper.getPasswordConfirmationStream(
                binding.edPassword,
                binding.edConfirmPassword
            )
        passwordConfirmationStream.subscribe {
            EditTextHelper.showEditTextExistAlert(
                binding.edConfirmPassword,
                it,
                getString(R.string.password_not_same)
            )
        }

        val invalidFieldsStream =
            ObservableHelper.getInvalidFieldsStream(
                emailStream,
                passwordStream,
                passwordConfirmationStream
            )
        invalidFieldsStream.subscribe { isValid -> binding.btnSignUp.isEnabled = isValid }

        binding.btnSignUp.setOnClickListener {
            if (InternetHelper.isConnected()) {
                binding.progressBar.visibility = View.VISIBLE

                val email = binding.edEmail.text.toString()
                val password = binding.edPassword.text.toString()

                viewModel.createUser(email, password).observe(this, { response ->
                    val toastText = when (response) {
                        is ApiResponse.Success -> {
                            resources.getString(R.string.account_created)
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
                    scopes.main().launch {
                        delay(1000)
                        if (response is ApiResponse.Success) {
                            onBackPressed()
                        }
                    }
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

}