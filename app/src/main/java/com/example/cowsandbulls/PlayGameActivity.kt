package com.example.cowsandbulls

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_play_game.*
import java.lang.Exception


lateinit var currentUsername: String
lateinit var opponentUsername: String
lateinit var sessionId: String
lateinit var currentName: String
lateinit var myNumber: String
lateinit var opponentNumber: String
lateinit var turn:String

private val myNumberAsSet = mutableSetOf<Int>()
private var opponentNumberAsSet = mutableSetOf<Int>()

lateinit var dialogAlert: Dialog
lateinit var exit: Button
lateinit var cancel: Button
lateinit var add: Button
lateinit var numberEt: EditText
lateinit var oppNumberTv: TextView
lateinit var oppMessageTv: TextView

lateinit var listView: ListView
lateinit var dialogNumber: Dialog
lateinit var dialogOpponentNumber: Dialog
lateinit var dialogWinner: Dialog
lateinit var winnerTv:TextView

private var opponentName: String? = null
private var list = ArrayList<DataModel>()
private var adapter: DataAdapter? = null
private var firstTimeFlag: Boolean = true

class PlayGameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_game)

        firstTimeFlag = true

        sessionId = intent.getStringExtra("sessionId").toString()
        currentUsername = intent.getStringExtra("currentUsername").toString()
        opponentUsername = intent.getStringExtra("opponentUsername").toString()
        currentName = intent.getStringExtra("currentName").toString()

        dialogWinner = Dialog(this)
        dialogWinner.setContentView(R.layout.dialog_winner)
        dialogWinner.setCancelable(false)
        winnerTv = dialogWinner.findViewById(R.id.tv_winner)

        dialogOpponentNumber = Dialog(this)
        dialogOpponentNumber.setContentView(R.layout.dialog_opponent_number)
        dialogOpponentNumber.setCancelable(false)
        oppNumberTv = dialogOpponentNumber.findViewById(R.id.tv_opponent_number)
        oppMessageTv = dialogOpponentNumber.findViewById(R.id.tv_opponent_message)

        dialogAlert = Dialog(this)
        dialogAlert.setContentView(R.layout.dialog_alert)
        dialogAlert.setCancelable(false)
        exit = dialogAlert.findViewById(R.id.btn_exit)
        cancel = dialogAlert.findViewById(R.id.btn_cancel_exit)

        dialogNumber = Dialog(this)
        dialogNumber.setContentView(R.layout.dialog_number)
        dialogNumber.setCancelable(true)
        add = dialogNumber.findViewById(R.id.btn_add)
        numberEt = dialogNumber.findViewById(R.id.et_number)

        dialogNumber.show()
        incomingCalls()

        listView = findViewById<ListView>(R.id.list_view)
//        list.add(DataModel(371, 0, 1))
//        list.add(DataModel(192, 2, 0))
//        list.add(DataModel(478, 1, 0))
//        list.add(DataModel(987, 1, 1))
//        list.add(DataModel(257, 0, 2))
//        list.add(DataModel(371, 0, 1))
//        list.add(DataModel(192, 2, 0))
//        list.add(DataModel(478, 1, 0))
//        list.add(DataModel(987, 1, 1))
//        list.add(DataModel(257, 0, 2))

        adapter = DataAdapter(this, R.layout.list_view_item, list)
        listView.adapter = adapter
//        adapter!!.notifyDataSetChanged()

        users.child(opponentUsername).child("Name").get().addOnSuccessListener {
            opponentName = it.value.toString()
            tv_match_title.setText(currentName + " VS. " + opponentName)
        }

        btn_guess.setOnClickListener({
            numberEt.setHint("Enter your Guess")
            dialogNumber.show()
        })

        add.setOnClickListener({
            var flag = isNumberProper(numberEt.text.toString())
            if ((firstTimeFlag) && (flag)) {
                myNumber = numberEt.text.toString()
                myRef.child("PlayGame")
                    .child(sessionId!!)
                    .child("History")
                    .child(currentUsername)
                    .child("Number").setValue(myNumber)

                firstTimeFlag = false
                tv_my_number.setText(myNumber)
                dialogNumber.dismiss()
            } else if (flag) {
                myNumber = numberEt.text.toString()
                myRef.child("PlayGame")
                    .child(sessionId!!)
                    .child("History")
                    .child(currentUsername)
                    .child("Recent").setValue(myNumber)
                myRef.child("PlayGame")
                    .child(sessionId!!)
                    .child("Turn").setValue(opponentUsername)
                addGuess()
                dialogNumber.dismiss()
            } else {
                showToast("Invalid Number: Digits cannot be repeated")
            }
            numberEt.setText("")

        })

    }

    override fun onBackPressed() {
        dialogAlert.show()
        exit.setOnClickListener({
            list.clear()
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        })

        cancel.setOnClickListener({
            dialogAlert.dismiss()
        })
    }

    fun incomingCalls() {
        myRef.child("PlayGame")
            .child(sessionId!!)
            .child("History")
            .child(opponentUsername)
            .child("Number").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    opponentNumber = snapshot.value.toString()
                    opponentNumberAsSet = numberStringToSet(opponentNumber)

                }

                override fun onCancelled(error: DatabaseError) {}
            })

        myRef.child("PlayGame")
            .child(sessionId!!)
            .child("History")
            .child(opponentUsername)
            .child("Recent").addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var number = snapshot.value.toString().toInt()
                    if (number==0) return

                    dialogOpponentNumber.show()
                    oppNumberTv.setText(""+number)
                    oppMessageTv.setText(opponentName+"'s Guess")
                    Handler().postDelayed({
                        dialogOpponentNumber.dismiss()
                    }, 2500)

                }

                override fun onCancelled(error: DatabaseError) {}
            })

        myRef.child("PlayGame")
            .child(sessionId!!)
            .child("Winner").addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var value = snapshot.value.toString()
                    if (value=="0") return
                    dialogWinner.show()
                    winnerTv.setText(value+" Wins The Match")
                    Handler().postDelayed({
                        myRef.child("PlayGame")
                            .child(sessionId!!).setValue(null)
                        val intent = Intent(applicationContext, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    },4500)


                }

                override fun onCancelled(error: DatabaseError) {}
            })

        myRef.child("PlayGame")
            .child(sessionId!!)
            .child("Turn")
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var value = snapshot.value.toString()

                    if(value== currentUsername){
                        tv_turn.setText("Your Turn")
                        btn_color_indicator.setBackgroundColor(resources.getColor(R.color.green))
                        btn_guess.isEnabled = true
                    }else{
                        tv_turn.setText("Opponent's Turn")
                        btn_color_indicator.setBackgroundColor(resources.getColor(R.color.red))
                        btn_guess.isEnabled = false
                    }

                }

                override fun onCancelled(error: DatabaseError) {}
            })


    }

    fun addGuess() {

        var cowsandbulls:Array<Int> = getCowsAndBulls()
        list.add(DataModel(myNumber.toInt(), cowsandbulls.get(1), cowsandbulls.get(0) ))
        adapter!!.notifyDataSetChanged()
    }

    fun getCowsAndBulls():Array<Int>{
        var myUnits = myNumber[2].toString().toInt()
        var myTens = myNumber[1].toString().toInt()
        var myHundred = myNumber[0].toString().toInt()

        Log.i("TAG", "OpponentNumber: "+opponentNumber)

        var oppUnits = opponentNumber[2].toString().toInt()
        var oppTens = opponentNumber[1].toString().toInt()
        var oppHundred = opponentNumber[0].toString().toInt()

        var cows:Int = 0
        var bulls:Int = 0

        if(myUnits==oppUnits){
            bulls += 1
        }
        if(myTens==oppTens){
            bulls += 1
        }
        if(myHundred==oppHundred){
            bulls += 1
        }

        if((myUnits==oppTens) || (myUnits==oppHundred)){
            cows += 1
        }
        if((myTens==oppUnits) || (myTens==oppHundred)){
            cows += 1
        }
        if((myHundred==oppTens) || (myHundred==oppUnits)){
            cows += 1
        }

        if(bulls==3){
            myRef.child("PlayGame")
                .child(sessionId!!)
                .child("Winner")
                .setValue(currentName)
        }

        return arrayOf(cows, bulls)

    }


    fun isNumberProper(number: String): Boolean {
        myNumberAsSet.clear()
        try {
            for (s in number) {
                myNumberAsSet.add(s.toInt())
            }
        } catch (e: Exception) {
        }
        if (myNumberAsSet.size != 3) {
            return false
        }
        return true
    }

    fun numberStringToSet(number: String): MutableSet<Int> {
        var set = mutableSetOf<Int>()
        for (s in number) {
            set.add(s.toInt())
        }
        return set
    }

    fun buColorChange(view: View) {
        val button: Button = view as Button

        if (button.text.equals("")) {
            button.setBackgroundColor(resources.getColor(R.color.primary))
            when (button.id) {
                R.id.btn_0 -> button.setText("0")
                R.id.btn_1 -> button.setText("1")
                R.id.btn_2 -> button.setText("2")
                R.id.btn_3 -> button.setText("3")
                R.id.btn_4 -> button.setText("4")
                R.id.btn_5 -> button.setText("5")
                R.id.btn_6 -> button.setText("6")
                R.id.btn_7 -> button.setText("7")
                R.id.btn_8 -> button.setText("8")
                R.id.btn_9 -> button.setText("9")
            }
        } else {
            button.setBackgroundColor(resources.getColor(R.color.dark_grey))
            button.setText("")
        }
    }

    fun showToast(message: String) {
        Toast.makeText(this, "" + message, Toast.LENGTH_SHORT).show()
    }
}