package com.hsc.quupa.data.model.quupa.qda

data class QdaPositionResponse(
    val responseTimestampEpoch: Int,
    val color: String,
    val positionTimestamp: String,
    val version: String,
    val positionTimestampEpoch: Int,
    val positionX: Double,
    val positionY: Double,
    val positionZ: Double,
    val areaId: String,
    val areaName: String,
    val id: String,
    val smoothedPositionZ: Double,
    val smoothedPositionY: Double,
    val smoothedPositionX: Double
)