package com.thelatenightstudio.favi

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.thelatenightstudio.favi.databinding.ActivityHomeBinding
import java.nio.charset.Charset
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class HomeActivity : AppCompatActivity() {

    companion object {
        private val TAG = HomeActivity::class.java.simpleName

        private const val REQUEST_CODE = 101
        private const val AUTH_KEY = "auth_key"
    }

    private lateinit var binding: ActivityHomeBinding

    private lateinit var biometricPrompt: BiometricPrompt

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        generateSecretKey(
            KeyGenParameterSpec.Builder(
                AUTH_KEY,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                // Invalidate the keys if the user has registered a new biometric
                // credential, such as a new fingerprint. Can call this method only
                // on Android 7.0 (API level 24) or higher. The variable
                // "invalidatedByBiometricEnrollment" is true by default.
                .setInvalidatedByBiometricEnrollment(true)
                .build()
        )

        biometricPrompt = createBiometricPrompt()

        binding.btnBiometric.setOnClickListener {
            val cipher = getCipher()
            val secretKey = getSecretKey()
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val promptInfo = createPromptInfo()

            val biometricManager = BiometricManager.from(this)
            when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    Log.d(TAG, "App can authenticate using biometrics.")

                    biometricPrompt.authenticate(
                        promptInfo,
                        BiometricPrompt.CryptoObject(cipher)
                    )
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

                val message = "This is a secret message so don't tell anyone!"
                val encryptedInfo: ByteArray? = result.cryptoObject?.cipher?.doFinal(
                    message.toByteArray(Charset.defaultCharset())

                )

                Log.d(
                    TAG, "Encrypted information: " +
                            encryptedInfo.contentToString()
                )
                // Proceed with viewing the private encrypted message.
                //showEncryptedMessage(result.cryptoObject)

                val intent = Intent(this@HomeActivity, SecondActivity::class.java)
                startActivity(intent)
            }
        }

        return BiometricPrompt(this, executor, callback)

    }

    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.prompt_info_title))
            .setSubtitle(getString(R.string.prompt_info_subtitle))
            .setDescription(getString(R.string.prompt_info_description))
            // Authenticate without requiring the user to press a "confirm"
            // button after satisfying the biometric check
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .setConfirmationRequired(false)
            .setNegativeButtonText(getString(R.string.prompt_info_use_app_password))
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")

        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null)
        return keyStore.getKey(AUTH_KEY, null) as SecretKey
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getCipher(): Cipher {
        return Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7
        )
    }

}