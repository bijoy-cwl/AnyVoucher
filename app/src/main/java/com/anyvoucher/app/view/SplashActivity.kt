package com.anyvoucher.app.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.anyvoucher.app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth
        // [END initialize_auth]


        Handler().postDelayed(Runnable {
            val user = Firebase.auth.currentUser
            if (user == null) {
                val intent2 = Intent(this@SplashActivity, InitActivity::class.java)
                startActivity(intent2)
                finish()
            } else {
                val intent2 = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent2)
                finish()
            }
        }, 1000)
    }


}