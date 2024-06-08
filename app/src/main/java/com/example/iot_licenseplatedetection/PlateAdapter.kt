package com.example.iot_licenseplatedetection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlateAdapter(private val onDeletePlate: (String) -> Unit) : RecyclerView.Adapter<PlateAdapter.PlateViewHolder>() {
    private val plates = mutableListOf<String>()

    fun updatePlates(newPlates: List<String>) {
        plates.clear()
        plates.addAll(newPlates)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_plate, parent, false)
        return PlateViewHolder(view, onDeletePlate)
    }

    override fun onBindViewHolder(holder: PlateViewHolder, position: Int) {
        holder.bind(plates[position])
    }

    override fun getItemCount() = plates.size

    class PlateViewHolder(itemView: View, private val onDeletePlate: (String) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val tvPlateNumber: TextView = itemView.findViewById(R.id.tv_plate_number)
        private val btnDeletePlate: ImageButton = itemView.findViewById(R.id.btn_delete_plate)

        fun bind(plateNumber: String) {
            tvPlateNumber.text = plateNumber
            btnDeletePlate.setOnClickListener {
                onDeletePlate(plateNumber)
            }
        }
    }
}