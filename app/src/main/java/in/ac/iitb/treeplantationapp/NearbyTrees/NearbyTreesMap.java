package in.ac.iitb.treeplantationapp.NearbyTrees;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
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

public class NearbyTreesMap extends FragmentActivity implements OnMapReadyCallback {


    private SupportMapFragment mapFragment;
    private Location currLocation;
    private ArrayList<PlantedTreeModel> plantedTrees;
    private String selectedTreeId;
    private Location centerLocation;
    private LatLng centerLatLng;

    boolean isMapMoved;
    boolean mapMadeForFirstTime;

    private GoogleMap mMap;

    LocationManager lm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_fragment_nearby_trees);

        plantedTrees = new ArrayList<>();

        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        currLocation = getLastKnownLocation();
        centerLocation = currLocation;
        isMapMoved = false;
        mapMadeForFirstTime = true;


        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.spotTreesNearby);


        Button btnDetails = (Button) findViewById(R.id.btnDetails);
        btnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedTreeId==null){
                    Toast.makeText(NearbyTreesMap.this, "Select a tree first", Toast.LENGTH_SHORT).show();
                }else{
                    showDetails(selectedTreeId);
                }
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
            Toast.makeText(NearbyTreesMap.this, "Error in getting your location", Toast.LENGTH_LONG).show();
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
            Toast.makeText(NearbyTreesMap.this, "Cannot get your location",Toast.LENGTH_SHORT).show();
        }else{
            getNearbyTreeLocations(currLocation);
        }

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        /*Log.i("myTag", "before newlatlon");
        LatLng newLatLng = mMap.getCameraPosition().target;

        Log.i("myTag-lat", Double.toString(newLatLng.latitude));
        Log.i("myTag-lon", Double.toString(newLatLng.longitude));*/

/*        int imageParentWidth = mMarkerParentView.getWidth();
        int imageParentHeight = mMarkerParentView.getHeight();
        int imageHeight = mMarkerImageView.getHeight();

        centerX = imageParentWidth / 2;
        centerY = (imageParentHeight / 2) + (imageHeight / 2);*/
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
                            Toast.makeText(NearbyTreesMap.this, "Cannot fetch trees", Toast.LENGTH_LONG).show();
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

            mapFragment.getMapAsync(NearbyTreesMap.this);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("myTag","exceptionJSON in getLatLng");
        }
    }

    private void showDetails(final String selectedTreeId) {
        final ProgressDialog loading = ProgressDialog.show(this,"Fetching details...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, NearbyTreeConfig.TREE_DETAILS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if(!response.trim().equals(NearbyTreeConfig.PLANTED_FAILURE)){
                            showTreeDialog(response);
                        }else{
                            Toast.makeText(NearbyTreesMap.this, "Cannot fetch tree details", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Log.i("myTag","volleyError on showing tree details");
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put(NearbyTreeConfig.KEY_TREE_ID, selectedTreeId);

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void showTreeDialog(String response) {
        JSONObject jsonObject;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Your selected tree");
        ArrayList<String> details = new ArrayList<>();

        try {
            jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray(NearbyTreeConfig.JSON_ARRAY);
            JSONObject jo = result.getJSONObject(0);
            final String RES_TREE_ID = jo.getString(NearbyTreeConfig.KEY_TREE_ID);
            final String RES_USERNAME = jo.getString(NearbyTreeConfig.KEY_USERNAME);
            final String RES_LAT = jo.getString(NearbyTreeConfig.KEY_LATITUDE);
            final String RES_LON = jo.getString(NearbyTreeConfig.KEY_LONGITUDE);
            final String RES_PLANTED_ON = jo.getString(NearbyTreeConfig.KEY_DATE);
            final String RES_SPECIES= jo.getString(NearbyTreeConfig.KEY_SPECIES);

            details.add("Planted by: " + RES_USERNAME);
            details.add("Coordinates: \n" + RES_LAT + ", " + RES_LON );
            details.add("Planted on: " + RES_PLANTED_ON);
            details.add("Species: " + RES_SPECIES);

            CharSequence[] cs = details.toArray(new CharSequence[details.size()]);
            alertDialogBuilder
                    .setItems(cs, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing on clicking list items
                        }
                    });

            alertDialogBuilder.setPositiveButton("See this tree",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            String[] detailsArray = {RES_TREE_ID, RES_USERNAME, RES_LAT, RES_LON, RES_PLANTED_ON, RES_SPECIES};

                            Intent treeDetail = addExtrasToIntent(detailsArray);
                            startActivity(treeDetail);
                        }
                    });

            alertDialogBuilder.setNegativeButton("Back",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("myTag","exceptionJSON in showTreeDialog");
        }
    }

    private Intent addExtrasToIntent(String[] detailsArray) {
        Intent i = new Intent(NearbyTreesMap.this, TreeDetail.class);

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
