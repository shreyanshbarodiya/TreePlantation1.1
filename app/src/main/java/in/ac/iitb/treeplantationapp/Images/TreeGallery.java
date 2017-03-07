package in.ac.iitb.treeplantationapp.Images;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import in.ac.iitb.treeplantationapp.Configurations.LoginConfig;
import in.ac.iitb.treeplantationapp.Configurations.NearbyTreeConfig;
import in.ac.iitb.treeplantationapp.Models.DirectoryModel;
import in.ac.iitb.treeplantationapp.Models.PlantedTreeModel;
import in.ac.iitb.treeplantationapp.NearbyTrees.TreeDetail;
import in.ac.iitb.treeplantationapp.R;

public class TreeGallery extends AppCompatActivity {
    TextView tvDetails;
    ListView lvDirectory;
    String username;
    PlantedTreeModel tree;
    List<DirectoryModel> directoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_gallery);

        tvDetails = (TextView) findViewById(R.id.tvDetails);
        lvDirectory = (ListView) findViewById(R.id.lvDirectory);

        SharedPreferences sharedPreferences = getSharedPreferences(LoginConfig.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        username = sharedPreferences.getString(LoginConfig.USERNAME_SHARED_PREF,"Not Available");

        Intent intent = getIntent();
        tree = getTreeFromIntent(intent);

        tvDetails.setText(tree.getSpecies() + "\nPlanted by- " + tree.getUsername() + "\nPlanted On- " + tree.getPlanted_on());

        directoryList = new ArrayList<>();
        populateListView();

        lvDirectory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                DirectoryModel directory = directoryList.get(i);
                Intent directoryIntent = addDirectoryToIntent(directory, TreeImages.class);
                startActivity(directoryIntent);
            }
        });
    }

    private Intent addDirectoryToIntent(DirectoryModel directory, Class<?> classObject) {
        Intent i = new Intent(TreeGallery.this, classObject);

        i.putExtra(NearbyTreeConfig.KEY_DIRECTORY_ID, directory.getDirectory_id());
        i.putExtra(NearbyTreeConfig.KEY_TREE_ID, directory.getTree_id());
        i.putExtra(NearbyTreeConfig.KEY_UPLOADED_BY, directory.getUploaded_by());
        i.putExtra(NearbyTreeConfig.KEY_CLICKED_ON, directory.getClicked_on());

        return i;

    }


    private void populateListView() {

        final List<String> list = new ArrayList<>();

        final ProgressDialog loading = ProgressDialog.show(this,"Fetching...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, NearbyTreeConfig.DIRECTORY_URL,
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

                                    String RES_DIRECTORY_ID = jo.getString(NearbyTreeConfig.KEY_DIRECTORY_ID);
                                    String RES_UPLOADED_BY = jo.getString(NearbyTreeConfig.KEY_UPLOADED_BY);
                                    String RES_CLICKED_ON = jo.getString(NearbyTreeConfig.KEY_CLICKED_ON);

                                    DirectoryModel dm = new DirectoryModel(RES_DIRECTORY_ID, tree.getTree_id(),RES_UPLOADED_BY, RES_CLICKED_ON);
                                    directoryList.add(dm);
                                    list.add("Uploaded by - " + RES_UPLOADED_BY + "\n" +
                                             "Uploaded On - " + RES_CLICKED_ON);

                                }

                                ListAdapter listAdapter = new ArrayAdapter<>(TreeGallery.this,android.R.layout.simple_list_item_1,list);
                                lvDirectory.setAdapter(listAdapter);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.i("myTag","exceptionJSON");
                            }
                        }
                        else{
                            Toast.makeText(TreeGallery.this, "Unable to fetch directories", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Log.i("myTag","volleyError");
                        Toast.makeText(TreeGallery.this, "Unable to fetch directories", Toast.LENGTH_SHORT).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put(NearbyTreeConfig.KEY_TREE_ID, tree.getTree_id());

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

        return new PlantedTreeModel(tree_id,username,Double.parseDouble(lat),Double.parseDouble(lon),planted_on,species);
    }

}
