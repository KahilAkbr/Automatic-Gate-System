package com.example.iot_licenseplatedetection

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iot_licenseplatedetection.databinding.ActivityRegisterPlateBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class RegisterPlateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterPlateBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var plateRef: DatabaseReference
    private lateinit var plateAdapter: PlateAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterPlateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        plateRef = database.getReference("plates")

        plateAdapter = PlateAdapter { plateNumber ->
            deletePlate(plateNumber)
        }
        binding.rvPlates.apply {
            layoutManager = LinearLayoutManager(this@RegisterPlateActivity)
            adapter = plateAdapter
        }

        binding.btnAdd.setOnClickListener {
            addPlate()
        }

        plateRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val plates = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                plateAdapter.updatePlates(plates)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RegisterPlateActivity, "Failed to load plates: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addPlate() {
        val plateNumber = binding.etPlate.text.toString().trim().toUpperCase(Locale.ROOT)

        if (plateNumber.isEmpty()) {
            Toast.makeText(this, "Plate number cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        plateRef.child(plateNumber).setValue(plateNumber)
            .addOnSuccessListener {
                Toast.makeText(this, "Plate added successfully", Toast.LENGTH_SHORT).show()
                binding.etPlate.text.clear()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add plate: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deletePlate(plateNumber: String) {
        plateRef.child(plateNumber).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Plate deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete plate: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}