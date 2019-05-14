package com.example.iot;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();
    private Button btn1, btn2;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView(){
        btn1 = (Button) findViewById(R.id.button);
        btn2 = (Button) findViewById(R.id.button2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch(v.getId()){
            case R.id.button:
                loginPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                loginPrefsEditor = loginPreferences.edit();
                Boolean login = loginPreferences.getBoolean("login", false);
                if(login){
                    loginPrefsEditor.putBoolean("login", false);
                    loginPrefsEditor.putString("email", null);
                    loginPrefsEditor.putString("password", null);
                    loginPrefsEditor.commit();
                    Toast.makeText(this, "Logout.", Toast.LENGTH_LONG).show();
                }
                i = new Intent(MainActivity.this, RegisterActivity.class);
                break;
            case R.id.button2:
                i = new Intent(MainActivity.this, LoginActivity.class);
                break;
            default:
                i = new Intent(MainActivity.this, MainActivity.class);
                break;
        }
        startActivity(i);
    }


}
