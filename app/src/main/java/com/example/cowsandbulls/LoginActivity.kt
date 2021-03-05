package com.example.cowsandbulls

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

private var mAuth: FirebaseAuth? = null

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        loadMain()

        tv_register.setOnClickListener({
            intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        })

        btn_login.setOnClickListener({
            when {
                TextUtils.isEmpty(et_username_login.text.toString().trim { it <= ' ' }) -> {
                    showToast("Please Enter Email id")
                }
                TextUtils.isEmpty(et_password.text.toString().trim { it <= ' ' }) -> {
                    showToast("Please Enter Passsword")
                }
                else -> {
                    var username = et_username_login.text.toString().trim { it <= ' ' }
                    val password = et_password.text.toString().trim { it <= ' ' }
                    var email:String? = null
                    if (containAt(username)){
                        email = username
                        username = splitEmail(username)
                    }else {
                        email = username+"@gmail.com"
                    }
                    Log.i("TAG", email+" : Email")

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->


                            if (task.isSuccessful) {
                                showToast("Login Successful")
                                var currentUser: FirebaseUser =
                                    FirebaseAuth.getInstance().currentUser!!
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("username", username)
                                intent.putExtra("email", email)
                                intent.putExtra("uid", currentUser!!.uid)
                                startActivity(intent)
                                finish()
                            } else {
                                showToast("Login Failed: " + task.exception!!.message.toString())
                            }

                        }
                }
            }

        })

    }

    private fun containAt(username: String): Boolean {
        for (s in username){
            Log.i("TAG", ""+(s=='@'))
            if (s == '@'){
                return true
            }
        }
        return false

    }


    fun loadMain() {
        var currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            var username = splitEmail(currentUser.email.toString())
            showToast("Login Successful: "+username)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("email", currentUser.email)
            intent.putExtra("uid", FirebaseAuth.getInstance().currentUser!!.uid)
            intent.putExtra("username", username)
            startActivity(intent)
            finish()
        }
    }

    fun hideKeyboard() {
        var view = currentFocus
        if (view != null) {
            val hideMe = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideMe.hideSoftInputFromWindow(view.windowToken, 0)
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    fun splitEmail(str: String) = str.split("@")[0]

    fun showToast(message: String) {
        Toast.makeText(this, "" + message, Toast.LENGTH_LONG).show()
    }
}