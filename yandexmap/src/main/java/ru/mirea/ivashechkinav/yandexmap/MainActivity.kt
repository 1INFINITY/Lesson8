package ru.mirea.ivashechkinav.yandexmap

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.Manifest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CompositeIcon
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import ru.mirea.ivashechkinav.yandexmap.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), UserLocationObjectListener {
    private lateinit var binding: ActivityMainBinding
    private var userLocationLayer: UserLocationLayer? = null
    private var mapView: MapView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MapKitFactory.initialize(this)

        mapView = binding.mapview
        if(checkPermissions().isEmpty())
            initMain()
        else
            checkAndRequestPermissions()
    }
    fun initMain() {
        mapView!!.map.move(
            CameraPosition(
                Point(55.751574, 37.573856), 11.0f, 0.0f,
                0.0f
            ),
            Animation(Animation.Type.SMOOTH, 0f),
            null
        )
        loadUserLocationLayer()
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
    override fun onStop() {
        super.onStop()
        // Вызов onStop нужно передавать инстансам MapView и MapKit.
        mapView!!.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onStart() {
// Вызов onStart нужно передавать инстансам MapView и MapKit.
        super.onStart()
        mapView!!.onStart()
        MapKitFactory.getInstance().onStart()
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

    private fun loadUserLocationLayer() {
        val mapKit = MapKitFactory.getInstance()
        mapKit.resetLocationManagerToDefault()
        userLocationLayer = mapKit.createUserLocationLayer(mapView!!.mapWindow)
        userLocationLayer!!.setVisible(true)
        userLocationLayer!!.setHeadingEnabled(true)
        userLocationLayer!!.setObjectListener(this)
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        userLocationLayer ?: return
        userLocationLayer!!.setAnchor(
            PointF((mapView!!.width * 0.5).toFloat(), (mapView!!.height * 0.5).toFloat()),
            PointF((mapView!!.width * 0.5).toFloat(), (mapView!!.height * 0.83).toFloat())
        )
// При определении направления движения устанавливается следующая иконка
// При определении направления движения устанавливается следующая иконка
        userLocationView.getArrow().setIcon(
            ImageProvider.fromResource(
                this, R.drawable.arrow_up_float
            )
        )
// При получении координат местоположения устанавливается следующая иконка
// При получении координат местоположения устанавливается следующая иконка
        val pinIcon: CompositeIcon = userLocationView.getPin().useCompositeIcon()
        pinIcon.setIcon(
            "pin",
            ImageProvider.fromResource(this, R.drawable.search_result),
            IconStyle().setAnchor(PointF(0.5f, 0.5f))
                .setRotationType(RotationType.ROTATE)
                .setZIndex(1f)
                .setScale(0.5f)
        )
        userLocationView.getAccuracyCircle().setFillColor(Color.BLUE and -0x66000001)
    }

    override fun onObjectRemoved(p0: UserLocationView) {
        Log.d(this::class.simpleName, "Removed")
    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {
        Log.d(this::class.simpleName, "Updated")
    }

    companion object {
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }
}