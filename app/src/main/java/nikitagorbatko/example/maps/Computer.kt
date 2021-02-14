package nikitagorbatko.example.maps

import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt

object Computer {

    //The fun is from https://habr.com/ru/post/523440/
    fun isCrossed(a: LatLng, b: LatLng, c: LatLng, d: LatLng): LatLng? {
        val n = if (b.latitude - a.latitude != 0.toDouble()) {
            val q = (b.longitude - a.longitude) / (a.latitude - b.latitude)
            val sn = (c.longitude - d.longitude) + (c.latitude - d.latitude) * q
            //if (!sn) return null
            val fn = (c.longitude - a.longitude) + (c.latitude - a.latitude) * q
            fn / sn
        } else {
            val qwe = c.latitude - d.latitude
            //if (!(c.latitude - d.latitude))  return null
            (c.latitude - a.latitude) / (c.latitude - d.latitude)
        }
        return LatLng(
            c.latitude + (d.latitude - c.latitude) * n,
            c.longitude + (d.longitude - c.longitude) * n
        )
    }

    fun computeOffset(from: LatLng, distance: Double, heading: Double): LatLng? {
        var distance = distance
        var heading = heading
        distance /= 6371009.0 //earth_radius = 6371009 # in meters
        heading = Math.toRadians(heading)
        val fromLat = Math.toRadians(from.latitude)
        val fromLng = Math.toRadians(from.longitude)
        val cosDistance = Math.cos(distance)
        val sinDistance = Math.sin(distance)
        val sinFromLat = Math.sin(fromLat)
        val cosFromLat = Math.cos(fromLat)
        val sinLat = cosDistance * sinFromLat + sinDistance * cosFromLat * Math.cos(heading)
        val dLng = Math.atan2(
            sinDistance * cosFromLat * Math.sin(heading),
            cosDistance - sinFromLat * sinLat
        )
        return LatLng(Math.toDegrees(Math.asin(sinLat)), Math.toDegrees(fromLng + dLng))
    }

    private fun radianToDegree(radian: Double) = radian * 180 / Math.PI

    //fun round(num: Double) = Math.round(num * 100.0000) / 100.0000

    fun getLength(first: LatLng?, second: LatLng?)
            = sqrt(
        (second!!.longitude - first!!.longitude).pow(2) +
                (second.latitude - first.latitude).pow(2)
    )

    fun getDegreesA(a: Double, b: Double): Double
            = radianToDegree(atan(a / b))

    fun getLatLangC(latLngA: LatLng?, latLngB: LatLng?)
            = LatLng(latLngA!!.latitude, latLngB!!.longitude)
}