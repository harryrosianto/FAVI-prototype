package com.thelatenightstudio.favi.signin

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.core.utils.InternetHelper.isConnected
import com.thelatenightstudio.favi.core.utils.LiveDataHelper.observeOnce
import com.thelatenightstudio.favi.core.utils.ObserverHelper.getSignInObserver
import com.thelatenightstudio.favi.core.utils.ToastHelper.showToast
import com.thelatenightstudio.favi.databinding.ActivitySignInBinding
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch

object SignInBiometric {

    fun SignInActivity.getBiometricPrompt(
        binding: ActivitySignInBinding,
        viewModel: SignInViewModel
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(this@getBiometricPrompt)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)

                lifecycleScope.launch {
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        showToast(getString(R.string.biometric_error))
                    }
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()

                lifecycleScope.launch {
                    showToast(getString(R.string.failed))
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                binding.progressBar.visibility = View.VISIBLE
                lifecycleScope.launch {
                    if (isConnected()) {
                        (IO){ viewModel.signInWithBiometric() }
                            .observeOnce(this@getBiometricPrompt, getSignInObserver(binding))
                    } else {
                        binding.progressBar.visibility = View.GONE
                        showToast(getString(R.string.no_internet))
                    }
                }
            }
        }

        return BiometricPrompt(this@getBiometricPrompt, executor, callback)
    }

    fun SignInActivity.getPromptInfo(): BiometricPrompt.PromptInfo =
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.sign_in_with_biometric))
            .setSubtitle(getString(R.string.biometric_auth))

            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            // Authenticate without requiring the user to press a "confirm" button after satisfying the biometric check
            .setConfirmationRequired(false)
            .setNegativeButtonText(getString(R.string.biometric_error_button))
            .build()

    fun SignInActivity.biometricAuthProcess(
        viewModel: SignInViewModel,
        biometricPrompt: BiometricPrompt,
        promptInfo: BiometricPrompt.PromptInfo
    ) {
        val biometricManager = BiometricManager.from(this@biometricAuthProcess)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                lifecycleScope.launch {
                    if ((IO){ viewModel.isBiometricActive() })
                        biometricPrompt.authenticate(promptInfo)
                    else showToast(getString(R.string.biometric_auth_is_off))
                }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                lifecycleScope.launch { showToast(getString(R.string.biometric_error_no_hardware)) }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                lifecycleScope.launch { showToast(getString(R.string.biometric_error_hw_unavailable)) }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                lifecycleScope.launch { showToast(getString(R.string.biometric_error_none_enrolled)) }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val enrollIntent =
                        Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                            putExtra(
                                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                BiometricManager.Authenticators.BIOMETRIC_STRONG
                            )
                        }
                    startActivityForResult(
                        enrollIntent,
                        SignInActivity.BIOMETRIC_SETUP_REQUEST_CODE
                    )
                } else {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
        }
    }

}

