package com.thelatenightstudio.favi.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thelatenightstudio.favi.databinding.ActivitySecondBinding

class SecondActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STRING = "extra_string"
    }

    private lateinit var binding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.extras != null) {
            val text = intent.getStringExtra(EXTRA_STRING)
            binding.textView.text = text
        }
    }
}