package com.example.cowsandbulls

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

private var mAuth: FirebaseAuth? = null
var myRef = FirebaseDatabase.getInstance().reference
var users = myRef.child("Users")

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()

        tv_login.setOnClickListener({
            intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        })

        btn_register.setOnClickListener({

            when {
                TextUtils.isEmpty(et_name_register.text.toString().trim { it <= ' ' }) -> {
                    showToast("Please Enter Name")
                }
                TextUtils.isEmpty(et_username_register.text.toString().trim { it <= ' ' }) -> {
                    showToast("Please Enter Username")
                }
                TextUtils.isEmpty(et_password_register.text.toString().trim { it <= ' ' }) -> {
                    showToast("Please Enter Passsword")
                }
                containsDot(et_username_register.text.toString()) ->{
                    showToast("Username cannot contain '.' ")
                }
                else -> {
                    val password = et_password_register.text.toString().trim { it <= ' ' }
                    val name = et_name_register.text.toString().trim { it <= ' ' }
                    val username = et_username_register.text.toString().trim { it <= ' ' }
                    val email = username+"@gmail.com"


                    mAuth!!.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener({ task ->
                            if (task.isSuccessful) {
                                var firebaseUser: FirebaseUser = task.result!!.user!!
                                showToast("Registration Successful")


                                users.child(username).child("Email").setValue(email)
                                users.child(username).child("Name").setValue(name)
                                users.child(username).child("Password").setValue(password)
                                users.child(username).child("uid").setValue(firebaseUser.uid)
                                users.child(username).child("Requests").setValue(false)

                                var intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("username", username)
                                intent.putExtra("email", email)
                                intent.putExtra("uid", firebaseUser.uid)
                                startActivity(intent)
                                finish()

                            } else {
                                showToast("Failed to Register:" + task.exception!!.message)
                            }

                        })


                }
            }

        })

    }

    fun containsDot(str:String):Boolean{
        for (i in 0..(str.length-1)){
            if (str[i].toString().equals(".")){
                return true
            }
        }
        return false
    }

    fun loadMain() {
        var currentUser = mAuth!!.currentUser
        if (currentUser != null) {

            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra("email", currentUser.email)
            startActivity(intent)
        }
    }

    fun splitEmail(str: String) = str.split("@")[0]

    fun showToast(message: String) {
        Toast.makeText(this, "" + message, Toast.LENGTH_SHORT).show()
    }
}