package nikitagorbatko.example.maps

import com.google.android.gms.maps.model.LatLng

object Computer {
    //https://habr.com/ru/post/523440/
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
        return LatLng(c.latitude + (d.latitude - c.latitude) * n,
            c.longitude + (d.longitude - c.longitude) * n )
    }
}