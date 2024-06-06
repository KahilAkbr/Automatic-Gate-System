package com.example.iot_licenseplatedetection

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.iot_licenseplatedetection.api.ApiConfig
import com.example.iot_licenseplatedetection.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOn.setOnClickListener {
            val client = ApiConfig.getApiSevice().greenLEDListener("2MnrK2xNpTAoKrSQaJXY8I6X6jaeurv7", 1)
            client.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.d("BJIR", "Berhasil")
                    Log.d("BJIR", "Respons: ${response.toString()}")
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    // Tangani kesalahan di sini
                }
            })
        }

        binding.btnOff.setOnClickListener {
            val client = ApiConfig.getApiSevice().greenLEDListener("2MnrK2xNpTAoKrSQaJXY8I6X6jaeurv7", 0)
            client.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.d("BJIR", "Berhasil")
                    Log.d("BJIR", "Respons: ${response.toString()}")
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    // Tangani kesalahan di sini
                }
            })
        }
        readData()
        databaseListener()
    }

    private fun databaseListener() {
        database = FirebaseDatabase.getInstance().getReference("Sensor")
        val distanceListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val distance = snapshot.child("distance").value
                binding.tvDistance.text = distance.toString()
                if(distance.toString().toDouble() < 2000) {
                    val client = ApiConfig.getApiSevice().servoListener("2MnrK2xNpTAoKrSQaJXY8I6X6jaeurv7", 90)
                    client.enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            Log.d("BJIR", "Berhasil")
                            Log.d("BJIR", "Respons: ${response.toString()}")
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            // Tangani kesalahan di sini
                        }
                    })
                }else{
                    val client = ApiConfig.getApiSevice().servoListener("2MnrK2xNpTAoKrSQaJXY8I6X6jaeurv7", 180)
                    client.enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            Log.d("BJIR", "Berhasil")
                            Log.d("BJIR", "Respons: ${response.toString()}")
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            // Tangani kesalahan di sini
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        database.addValueEventListener(distanceListener)
    }

    private fun readData() {
        database = FirebaseDatabase.getInstance().getReference("Sensor")
        database.child("distance").get().addOnSuccessListener {
            if(it.exists()){
                val distance : Double = it.value.toString().toDouble()
                binding.tvDistance.text = distance.toString()
            }
        }
    }
}