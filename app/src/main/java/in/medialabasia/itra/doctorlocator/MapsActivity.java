package in.medialabasia.itra.doctorlocator;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonObject;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LatLng latLng;
    private Marker currLocationMarker;
    private LocationRequest mLocationRequest;
    private List<Polyline> polyLineList;
    private Map<Marker, MarkerData> markerDataMap;
    private boolean noFilters = false;


    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }


    //TODO: Implement these
    //http://stackoverflow.com/questions/14226453/google-maps-api-v2-how-to-make-markers-clickable
    //https://developers.google.com/maps/documentation/directions/intro


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mGoogleApiClient.connect();
        getAllResources();
        //Add a marker in Sydney and move the camera
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.setOnMarkerClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }



    private void getAllResources()
    {
        String url = "http://192.168.43.245:3000/test";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray List) {
                try {
                    markerDataMap = new HashMap<>();
//                    JSONArray locationList = response.getJSONArray("location");
                    for(int i =0; i<List.length(); i++)
                    {
                        JSONObject resource = List.getJSONObject(i);
                        String[] location = resource.get("location").toString().split(",");
                        double lat = Double.parseDouble(location[0]);
                        double lng = Double.parseDouble(location[1]);
                        MarkerData markerData
                                = new MarkerData(resource.getString("name"),
                                resource.getString("specialization"),
                                resource.getString("email"),
                                resource.getString("phoneNumber"),
                                resource.getString("beds")
                        );

                        markerDataMap.put(addMarker(new LatLng(lat,lng),resource.getString("name")),markerData);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener(){
            public void onErrorResponse(VolleyError error)
            {
                Log.d("MESSAGE"," Volley error " + error.toString());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);


    }

    private Marker addMarker(LatLng latLng, String name)
    {
        return mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(name)
//                .icon(BitmapDescriptorFactory
//                        .fromResource(R.drawable.ic_local_hospital_black_24dp)
//                )
        );
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    12);
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null)
        {
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
//            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.position(latLng);
//            markerOptions.title("Current Position");
//            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//            currLocationMarker = mMap.addMarker(markerOptions);


            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng).zoom(14).build();

            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //5 seconds
        mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onLocationChanged(Location location) {
//        if (currLocationMarker != null) {
//            currLocationMarker.remove();
//        }
//        latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);
//        markerOptions.title("Current Position");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//        currLocationMarker = mMap.addMarker(markerOptions);
//
//        Toast.makeText(this,"Location Changed",Toast.LENGTH_SHORT).show();
        //zoom to current position:
        //If you only need one location, unregister the listener
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);


    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if(!marker.equals(currLocationMarker))
        {
//            String text = marker.getPosition().toString();
//            Toast toast = Toast.makeText(getBaseContext(), text, Toast.LENGTH_LONG);
//            toast.show();

            GoogleDirection.withServerKey(getString(R.string.server_key))
                    .from(currLocationMarker.getPosition())
                    .to(marker.getPosition())
                    .execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(Direction direction, String rawBody) {
                            Toast toast = Toast.makeText(getBaseContext(), "Directions Retrieved", Toast.LENGTH_LONG);
                            Route route =  direction.getRouteList().get(0);
                            Leg leg  = route.getLegList().get(0);
                            ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.RED);

                            if(polyLineList!=null && !polyLineList.isEmpty())
                            {
                                for (Polyline line:polyLineList)
                                {
                                    line.remove();
                                }
                            }
                            else
                            {
                                polyLineList = new ArrayList<Polyline>();
                            }

//                            mMap.addPolyline(polylineOptions);

                            polyLineList.add(mMap.addPolyline(polylineOptions));
                        }

                        @Override
                        public void onDirectionFailure(Throwable t) {
                            t.printStackTrace();
                            Toast toast = Toast.makeText(getBaseContext(), "Directions Failed", Toast.LENGTH_LONG);
                        }
                    });

        }
        return false;
    }

    public void ApplyFilters(View view) {
        {
            LinearLayout filterContainer = (LinearLayout) findViewById(R.id.filters);
            ArrayList<String> filterList = new ArrayList<>(10);
            boolean noneChecked = true;
            for(int i =0;i<filterContainer.getChildCount();i++)
            {
                CheckBox checkBox =(CheckBox)filterContainer.getChildAt(i);
                if(checkBox.isChecked())
                {
                    filterList.add(checkBox.getText().toString());
                    noneChecked = false;
                }
            }

            if(!noneChecked) {
                for (Map.Entry<Marker,MarkerData> entry: markerDataMap.entrySet())
                    {
                        Marker marker = entry.getKey();

                        boolean flag = false;
                        for (String string : filterList)
                        {
                            if(flag = flag||entry.getValue().getSpecialization().equals(string));
                            {
                                marker.setVisible(true);
                            }
                        }
                        if(!flag)
                        {
                            entry.getKey().setVisible(false);
                        }
                    }
            }
            else
                for (Map.Entry<Marker,MarkerData> entry: markerDataMap.entrySet())
                {
                    entry.getKey().setVisible(true);
                }
        }
    }

}
