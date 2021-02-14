package nikitagorbatko.example.maps

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlin.math.sin


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    //longitude - Долгота x
    //latitude - широта y

    private lateinit var mMap: GoogleMap
    private lateinit var buttonAB: Button
    private var options: MarkerOptions? = null
    private val sydneyLatLng = LatLng(-34.0, 151.0)
    private var latLngA: LatLng? = null
    private var latLngB: LatLng? = null

    private var northeast: LatLng? = null
    private var southwest: LatLng? = null
    private var northwest: LatLng? = null
    private var southeast: LatLng? = null
    private val DISTANCEM = 0.00018

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        buttonAB = findViewById<Button>(R.id.button).also {
            it.setOnClickListener {
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
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydneyLatLng))

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

            mMap.addPolyline(buildOptions(latLngA, latLngB))

            val a = Computer.getLength(latLngA, latLngC)
            val b = Computer.getLength(latLngB, latLngC)

            val cornerA = Computer.getDegreesA(a, b)
            val cornerB = 180 - 90 - cornerA

            draw()

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
                mMap.addPolyline(buildOptions(latLngA, latLngB))
                draw()
            } catch (ex: NullPointerException) { }
        }
    }

    //hardcode
    private fun draw() {
        val dotsRight = Computer.isCrossed(latLngA!!, latLngB!!, northeast!!, southeast!!)
        val dotsLeft = Computer.isCrossed(latLngA!!, latLngB!!, northwest!!, southwest!!)
        val dotsTop = Computer.isCrossed(latLngA!!, latLngB!!, northwest!!, northeast!!)
        val dotsBottom = Computer.isCrossed(latLngA!!, latLngB!!, southeast!!, southwest!!)

        val latLngC = Computer.getLatLangC(latLngA, latLngB)

        mMap.addPolyline(buildOptions(latLngA, latLngB))

        val a = Computer.getLength(latLngA, latLngC)
        val b = Computer.getLength(latLngB, latLngC)

        val cornerA = Computer.getDegreesA(a, b)
        val cornerB = 180 - 90 - cornerA

        val latLngRightCross = Computer.isCrossed(
            latLngA!!,
            latLngB!!,
            northeast!!,
            southeast!!
        )
        val latLngLeftCross = Computer.isCrossed(latLngA!!, latLngB!!, northwest!!, southwest!!)
        val latLngTopCross = Computer.isCrossed(latLngA!!, latLngB!!, northwest!!, northeast!!)
        val latLngBottomCross = Computer.isCrossed(
            latLngA!!,
            latLngB!!,
            southeast!!,
            southwest!!
        )
        val step: Double

        if (Computer.getLength(latLngLeftCross, latLngRightCross) > Computer.getLength(
                latLngTopCross,
                latLngBottomCross
            )) {
            mMap.addPolyline(buildOptions(latLngTopCross, latLngBottomCross))
            step = DISTANCEM / sin(cornerA)
            var i = 20
            var counter1 = latLngTopCross!!.longitude + step
            var counter2 = latLngBottomCross!!.longitude + step

            while (mMap.projection.visibleRegion.latLngBounds.contains(LatLng(counter1, latLngTopCross.latitude)) || mMap.projection.visibleRegion.latLngBounds.contains(LatLng(counter2, latLngBottomCross.latitude))) {
                mMap.addPolyline(
                    buildOptions(
                        LatLng(counter1, latLngTopCross.latitude), LatLng(
                            counter2,
                            latLngBottomCross.latitude
                        )
                    )
                )
                counter1 += step
                counter2 += step
                i--
            }
            counter1 = latLngTopCross.longitude - step
            counter2 = latLngBottomCross.longitude - step
            var k = 20
            while (mMap.projection.visibleRegion.latLngBounds.contains(LatLng(counter1, latLngTopCross.latitude)) || mMap.projection.visibleRegion.latLngBounds.contains(LatLng(counter2, latLngBottomCross.latitude))) {
                mMap.addPolyline(
                    buildOptions(
                        LatLng(counter1, latLngTopCross.latitude), LatLng(
                            counter2,
                            latLngBottomCross.latitude
                        )
                    )
                )
                counter1 -= step
                counter2 -= step
                k--
            }
        } else {
            mMap.addPolyline(buildOptions(latLngLeftCross, latLngRightCross))
            step = DISTANCEM / sin(cornerB)
            var i = 20
            var counter1 = latLngLeftCross!!.latitude + step
            var counter2 = latLngRightCross!!.latitude + step
            while (mMap.projection.visibleRegion.latLngBounds.contains(LatLng(counter1, latLngLeftCross.longitude)) || mMap.projection.visibleRegion.latLngBounds.contains(LatLng(counter2, latLngRightCross.longitude))) {
                mMap.addPolyline(
                    buildOptions(
                        LatLng(counter1, latLngLeftCross.longitude), LatLng(
                            counter2,
                            latLngRightCross.longitude
                        )
                    )
                )
                counter1 += step
                counter2 += step
                i--
            }
            counter1 = latLngLeftCross.latitude - step
            counter2 = latLngRightCross.latitude - step
            var k = 20
            while (mMap.projection.visibleRegion.latLngBounds.contains(LatLng(counter1, latLngLeftCross.longitude)) || mMap.projection.visibleRegion.latLngBounds.contains(LatLng(counter2, latLngRightCross.longitude))) {
                mMap.addPolyline(
                    buildOptions(
                        LatLng(counter1, latLngLeftCross.longitude), LatLng(
                            counter2,
                            latLngRightCross.longitude
                        )
                    )
                )
                counter1 -= step
                counter2 -= step
                k--
            }
        }
    }

    private fun addMarker(latLng: LatLng, title: String) {
        options = MarkerOptions().position(latLng).title(title)
        mMap.addMarker(options)
    }

    private fun buildOptions(a: LatLng?, b: LatLng?) = PolylineOptions()
        .width(3f)
        .color(Color.BLUE)
        .jointType(JointType.ROUND)
        .add(a, b)

}