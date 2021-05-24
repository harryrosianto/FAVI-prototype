package com.thelatenightstudio.favi.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.core.security.CryptographyManager
import com.thelatenightstudio.favi.databinding.ActivityHomeBinding
import com.thelatenightstudio.favi.signin.SignInActivity
import com.thelatenightstudio.favi.signup.SignUpActivity
import org.koin.android.ext.android.inject
import java.nio.charset.Charset

class HomeActivity : AppCompatActivity() {

    companion object {
        private val TAG = HomeActivity::class.java.simpleName

        private const val REQUEST_CODE = 101
    }

    private lateinit var binding: ActivityHomeBinding

    private lateinit var biometricPrompt1: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var secretKeyName: String
    private lateinit var ciphertext: ByteArray
    private lateinit var initializationVector: ByteArray
    private val cryptographyManager: CryptographyManager by inject()
    private var readyToEncrypt: Boolean = false

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        secretKeyName = getString(R.string.secret_key_name)
        biometricPrompt1 = createBiometricPrompt()
        promptInfo = createPromptInfo()
        binding.btnEncrypt.setOnClickListener {
            authenticateToEncrypt()
        }
        binding.btnDecrypt.setOnClickListener {
            authenticateToDecrypt()
        }

        binding.btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        binding.btnSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createBiometricPrompt(): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(this)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)

                Log.d(TAG, "$errorCode :: $errString")

                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {

                    Log.d(TAG, "onAuthenticationError: negative button is clicked")
                    //loginWithPassword()
                    // Because in this app, the negative button allows the user to enter an account password. This is completely optional and your app doesn’t have to do it.
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()

                Log.d(TAG, "Authentication failed for an unknown reason")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                Log.d(TAG, "Authentication was successful")

                processData(result.cryptoObject)
            }
        }

        return BiometricPrompt(this, executor, callback)

    }

    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.sign_in_with_biometric))
            .setSubtitle(getString(R.string.biometric_auth))

            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            // Authenticate without requiring the user to press a "confirm" button after satisfying the biometric check
            .setConfirmationRequired(false)
            .setNegativeButtonText(getString(R.string.prompt_info_use_app_password))
            .build()
    }

    private fun authenticateToEncrypt() {
        readyToEncrypt = true

        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "App can authenticate using biometrics.")

                val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
                biometricPrompt1.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e(TAG, "No biometric features available on this device.")

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e(TAG, "Biometric features are currently unavailable.")

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val enrollIntent =
                        Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                            putExtra(
                                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                BIOMETRIC_STRONG
                            )
                        }
                    startActivityForResult(enrollIntent, REQUEST_CODE)
                } else {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
        }
    }

    private fun authenticateToDecrypt() {
        readyToEncrypt = false

        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "App can authenticate using biometrics.")

                val cipher = cryptographyManager.getInitializedCipherForDecryption(
                    secretKeyName,
                    initializationVector
                )
                biometricPrompt1.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e(TAG, "No biometric features available on this device.")

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e(TAG, "Biometric features are currently unavailable.")

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val enrollIntent =
                        Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                            putExtra(
                                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                BIOMETRIC_STRONG
                            )
                        }
                    startActivityForResult(enrollIntent, REQUEST_CODE)
                } else {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
        }
    }

    private fun processData(cryptoObject: BiometricPrompt.CryptoObject?) {
        val data = if (readyToEncrypt) {
            val text = "Hello World!"
            val encryptedData = cryptographyManager.encryptData(text, cryptoObject?.cipher!!)
            ciphertext = encryptedData.ciphertext
            initializationVector = encryptedData.initializationVector

            String(ciphertext, Charset.forName("UTF-8"))
        } else {
            cryptographyManager.decryptData(ciphertext, cryptoObject?.cipher!!)
        }
        binding.textView.text = data
    }

}