package com.example.projectnoted.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.projectnoted.R
import com.example.projectnoted.databinding.ActivitySignUpBinding
import com.example.projectnoted.firebase.FireStoreClass
import com.example.projectnoted.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {
    private lateinit var binding:ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        setupActionBar()
        binding.btnConfirm.setOnClickListener(){
            registerUser()
        }

    }

    fun userRegisteredSuccess(){
        Toast.makeText(
            this,  "You have successfully registered "
                    , Toast.LENGTH_LONG
        ).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }
    private fun setupActionBar(){
        setSupportActionBar(binding.ToolbarSignUp)
        val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.icon_back_arrow)
        }
        binding.ToolbarSignUp.setNavigationOnClickListener{onBackPressed()}


    }
    private fun registerUser(){
        //如果user打到空白
        val name:String = binding.editTextTextPersonName.text.toString().trim{it<=' '}
        val email:String = binding.editTextTextEmailAddress.text.toString().trim{it<=' '}
        val password:String = binding.editTextTextPassword.text.toString().trim{it<=' '}

        if(validateForm(name,email,password)){
           showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid,name,registeredEmail)
                        FireStoreClass().registerUser(this,user)
                    } else {
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }
    //查看又没有填
    private fun validateForm(name:String, email: String, password:String):Boolean{
        return when{
            TextUtils.isEmpty(name)->{
                showErrorSnackBar("Please enter a name")
                false
            }
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