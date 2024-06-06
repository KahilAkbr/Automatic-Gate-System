package com.example.iot_licenseplatedetection

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import android.Manifest
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.example.iot_licenseplatedetection.CameraActivity.Companion.CAMERAX_RESULT

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database : DatabaseReference
    private var currentImageUri: Uri? = null

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        binding.btnCam.setOnClickListener{
            startCameraX()
        }

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

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERAX_RESULT) {
            currentImageUri = it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            showImage()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.plat.setImageURI(it)
        }
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}