package in.ac.iitb.treeplantationapp.PlantTree;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import in.ac.iitb.treeplantationapp.Configurations.LoginConfig;
import in.ac.iitb.treeplantationapp.Configurations.PlantTreeConfig;
import in.ac.iitb.treeplantationapp.R;
import in.ac.iitb.treeplantationapp.UserProfile;

import static android.location.LocationManager.GPS_PROVIDER;

public class PlantNewTree extends AppCompatActivity implements MapWrapperLayout.OnDragListener, OnMapReadyCallback{

    // Google Map
    private GoogleMap googleMap;

    private View mMarkerParentView;
    private ImageView mMarkerImageView;
    private EditText etDate;

    private int centerX = -1;
    private int centerY = -1;

    private LatLng currLatLng; //of the center of the map
    private Location currLocation; //of your phone

    private TextView mLocationTextView;
    private Calendar myCalendar;

    LocationManager lm;
    String provider;

    LocationListener locationListener;
    CustomMapFragment mCustomMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_new_tree);

        initializeUI();

        mCustomMapFragment = ((CustomMapFragment) getFragmentManager().findFragmentById(R.id.map));
        mCustomMapFragment.setOnDragListener(PlantNewTree.this);
        mCustomMapFragment.getMapAsync(this);

        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        currLocation = getLastKnownLocation();

        //for creating a datepicker dialog on clicking the etDate
        myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(PlantNewTree.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    }

    private void updateLabel() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etDate.setText(sdf.format(myCalendar.getTime()));
    }

    private void initializeUI() {
        try {
            initializeMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mLocationTextView = (TextView) findViewById(R.id.location_text_view);
        mMarkerParentView = findViewById(R.id.marker_view_incl);
        mMarkerImageView = (ImageView) findViewById(R.id.marker_icon_view);
        Button btnNext = (Button) findViewById(R.id.btnNext);
        final EditText etSpecies = (EditText) findViewById(R.id.etSpecies);
        etDate = (EditText) findViewById(R.id.etDate);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currLatLng != null){
                    if(etSpecies.getText().toString().equals("") || etDate.getText().toString().equals("")){
                        Toast.makeText(PlantNewTree.this, "Please fill species and date of planting", Toast.LENGTH_LONG).show();
                    }else{
                        plantNow(currLatLng,etSpecies.getText().toString(), etDate.getText().toString());
                    }
                }else{
                    Toast.makeText(PlantNewTree.this, "Please select a location", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void plantNow(final LatLng currLocation, final String species, final String date) {
        SharedPreferences sharedPreferences = getSharedPreferences(LoginConfig.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        final String username = sharedPreferences.getString(LoginConfig.USERNAME_SHARED_PREF,"Not Available");

        final String tree_id;
        if(username.length()>10){
            tree_id = username.substring(0,10) + "_" + UUID.randomUUID().toString();
        }else{
            tree_id = username + "_" + UUID.randomUUID().toString();
        }

        final ProgressDialog loading = ProgressDialog.show(this,"Uploading...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, PlantTreeConfig.PLANT_TREE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if(response.trim().equals(PlantTreeConfig.PLANTED_SUCCESS)){
                            Toast.makeText(PlantNewTree.this,"Successfully planted", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PlantNewTree.this, UserProfile.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(PlantNewTree.this, "Could not plant", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Log.i("myTag","volleyError");
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put(PlantTreeConfig.KEY_LATITUDE, String.valueOf(currLocation.latitude));
                params.put(PlantTreeConfig.KEY_LONGITUDE, String.valueOf(currLocation.longitude));
                params.put(PlantTreeConfig.KEY_USERNAME, username );
                params.put(PlantTreeConfig.KEY_DATE, date);
                params.put(PlantTreeConfig.KEY_SPECIES, species);
                params.put(PlantTreeConfig.KEY_TREE_ID, tree_id);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        int imageParentWidth = mMarkerParentView.getWidth();
        int imageParentHeight = mMarkerParentView.getHeight();
        int imageHeight = mMarkerImageView.getHeight();

        centerX = imageParentWidth / 2;
        centerY = (imageParentHeight / 2) + (imageHeight / 2);
    }

    private void initializeMap() {
        mCustomMapFragment.getMapAsync(this);

        if (googleMap == null) {
            mCustomMapFragment.getMapAsync(this);

            if (googleMap == null) {
                mCustomMapFragment.getMapAsync(this);
                Toast.makeText(getApplicationContext(),
                        "Creating maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mCustomMapFragment.getMapAsync(this);
    }

    @Override
    public void onDrag(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            Projection projection = (googleMap != null && googleMap
                    .getProjection() != null) ? googleMap.getProjection()
                    : null;

            if (projection != null) {
                currLatLng = projection.fromScreenLocation(new Point(
                        centerX, centerY));
                updateLocation(currLatLng);
            }
        }
    }

    private void updateLocation(LatLng centerLatLng) {
        if (centerLatLng != null) {
            Geocoder geocoder = new Geocoder(PlantNewTree.this,
                    Locale.getDefault());

            List<Address> addresses = new ArrayList<>();
            try {
                addresses = geocoder.getFromLocation(centerLatLng.latitude,
                        centerLatLng.longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {

                String addressIndex0 = (addresses.get(0).getAddressLine(0) != null) ? addresses
                        .get(0).getAddressLine(0) : null;
                String addressIndex1 = (addresses.get(0).getAddressLine(1) != null) ? addresses
                        .get(0).getAddressLine(1) : null;
                String addressIndex2 = (addresses.get(0).getAddressLine(2) != null) ? addresses
                        .get(0).getAddressLine(2) : null;
                String addressIndex3 = (addresses.get(0).getAddressLine(3) != null) ? addresses
                        .get(0).getAddressLine(3) : null;

                String completeAddress = addressIndex0 + "," + addressIndex1;

                if (addressIndex2 != null) {
                    completeAddress += "," + addressIndex2;
                }
                if (addressIndex3 != null) {
                    completeAddress += "," + addressIndex3;
                }
                if (completeAddress != null) {
                    mLocationTextView.setText(completeAddress);
                }
            }
        }
    }



    @Override
    public void onMapReady(GoogleMap mMap) {
        googleMap = mMap;

        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION },1);
        }

        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) !=
                PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION },1);
        }
        if (currLocation == null) {
            //criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            lm.requestLocationUpdates(provider,0,0, locationListener);
            currLocation = getLastKnownLocation();
        }

        Log.i("myTag","onMapReady came here");

        if (currLocation != null)
        {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currLocation.getLatitude(), currLocation.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(currLocation.getLatitude(), currLocation.getLongitude()))
                    .zoom(17)
                    .bearing(0)
                    .tilt(40)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }else{
            Toast.makeText(PlantNewTree.this, "Error in getting your location", Toast.LENGTH_LONG).show();
        }
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
