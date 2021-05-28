package com.thelatenightstudio.favi.mainmenu

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.addfundmenu.AddFundActivity
import com.thelatenightstudio.favi.core.utils.ToastHelper.showToast
import com.thelatenightstudio.favi.databinding.ActivityMainMenuBinding
import com.thelatenightstudio.favi.transfermenu.TransferMenuActivity
import kotlinx.coroutines.Dispatchers.IO
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

        binding.btnActivateBiometric.setOnClickListener {
            lifecycleScope.launch(IO) {
                val text =
                    if (viewModel.activateBiometric()) getString(R.string.biometric_activated)
                    else getString(R.string.biometric_deactivated)
                showToast(text)
            }
        }


        //Only move to add fund activity balance activity with intent
        binding.btnAddFund.setOnClickListener {
            val intent = Intent(this, AddFundActivity::class.java)
            startActivity(intent)
        }
        binding.btnBalanceTransfer.setOnClickListener {
            val intent = Intent(this, TransferMenuActivity::class.java)
            startActivity(intent)
        }


    }

    override fun onDestroy() {
        lifecycleScope.launch(IO) {
            viewModel.signOut()
        }
        super.onDestroy()
    }
}