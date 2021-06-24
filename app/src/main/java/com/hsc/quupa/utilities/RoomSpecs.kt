package com.hsc.quupa.utilities

import kotlin.math.abs

class RoomSpecs(
    var imageWidth: Int = 0,
    var imageHeight: Int = 0,
    var width: Int = 0,
    var height: Int = 0,
    var west: Double = 0.0,
    var north: Double = 0.0,
    var east: Double = 0.0,
    var south: Double = 0.0
) {

    fun getRoomSpec(room: String): RoomSpecs {
        return when(room) {
            "107" -> RoomSpecs(1080, 482, 1300, 580, -4.879518, 53.012048, 125.240963, -5.120481)
            "108" -> RoomSpecs(1013, 434, 1266, 542, -2.857142, 77.142858, 123.116883, 133.857142)
            "109" -> RoomSpecs(1010, 435, 1254, 540, -2.857142, 27.142858, 123.116883, 85.857142)
            "110" -> RoomSpecs(1029, 400, 1147, 456, 8.857142,-2.068965, 112.448979, 37.068965)
            "121" -> RoomSpecs(874, 670, 508, 390, -1.159420, -1.451631, 37.451631, 49.507246)
            "122" -> RoomSpecs(880, 667, 511, 388, -1.159420, 34.548369, 73.451631, 49.507246)
            "111" -> RoomSpecs(1013, 434, 1266, 542, -2.857142, -2.068965, 123.116883, 50.068965)
            "112" -> RoomSpecs(1010, 435, 1254, 540, -2.857142, 27.142858, 123.116883, 82.857142)
            "fullmap" -> RoomSpecs(860, 783, 1287, 1262, -4.032258, 123.548387, 133.870967, -3.387096)
            "full_12x" -> RoomSpecs(767, 527, 767, 527, -2.4, 50.4, 74.4, -2.4)
            else -> RoomSpecs()
        }
    }

    fun getMoveX(value: Double, scale: Float, room: String): Double {
        return when(room) {
            "107" -> {
                when {
                    value + 100 > 1300 -> {
                        value - (abs(1300 - value) + (200 / scale))
                    }
                    value - 100 < 20 -> {
                        value + ((170.0 - (scale * 10)) + 15)
                    }
                    else -> {
                        value
                    }
                }
            }
            "108" -> {
                when {
                    value + 100 > 1266 -> {
                        value - (abs(1266 - value) + (200 / scale))
                    }
                    value - 100 < 20 -> {
                        value + ((170.0 - (scale * 10)) + 15)
                    }
                    else -> {
                        value
                    }
                }
            }
            "109" -> {
                when {
                    value + 100 > 1254 -> {
                        value - (abs(1254 - value) + (320 / scale))
                    }
                    value - 100 < 20 -> {
                        value + ((170.0 - (scale * 10)) + 15)
                    }
                    else -> {
                        value
                    }
                }
            }
            "110" -> {
                when {
                    value + 100 > 1147 -> {
                        value - (abs(1147 - value) + (320 / scale))
                    }
                    value - 100 < 20 -> {
                        value + ((170.0 - (scale * 10)) + 15)
                    }
                    else -> {
                        value
                    }
                }
            }
            else -> {
                return value
            }
        }
    }

    fun getMoveY(value: Double, scale: Float, room: String) : Double {
        return when(room) {
            "107" -> {
                if(value + 100 > 580) {
                    value - abs(580 - (value + (260 / scale)))
                } else {
                    value
                }
            }
            "108" -> {
                if(value + 100 > 542) {
                    value - abs(542 - (value + (260 / scale)))
                } else {
                    value
                }
            }
            "109" -> {
                if(value + 100 > 540) {
                    value - abs(540 - (value + (260 / scale)))
                } else {
                    value
                }
            }
            "110" -> {
                if(value + 100 > 456) {
                    value - abs(456 - (value + (260 / scale)))
                } else {
                    value
                }
            }
            else -> {
                value
            }
        }
    }
}