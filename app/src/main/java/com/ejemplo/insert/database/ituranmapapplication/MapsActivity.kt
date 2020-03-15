package com.ejemplo.insert.database.ituranmapapplication

import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener,GoogleMap.OnMarkerDragListener {
    //VARIABLES PARA LA UBICACION
    private val permisoFineLocation=android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permisoCoarseLocation=android.Manifest.permission.ACCESS_FINE_LOCATION
    private val CODIGO_SOLICITUD_PERMISO=100 //Codigo de permiso
    var fusedLocationClient: FusedLocationProviderClient?=null//permiso para pedir la Latitud
    var locationRequest: LocationRequest?=null
    var callback: LocationCallback?=null

    //variable para guardar un marcado dado por el usuario
    private var listaMarcadores: ArrayList<Marker>? = null

    //Variable para obtener la posiscion de forma local
    private var miPosicion:LatLng? = null
    //Variable para actualizar  la ubicaicón
    private var nuevaruta:Polyline?=null

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
               //Se verifica si existe el mapa.. se añade boton para obtener ubicación
                if(mMap!=null){
                    //boton para obtener la ubicacion
                    mMap.isMyLocationEnabled=true
                    mMap.uiSettings.isMyLocationButtonEnabled=true
                for (ubicacion in locationResoult?.locations!!){
                    //Add a marker in Sydney and move the camera
                    miPosicion=LatLng(ubicacion.latitude, ubicacion.longitude)
                    mMap.addMarker(MarkerOptions().position(miPosicion!!).title("Te encuentras aquí"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(miPosicion))
                }
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





        Listeners()

        //funcion para preparar los marcadores
        prepararMarcadores()
    }


    //POLYLINE------------------
    private fun dibujarLineas() {

        //Se importaPolylineOptions
        val coordenadasLineas= PolylineOptions()
            .add(LatLng(19.43420011141154, -99.147705696523))
            .add(LatLng(19.44120011141154, -99.146705696582))
            .add(LatLng(19.44400011141154, -99.140705696582))
            .add(LatLng(19.43720011141154, -99.137705696582))

            mMap.addPolyline(coordenadasLineas)
    }

    private fun Listeners() {
        //metodo que hace referencia a los marcadores
        mMap.setOnMarkerClickListener(this)
        //metodo para aÒadir ubicacion a un nuevo marcador
        //para esto aÒadimos extends implemts...GoogleMap.OnMarkerDragListener
        mMap.setOnMarkerDragListener(this)
    }

    private fun prepararMarcadores() {
        listaMarcadores=ArrayList()

        //se ocupa onLongClickListener porque es lo mejor para los mapas ya que pueden tener diferentes tipos de listener
        mMap.setOnMapLongClickListener {
                location:LatLng? ->

            listaMarcadores?.add(mMap.addMarker(MarkerOptions()
                .position(location!!)
                .title("Tu marcador"))
            )

            //para mover los marcadores que aÒadimos con un click
            listaMarcadores?.last()!!.isDraggable=true

            //coordenadas va a obtener el ultimo marcador
            val cooredenadas=LatLng(listaMarcadores?.last()!!.position.latitude, listaMarcadores?.last()!!.position.longitude)

            //se manda allamar la URL para hacer la uniion entre las dos corendadas
            val origen = "origin="+ miPosicion?.latitude + "," + miPosicion?.longitude + "&"
            val destino = "destination="+cooredenadas.latitude + "," + cooredenadas.longitude + "&"
            val parametros = origen + destino +"sensor=false&mode=driving"

            Log.d("URL","http://maps.googleapis.com/maps/api/directions/json?" +parametros)
            cargarURL("http://maps.googleapis.com/maps/api/directions/json?" +parametros )
        }
    }

    //VALIDAR PERMISOS
    private fun validarPermisosUbicacion():Boolean{
        val hayUbicacionPrecisa=ActivityCompat.checkSelfPermission(this,permisoFineLocation)==PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria=ActivityCompat.checkSelfPermission(this, permisoCoarseLocation)==PackageManager.PERMISSION_GRANTED

        return hayUbicacionPrecisa && hayUbicacionOrdinaria
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

    //SOLICITUD HTTPS
    private fun cargarURL(url:String){
        val queue = Volley.newRequestQueue(this)

        val solicitud = StringRequest(Request.Method.GET, url, Response.Listener<String>{
                response ->
            Log.d("HTTP",response)

            //METODO HA HACER
                val  coordenadas=obtenerCoordenadas(response)
            //Se actualiza la posoisción
            if(nuevaruta!=null){
                nuevaruta?.remove()
            }
               nuevaruta= mMap.addPolyline(coordenadas)
        }, Response.ErrorListener{})
        //añadir al quequue la solicitud
        queue.add(solicitud)
    }


    //se mapea la respuesta JSON
    private fun obtenerCoordenadas(json:String):PolylineOptions{
        val gson = Gson()
        val objeto = gson.fromJson(json, com.ejemplo.insert.database.Modelos.Response::class.java)
        val puntos =objeto.routes?.get(0)!!.legs?.get(0)!!.steps!!
        var coordenadas=PolylineOptions()

        for(punto in puntos){
            //coordenadas.add(LatLng(punto.start_location))
            coordenadas.add(punto.start_location?.toLatLng())
            coordenadas.add(punto.end_location?.toLatLng())
        }

        coordenadas.color(Color.BLACK)
        return coordenadas
    }


    //MARCADORES-------------------------------------------
    override fun onMarkerDragEnd(p0: Marker?) {
        val index =listaMarcadores?.indexOf(p0!!)
        Log.d("MARCADOR FINAL", listaMarcadores?.get(index!!)!!.position?.latitude.toString())    }

    override fun onMarkerDragStart(p0: Marker?) {
//variable que nos da el marcador
        Log.d("MARCADOR INICIAL", p0?.position?.latitude.toString())
        val index =listaMarcadores?.indexOf(p0!!)
        Log.d("MARCADOR FINAL", listaMarcadores?.get(index!!)!!.position?.latitude.toString())
    }

    override fun onMarkerDrag(p0: Marker?) {
        title =p0?.position?.latitude.toString() + "-" +p0?.position?.longitude.toString()
    }


    override fun onMarkerClick(p0: Marker?): Boolean {
        var numerodeClick = p0?.tag as? Int

        if(numerodeClick!= null){
            numerodeClick++
            p0?.tag=numerodeClick
            Toast.makeText(this, "se han dado" +numerodeClick.toString()+ "clciks", Toast.LENGTH_SHORT).show()
        }

        return false
    }



    //CICLO DE VIDA-------------------------------------------------------

    override fun onStart(){
        super.onStart()

        if(validarPermisosUbicacion()){
            obtenerUbicacion()
        }else{
            pedirPermisos()
        }
    }
    override fun onPause() {
        super.onPause()
        detenerActualizaciondeUbicacion()
    }

}
