package com.example.cowsandbulls

import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_invite.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null;
    private var currentUser: FirebaseUser? = null
    private var currentName: String? = null
    private var opponentUsername: String? = null
    private var sessionId: String? = null
    private var matchStartFlag: Boolean = false
    private lateinit var email: String
    private lateinit var uid: String
    private lateinit var currentUsername: String

    private lateinit var invite: Button
    private lateinit var cancel: Button
    private lateinit var accept: Button
    private lateinit var decline: Button

    private lateinit var invitationUsernameEt: EditText
    private lateinit var requestUsernameTv: TextView

    lateinit var dialogInvite: Dialog
    lateinit var dialogRequest: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser

        email = intent.getStringExtra("email").toString()
        uid = intent.getStringExtra("uid").toString()
        currentUsername = intent.getStringExtra("username").toString()

        Log.i("TAG", "Username: " + currentUsername)
        users.child(currentUsername).child("Name").get().addOnSuccessListener {
            currentName = it.value.toString()
            Log.i("TAG", "Name : " + currentName)
            tv_name.setText("Hello " + currentName!! + " !")
            tv_emailid.setText("Email: " + email)
            tv_uid.setText("Uid: " + uid)
        }

        dialogInvite = Dialog(this)
        dialogInvite.setContentView(R.layout.dialog_invite)
        dialogInvite.setCancelable(true)
        invite = dialogInvite.findViewById(R.id.btn_invite)
        cancel = dialogInvite.findViewById(R.id.btn_cancel)
        invitationUsernameEt = dialogInvite.findViewById(R.id.et_username)


        dialogRequest = Dialog(this)
        dialogRequest.setContentView(R.layout.dialog_request)
        dialogRequest.setCancelable(true)
        accept = dialogRequest.findViewById(R.id.btn_accept)
        decline = dialogRequest.findViewById(R.id.btn_decline)
        requestUsernameTv = dialogRequest.findViewById(R.id.tv_request_username)

        btn_invite_friends.setOnClickListener({
            invite()
        })

        btn_play_offline.setOnClickListener({
            showToast("This Feature is not available yet")
        })

        inMatchCalls()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        when (id) {

            R.id.action_signout -> {
                signout()
            }
            R.id.action_invite -> {
                invite()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun invite() {
        dialogInvite.show()
        invite.setOnClickListener({
            hideKeyboard()
            var invitationUsername = invitationUsernameEt.text.toString()
            opponentUsername = invitationUsername

            users.child(opponentUsername!!).get().addOnSuccessListener {
                if (it.value.toString().equals("null")) {
                    showToast("Invalid Username: " + opponentUsername)
                } else {
                    sessionId = currentUsername + opponentUsername

                    users.child(opponentUsername!!).child("Requests").push()
                        .setValue(currentUsername)
                    showToast("Invitation Sent to " + opponentUsername)
                    myRef.child("PlayGame")
                        .child(sessionId!!)
                        .child("MatchStart")
                        .setValue(false)

                    matchStartCall()
                    invitationUsernameEt.setText("")
                    dialogInvite.dismiss()
                }
            }
        })

        cancel.setOnClickListener({
            dialogInvite.dismiss()
        })
    }

    private fun replyRequest() {
        dialogRequest.show()
        requestUsernameTv.setText(opponentUsername)
        accept.setOnClickListener({
            sessionId = opponentUsername+currentUsername
            matchStartCall()
            myRef.child("PlayGame")
                .child(sessionId!!)
                .child("History")
                .child(currentUsername)
                .child("Number")
                .setValue("0")
            myRef.child("PlayGame")
                .child(sessionId!!)
                .child("History")
                .child(currentUsername)
                .child("Recent").setValue("0")

            myRef.child("PlayGame")
                .child(sessionId!!)
                .child("History")
                .child(opponentUsername!!)
                .child("Number")
                .setValue("0")
            myRef.child("PlayGame")
                .child(sessionId!!)
                .child("History")
                .child(opponentUsername!!)
                .child("Recent").setValue("0")

            myRef.child("PlayGame")
                .child(sessionId!!)
                .child("Winner")
                .setValue("0")

            myRef.child("PlayGame")
                .child(sessionId!!)
                .child("Turn").setValue(currentUsername)

            myRef.child("PlayGame")
                .child(sessionId!!)
                .child("MatchStart")
                .setValue(true)
            dialogRequest.dismiss()
        })

        decline.setOnClickListener({
            myRef.child("PlayGame").child(sessionId!!).setValue(null)
            dialogRequest.dismiss()
        })
    }

    private fun inMatchCalls() {
        users.child(currentUsername).child("Requests")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try{
                        if (!(snapshot.value is Boolean)) {
                            val td = snapshot.value as HashMap<String, Any>
                            if (td != null) {
                                var value: String
                                for (key in td.keys) {
                                    opponentUsername = td[key].toString()
                                    break
                                }
                                sessionId = opponentUsername+currentUsername
                                users.child(currentUsername).child("Requests").setValue(false)
                                replyRequest()
                            }
                        }
                    }catch (e:Exception){}
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun matchStartCall() {
        myRef.child("PlayGame")
            .child(sessionId!!)
            .child("MatchStart").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    try{
                        matchStartFlag = snapshot.value as Boolean
                    }catch (e:Exception){
                        matchStartFlag = false
                        showToastLong("Invitation was Declined")
                    }

                    if (matchStartFlag) {
                        Log.i("TAG","CurrentUsername: $currentUsername, OpponentUsername: $opponentUsername")
                        val intent = Intent(applicationContext, PlayGameActivity::class.java)
                        intent.putExtra("sessionId", sessionId)
                        intent.putExtra("currentUsername", currentUsername)
                        intent.putExtra("opponentUsername", opponentUsername)
                        intent.putExtra("currentName", currentName)
                        startActivity(intent)

                    }

                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun signout() {
        mAuth!!.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    fun hideKeyboard() {
        var view = et_username
        if (view != null) {
            val hideMe = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideMe.hideSoftInputFromWindow(view.windowToken, 0)
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    fun showToast(message: String) {
        Toast.makeText(this, "" + message, Toast.LENGTH_SHORT).show()
    }
    fun showToastLong(message: String) {
        Toast.makeText(this, "" + message, Toast.LENGTH_LONG).show()
    }
}