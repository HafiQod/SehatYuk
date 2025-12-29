package com.example.mediplus.uii

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediplus.R
import com.example.mediplus.uii.database.AppointmentModel

class AppointmentAdapter(
    private val appointmentList: ArrayList<AppointmentModel>,
    private val isHome: Boolean = false, // Parameter baru: cek apakah ini di Home?
    private val onItemClick: (AppointmentModel) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = appointmentList[position]

        holder.tvPurpose.text = currentItem.purpose
        holder.tvDate.text = currentItem.date

        // LOGIKA BARU: Jika di Home, sembunyikan View Details
        if (isHome) {
            holder.tvViewDetails.visibility = View.GONE
        } else {
            holder.tvViewDetails.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            onItemClick(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return appointmentList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPurpose: TextView = itemView.findViewById(R.id.tvPurpose)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        // Tambahkan ID View Details
        val tvViewDetails: TextView = itemView.findViewById(R.id.tvViewDetails)
    }
}