package com.icesi.reto1mapasxd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private static final int REQUEST_CODE = 11 ;

    private GoogleMap mMap;

//    private FusedLocationProviderClient fusedLocation;

    private LocationManager manager;

    public EditText textView_cercano;

    //private FloatingActionButton boton;

    public Dialog epicDialog;

    private EditText et_lugar;

    private Button btn_agregar;

    private ImageView close_popup;

    public Double lat;

    public Double lon;

    public LatLng newLatLng;

    public MarkerOptions actual;

    public ArrayList<MarkerOptions> marcadores;

    public Marker marc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        epicDialog = new Dialog(this);
        textView_cercano = findViewById(R.id.textView_cercano);
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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, REQUEST_CODE);

        //boton = (FloatingActionButton) findViewById(R.id.fab);


        //boton.setOnClickListener(this);
        marcadores= new ArrayList<MarkerOptions>();


        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, new LocationListener() {

            private boolean first=false;
            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();
                LatLng ln = new LatLng(lat,lon);


                if(first==false){
                    actual = new MarkerOptions().position(ln).title("Mi ubicación").icon(BitmapDescriptorFactory.fromResource(R.drawable.manx));
                    marc = mMap.addMarker(actual);
                    //mMap.addMarker(actual);
                    first=true;
                }
                else{

                    marc.remove();

                    actual = new MarkerOptions().position(ln).title("Mi ubicación").icon(BitmapDescriptorFactory.fromResource(R.drawable.manx));
                    marc = mMap.addMarker(actual);

                    //mMap.addMarker(actual);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(ln));

                    Toast.makeText(getApplicationContext(), "Ubicación actual actualizada", Toast.LENGTH_LONG).show();

                    String d = masCercano();
                    textView_cercano.setText(d);
                }



            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });



        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {


                    String msj = "Dirección: ";

                    try {
                        Geocoder geo = new Geocoder(MapsActivity.this.getApplicationContext(), Locale.getDefault());
                        List<Address> addresses = geo.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
                        if (addresses.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Esperando por la dirección...", Toast.LENGTH_LONG).show();
                        }
                        else {
                            if (addresses.size() > 0) {
                                    marker.setSnippet(msj + addresses.get(0).getAddressLine(0) + addresses.get(0).getAdminArea());

                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace(); // getFromLocation() may sometimes fail
                    }



                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                newLatLng = latLng;
                showPopup();

            }
        });



        }

        private void showPopup(){

            epicDialog.setContentView(R.layout.epic_popup);
            close_popup = (ImageView) epicDialog.findViewById(R.id.btn_cerrar);
            btn_agregar= epicDialog.findViewById(R.id.btn_agregarLugar);
            et_lugar = epicDialog.findViewById(R.id.et_lugar);

            close_popup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    epicDialog.dismiss();
                }
            });

            btn_agregar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    guardarLugar( et_lugar.getText().toString());
                    epicDialog.dismiss();
                }
            });


            epicDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            epicDialog.show();


        }

        public void guardarLugar(String lugar){
            MarkerOptions nuevo = new MarkerOptions().position(newLatLng).title(lugar).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
            marcadores.add(nuevo);
            mMap.addMarker(nuevo);
            String d = masCercano();
            textView_cercano.setText(d);


            Toast.makeText(this, "Nuevo marcador agregado", Toast.LENGTH_LONG).show();

        }


        public String masCercano(){
            int n = marcadores.size();
            String m = "El lugar más cercano es: ";

            if(n==0){
                return m;
            }


            Location local = new Location("local");
            local.setLatitude(actual.getPosition().latitude);
            local.setLongitude(actual.getPosition().longitude);
            List<Float> flotantes = new ArrayList<>();


            for (int i =0; i < n;i++ ) {
                Location l = new Location("nuevo" + i);
                l.setLatitude(marcadores.get(i).getPosition().latitude);
                l.setLongitude(marcadores.get(i).getPosition().longitude);
                float x = local.distanceTo(l);
                flotantes.add(x);
            }

            int indice =0;


               Float  mayor= flotantes.get(0);



            for (int i =0; i < n;i++){
                if(i==0){
                    indice=i;
                }
                else{
                    if(flotantes.get(i) < mayor){
                        mayor=flotantes.get(i);
                        indice=i;
                    }
                }
            }
            float cer = 501;

            if(mayor < cer) {
                m += marcadores.get(indice).getTitle() + " a " + mayor + " metros";
            }
            else{
                m += marcadores.get(indice).getTitle();
            }


            return m;
        }


    @Override
    public void onClick(View v) {
        showPopup();
    }
}
