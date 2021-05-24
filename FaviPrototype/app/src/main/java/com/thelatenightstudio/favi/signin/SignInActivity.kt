package com.thelatenightstudio.favi.signin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.security.SharedPreferencesManager
import com.thelatenightstudio.favi.core.utils.EditTextHelper
import com.thelatenightstudio.favi.core.utils.InternetHelper
import com.thelatenightstudio.favi.core.utils.ObservableHelper
import com.thelatenightstudio.favi.core.utils.ToastHelper
import com.thelatenightstudio.favi.databinding.ActivitySignInBinding
import com.thelatenightstudio.favi.ui.MainMenuActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignInActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE = 101
    }

    private lateinit var binding: ActivitySignInBinding

    private val viewModel: SignInViewModel by viewModel()
    private val spmanager: SharedPreferencesManager by inject()

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.parentLayout.requestFocus()

        biometricPrompt = createBiometricPrompt()
        promptInfo = createPromptInfo()

        binding.btnBiometric.setOnClickListener {
            biometricAuthProcess()
        }

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

        val invalidFieldsStream =
            ObservableHelper.getInvalidFieldsStream(emailStream, passwordStream)
        invalidFieldsStream.subscribe { isValid -> binding.btnSignUp.isEnabled = isValid }

        binding.btnSignUp.setOnClickListener {
            if (InternetHelper.isConnected()) {
                binding.progressBar.visibility = View.VISIBLE

                val email = binding.edEmail.text.toString()
                val password = binding.edPassword.text.toString()

                viewModel.signIn(email, password).observe(
                    this, getSignInObserver(email, password)
                )
            } else {
                ToastHelper.showToast(this, getString(R.string.no_internet))
            }
        }
    }

    private fun getSignInObserver(
        username: String,
        password: String
    ) =
        Observer<ApiResponse<Boolean>> { response ->
            val toastText = when (response) {
                is ApiResponse.Success -> {
                    getString(R.string.successful)
                }
                is ApiResponse.Error -> {
                    response.errorMessage
                        ?: getString(R.string.error)
                }
                is ApiResponse.Empty -> {
                    getString(R.string.empty)
                }
            }
            ToastHelper.showToast(this, toastText)

            binding.progressBar.visibility = View.GONE
            lifecycleScope.launch {
                delay(1000)
                if (response is ApiResponse.Success) {
                    spmanager.signIn(username, password)

                    val intent = Intent(this@SignInActivity, MainMenuActivity::class.java)
                    intent.putExtra(MainMenuActivity.EXTRA_STRING, "SIGN IN BERHASIL")
                    startActivity(intent)
                }
            }
        }

    private fun getSignInObserver() =
        Observer<ApiResponse<Boolean>> { response ->
            val toastText = when (response) {
                is ApiResponse.Success -> {
                    getString(R.string.successful)
                }
                is ApiResponse.Error -> {
                    response.errorMessage
                        ?: getString(R.string.error)
                }
                is ApiResponse.Empty -> {
                    getString(R.string.empty)
                }
            }
            ToastHelper.showToast(this, toastText)

            binding.progressBar.visibility = View.GONE
            lifecycleScope.launch {
                delay(1000)
                if (response is ApiResponse.Success) {
                    val intent = Intent(this@SignInActivity, MainMenuActivity::class.java)
                    intent.putExtra(MainMenuActivity.EXTRA_STRING, "SIGN IN BERHASIL")
                    startActivity(intent)
                }
            }
        }

    private fun createBiometricPrompt(): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(this)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)

                Log.d("Cek", "$errorCode :: $errString")

                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    ToastHelper.showToast(this@SignInActivity, getString(R.string.biometric_error))
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()

                ToastHelper.showToast(this@SignInActivity, getString(R.string.failed))
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                if (InternetHelper.isConnected()) {
                    binding.progressBar.visibility = View.VISIBLE

                    val email = spmanager.getString(SharedPreferencesManager.USERNAME)
                    val password = spmanager.getString(SharedPreferencesManager.PASSWORD)

                    viewModel.signIn(email, password).observe(
                        this@SignInActivity, getSignInObserver()
                    )
                } else {
                    ToastHelper.showToast(this@SignInActivity, getString(R.string.no_internet))
                }
            }
        }

        return BiometricPrompt(this, executor, callback)
    }

    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.sign_in_with_biometric))
            .setSubtitle(getString(R.string.biometric_auth))

            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            // Authenticate without requiring the user to press a "confirm" button after satisfying the biometric check
            .setConfirmationRequired(false)
            .setNegativeButtonText(getString(R.string.biometric_error_button))
            .build()
    }

    private fun biometricAuthProcess() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                if (spmanager.getBoolean(SharedPreferencesManager.BIOMETRIC_AUTH))
                    biometricPrompt.authenticate(promptInfo)
                else ToastHelper.showToast(
                    this@SignInActivity,
                    getString(R.string.biometric_auth_is_off)
                )

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                ToastHelper.showToast(
                    this@SignInActivity,
                    getString(R.string.biometric_error_no_hardware)
                )

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                ToastHelper.showToast(
                    this@SignInActivity,
                    getString(R.string.biometric_error_hw_unavailable)
                )

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                ToastHelper.showToast(
                    this@SignInActivity,
                    getString(R.string.biometric_error_none_enrolled)
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val enrollIntent =
                        Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                            putExtra(
                                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                BiometricManager.Authenticators.BIOMETRIC_STRONG
                            )
                        }
                    startActivityForResult(enrollIntent, REQUEST_CODE)
                } else {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
        }
    }

}