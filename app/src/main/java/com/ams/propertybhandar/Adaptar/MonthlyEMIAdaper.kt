package com.ams.propertybhandar.model

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ams.propertybhandar.R

class MonthlyEMIAdaper(private val monthlyEMIList: List<MonthlyEMI>) :
    RecyclerView.Adapter<MonthlyEMIAdaper.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val monthTextView: TextView = itemView.findViewById(R.id.monthTextView)
        val principalTextView: TextView = itemView.findViewById(R.id.principalTextView)
        val interestTextView: TextView = itemView.findViewById(R.id.interestTextView)
        val outstandingTextView: TextView = itemView.findViewById(R.id.outstandingTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_monthly_emi, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val monthlyEMI = monthlyEMIList[position]
        holder.monthTextView.text = "${monthlyEMI.month}"
        holder.principalTextView.text = "₹${String.format("%.2f", monthlyEMI.principalComponent)}"
        holder.interestTextView.text = "₹${String.format("%.2f", monthlyEMI.interestComponent)}"
        holder.outstandingTextView.text = "₹${String.format("%.2f", monthlyEMI.outstandingBalance)}"
    }

    override fun getItemCount(): Int {
        return monthlyEMIList.size
    }
}