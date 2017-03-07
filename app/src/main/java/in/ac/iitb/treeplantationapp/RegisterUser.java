package in.ac.iitb.treeplantationapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import in.ac.iitb.treeplantationapp.Configurations.LoginConfig;

public class RegisterUser extends AppCompatActivity {

    EditText etUsername;
    EditText etName;
    EditText etPassword;
    EditText etPhone;
    EditText etEmail;

    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        etUsername = (EditText) findViewById(R.id.etUsername);
        etName = (EditText) findViewById(R.id.etName);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etEmail = (EditText) findViewById(R.id.etEmail);
        btnRegister = (Button) findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    public void register(){
        final String req_username = etUsername.getText().toString().trim();
        final String req_name = etName.getText().toString().trim();
        final String req_password = etPassword.getText().toString().trim();
        final String req_phone = etPhone.getText().toString().trim();
        final String req_email = etEmail.getText().toString().trim();

        final ProgressDialog loading = ProgressDialog.show(this,"Registering...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, LoginConfig.REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if(response.trim().equalsIgnoreCase(LoginConfig.REGISTER_SUCCESS)){
                            Toast.makeText(RegisterUser.this, "Successfully registered the user", Toast.LENGTH_SHORT).show();
                            Intent loginIntent = new Intent(RegisterUser.this, LoginActivity.class);
                            startActivity(loginIntent);
                        }else if(response.split(" ")[0].equalsIgnoreCase("Duplicate")){
                            Toast.makeText(RegisterUser.this, "Username already taken", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(RegisterUser.this, response, Toast.LENGTH_LONG).show();
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
                params.put(LoginConfig.KEY_USERNAME, req_username);
                params.put(LoginConfig.KEY_NAME, req_name);
                params.put(LoginConfig.KEY_PASSWORD, req_password);
                params.put(LoginConfig.KEY_PH_NO, req_phone);
                params.put(LoginConfig.KEY_EMAIL, req_email);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
