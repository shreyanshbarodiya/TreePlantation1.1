package in.ac.iitb.treeplantationapp.NearbyTrees;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
import in.ac.iitb.treeplantationapp.R;

public class TreeDetail extends AppCompatActivity {

    TextView tvDetails;
    Button btnAdopt, btnUpload, btnWriteReview, btnSeeReviews, btnGallery, btnAlert;
    PlantedTreeModel tree;
    String[] treeDetails;

    //String imageEncoded;
    String username;
    String clicked_on; // the date on which an album was clicked, used in uploading an album
    String adopted_on; // the date on which the user adopted the tree

    private Calendar myCalendar;


    private static final int REQUEST_GALLERY = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_detail);

        tvDetails = (TextView) findViewById(R.id.tvDetails);
        btnAdopt = (Button) findViewById(R.id.btnAdopt);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnWriteReview = (Button) findViewById(R.id.btnWriteReview);
        btnSeeReviews = (Button) findViewById(R.id.btnSeeReviews);
        btnGallery = (Button) findViewById(R.id.btnGallery);
        btnAlert = (Button) findViewById(R.id.btnAlert);

        SharedPreferences sharedPreferences = getSharedPreferences(LoginConfig.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        username = sharedPreferences.getString(LoginConfig.USERNAME_SHARED_PREF,"Not Available");

        Intent i = getIntent();
        tree = getTreeFromIntent(i);

        tvDetails.setText(tree.getSpecies() + "\nPlanted by- " + tree.getUsername() + "\nPlanted On- " + tree.getPlanted_on());

        myCalendar = Calendar.getInstance();


        btnWriteReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent writeReviewIntent = addTreeToIntent(treeDetails, WriteReview.class);
                startActivity(writeReviewIntent);
            }
        });

        btnSeeReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent seeReviewIntent = addTreeToIntent(treeDetails, SeeReviews.class);
                startActivity(seeReviewIntent);

            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
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
                new DatePickerDialog(TreeDetail.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent treeGalleryIntent = addTreeToIntent(treeDetails, TreeGallery.class);
                startActivity(treeGalleryIntent);
            }
        });

        btnAdopt.setOnClickListener(new View.OnClickListener() {
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
                new DatePickerDialog(TreeDetail.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateLabelAndLaunchGallery() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        clicked_on = sdf.format(myCalendar.getTime());
        initGalleryIntent();
    }

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
                            Toast.makeText(TreeDetail.this, "Congrats! Successfully adopted!", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Log.i("myTag", s);
                            Toast.makeText(TreeDetail.this, "Unable to adopt now", Toast.LENGTH_LONG).show();
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

                params.put(NearbyTreeConfig.KEY_TREE_ID, tree.getTree_id());
                params.put(NearbyTreeConfig.KEY_USERNAME, username);
                params.put(NearbyTreeConfig.KEY_ADOPTED_ON, adopted_on);

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private PlantedTreeModel getTreeFromIntent(Intent i) {
        String tree_id = i.getStringExtra(NearbyTreeConfig.KEY_TREE_ID);
        String username = i.getStringExtra(NearbyTreeConfig.KEY_USERNAME);
        String planted_on = i.getStringExtra(NearbyTreeConfig.KEY_DATE);
        String lat = i.getStringExtra(NearbyTreeConfig.KEY_LATITUDE);
        String lon = i.getStringExtra(NearbyTreeConfig.KEY_LONGITUDE);
        String species = i.getStringExtra(NearbyTreeConfig.KEY_SPECIES);
        String[] array = {tree_id, username, lat, lon, planted_on, species};
        treeDetails = array;
        return new PlantedTreeModel(tree_id,username,Double.parseDouble(lat),Double.parseDouble(lon),planted_on,species);
    }

    private Intent addTreeToIntent(String[] detailsArray, Class<?> classObject) {
        Intent i = new Intent(TreeDetail.this, classObject);

        i.putExtra(NearbyTreeConfig.KEY_TREE_ID, detailsArray[0]);
        i.putExtra(NearbyTreeConfig.KEY_USERNAME, detailsArray[1]);
        i.putExtra(NearbyTreeConfig.KEY_LATITUDE, detailsArray[2]);
        i.putExtra(NearbyTreeConfig.KEY_LONGITUDE, detailsArray[3]);
        i.putExtra(NearbyTreeConfig.KEY_DATE, detailsArray[4]);
        i.putExtra(NearbyTreeConfig.KEY_SPECIES, detailsArray[5]);

        return i;
    }

    // start the gallery intent
    private void initGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    // activity result for gallery, used to upload photos to the server,
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
                Toast.makeText(TreeDetail.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(TreeDetail.this, "Something went wrong", Toast.LENGTH_LONG).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // volley request to the server to upload the images
    private void uploadImagesOnServer(final ArrayList<Bitmap> bitmapArray) {
        final ProgressDialog loading = ProgressDialog.show(this,"Uploading...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, NearbyTreeConfig.UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loading.dismiss();
                        Log.i("myTag", "response after uploading " + s);
                        Toast.makeText(TreeDetail.this, "succesfully uploaded", Toast.LENGTH_LONG).show();
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

                params.put(NearbyTreeConfig.KEY_TREE_ID, tree.getTree_id());
                params.put(NearbyTreeConfig.KEY_USERNAME, username);
                params.put(NearbyTreeConfig.KEY_CLICKED_ON, clicked_on);
                params.put(NearbyTreeConfig.KEY_URL_MAIN, NearbyTreeConfig.URL_MAIN); //This should be changed when we have a real server

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

}
