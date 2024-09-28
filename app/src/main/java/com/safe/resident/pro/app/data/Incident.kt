package com.safe.resident.pro.app.data

data class Incident(
    val incidentID: String = "",
    val userID: String = "",
    val incidentName: String = "",
    val latLong: String = "",
    val status: Boolean = false,
    val dateTime: String = ""
)