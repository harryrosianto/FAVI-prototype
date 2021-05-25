package com.thelatenightstudio.favi.mainmenu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.core.utils.ToastHelper
import com.thelatenightstudio.favi.databinding.ActivityMainMenuBinding
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
            viewModel.activateBiometric()
            val text = getString(R.string.successful)
            ToastHelper.showToast(this, text)
        }
    }

    override fun onDestroy() {
        viewModel.signOut()
        super.onDestroy()
    }
}