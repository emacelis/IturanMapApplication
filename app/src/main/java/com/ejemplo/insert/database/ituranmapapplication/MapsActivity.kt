package com.ejemplo.insert.database.ituranmapapplication

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    //VARIABLES PARA LA UBICACION
    private val permisoFineLocation=android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permisoCoarseLocation=android.Manifest.permission.ACCESS_FINE_LOCATION
    private val CODIGO_SOLICITUD_PERMISO=100 //Codigo de permiso
    var fusedLocationClient: FusedLocationProviderClient?=null//permiso para pedir la Latitud
    var locationRequest: LocationRequest?=null
    var callback: LocationCallback?=null


    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient= FusedLocationProviderClient(this)
        inicializarLocationRequest()

        callback=object :LocationCallback(){

            override fun onLocationResult(locationResoult: LocationResult?) {
                super.onLocationResult(locationResoult)//location resoult entregar un objeto que tiene un arreglo de las ubcicaciones mas recientes

                for (ubicacion in locationResoult?.locations!!){
                    Toast.makeText(applicationContext,ubicacion.latitude.toString()+","+ubicacion.longitude.toString(),
                        Toast.LENGTH_LONG).show()

                    //Add a marker in Sydney and move the camera
                 val miPosicion=LatLng(ubicacion.latitude, ubicacion.longitude)
                    mMap.addMarker(MarkerOptions().position(miPosicion!!).title("Aqui estoy"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(miPosicion))
                }
            }
        }
    }

    //Se tiene que configurtar los parametros de actualizacion
    private fun inicializarLocationRequest(){
        locationRequest= LocationRequest()
        locationRequest?.interval=1000//intervalo de actualizacion
        locationRequest?.fastestInterval=5000//rango maximo de tiempo
        locationRequest?.priority=LocationRequest.PRIORITY_HIGH_ACCURACY//10m de variacion
    }




    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //Se carga el metodo desde la inicializacion del mapa
        //Generaba error al cargaro en el onStart
        super.onStart()

        if (validarPermisosUbuicacion()){
            obtenerUbicacion()
        }
        else{
            pedirPermisos()
        }

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun validarPermisosUbuicacion(): Boolean {
        val hayUbicacionPrecisa=
            ActivityCompat.checkSelfPermission(this,permisoFineLocation)== PackageManager.PERMISSION_GRANTED//checkSelfPermission es para
        //verificar servicios entre si
        val hayubicacionOrdinaria=
            ActivityCompat.checkSelfPermission(this,permisoCoarseLocation)== PackageManager.PERMISSION_GRANTED
        //Se verifican los permisos de la app
        return hayUbicacionPrecisa && hayubicacionOrdinaria
    }

    private fun obtenerUbicacion() {
        fusedLocationClient?.requestLocationUpdates(locationRequest,callback,null)
    }
    private fun pedirPermisos() {
        //shouldShowRequestPermissionRationale si el uduario nego los permisos, se le da un contexto a el usuario porque es enecesario
        //el permiso
        val deboProveerContexto=
            ActivityCompat.shouldShowRequestPermissionRationale(this,permisoFineLocation)
        if(deboProveerContexto){
            solicitudPermiso()
        }
        else{
            solicitudPermiso()
        }
    }
    private fun solicitudPermiso() {
        // Con esto se mapea mi codigo con los servicios
        requestPermissions(arrayOf(permisoFineLocation,permisoCoarseLocation),CODIGO_SOLICITUD_PERMISO)
    }
    private fun detenerActualizaciondeUbicacion() {
        fusedLocationClient?.removeLocationUpdates(callback)
    }


    override fun onPause() {
        super.onPause()
        detenerActualizaciondeUbicacion()
    }

}
