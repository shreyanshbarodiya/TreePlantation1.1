package in.ac.iitb.treeplantationapp.NearbyTrees;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Rating;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import in.ac.iitb.treeplantationapp.Configurations.LoginConfig;
import in.ac.iitb.treeplantationapp.Configurations.NearbyTreeConfig;
import in.ac.iitb.treeplantationapp.Configurations.NotificationConfig;
import in.ac.iitb.treeplantationapp.Models.PlantedTreeModel;
import in.ac.iitb.treeplantationapp.R;

public class WriteReview extends AppCompatActivity {

    String username;
    PlantedTreeModel tree;
    TextView tvDetails;
    EditText etDate, etTitle, etReviewText;
    private Calendar myCalendar;
    Button btnAddReview;
    RatingBar ratingReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);
        tvDetails = (TextView) findViewById(R.id.tvDetails);
        etDate = (EditText) findViewById(R.id.etDate);
        etTitle = (EditText) findViewById(R.id.etTitle);
        etReviewText = (EditText) findViewById(R.id.etReviewText);
        btnAddReview = (Button) findViewById(R.id.btnAddReview);
        ratingReview = (RatingBar) findViewById(R.id.ratingReview);
        ratingReview.setNumStars(5);

        SharedPreferences sharedPreferences = getSharedPreferences(LoginConfig.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        username = sharedPreferences.getString(LoginConfig.USERNAME_SHARED_PREF,"Not Available");

        Intent i = getIntent();
        tree = getTreeFromIntent(i);

        tvDetails.setText(tree.getSpecies() + "\nPlanted by- " + tree.getUsername() + "\nPlanted On- " + tree.getPlanted_on());

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
                new DatePickerDialog(WriteReview.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btnAddReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitReview();
            }
        });


    }

    private PlantedTreeModel getTreeFromIntent(Intent i) {
        String tree_id = i.getStringExtra(NearbyTreeConfig.KEY_TREE_ID);
        String username = i.getStringExtra(NearbyTreeConfig.KEY_USERNAME);
        String planted_on = i.getStringExtra(NearbyTreeConfig.KEY_DATE);
        String lat = i.getStringExtra(NearbyTreeConfig.KEY_LATITUDE);
        String lon = i.getStringExtra(NearbyTreeConfig.KEY_LONGITUDE);
        String species = i.getStringExtra(NearbyTreeConfig.KEY_SPECIES);

        return new PlantedTreeModel(tree_id,username,Double.parseDouble(lat),Double.parseDouble(lon),planted_on,species);
    }

    private void updateLabel() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etDate.setText(sdf.format(myCalendar.getTime()));
    }

    private void submitReview() {

        final ProgressDialog loading = ProgressDialog.show(this,"Adding...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, NearbyTreeConfig.WRITE_REVIEW_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if(response.trim().equals(NearbyTreeConfig.REVIEWED_SUCCESS)){
                            Toast.makeText(WriteReview.this, "Successfully added review", Toast.LENGTH_LONG).show();
                            String[] detailsArray = {tree.getTree_id(),tree.getUsername(),
                                                    String.valueOf(tree.getLatitude()), String.valueOf(tree.getLongitude()),
                                                    tree.getPlanted_on(), tree.getSpecies()};
                            notifyOwnerAboutReview(tree.getTree_id(), tree.getSpecies(), username);
//                            startActivity(addExtrasToIntent(detailsArray, SeeReviews.class));

                        }
                        else{
                            Log.i("myTag", response);
                            Toast.makeText(WriteReview.this, "Unable to add review", Toast.LENGTH_LONG).show();
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
                params.put(NearbyTreeConfig.KEY_USERNAME, username);
                params.put(NearbyTreeConfig.KEY_TREE_ID, tree.getTree_id());
                params.put(NearbyTreeConfig.KEY_REVIEW_TEXT, etReviewText.getText().toString());
                params.put(NearbyTreeConfig.KEY_REVIEW_DATE, etDate.getText().toString());
                params.put(NearbyTreeConfig.KEY_TITLE, etTitle.getText().toString());
                params.put(NearbyTreeConfig.KEY_REVIEW_RATINGS, Float.toString(ratingReview.getRating()));

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void notifyOwnerAboutReview(final String tree_id, final String species, final String u_name) {
        final ProgressDialog progressDialog = ProgressDialog.show(this,"Notifying owner...","Please wait...",false,false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, NotificationConfig.URL_SEND_MULTIPLE_PUSH,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.i("myTag", response);
                        //Toast.makeText(ActivitySendPushNotification.this, response, Toast.LENGTH_LONG).show();
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("myTag", "error");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("tree_id", tree_id.trim());
                params.put("title", "Tree Reviewed");
                params.put("message", "Your tree " + species + " was reviewed by: " + u_name);

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private Intent addExtrasToIntent(String[] detailsArray, Class<?> classObject) {
        Intent i = new Intent(WriteReview.this, classObject);

        i.putExtra(NearbyTreeConfig.KEY_TREE_ID, detailsArray[0]);
        i.putExtra(NearbyTreeConfig.KEY_USERNAME, detailsArray[1]);
        i.putExtra(NearbyTreeConfig.KEY_LATITUDE, detailsArray[2]);
        i.putExtra(NearbyTreeConfig.KEY_LONGITUDE, detailsArray[3]);
        i.putExtra(NearbyTreeConfig.KEY_DATE, detailsArray[4]);
        i.putExtra(NearbyTreeConfig.KEY_SPECIES, detailsArray[5]);

        return i;
    }
}
