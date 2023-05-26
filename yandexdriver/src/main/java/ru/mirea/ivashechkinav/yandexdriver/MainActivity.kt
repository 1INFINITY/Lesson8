package ru.mirea.ivashechkinav.yandexdriver

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.*
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError
import ru.mirea.ivashechkinav.yandexdriver.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), DrivingSession.DrivingRouteListener,
    UserLocationObjectListener {
    private lateinit var binding: ActivityMainBinding
    private var userLocationLayer: UserLocationLayer? = null
    private var ROUTE_START_LOCATION: Point = Point(55.670005, 37.479894)
    private val ROUTE_END_LOCATION: Point = Point(55.794259, 37.701448)
    private var SCREEN_CENTER: Point = Point(
        (ROUTE_START_LOCATION.getLatitude() + ROUTE_END_LOCATION.getLatitude()) / 2,
        (ROUTE_START_LOCATION.getLongitude() + ROUTE_END_LOCATION.getLongitude()) / 2
    )
    private var mapView: MapView? = null
    private var mapObjects: MapObjectCollection? = null
    private var drivingRouter: DrivingRouter? = null
    private var drivingSession: DrivingSession? = null
    private val colors = intArrayOf(-0x10000, -0xff0100, 0x00FFBBBB, -0xffff01)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(this)
        DirectionsFactory.initialize(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        mapView = binding.mapview

        if(checkPermissions().isEmpty())
            initMain()
        else
            checkAndRequestPermissions()
    }

    override fun onStop() {
        mapView!!.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView!!.onStart()
    }
    fun initMain() {
        mapView!!.map.isRotateGesturesEnabled = false
        // Устанавливаем начальную точку и масштаб
        mapView!!.map.move(
            CameraPosition(
                SCREEN_CENTER, 10f, 0f, 0f
            )
        )
        // Ининциализируем объект для создания маршрута водителя
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter()
        mapObjects = mapView!!.map.mapObjects.addCollection()
        loadUserLocationLayer()
        addDescription()
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
    private fun submitRequest() {
        val drivingOptions = DrivingOptions()
        val vehicleOptions = VehicleOptions()
        // Кол-во альтернативных путей
        drivingOptions.routesCount = 4
        val requestPoints: ArrayList<RequestPoint> = ArrayList()
        // Устанавка точек маршрута
        requestPoints.add(
            RequestPoint(
                ROUTE_START_LOCATION,
                RequestPointType.WAYPOINT,
                null
            )
        )
        requestPoints.add(
            RequestPoint(
                ROUTE_END_LOCATION,
                RequestPointType.WAYPOINT,
                null
            )
        )
        // Отправка запроса на сервер
        drivingSession = drivingRouter!!.requestRoutes(
            requestPoints, drivingOptions,
            vehicleOptions, this
        )
    }
    override fun onDrivingRoutes(list: List<DrivingRoute>) {
        var color: Int
        for (i in list.indices) {
// настроиваем цвета для каждого маршрута
            color = colors[i]
            // добавляем маршрут на карту
            mapObjects!!.addPolyline(list[i].geometry).setStrokeColor(color)
        }
    }

    override fun onDrivingRoutesError(error: Error) {
        var errorMessage ="Error"
        if (error is RemoteError) {
            errorMessage = "remote error"
        } else if (error is NetworkError) {
            errorMessage = "network error"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
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
        val lat = userLocationView.pin.geometry.latitude
        val lng = userLocationView.pin.geometry.longitude

        ROUTE_START_LOCATION = Point(lat, lng)
        SCREEN_CENTER = Point(
        (ROUTE_START_LOCATION.getLatitude() + ROUTE_END_LOCATION.getLatitude()) / 2,
        (ROUTE_START_LOCATION.getLongitude() + ROUTE_END_LOCATION.getLongitude()) / 2
        )
        submitRequest()
        addDescription()
    }

    override fun onObjectRemoved(p0: UserLocationView) {
        Log.d(this::class.simpleName, "Removed")
    }

    override fun onObjectUpdated(userLocationView: UserLocationView, p1: ObjectEvent) {
        userLocationLayer ?: return
        val lat = userLocationView.pin.geometry.latitude
        val lng = userLocationView.pin.geometry.longitude

        ROUTE_START_LOCATION = Point(lat, lng)
        SCREEN_CENTER = Point(
            (ROUTE_START_LOCATION.getLatitude() + ROUTE_END_LOCATION.getLatitude()) / 2,
            (ROUTE_START_LOCATION.getLongitude() + ROUTE_END_LOCATION.getLongitude()) / 2
        )
        submitRequest()
        addDescription()
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
    fun addDescription() {
        val marker = mapView!!.map.mapObjects.addPlacemark(
            ROUTE_END_LOCATION,
            ImageProvider.fromResource(this, R.drawable.ic_smile)
        )
        marker.addTapListener { mapObject, point ->
            Toast.makeText(
                application, "МИРЕАААА!!!!",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }
    companion object {
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }
}