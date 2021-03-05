package com.example.cowsandbulls

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class DataAdapter(var ctx:Context, var resources:Int, var items: List<DataModel>) :ArrayAdapter<DataModel>(ctx, resources, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater:LayoutInflater = LayoutInflater.from(ctx)
        val view:View = layoutInflater.inflate(resources, null )

        val serialNumberTv:TextView = view.findViewById(R.id.tv_serial_number)
        val guessNumberTv:TextView = view.findViewById(R.id.tv_guess_number)
        val cowsTv:TextView = view.findViewById(R.id.tv_cows)
        val bullsTv:TextView = view.findViewById(R.id.tv_bulls)

        var currentItem:DataModel = items[position]

        serialNumberTv.setText(""+(position+1)+")")
        guessNumberTv.setText(""+currentItem.guessNumber+" - ")
        bullsTv.setText(""+currentItem.bulls+"")
        cowsTv.setText(""+currentItem.cows+"")

        return view
    }
}