package com.thelatenightstudio.favi.mainmenu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.core.utils.ToastHelper.showToast
import com.thelatenightstudio.favi.databinding.ActivityMainMenuBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainMenuActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STRING = "extra_string"
    }

    private lateinit var binding: ActivityMainMenuBinding

    private val viewModel: MainMenuViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.extras != null) {
            val text = intent.getStringExtra(EXTRA_STRING)
            binding.textView.text = text
        }

        binding.btnActivateBiometric.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val text =
                    if (viewModel.activateBiometric()) getString(R.string.biometric_activated)
                    else getString(R.string.biometric_deactivated)
                showToast(text)
            }
        }
    }

    override fun onDestroy() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.signOut()
        }
        super.onDestroy()
    }
}