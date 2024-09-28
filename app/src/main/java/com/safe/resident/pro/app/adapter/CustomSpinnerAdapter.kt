package com.safe.resident.pro.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.safe.resident.pro.app.R

class CustomSpinnerAdapter(
    context: Context,
    private val items: Array<String>
) : ArrayAdapter<String>(context, R.layout.spinner_category, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_category, parent, false)
        val spinnerText = view.findViewById<TextView>(R.id.spinnerText)
        spinnerText.text = items[position]
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_category, parent, false)
        val spinnerText = view.findViewById<TextView>(R.id.spinnerText)
        spinnerText.text = items[position]
        return view
    }
}