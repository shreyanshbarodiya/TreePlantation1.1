package in.ac.iitb.treeplantationapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.HashMap;
import java.util.Map;

import in.ac.iitb.treeplantationapp.Configurations.LoginConfig;
import in.ac.iitb.treeplantationapp.Configurations.NotificationConfig;
import in.ac.iitb.treeplantationapp.Notifications.SharedPrefManager;

public class LoginActivity extends AppCompatActivity {

    EditText etServer;

    EditText etUsername;
    EditText etPassword;
    Button btnSignIn;
    TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        tvRegister = (TextView) findViewById(R.id.tvRegister);
        etServer = (EditText) findViewById(R.id.etServer);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginConfig.setUrlServer(etServer.getText().toString());
                //LoginConfig.URL_SERVER = etServer.getText().toString();
                Log.i("myTag", LoginConfig.LOGIN_URL);
                login();
            }
        });
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginConfig.setUrlServer(etServer.getText().toString());
                //LoginConfig.URL_SERVER = etServer.getText().toString();
                Intent registerIntent = new Intent(LoginActivity.this, RegisterUser.class);
                startActivity(registerIntent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //In onresume fetching value from sharedpreference
        SharedPreferences sharedPreferences = getSharedPreferences(LoginConfig.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        boolean loggedIn = sharedPreferences.getBoolean(LoginConfig.LOGGEDIN_SHARED_PREF, false);

        if(loggedIn){
            Intent intent = new Intent(LoginActivity.this, UserProfile.class);
            startActivity(intent);
        }
    }

    private void login(){
        final String username = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        final ProgressDialog loading = ProgressDialog.show(this,"Logging in...","Please wait...",false,false);
        Log.i("myTag", LoginConfig.LOGIN_URL);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, LoginConfig.LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        //If we are getting success from server
                        if(!response.equalsIgnoreCase(LoginConfig.LOGIN_FAILURE)){

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray result = jsonObject.getJSONArray(LoginConfig.JSON_ARRAY);
                                JSONObject userData = result.getJSONObject(0);
                                String res_username  = userData.getString(LoginConfig.KEY_USERNAME);
                                String res_name  = userData.getString(LoginConfig.KEY_NAME);

                                //Creating a shared preference
                                SharedPreferences sharedPreferences = LoginActivity.this.getSharedPreferences(LoginConfig.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                                //Creating editor to store values to shared preferences
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean(LoginConfig.LOGGEDIN_SHARED_PREF, true);
                                editor.putString(LoginConfig.USERNAME_SHARED_PREF, res_username);
                                editor.putString(LoginConfig.NAME_SHARED_PREF, res_name);
                                editor.apply();

                                sendTokenToServer(res_username);
                                Intent intent = new Intent(LoginActivity.this, UserProfile.class);
                                startActivity(intent);

                            } catch (JSONException e) {
                                Log.i("myTag", "JSON exception");
                                e.printStackTrace();
                            }


                        }else{
                            //If the server response is not success
                            //Displaying an error message on toast
                            Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        //String statusCode = String.valueOf(error.networkResponse.statusCode);
                        Log.i("myTag","volleyError");

                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                //Adding parameters to request
                params.put(LoginConfig.KEY_USERNAME, username);
                params.put(LoginConfig.KEY_PASSWORD, password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    //storing token to mysql server
    private void sendTokenToServer(final String username) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering Device...");
        progressDialog.show();

        final String token = SharedPrefManager.getInstance(this).getDeviceToken();

        if (token == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Token not generated", Toast.LENGTH_LONG).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, NotificationConfig.URL_REGISTER_DEVICE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.i("myTag", response);
/*                        try {
                            JSONObject obj = new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }*/
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Log.i("myTag", "error");
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("token", token);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
