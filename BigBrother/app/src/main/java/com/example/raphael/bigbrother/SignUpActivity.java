package com.example.raphael.bigbrother;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }

    public void doRegister(View view) {
        String url = getResources().getString(R.string.registerUrl);

        // Get EditText Values
        EditText firstNameField = findViewById(R.id.firstNameField);
        EditText lastNameField = findViewById(R.id.lastNameField);
        EditText emailTextField = findViewById(R.id.emailField);
        EditText usernameTextField = findViewById(R.id.usernameField);
        EditText passwordTextField = findViewById(R.id.passwordField);
        EditText passwordVerificationTextField = findViewById(R.id.passwordVerificationField);
        String firstName = firstNameField.getText().toString().trim();
        String lastName = lastNameField.getText().toString().trim();
        String email = emailTextField.getText().toString().trim();
        String username = usernameTextField.getText().toString().trim();
        String password = passwordTextField.getText().toString().trim();
        String password2 = passwordVerificationTextField.getText().toString().trim();

        if (!password.equals(password2)) {
            Toast.makeText(this, "Password entries do not match. Please re-enter your password.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create body of JSON object to send to Web server
        final JSONObject body = new JSONObject();
        try {
            body.put("firstName", firstName);
            body.put("lastName", lastName);
            body.put("email", email);
            body.put("username", username);
            body.put("password", password);
        } catch (JSONException e) {
            // TODO: Fallback if username or password cannot be used to create JSON Object
        }

        // Create a request
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, body, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Successful login
                        try {
                            ConnectionHandler.user.username = body.getString("username");
                            ConnectionHandler.user.firstName = body.getString("firstName");
                            ConnectionHandler.user.lastName = body.getString("lastName");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        goHome();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showHttpResponseError(error);
                    }
                });

        // Make the request (add it to the request queue)
        ConnectionHandler.getInstance(this).addToRequestQueue(jsObjRequest);
    }

    public void showHttpResponseError(VolleyError error) {
        // If the server could be access, but error code was returned
        if (error.networkResponse != null) {
            switch(error.networkResponse.statusCode) {
                case 404:
                    Toast.makeText(this, "Error (" + error.networkResponse.statusCode + ")\nNot found.", Toast.LENGTH_SHORT).show();
                    break;
                case 500:
                    Toast.makeText(this, "Error (" + error.networkResponse.statusCode + ")\nCould not complete registration.", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this, "Unknown error occurred.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        else {
            // In case no connection could be established at all (i.e., server is down)
            Toast.makeText(this, "Could not connect to server.", Toast.LENGTH_SHORT).show();
        }
    }

    public void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}