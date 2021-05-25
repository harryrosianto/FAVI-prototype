package com.thelatenightstudio.favi.core.utils

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.thelatenightstudio.favi.R
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.utils.ToastHelper.showToast
import com.thelatenightstudio.favi.databinding.ActivitySignInBinding
import com.thelatenightstudio.favi.mainmenu.MainMenuActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ObserverHelper {

    fun AppCompatActivity.getSignInObserver(
        binding: ActivitySignInBinding,
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
            showToast(toastText)

            binding.progressBar.visibility = View.GONE
            lifecycleScope.launch {
                delay(1000)
                if (response is ApiResponse.Success) {
                    val intent = Intent(this@getSignInObserver, MainMenuActivity::class.java)
                    intent.putExtra(MainMenuActivity.EXTRA_STRING, "SIGN IN BERHASIL")
                    startActivity(intent)
                }
            }
        }

}