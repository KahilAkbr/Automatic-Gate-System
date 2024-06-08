package com.example.iot_licenseplatedetection.api.response

import com.google.gson.annotations.SerializedName

data class LicensePlateResponse(

	@field:SerializedName("processing_time")
	val processingTime: Any,

	@field:SerializedName("filename")
	val filename: String,

	@field:SerializedName("camera_id")
	val cameraId: Any,

	@field:SerializedName("results")
	val results: List<ResultsItem>,

	@field:SerializedName("version")
	val version: Int,

	@field:SerializedName("timestamp")
	val timestamp: String
)

data class Box(

	@field:SerializedName("ymin")
	val ymin: Int,

	@field:SerializedName("xmin")
	val xmin: Int,

	@field:SerializedName("ymax")
	val ymax: Int,

	@field:SerializedName("xmax")
	val xmax: Int
)

data class CandidatesItem(

	@field:SerializedName("score")
	val score: Any,

	@field:SerializedName("plate")
	val plate: String
)

data class Region(

	@field:SerializedName("score")
	val score: Any,

	@field:SerializedName("code")
	val code: String
)

data class Vehicle(

	@field:SerializedName("score")
	val score: Any,

	@field:SerializedName("box")
	val box: Box,

	@field:SerializedName("type")
	val type: String
)

data class ResultsItem(

	@field:SerializedName("score")
	val score: Any,

	@field:SerializedName("candidates")
	val candidates: List<CandidatesItem>,

	@field:SerializedName("dscore")
	val dscore: Any,

	@field:SerializedName("box")
	val box: Box,

	@field:SerializedName("plate")
	val plate: String,

	@field:SerializedName("region")
	val region: Region,

	@field:SerializedName("vehicle")
	val vehicle: Vehicle
)
