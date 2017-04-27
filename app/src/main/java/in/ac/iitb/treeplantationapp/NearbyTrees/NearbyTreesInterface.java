package in.ac.iitb.treeplantationapp.NearbyTrees;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import in.ac.iitb.treeplantationapp.Configurations.LoginConfig;
import in.ac.iitb.treeplantationapp.Configurations.NearbyTreeConfig;
import in.ac.iitb.treeplantationapp.Images.ImageUtils;
import in.ac.iitb.treeplantationapp.Images.TreeGallery;
import in.ac.iitb.treeplantationapp.Models.PlantedTreeModel;
import in.ac.iitb.treeplantationapp.Models.ReviewModel;
import in.ac.iitb.treeplantationapp.NearbyTrees.NearbyTreesInterfaceLib.BottomSheetBehaviorGoogleMapsLike;
import in.ac.iitb.treeplantationapp.NearbyTrees.NearbyTreesInterfaceLib.MergedAppBarLayoutBehavior;
import in.ac.iitb.treeplantationapp.R;


public class NearbyTreesInterface extends AppCompatActivity implements OnMapReadyCallback {

    String username;


    TextView bottomSheetTextView;

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
    BottomSheetBehaviorGoogleMapsLike behavior;

    ImageButton BtnAdopt, BtnAlert, BtnGallery, BtnAddReview;
    Button BtnPlantedBy, BtnPlantedOn, BtnSpecies;
    FloatingActionButton fabAddImage;

    ViewPager viewPager;
    ItemPagerAdapter adapter;

    String[] treeDetails;

    ListView listViewReviews;

    List<ReviewModel> reviewList;
    List<String> reviewTextList;

    private Calendar myCalendar;
    String clicked_on; // the date on which an album was clicked, used in uploading an album
    private static final int REQUEST_GALLERY = 1002;
    String adopted_on; // the date on which the user adopted the tree

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_trees_interface);

        // Utilities for creating map
        plantedTrees = new ArrayList<>();

        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        myCalendar = Calendar.getInstance();
        currLocation = getLastKnownLocation();
        centerLocation = currLocation;
        isMapMoved = false;
        mapMadeForFirstTime = true;

        initializeButtons();
        SharedPreferences sharedPreferences = getSharedPreferences(LoginConfig.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        username = sharedPreferences.getString(LoginConfig.USERNAME_SHARED_PREF,"Not Available");

        listViewReviews = (ListView) findViewById(R.id.listViewReviews);
        reviewTextList = new ArrayList<>();
        reviewList = new ArrayList<>();

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.spotTreesNearby);


        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);

        behavior = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);
        behavior.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
/*                switch (newState) {
                    case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                        Log.d("bottomsheet-", "STATE_COLLAPSED");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                        Log.d("bottomsheet-", "STATE_DRAGGING");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                        Log.d("bottomsheet-", "STATE_EXPANDED");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:
                        Log.d("bottomsheet-", "STATE_ANCHOR_POINT");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN:
                        Log.d("bottomsheet-", "STATE_HIDDEN");
                        break;
                    default:
                        Log.d("bottomsheet-", "STATE_SETTLING");
                        break;
                }*/
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        AppBarLayout mergedAppBarLayout = (AppBarLayout) findViewById(R.id.merged_appbarlayout);
        MergedAppBarLayoutBehavior mergedAppBarLayoutBehavior = MergedAppBarLayoutBehavior.from(mergedAppBarLayout);
        mergedAppBarLayoutBehavior.setToolbarTitle("TreePlantation App");
        mergedAppBarLayoutBehavior.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
            }
        });

        bottomSheetTextView = (TextView) bottomSheet.findViewById(R.id.bottom_sheet_title);
        ArrayList<String> mUrls = new ArrayList<>();
        adapter = new ItemPagerAdapter(this,mUrls);
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(adapter);

        behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        listViewReviews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ReviewModel review = reviewList.get(i);
                alertDialogBuilder.setTitle(String.valueOf(review.getReview_no()) + ". " + review.getTitle())
                        .setMessage(" " + review.getReview_stars() + " stars\n " +
                                "Date     - " + review.getReviewed_on() + "\n " +
                                "Reviewer - " + review.getReviewed_by() + "\n " +
                                review.getReview_text());

                alertDialogBuilder.setNegativeButton("Back",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                            }
                        });

                alertDialogBuilder.show();
            }
        });
    }

    /**
     * INITIALIZE BUTTONS AND IMAGEVIEWS
     */
    private void initializeButtons() {
        BtnAdopt = (ImageButton) findViewById(R.id.BtnAdopt);
        BtnAlert = (ImageButton) findViewById(R.id.BtnAlert);
        BtnGallery = (ImageButton) findViewById(R.id.BtnGallery);
        BtnAddReview = (ImageButton) findViewById(R.id.BtnAddReview);

        BtnPlantedBy = (Button) findViewById(R.id.BtnPlantedBy);
        BtnPlantedOn = (Button) findViewById(R.id.BtnPlantedOn);
        BtnSpecies =  (Button) findViewById(R.id.BtnSpecies);

        fabAddImage = (FloatingActionButton) findViewById(R.id.fabAddImage);

        BtnPlantedBy.setClickable(false);
        BtnPlantedOn.setClickable(false);
        BtnSpecies.setClickable(false);

        BtnAdopt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateLabelAndAdopt();
                    }
                };
                new DatePickerDialog(NearbyTreesInterface.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        BtnAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        BtnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent treeGalleryIntent = addTreeToIntent(treeDetails, TreeGallery.class);
                startActivity(treeGalleryIntent);
            }
        });
        BtnAddReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent writeReviewIntent = addTreeToIntent(treeDetails, WriteReview.class);
                startActivity(writeReviewIntent);
            }
        });

        fabAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateLabelAndLaunchGallery();
                    }
                };
                new DatePickerDialog(NearbyTreesInterface.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    /**
     * UPLOADING IMAGES OF A TREE
     */
    private void updateLabelAndLaunchGallery() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        clicked_on = sdf.format(myCalendar.getTime());
        initGalleryIntent();
    }

    private void initGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK
                    && null != data) {

                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                //List<String> imagesEncodedList = new ArrayList<>(); // path
                ArrayList<Uri> mArrayUri = new ArrayList<>(); // important - uri
                ArrayList<Bitmap> bitmapArray; // all the bitmaps

                if (data.getClipData() != null) {
                    ClipData mClipData = data.getClipData();
                    for (int i = 0; i < mClipData.getItemCount(); i++) {
                        ClipData.Item item = mClipData.getItemAt(i);
                        Uri uri = item.getUri();
                        mArrayUri.add(uri);
                        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        //imageEncoded  = cursor.getString(columnIndex);
                        //imagesEncodedList.add(imageEncoded);
                        cursor.close();
                    }
                    Log.i("myTag", "many");
                }else{
                    if(data.getData()!=null){
                        Uri mImageUri=data.getData();
                        mArrayUri.add(mImageUri);
                        Cursor cursor = getContentResolver().query(mImageUri, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        //imageEncoded  = cursor.getString(columnIndex);
                        //imagesEncodedList.add(imageEncoded);
                        cursor.close();
                        Log.i("myTag", "one");
                    }
                }

                bitmapArray = new ArrayList<>();
                Bitmap mBitmap;
                for(int i=0; i<mArrayUri.size(); i++){
                    mBitmap = ImageUtils.getScaledImage(mArrayUri.get(i), this);
                    bitmapArray.add(mBitmap);
                }
                uploadImagesOnServer(bitmapArray);

            } else {
                Toast.makeText(NearbyTreesInterface.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(NearbyTreesInterface.this, "Something went wrong", Toast.LENGTH_LONG).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadImagesOnServer(final ArrayList<Bitmap> bitmapArray) {
        final ProgressDialog loading = ProgressDialog.show(this,"Uploading...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, NearbyTreeConfig.UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loading.dismiss();
                        Log.i("myTag", "response after uploading " + s);
                        Toast.makeText(NearbyTreesInterface.this, "succesfully uploaded", Toast.LENGTH_LONG).show();
                        getImageUrls(selectedTreeId);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading.dismiss();
                        Log.i("myTag", "response after error");
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put(NearbyTreeConfig.KEY_NO_OF_IMAGES, Integer.toString(bitmapArray.size()));

                String tag= NearbyTreeConfig.KEY_IMAGE_NUMBER;

                for(int i=0; i<bitmapArray.size(); i++){
                    params.put(tag+i, ImageUtils.getStringImage(bitmapArray.get(i)));
                }

                params.put(NearbyTreeConfig.KEY_TREE_ID, treeDetails[0]);
                params.put(NearbyTreeConfig.KEY_USERNAME, username);
                params.put(NearbyTreeConfig.KEY_CLICKED_ON, clicked_on);
                params.put(NearbyTreeConfig.KEY_URL_MAIN, NearbyTreeConfig.URL_MAIN); //This should be changed when we have a real server

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    /**
     * ADOPTING A TREE
     */
    private void updateLabelAndAdopt() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        adopted_on = sdf.format(myCalendar.getTime());
        adoptSendToServer(adopted_on);
    }

    private void adoptSendToServer(final String adopted_on) {
        final ProgressDialog loading = ProgressDialog.show(this,"Uploading on server...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, NearbyTreeConfig.ADOPT_TREE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loading.dismiss();
                        if(s.trim().equals(NearbyTreeConfig.ADOPTED_SUCCESS)){
                            Toast.makeText(NearbyTreesInterface.this, "Congrats! Successfully adopted!", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Log.i("myTag", s);
                            Toast.makeText(NearbyTreesInterface.this, "Unable to adopt now", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading.dismiss();
                        Log.i("myTag", "volleyError");
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put(NearbyTreeConfig.KEY_TREE_ID, treeDetails[0]);
                params.put(NearbyTreeConfig.KEY_USERNAME, username);
                params.put(NearbyTreeConfig.KEY_ADOPTED_ON, adopted_on);

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    /**
     * ADDING NECESSARY EXTRAS IN INTENT
     */
    private Intent addTreeToIntent(String[] detailsArray, Class<?> classObject) {
        Intent i = new Intent(NearbyTreesInterface.this, classObject);

        i.putExtra(NearbyTreeConfig.KEY_TREE_ID, detailsArray[0]);
        i.putExtra(NearbyTreeConfig.KEY_USERNAME, detailsArray[1]);
        i.putExtra(NearbyTreeConfig.KEY_LATITUDE, detailsArray[2]);
        i.putExtra(NearbyTreeConfig.KEY_LONGITUDE, detailsArray[3]);
        i.putExtra(NearbyTreeConfig.KEY_DATE, detailsArray[4]);
        i.putExtra(NearbyTreeConfig.KEY_SPECIES, detailsArray[5]);

        return i;
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

        if(selectedTreeId!= null){
            fetchReviewText(selectedTreeId);
            getImageUrls(selectedTreeId);
        }

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
                bottomSheetTextView.setText(selectedTreeId);
                showDetails(selectedTreeId);
                getImageUrls(selectedTreeId);
                behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                return true;
            }
        });


        if (centerLocation != null)
        {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(centerLocation.getLatitude(), centerLocation.getLongitude()), googleMap.getCameraPosition().zoom));

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
    public void onWindowFocusChanged(boolean hasFocus) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

    }

    /**
     * MAP HELPER FUNCTIONS
     */
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

    /**
     * SHOWING DETAILS IN THE BOTTOM SHEET
     */
    private void showDetails(final String selectedTreeId) {
        final ProgressDialog loading = ProgressDialog.show(this,"Fetching details...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, NearbyTreeConfig.TREE_DETAILS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if(!response.trim().equals(NearbyTreeConfig.PLANTED_FAILURE)){
                            fetchReviewText(selectedTreeId);
                            showTreeDialog(response);
                        }else{
                            Toast.makeText(NearbyTreesInterface.this, "Cannot fetch tree details", Toast.LENGTH_LONG).show();
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

        try {
            jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray(NearbyTreeConfig.JSON_ARRAY);
            JSONObject jo = result.getJSONObject(0);
            String RES_TREE_ID = jo.getString(NearbyTreeConfig.KEY_TREE_ID);
            String RES_USERNAME = jo.getString(NearbyTreeConfig.KEY_USERNAME);
            String RES_LAT = jo.getString(NearbyTreeConfig.KEY_LATITUDE);
            String RES_LON = jo.getString(NearbyTreeConfig.KEY_LONGITUDE);
            String RES_PLANTED_ON = jo.getString(NearbyTreeConfig.KEY_DATE);
            String RES_SPECIES= jo.getString(NearbyTreeConfig.KEY_SPECIES);

            BtnPlantedBy.setText("Planted By : " + RES_USERNAME);
            BtnPlantedOn.setText("Planted On : " + RES_PLANTED_ON);
            BtnSpecies.setText("Species : " + RES_SPECIES);

            String[] array = {RES_TREE_ID, RES_USERNAME, RES_LAT, RES_LON, RES_PLANTED_ON, RES_SPECIES};
            treeDetails = array;

        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("myTag","exceptionJSON in showTreeDialog");
        }
    }

    /**
     * FETCHING REVIEWS AND UPDATING REVIEW LIST
     */
    private void fetchReviewText(final String tree_id) {
//        final List<String> list = new ArrayList<>();
        reviewTextList = new ArrayList<>();
        reviewList = new ArrayList<>();

        final ProgressDialog loading = ProgressDialog.show(this,"Fetching Reviews...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, NearbyTreeConfig.SEE_REVIEWS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if(!response.trim().equals(NearbyTreeConfig.REVIEW_FAILURE)){
                            JSONObject jsonObject;
                            try {
                                jsonObject = new JSONObject(response);
                                JSONArray result = jsonObject.getJSONArray(NearbyTreeConfig.JSON_ARRAY);

                                for(int i = 0; i<result.length(); i++){
                                    JSONObject jo = result.getJSONObject(i);

                                    String RES_TREE_ID = jo.getString(NearbyTreeConfig.KEY_TREE_ID);
                                    String RES_REVIEWED_BY = jo.getString(NearbyTreeConfig.KEY_USERNAME);
                                    String RES_REVIEW_TEXT = jo.getString(NearbyTreeConfig.KEY_REVIEW_TEXT);
                                    String RES_REVIEW_DATE = jo.getString(NearbyTreeConfig.KEY_REVIEW_DATE);
                                    String RES_TITLE = jo.getString(NearbyTreeConfig.KEY_TITLE);
                                    Double RES_REVIEW_STARS = jo.getDouble(NearbyTreeConfig.KEY_REVIEW_RATINGS);
                                    int RES_REVIEW_NO = jo.getInt(NearbyTreeConfig.KEY_REVIEW_NO);

                                    ReviewModel review = new ReviewModel(RES_TREE_ID, RES_REVIEW_TEXT, RES_REVIEWED_BY, RES_REVIEW_DATE, RES_TITLE, RES_REVIEW_NO, RES_REVIEW_STARS );
                                    reviewList.add(review);

                                    reviewTextList.add(String.valueOf(RES_REVIEW_NO) + ". " + RES_TITLE + "\n " +
                                            String.valueOf(RES_REVIEW_STARS) + "\n " + RES_REVIEW_DATE);
                                }

                                setReviewListAdapter();

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.i("myTag","exceptionJSON");
                            }
                        }
                        else{
                            Toast.makeText(NearbyTreesInterface.this, "Unable to fetch reviews", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Log.i("myTag","volleyError");
                        Toast.makeText(NearbyTreesInterface.this, "Unable to fetch reviews", Toast.LENGTH_SHORT).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put(NearbyTreeConfig.KEY_TREE_ID, tree_id);

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void setReviewListAdapter() {
        ProgressDialog loading = ProgressDialog.show(this,"Fetching details...","Please wait...",false,false);
        ListAdapter listAdapter = new ArrayAdapter<>(NearbyTreesInterface.this,android.R.layout.simple_list_item_1,reviewTextList);
        listViewReviews.setAdapter(listAdapter);
        setListViewHeightBasedOnChildren(listViewReviews);
        loading.dismiss();
    }

    /**
     * FETCHING IMAGES AND SHOWING THEM IN THE BOTTOM SHEET
     */
    private void getImageUrls(final String tree_id) {
        final ArrayList<String> urls = new ArrayList<>();

        final ProgressDialog loading = ProgressDialog.show(this,"Fetching Images...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, NearbyTreeConfig.TREE_THUMBNAILS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if(!response.trim().equals(NearbyTreeConfig.IMAGES_FAILURE)){
                            JSONObject jsonObject;
                            try {
                                jsonObject = new JSONObject(response);
                                JSONArray result = jsonObject.getJSONArray(NearbyTreeConfig.JSON_ARRAY);

                                for(int i = 0; i<result.length(); i++){
                                    JSONObject jo = result.getJSONObject(i);

                                    String RES_IMAGE_URL = jo.getString(NearbyTreeConfig.KEY_IMAGE_URL);

                                    urls.add(LoginConfig.URL_MAIN + RES_IMAGE_URL);
                                }
                                adapter = new ItemPagerAdapter(NearbyTreesInterface.this, urls);
                                viewPager.setAdapter(adapter);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.i("myTag","exceptionJSON");
                            }
                        }
                        else{
                            Toast.makeText(NearbyTreesInterface.this, "No images uploaded", Toast.LENGTH_SHORT).show();
                            adapter = new ItemPagerAdapter(NearbyTreesInterface.this, urls);
                            viewPager.setAdapter(adapter);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Log.i("myTag","volleyError");
                        Toast.makeText(NearbyTreesInterface.this, "Unable to fetch image urls", Toast.LENGTH_SHORT).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put(NearbyTreeConfig.KEY_TREE_ID, tree_id);

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
