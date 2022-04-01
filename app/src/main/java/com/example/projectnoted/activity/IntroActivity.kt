package com.example.projectnoted.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.example.projectnoted.R
import com.example.projectnoted.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {
    private lateinit var binding: ActivityIntroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_intro)
        binding.btnSignUp.setOnClickListener(){
            startActivity(Intent(this, SignUpActivity::class.java))
            
        }
        binding.btnSignIn.setOnClickListener(){
            startActivity(Intent(this,SignInActivity::class.java))
        }


    }

}