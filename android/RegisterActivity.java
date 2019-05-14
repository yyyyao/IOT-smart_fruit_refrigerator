package com.example.iot;
import java.util.Map;
import java.util.HashMap;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;



public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextEmail, editTextPassword, editTextName, editTextMachine;
    private Button signupSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextEmail = (EditText) findViewById(R.id.signupEmail);
        editTextPassword = (EditText) findViewById(R.id.signupPassword);
        editTextName = (EditText) findViewById(R.id.signupName);
        editTextMachine= (EditText) findViewById(R.id.signupMachine);
        signupSendButton = (Button) findViewById(R.id.signupSend);
        signupSendButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        String name = editTextName.getText().toString();
        String machine = editTextMachine.getText().toString();

        if(email.trim().length()==0 || password.trim().length() == 0 || name.trim().length() == 0 || machine.trim().length() == 0){
            Toast.makeText(this, "Empty input", Toast.LENGTH_LONG);
            return;
        }else{

            Log.d("Register Event", email+" "+ password);

            RegisterActivity.RegisterTask registerTask = new RegisterActivity.RegisterTask();
            registerTask.execute(email, password, name, machine);

        }

    }

    private class RegisterTask extends AsyncTask<String, String, String>{
        private String signupurl = "";

        protected String doInBackground(String...params){

            String email = params[0];
            String password = params[1];
            String name = params[2];
            String machine = params[3];
            Map<String, String> map = new HashMap<>();

            map.put("email", email);
            map.put("password", password);
            map.put("name", name);
            map.put("machine", machine);

            String response = HttpManager.send(signupurl, map, "POST");

            return response;
        }

        protected void onPostExecute(String res){
            String[] response = res.split("[,]");
            Log.d("Response",res);
            String Signup = response[0];
            if(Signup.equals("true")){
                Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                startActivity(intent);
                return;
            }else{
                Toast.makeText(RegisterActivity.this, "Register error", Toast.LENGTH_LONG);
                return;
            }

        }
    }
}
