package in.ac.iitb.treeplantationapp.Images;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageSwitcher;
import android.widget.ListAdapter;
import android.widget.ListView;
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
import in.ac.iitb.treeplantationapp.R;

public class TreeImages extends AppCompatActivity {

    ArrayList<String> imageUrls;
    ListView lvImages;
    AdapterImages adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_images);

        lvImages = (ListView) findViewById(R.id.lvImages);

        Intent intent = getIntent();
        DirectoryModel directory = getDirectoryFromIntent(intent);

        imageUrls = new ArrayList<>();

        getImageUrls(directory);

    }

    private void getImageUrls(final DirectoryModel dir) {

        final ProgressDialog loading = ProgressDialog.show(this,"Fetching Images...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, NearbyTreeConfig.IMAGES_URL,
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

                                    imageUrls.add(LoginConfig.URL_MAIN + RES_IMAGE_URL);
                                }
                                adapter = new AdapterImages(TreeImages.this, imageUrls);
                                lvImages.setAdapter(adapter);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.i("myTag","exceptionJSON");
                            }
                        }
                        else{
                            Toast.makeText(TreeImages.this, "Unable to fetch image urls", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Log.i("myTag","volleyError");
                        Toast.makeText(TreeImages.this, "Unable to fetch image urls", Toast.LENGTH_SHORT).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put(NearbyTreeConfig.KEY_TREE_ID, dir.getTree_id());
                params.put(NearbyTreeConfig.KEY_DIRECTORY_ID, dir.getDirectory_id());

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private DirectoryModel getDirectoryFromIntent(Intent intent) {
        String tree_id = intent.getStringExtra(NearbyTreeConfig.KEY_TREE_ID);
        String directory_id = intent.getStringExtra(NearbyTreeConfig.KEY_DIRECTORY_ID);
        String uploaded_by = intent.getStringExtra(NearbyTreeConfig.KEY_UPLOADED_BY);
        String clicked_on = intent.getStringExtra(NearbyTreeConfig.KEY_CLICKED_ON);

        return new DirectoryModel(directory_id,tree_id,uploaded_by,clicked_on);
    }


}
