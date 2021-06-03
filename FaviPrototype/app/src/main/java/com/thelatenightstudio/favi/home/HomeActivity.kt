package com.thelatenightstudio.favi.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thelatenightstudio.favi.core.service.SignOutService
import com.thelatenightstudio.favi.databinding.ActivityHomeBinding
import com.thelatenightstudio.favi.signin.SignInActivity
import com.thelatenightstudio.favi.signup.SignUpActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val signOutService = Intent(this, SignOutService::class.java)
        SignOutService.enqueueWork(this, signOutService)

        binding.btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        binding.btnSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }

}