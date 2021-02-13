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
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt


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
        //(y1 - y2)x + (x2 - x1)y + (x1y2 - x2y1) = 0
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

            val latLngC = getLatLangC(latLngA, latLngB)

            val polylineAB = mMap.addPolyline(
                PolylineOptions()
                    .add(latLngA, latLngB)
                    .width(3f)
                    .color(Color.BLUE)
                    .jointType(JointType.ROUND)
            )

            val polylineAC = mMap.addPolyline(
                PolylineOptions()
                    .add(latLngA, latLngC)
                    .width(3f)
                    .color(Color.BLUE)
                    .jointType(JointType.ROUND)
            )

            val polylineBC = mMap.addPolyline(
                PolylineOptions()
                    .add(latLngB, latLngC)
                    .width(3f)
                    .color(Color.BLUE)
                    .jointType(JointType.ROUND)
            )

            val a = round(getLength(latLngA, latLngC))
            val b = round(getLength(latLngB, latLngC))

            val cornerA = round(getDegreesA(a, b))
            val cornerB = round(180 - 90 - cornerA)
            //longitude - Долгота x
            //latitude - широта y

            val dotsRight = Computer.isCrossed(latLngA!!, latLngB!!, northeast!!, southeast!!)
            val dotsLeft = Computer.isCrossed(latLngA!!, latLngB!!, northwest!!, southwest!!)
            val dotsTop = Computer.isCrossed(latLngA!!, latLngB!!, northwest!!, northeast!!)
            val dotsBottom = Computer.isCrossed(latLngA!!, latLngB!!, southeast!!, southwest!!)

            val polylineAx = mMap.addPolyline(
                PolylineOptions()
                    .add(dotsLeft, dotsRight)
                    .width(3f)
                    .color(Color.BLUE)
                    .jointType(JointType.ROUND)
            )

            val polylineAc = mMap.addPolyline(
                PolylineOptions()
                    .add(dotsTop, dotsBottom)
                    .width(3f)
                    .color(Color.BLUE)
                    .jointType(JointType.ROUND)
            )

            Toast.makeText(baseContext, "$cornerA \n$cornerB", Toast.LENGTH_LONG).show()
            mMap.addMarker(options)
        }

        mMap.setOnCameraIdleListener {
            northeast = mMap.projection.visibleRegion.latLngBounds.northeast
            southwest = mMap.projection.visibleRegion.latLngBounds.southwest
            northwest = LatLng(northeast!!.latitude, southwest!!.longitude)
            southeast = LatLng(southwest!!.latitude, northeast!!.longitude)
        }

        //mMap.setOnC
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun radianToDegree(radian: Double) = radian * 180 / Math.PI

    private fun round(num: Double) = Math.round(num * 100.000) / 100.000

    private fun getLength(first: LatLng?, second: LatLng?)
            = sqrt((second!!.longitude - first!!.longitude).pow(2) +
                (second!!.latitude - first!!.latitude).pow(2))

    private fun getDegreesA(a: Double, b: Double): Double = radianToDegree(atan(a / b))

    private fun getLatLangC(latLngA: LatLng?, latLngB: LatLng?)
            = LatLng(latLngA!!.latitude, latLngB!!.longitude)

    private fun addMarker(latLng: LatLng, title: String) {
        options = MarkerOptions().position(latLng).title(title)
        mMap.addMarker(options)
    }
}