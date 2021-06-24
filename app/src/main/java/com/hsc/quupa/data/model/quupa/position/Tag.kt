package com.hsc.quupa.data.model.quupa.position

data class Tag(
    val areaId: String,
    val areaName: String,
    val color: String,
    val coordinateSystemId: String,
    val coordinateSystemName: String,
    val covarianceMatrix: List<Double>,
    val id: String,
    val name: String,
    val position: List<Double>,
    val positionAccuracy: Double,
    val positionTS: Long,
    val smoothedPosition: List<Double>,
    val zones: List<Zone>
)