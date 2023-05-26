package ru.mirea.ivashechkinav.osmmaps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import ru.mirea.ivashechkinav.osmmaps.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var binding: ActivityMainBinding
    private lateinit var locationNewOverlay: MyLocationNewOverlay
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        mapView = binding.mapView
        mapView.setZoomRounding(true)
        mapView.setMultiTouchControls(true)
        val mapController = mapView.controller
        mapController.setZoom(15.0)
        val startPoint = GeoPoint(55.794229, 37.700772)
        mapController.setCenter(startPoint)

        if(checkPermissions().isEmpty())
            initMain()
        else
            checkAndRequestPermissions()
    }
    fun initMain() {
        locationNewOverlay =
            MyLocationNewOverlay(GpsMyLocationProvider(applicationContext), mapView)
        locationNewOverlay.enableMyLocation()
        mapView.overlays.add(this.locationNewOverlay)

        val compassOverlay = CompassOverlay(
            applicationContext, InternalCompassOrientationProvider(
                applicationContext
            ), mapView
        )
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)


        val context: Context = this.applicationContext
        val dm: DisplayMetrics = context.resources.displayMetrics
        val scaleBarOverlay = ScaleBarOverlay(mapView)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10)
        mapView.overlays.add(scaleBarOverlay)
        addMarker(GeoPoint(55.794229, 37.700772), "MIREA №1")
        addMarker(GeoPoint(55.670072, 37.479164), "MIREA №2")
        addMarker(GeoPoint(55.731688, 37.574496), "MIREA №3")
    }
    fun addMarker(geoPoint: GeoPoint, text: String) {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.setOnMarkerClickListener { marker, mapView ->
            Toast.makeText(
                applicationContext, text,
                Toast.LENGTH_SHORT
            ).show()
            true
        }
        mapView.overlays.add(marker)

        marker.icon = ResourcesCompat.getDrawable(
            resources, org.osmdroid.library.R.drawable.osm_ic_follow_me_on, null)

        marker.title = text
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty() && !grantResults.contains(PackageManager.PERMISSION_DENIED)){
            initMain()
        }
    }

    override fun onResume() {
        super.onResume()
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        Configuration.getInstance().save(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        mapView.onPause()
    }
    fun checkPermissions(): List<String> {
        val permissionsToRequest = mutableListOf<String>()
        for (permission in LOCATION_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }
        return permissionsToRequest
    }
    fun checkAndRequestPermissions() {
        val permissionsToRequest = checkPermissions()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                permissionsToRequest.toTypedArray(),
                123
            )
        }
    }
    companion object {
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }
}