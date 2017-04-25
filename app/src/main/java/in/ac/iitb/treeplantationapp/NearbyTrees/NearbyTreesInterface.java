package in.ac.iitb.treeplantationapp.NearbyTrees;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.ac.iitb.treeplantationapp.Configurations.NearbyTreeConfig;
import in.ac.iitb.treeplantationapp.Models.PlantedTreeModel;
import in.ac.iitb.treeplantationapp.R;

public class NearbyTreesInterface extends FragmentActivity implements OnMapReadyCallback {

    private BottomSheetBehavior mBottomSheetBehavior;
    private SupportMapFragment mapFragment;
    private Location currLocation;
    private ArrayList<PlantedTreeModel> plantedTrees;
    private String selectedTreeId;
    private Location centerLocation;
    private LatLng centerLatLng;
    private TextView tvTreeDetailInMap;

    boolean isMapMoved;
    boolean mapMadeForFirstTime;

    private GoogleMap mMap;

    LocationManager lm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_trees_interface);

        plantedTrees = new ArrayList<>();

        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        currLocation = getLastKnownLocation();
        centerLocation = currLocation;
        isMapMoved = false;
        mapMadeForFirstTime = true;


        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.spotTreesNearby);

        View bottomSheet = findViewById( R.id.bottom_sheet );

        tvTreeDetailInMap = (TextView) findViewById(R.id.tvTreeDetailInMap);

        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setPeekHeight(300);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mBottomSheetBehavior.setPeekHeight(0);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        for(int i=0; i< plantedTrees.size(); i++){
            PlantedTreeModel currPlantedTree = plantedTrees.get(i);
            LatLng latLng = new LatLng(currPlantedTree.getLatitude(),currPlantedTree.getLongitude());
            googleMap.addMarker(new MarkerOptions().
                    position(latLng).
                    title(currPlantedTree.getSpecies()).
                    snippet(currPlantedTree.getTree_id()).
                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            );
        }

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                selectedTreeId = marker.getSnippet();
                tvTreeDetailInMap.setText(selectedTreeId);
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                return true;
            }
        });

        if (centerLocation != null)
        {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(centerLocation.getLatitude(), centerLocation.getLongitude()), 15));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(centerLocation.getLatitude(), centerLocation.getLongitude()))
                    .zoom(17)
                    .bearing(0)
                    .tilt(40)
                    .build();
            if(mapMadeForFirstTime){
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }else{
            Toast.makeText(NearbyTreesInterface.this, "Error in getting your location", Toast.LENGTH_LONG).show();
        }


        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    isMapMoved = true;
                    mapMadeForFirstTime = false;
                }
            }
        });

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                centerLatLng = mMap.getCameraPosition().target;
                centerLocation.setLatitude(centerLatLng.latitude);
                centerLocation.setLongitude(centerLatLng.longitude);

            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if(isMapMoved){
                    isMapMoved = false;
                    getNearbyTreeLocations(centerLocation);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        currLocation = getLastKnownLocation();

        if(currLocation==null){
            Toast.makeText(NearbyTreesInterface.this, "Cannot get your location",Toast.LENGTH_SHORT).show();
        }else{
            getNearbyTreeLocations(currLocation);
        }

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

    }


    private void getNearbyTreeLocations(final Location currLocation) {
        final ProgressDialog loading = ProgressDialog.show(this,"Fetching trees...","Please wait...",false,false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, NearbyTreeConfig.NEARBY_TREES_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if(!response.trim().equals(NearbyTreeConfig.PLANTED_FAILURE)){
                            getAllLatLng(response);
                        }else{
                            Toast.makeText(NearbyTreesInterface.this, "Cannot fetch trees", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Log.i("myTag","volleyError in receiving nearby trees");
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put(NearbyTreeConfig.KEY_LATITUDE, String.valueOf(currLocation.getLatitude()));
                params.put(NearbyTreeConfig.KEY_LONGITUDE, String.valueOf(currLocation.getLongitude()));

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getAllLatLng(String response) {
        JSONObject jsonObject;

        try {
            plantedTrees.clear();
            jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray(NearbyTreeConfig.JSON_ARRAY);

            for(int i = 0; i<result.length(); i++){
                JSONObject jo = result.getJSONObject(i);

                String RES_TREE_ID = jo.getString(NearbyTreeConfig.KEY_TREE_ID);
                String RES_USERNAME = jo.getString(NearbyTreeConfig.KEY_USERNAME);
                double RES_LAT = jo.getDouble(NearbyTreeConfig.KEY_LATITUDE);
                double RES_LON = jo.getDouble(NearbyTreeConfig.KEY_LONGITUDE);
                String RES_PLANTED_ON = jo.getString(NearbyTreeConfig.KEY_DATE);
                String RES_SPECIES= jo.getString(NearbyTreeConfig.KEY_SPECIES);

                PlantedTreeModel newPlantedTree = new PlantedTreeModel(RES_TREE_ID,RES_USERNAME,RES_LAT,RES_LON,RES_PLANTED_ON,RES_SPECIES);
                plantedTrees.add(newPlantedTree);
            }

            mapFragment.getMapAsync(NearbyTreesInterface.this);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("myTag","exceptionJSON in getLatLng");
        }
    }

    private Intent addExtrasToIntent(String[] detailsArray) {
        Intent i = new Intent(NearbyTreesInterface.this, TreeDetail.class);

        i.putExtra(NearbyTreeConfig.KEY_TREE_ID, detailsArray[0]);
        i.putExtra(NearbyTreeConfig.KEY_USERNAME, detailsArray[1]);
        i.putExtra(NearbyTreeConfig.KEY_LATITUDE, detailsArray[2]);
        i.putExtra(NearbyTreeConfig.KEY_LONGITUDE, detailsArray[3]);
        i.putExtra(NearbyTreeConfig.KEY_DATE, detailsArray[4]);
        i.putExtra(NearbyTreeConfig.KEY_SPECIES, detailsArray[5]);

        return i;
    }

    private Location getLastKnownLocation() {
        List<String> providers = lm.getProviders(true);
        Location bestLocation = null;
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION },1);
        }

        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) !=
                PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION },1);
        }

        for (String provider : providers) {
            Location l = lm.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
}
