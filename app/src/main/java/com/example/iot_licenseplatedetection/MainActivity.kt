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
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import com.example.iot_licenseplatedetection.CameraActivity.Companion.CAMERAX_RESULT
import com.example.iot_licenseplatedetection.api.response.LicensePlateResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.util.Locale
import java.util.Properties

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database : DatabaseReference
    private var currentImageUri: Uri? = null
    private var photoFile : File? = null
    private lateinit var plateRef: DatabaseReference
    private var isWaiting = false

    private val registeredPlates = mutableListOf<String>()

    private var tokenBlynk = BuildConfig.TOKEN_BLYNK
    private var tokenReader = BuildConfig.TOKEN_READER

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

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("Sensor")
        plateRef = FirebaseDatabase.getInstance().getReference("plates")

        fetchRegisteredPlates()

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        binding.btnCam.setOnClickListener{
            startCameraX()
        }

        binding.btnAddPlate.setOnClickListener{
            startActivity(Intent(this, RegisterPlateActivity::class.java))
        }

        readData()
        databaseListener()
    }

    private fun databaseListener() {
        database = FirebaseDatabase.getInstance().getReference("Sensor")
        val distanceListener = object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onDataChange(snapshot: DataSnapshot) {
                val distance = snapshot.child("distance").value
                val distanceInt = distance.toString().toDouble().toInt()
                binding.tvDistance.text = "$distanceInt cm"
                if (isWaiting) {
                    return
                }
                if(distance.toString().toDouble() < 10) {
                    isWaiting = true
                    if (allPermissionsGranted()) {
                        startCameraX()
                    } else {
                        requestPermissionLauncher.launch(REQUIRED_PERMISSION)
                    }
                }else{
                    isWaiting = false
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
                binding.tvDistance.text = "${distance.toInt()} cm"
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERAX_RESULT) {
            currentImageUri = it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            showImage()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showImage() {
        currentImageUri?.let {uri ->
            photoFile = uriToFile(uri, this)
            Log.d("Image URI", "showImage: $uri")
            binding.plat.setImageURI(uri)
        }

        sendPhoto(photoFile, "id").observe(this@MainActivity, Observer { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    Log.d("Testt", "Sending photo...")
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Log.d("Testt", "Photo sent successfully: ${result.data}")
                    val detectedPlate = StringBuilder()
                    if(result.data.results.isEmpty()){
                        detectedPlate.append("-")
                    }else{
                        result.data.results.forEach{resultItem ->
                            detectedPlate.append("${resultItem.plate.toUpperCase()}\n")
                        }
                        Log.d("Testt", detectedPlate.toString())
                    }

                    val detectedPlateScoreText = StringBuilder()
                    result.data.results.forEach{resultsItem ->
                        val detectedPlateScore = resultsItem.score as Double
                        val scorePercentage = detectedPlateScore * 100
                        val formattedScore = String.format("%.1f%%", scorePercentage)
                        detectedPlateScoreText.append("${formattedScore}\n")
                    }

                    val candidatePlate = StringBuilder()
                    if(result.data.results.isEmpty()){
                        candidatePlate.append("-")
                    }else{
                        result.data.results.forEach { resultItem ->
                            resultItem.candidates.forEach { candidate ->
                                candidatePlate.append("${candidate.plate.toUpperCase()}\n")
                            }
                        }
                    }

                    val candidatePlateScoreText = StringBuilder()
                    result.data.results.forEach { resultItem ->
                        resultItem.candidates.forEach { candidate ->
                            val detectedPlateScore = candidate.score as Double
                            val scorePercentage = detectedPlateScore * 100
                            val formattedScore = String.format("%.1f%%", scorePercentage)
                            candidatePlateScoreText.append("${formattedScore}\n")
                        }
                    }

                    binding.tvPlate.text = detectedPlate.toString()
                    binding.tvPlateScore.text = detectedPlateScoreText.toString()
                    binding.tvCandidatePlate.text = candidatePlate.toString()
                    binding.tvCandidatePlateScore.text = candidatePlateScoreText.toString()
                    checkPlateNumber(result.data)
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Log.e(TAG, "Error sending photo: ${result.error}")
                }

                else -> {}
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun sendPhoto(
        file : File?,
        regions : String
    ) : LiveData<Result<LicensePlateResponse>> = liveData{
        emit(Result.Loading)
        try {
            Log.d(TAG, "Preparing to send photo...")
            val apiService = ApiConfig.getLicensePlateApiService(tokenReader)
            if (file != null) {
                val files = file.reduceFileImage()
                val region = regions.toRequestBody("text/plain".toMediaType())
                val imageFile = files.asRequestBody("image/jpeg".toMediaType())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "upload",
                    files.name,
                    imageFile
                )
                Log.d(TAG, "Sending request to API...")
                val response = withContext(Dispatchers.IO) {
                    apiService.sendLicensePlate(imageMultipart, region)
                }
                Log.d(TAG, "API response received: $response")
                emit(Result.Success(response))
            } else {
                Log.e(TAG, "Image file is null")
                emit(Result.Error("Image Kosong"))
            }
        }catch (e : HttpException){
            emit(Result.Error(e.message.toString()))
        }catch (e : Exception){
            emit(Result.Error(e.message.toString()))
        }
    }

    private fun checkPlateNumber(result: LicensePlateResponse) {
        var plateFound = false
        result.results.forEach { resultItem ->
            val detectedPlate = resultItem.plate.toUpperCase(Locale.ROOT)
            if (registeredPlates.contains(detectedPlate) || resultItem.candidates.any {
                    registeredPlates.contains(it.plate.toUpperCase(Locale.ROOT)) }) {
                Log.d("PlateNumber", "Plate number found in the registered plates list.")
                plateFound = true

                greenLEDListener(1) {
                    servoListener(90) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            greenLEDListener(0) {
                                servoListener(180) {
                                    isWaiting = false
                                }
                            }
                        }, 5000)
                    }
                }
            }
        }

        if (plateFound) {
            showAlertDialog("Plate Number Found")
        } else {
            showAlertDialog("Plate Number Not Found")
            Log.d("PlateNumber", "Plate number not found.")

            redLEDListener(1) {
                Handler(Looper.getMainLooper()).postDelayed({
                    redLEDListener(0) {
                        isWaiting = false
                    }
                }, 5000)
            }
        }
    }

    private fun fetchRegisteredPlates() {
        plateRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                registeredPlates.clear()
                for (plateSnapshot in snapshot.children) {
                    plateSnapshot.key?.let { plateNumber ->
                        registeredPlates.add(plateNumber)
                    }
                }
                Log.d("RegisteredPlates", "Plates: $registeredPlates")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RegisteredPlates", "Failed to read plates: ${error.message}")
            }
        })
    }

    private fun showAlertDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 2000)
    }

    private fun greenLEDListener(value: Int, onComplete: () -> Unit) {
        val client = ApiConfig.getApiSevice().greenLEDListener(tokenBlynk, value)
        client.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onComplete()
                    Log.d("Testt", "Green LED updated successfully")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Testt", "Failed to update red LED: ${t.message}")
            }
        })
    }

    private fun redLEDListener(value: Int, onComplete: () -> Unit) {
        val client = ApiConfig.getApiSevice().redLEDListener(tokenBlynk, value)
        client.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onComplete()
                    Log.d("Testt", "Red LED updated successfully")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Testt", "Failed to update red LED: ${t.message}")
            }
        })
    }

    private fun servoListener(value: Int, onComplete: () -> Unit) {
        val client = ApiConfig.getApiSevice().servoListener(tokenBlynk, value)
        client.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onComplete()
                    Log.d("Testt", "Servo updated successfully")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Testt", "Failed to update servo: ${t.message}")
            }
        })
    }


    companion object {
        private const val TAG = "Testt"
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}