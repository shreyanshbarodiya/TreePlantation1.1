package in.ac.iitb.treeplantationapp.NearbyTrees;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.ac.iitb.treeplantationapp.Configurations.NearbyTreeConfig;
import in.ac.iitb.treeplantationapp.Models.PlantedTreeModel;
import in.ac.iitb.treeplantationapp.Models.ReviewModel;
import in.ac.iitb.treeplantationapp.R;

public class SeeReviews extends AppCompatActivity {

    TextView tvDetails;
    PlantedTreeModel tree;
    List<String> reviewTextList;
    List<ReviewModel> reviewList;
    ListView listViewReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_reviews);

        tvDetails = (TextView) findViewById(R.id.tvDetails);
        listViewReviews = (ListView) findViewById(R.id.listViewReviews);

        Intent i = getIntent();
        tree = getTreeFromIntent(i);

        tvDetails.setText(tree.getSpecies() + "\nPlanted by- " + tree.getUsername() + "\nPlanted On- " + tree.getPlanted_on());

        reviewList = new ArrayList<>();
        reviewTextList = fetchReviewText(tree.getTree_id());


        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        listViewReviews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
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

    private List<String> fetchReviewText(final String tree_id) {
        final List<String> list = new ArrayList<>();

        final ProgressDialog loading = ProgressDialog.show(this,"Fetching...","Please wait...",false,false);
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

                                Log.i("myTag", response);
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

                                    list.add(String.valueOf(RES_REVIEW_NO) + ". " + RES_TITLE + "\n " +
                                            String.valueOf(RES_REVIEW_STARS) + "\n " + RES_REVIEW_DATE);
                                }

                                ListAdapter listAdapter = new ArrayAdapter<>(SeeReviews.this,android.R.layout.simple_list_item_1,reviewTextList);
                                listViewReviews.setAdapter(listAdapter);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.i("myTag","exceptionJSON");
                            }
                        }
                        else{
                            Toast.makeText(SeeReviews.this, "Unable to fetch reviews", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Log.i("myTag","volleyError");
                        Toast.makeText(SeeReviews.this, "Unable to fetch reviews", Toast.LENGTH_SHORT).show();
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


        return list;
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
}
