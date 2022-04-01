package com.example.projectnoted.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.projectnoted.R
import com.example.projectnoted.databinding.ActivitySignInBinding
import com.example.projectnoted.firebase.FireStoreClass
import com.example.projectnoted.models.User
import com.google.firebase.auth.FirebaseAuth


class SignInActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)
        auth = FirebaseAuth.getInstance()
        binding.btnConfirm2.setOnClickListener(){
            signInRegisteredUser()
        }
        setupActionBar()

    }

    fun signInSuccess(user: User){
       hideProgressDialog()
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }
    private fun setupActionBar(){
        setSupportActionBar(binding.toolbarSignin)
        val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.icon_back_arrow)
        }
        binding.toolbarSignin.setNavigationOnClickListener{onBackPressed()}
    }

    private fun signInRegisteredUser(){
        val email: String = binding.editTextTextEmailAddressSignIn.text.toString().trim{it<=' '}
        val password:String = binding.editTextTextPasswordSignIn.text.toString().trim{it<=' '}

        if(validateForm(email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext, "Sign in successful.",
                            Toast.LENGTH_SHORT).show()
                        FireStoreClass().loadUserData(this)

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Signin", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()

                    }
                }

        }
    }

    private fun validateForm( email: String, password:String):Boolean{
        return when{

            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Please enter a email")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter a password")
                false
            }
            else -> {true}
        }
    }
}