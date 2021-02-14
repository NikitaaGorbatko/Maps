package nikitagorbatko.example.maps

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.lang.NullPointerException
import kotlin.math.PI
import kotlin.math.sin

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    //longitude - Долгота x
    //latitude - широта y

    private lateinit var mMap: GoogleMap
    private lateinit var buttonAB: Button
    private var options: MarkerOptions? = null
    private var latLngA: LatLng? = null
    private var latLngB: LatLng? = null

    private var northeast: LatLng? = null
    private var southwest: LatLng? = null
    private var northwest: LatLng? = null
    private var southeast: LatLng? = null

    private var polylineABOptions: PolylineOptions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        buttonAB = findViewById<Button>(R.id.button).also {
            it.setOnClickListener {
                polylineABOptions = null
                mMap.clear()
                latLngA = null
                latLngB = null
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        buttonAB.isEnabled = true

        mMap.setOnMapClickListener {
            if (latLngA != null && latLngB != null) return@setOnMapClickListener

            if (latLngA == null) {
                latLngA = it
                addMarker(it, "A")
                return@setOnMapClickListener
            }

            if (latLngB == null) {
                latLngB = it
                addMarker(it, "B")
            }

            val latLngC = Computer.getLatLangC(latLngA, latLngB)

            polylineABOptions = PolylineOptions()
                .add(latLngA, latLngB)
                .width(3f)
                .color(Color.BLUE)
                .jointType(JointType.ROUND)

            val polylineAB = mMap.addPolyline(
                polylineABOptions
            )

            val a = Computer.getLength(latLngA, latLngC)
            val b = Computer.getLength(latLngB, latLngC)

            val cornerA = Computer.getDegreesA(a, b)
            val cornerB = 180 - 90 - cornerA

            val dotsRight = Computer.isCrossed(latLngA!!, latLngB!!, northeast!!, southeast!!)
            val dotsLeft = Computer.isCrossed(latLngA!!, latLngB!!, northwest!!, southwest!!)
            val dotsTop = Computer.isCrossed(latLngA!!, latLngB!!, northwest!!, northeast!!)
            val dotsBottom = Computer.isCrossed(latLngA!!, latLngB!!, southeast!!, southwest!!)
            var c: Double

            if (Computer.getLength(dotsLeft, dotsRight) > Computer.getLength(dotsTop, dotsBottom)) {
                val polylineAc = mMap.addPolyline(
                    PolylineOptions()
                        .add(dotsTop, dotsBottom)
                        .width(3f)
                        .color(Color.BLUE)
                        .jointType(JointType.ROUND)
                )
                c = 0.00018 / sin(cornerA)//0.00018 - 20 meters in LatLng
                var i = 100
                var counter1 = dotsTop!!.longitude + c
                var counter2 = dotsBottom!!.longitude + c
                while (i > 0) {
                    mMap.addPolyline(
                        PolylineOptions()
                            .add(LatLng(counter1, dotsTop.latitude), LatLng(counter2, dotsBottom.latitude))
                            .width(3f)
                            .color(Color.BLUE)
                            .jointType(JointType.ROUND)
                    )
                    counter1 += c
                    counter2 += c
                    i--
                }
                counter1 = dotsTop!!.longitude - c
                counter2 = dotsBottom!!.longitude - c
                var k = 100
                while (k > 0) {
                    mMap.addPolyline(
                        PolylineOptions()
                            .add(LatLng(counter1, dotsTop.latitude), LatLng(counter2, dotsBottom.latitude))
                            .width(3f)
                            .color(Color.BLUE)
                            .jointType(JointType.ROUND)
                    )
                    counter1 -= c
                    counter2 -= c
                    k--
                }

            } else {
                val polylineAx = mMap.addPolyline(
                    PolylineOptions()
                        .add(dotsLeft, dotsRight)
                        .width(3f)
                        .color(Color.BLUE)
                        .jointType(JointType.ROUND)
                )
                c = 0.00018 / sin(cornerB)//0.00018 - 20 meters in LatLng
                var i = 100
                var counter1 = dotsLeft!!.latitude + c
                var counter2 = dotsRight!!.latitude + c
                while (i > 0) {
                    mMap.addPolyline(
                        PolylineOptions()
                            .add(LatLng(counter1, dotsLeft.longitude), LatLng(counter2, dotsRight.longitude))
                            .width(3f)
                            .color(Color.BLUE)
                            .jointType(JointType.ROUND)
                    )
                    counter1 += c
                    counter2 += c
                    i--
                }
                counter1 = dotsLeft!!.latitude - c
                counter2 = dotsRight!!.latitude - c
                var k = 100
                while (k > 0) {
                    mMap.addPolyline(
                        PolylineOptions()
                            .add(LatLng(counter1, dotsLeft.longitude), LatLng(counter2, dotsRight.longitude))
                            .width(3f)
                            .color(Color.BLUE)
                            .jointType(JointType.ROUND)
                    )
                    counter1 -= c
                    counter2 -= c
                    k--
                }
            }
            Toast.makeText(baseContext, "$cornerA \n$cornerB\n$c", Toast.LENGTH_LONG).show()
            mMap.addMarker(options)
        }

        mMap.setOnCameraIdleListener {
            mMap.clear()
            northeast = mMap.projection.visibleRegion.latLngBounds.northeast
            southwest = mMap.projection.visibleRegion.latLngBounds.southwest
            northwest = LatLng(northeast!!.latitude, southwest!!.longitude)
            southeast = LatLng(southwest!!.latitude, northeast!!.longitude)

            try {
                addMarker(latLngA!!, "A")
                addMarker(latLngB!!, "B")
                mMap.addPolyline(polylineABOptions)
                val dotsRight = Computer.isCrossed(latLngA!!, latLngB!!, northeast!!, southeast!!)
                val dotsLeft = Computer.isCrossed(latLngA!!, latLngB!!, northwest!!, southwest!!)
                val dotsTop = Computer.isCrossed(latLngA!!, latLngB!!, northwest!!, northeast!!)
                val dotsBottom = Computer.isCrossed(latLngA!!, latLngB!!, southeast!!, southwest!!)

                if (Computer.getLength(dotsLeft, dotsRight) > Computer.getLength(dotsTop, dotsBottom)) {
                    val polylineAc = mMap.addPolyline(
                        PolylineOptions()
                            .add(dotsTop, dotsBottom)
                            .width(3f)
                            .color(Color.BLUE)
                            .jointType(JointType.ROUND)
                    )
                } else {
                    val polylineAx = mMap.addPolyline(
                        PolylineOptions()
                            .add(dotsLeft, dotsRight)
                            .width(3f)
                            .color(Color.BLUE)
                            .jointType(JointType.ROUND)
                    )
                }
            } catch (ex: NullPointerException) { }
        }
    }

    private fun addMarker(latLng: LatLng, title: String) {
        options = MarkerOptions().position(latLng).title(title)
        mMap.addMarker(options)
    }
}