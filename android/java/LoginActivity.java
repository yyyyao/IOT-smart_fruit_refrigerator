package com.example.iot;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.util.Patterns;
import android.widget.Toast;
import android.content.Intent;
import android.util.Log;

import java.util.Map;
import java.util.HashMap;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText editTextEmail, editTextPassword;
    private Button loginSendButton;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        Boolean login = loginPreferences.getBoolean("login", false);

        if(login){
            Intent i = new Intent(LoginActivity.this, PageActivity.class);
            startActivity(i);
            return;
        }

        setContentView(R.layout.activity_login);

        editTextEmail = (EditText) findViewById(R.id.email);
        editTextPassword = (EditText) findViewById(R.id.password);
        loginSendButton = (Button) findViewById(R.id.loginSend);
        loginSendButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if(editTextEmail.getText().toString().trim().length() == 0 ||
           editTextPassword.getText().toString().trim().length() == 0){
            Toast.makeText(this, "Empty email or password.", Toast.LENGTH_LONG).show();
            return;
        }
        else if(!isValidEmail(editTextEmail.getText())){
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_LONG).show();
            return;
        }
        else{

            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            Log.d("Login Event", email+" "+ password);

            LoginTask logintask = new LoginTask();
            logintask.execute(email, password);

        }

    }

    private static boolean isValidEmail(CharSequence email){
        if(email == null) return false;
        else return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private class LoginTask extends AsyncTask<String, Void, String>{
        private String loginurl = "";
        private String email;

        protected String doInBackground(String...params){
            email = params[0];
            String password = params[1];
            Map<String, String> map = new HashMap<>();
            map.put("email", email);
            map.put("password", password);
            String response = HttpManager.send(loginurl, map, "POST");

            return response;
        }

        protected void onPostExecute(String res){
            String[] response = res.split("[,]");
            String Login = response[0];
            if(Login.equals("true")){

                Intent intent = new Intent(LoginActivity.this, PageActivity.class);
                startActivity(intent);

                // save login status
                loginPrefsEditor.putBoolean("login", true);
                loginPrefsEditor.putString("email", email);
                loginPrefsEditor.commit();

                return;
            }else{
                Toast toast = Toast.makeText(LoginActivity.this, "login error", Toast.LENGTH_LONG);
                toast.show();

                return;
            }

        }
    }


}
