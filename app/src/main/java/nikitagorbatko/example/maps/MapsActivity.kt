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
    private val distanceM = 0.00018

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
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15f))

        mMap.setOnMapClickListener {
            if (mMap.cameraPosition.zoom < 15) {
                Toast.makeText(baseContext, resources.getString(R.string.zoom_is_unavailable), Toast.LENGTH_LONG).show()
                return@setOnMapClickListener
            }
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
            if (latLngA == null && latLngB == null) return@setOnCameraIdleListener
            if (mMap.cameraPosition.zoom < 15) {
                Toast.makeText(baseContext, resources.getString(R.string.zoom_is_unavailable), Toast.LENGTH_LONG).show()
                return@setOnCameraIdleListener
            }
            mMap.clear()
            try {
                addMarker(latLngA!!, "A")
                addMarker(latLngB!!, "B")
                mMap.addPolyline(buildOptions(latLngA, latLngB))
                draw()
            } catch (ex: NullPointerException) { }
        }
    }

    //hardcoded, не рефакторил, есть проблемы с геометрией
    private fun draw() {
        val visibleBounds = mMap.projection.visibleRegion.latLngBounds
        northeast = mMap.projection.visibleRegion.latLngBounds.northeast
        southwest = mMap.projection.visibleRegion.latLngBounds.southwest
        northwest = LatLng(northeast!!.latitude, southwest!!.longitude)
        southeast = LatLng(southwest!!.latitude, northeast!!.longitude)

        val latLngC = Computer.getLatLangC(latLngA, latLngB)

        mMap.addPolyline(buildOptions(latLngA, latLngB))

        val a = Computer.getLength(latLngA, latLngC)
        val b = Computer.getLength(latLngB, latLngC)

        val cornerA = Computer.getDegreesA(a, b)
        val cornerB = 180 - 90 - cornerA
        val corner = if (cornerA > cornerB) cornerB else cornerA

        val latLngRCross = Computer.isCrossed(latLngA!!, latLngB!!, northeast!!, southeast!!)
        val latLngLCross = Computer.isCrossed(latLngA!!, latLngB!!, northwest!!, southwest!!)
        val latLngTCross = Computer.isCrossed(latLngA!!, latLngB!!, northwest!!, northeast!!)
        val latLngBCross = Computer.isCrossed(latLngA!!, latLngB!!, southeast!!, southwest!!)
        val sinc = sin(corner)
        val step = distanceM / sinc

        if (Computer.getLength(latLngLCross, latLngRCross) > Computer.getLength(
                latLngTCross,
                latLngBCross
            )) {
            mMap.addPolyline(buildOptions(latLngTCross, latLngBCross))
            var counter1 = latLngTCross!!.longitude + step
            var counter2 = latLngBCross!!.longitude + step

            while (visibleBounds.contains(LatLng(latLngTCross.latitude, counter1))
                || visibleBounds.contains(LatLng(latLngBCross.latitude, counter2))) {
                mMap.addPolyline(
                    buildOptions(
                        LatLng(latLngTCross.latitude, counter1), LatLng(
                            latLngBCross.latitude,
                            counter2
                        )
                    )
                )
                counter1 += step
                counter2 += step
            }
            counter1 = latLngTCross.longitude - step
            counter2 = latLngBCross.longitude - step
            while (visibleBounds.contains(LatLng(latLngTCross.latitude, counter1))
                || visibleBounds.contains(LatLng(latLngBCross.latitude, counter2))) {
                mMap.addPolyline(
                    buildOptions(
                        LatLng(latLngTCross.latitude, counter1), LatLng(
                            latLngBCross.latitude,
                            counter2
                        )
                    )
                )
                counter1 -= step
                counter2 -= step
            }
        } else {
            mMap.addPolyline(buildOptions(latLngLCross, latLngRCross))
            var counter1 = latLngLCross!!.latitude + step
            var counter2 = latLngRCross!!.latitude + step
            while (visibleBounds.contains(LatLng(counter1, latLngLCross.longitude))
                || visibleBounds.contains(LatLng(counter2, latLngRCross.longitude))) {
                mMap.addPolyline(
                    buildOptions(
                        LatLng(counter1, latLngLCross.longitude), LatLng(
                            counter2,
                            latLngRCross.longitude
                        )
                    )
                )
                counter1 += step
                counter2 += step
            }
            counter1 = latLngLCross.latitude - step
            counter2 = latLngRCross.latitude - step

            while (visibleBounds.contains(LatLng(counter1, latLngLCross.longitude))
                || visibleBounds.contains(LatLng(counter2, latLngRCross.longitude))) {
                mMap.addPolyline(
                    buildOptions(
                        LatLng(counter1, latLngLCross.longitude), LatLng(
                            counter2,
                            latLngRCross.longitude
                        )
                    )
                )
                counter1 -= step
                counter2 -= step
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