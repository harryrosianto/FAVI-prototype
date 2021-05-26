package com.thelatenightstudio.favi.signup

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.utils.EditTextHelper.showEditTextExistAlert
import com.thelatenightstudio.favi.core.utils.InternetHelper.isConnected
import com.thelatenightstudio.favi.core.utils.ObservableHelper.getEmailStream
import com.thelatenightstudio.favi.core.utils.ObservableHelper.getInvalidFieldsStream
import com.thelatenightstudio.favi.core.utils.ObservableHelper.getPasswordConfirmationStream
import com.thelatenightstudio.favi.core.utils.ObservableHelper.getPasswordStream
import com.thelatenightstudio.favi.core.utils.ToastHelper.showToast
import com.thelatenightstudio.favi.databinding.ActivitySignUpBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private val viewModel: SignUpViewModel by viewModel()

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.parentLayout.requestFocus()

        val emailStream =
            getEmailStream(binding.edEmail)
        emailStream.subscribe {
            showEditTextExistAlert(
                binding.edEmail,
                it,
                getString(R.string.email_not_valid)
            )
        }

        val passwordStream =
            getPasswordStream(binding.edPassword)
        passwordStream.subscribe {
            showEditTextExistAlert(
                binding.edPassword,
                it,
                getString(R.string.password_not_valid)
            )
        }

        val passwordConfirmationStream =
            getPasswordConfirmationStream(
                binding.edPassword,
                binding.edConfirmPassword
            )
        passwordConfirmationStream.subscribe {
            showEditTextExistAlert(
                binding.edConfirmPassword,
                it,
                getString(R.string.password_not_same)
            )
        }

        val invalidFieldsStream =
            getInvalidFieldsStream(
                emailStream,
                passwordStream,
                passwordConfirmationStream
            )
        invalidFieldsStream.subscribe { isValid -> binding.btnSignUp.isEnabled = isValid }

        binding.btnSignUp.setOnClickListener {
            lifecycleScope.launch {
                if (isConnected()) {
                    binding.progressBar.visibility = View.VISIBLE

                    val email = binding.edEmail.text.toString()
                    val password = binding.edPassword.text.toString()

                    (IO) { viewModel.createUser(email, password) }
                        .observe(this@SignUpActivity, getCreateUserObservable())
                } else {
                    showToast(getString(R.string.no_internet))
                }
            }
        }
    }

    private fun getCreateUserObservable(): Observer<ApiResponse<Boolean>> =
        Observer<ApiResponse<Boolean>> { response ->
            val toastText = when (response) {
                is ApiResponse.Success -> {
                    getString(R.string.account_created)
                }
                is ApiResponse.Error -> {
                    response.errorMessage
                        ?: getString(R.string.error)
                }
                is ApiResponse.Empty -> {
                    getString(R.string.empty)
                }
            }

            lifecycleScope.launch {
                showToast(toastText)
                binding.progressBar.visibility = View.GONE

                delay(1000)
                if (response is ApiResponse.Success) {
                    onBackPressed()
                }
            }
        }

}