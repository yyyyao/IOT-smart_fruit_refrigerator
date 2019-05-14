package com.example.iot;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;


public class FruitActivity extends AppCompatActivity implements View.OnClickListener {

    private static int id = ViewCompat.generateViewId();
    private static Map<String, Integer> amountMap = new HashMap<>();

    private Button refresh, logout, save;
    private TableLayout table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruit);
        initView();

    }

    private void initView(){
        refresh = (Button) findViewById(R.id.refresh);
        save = (Button) findViewById(R.id.savefruit);

        table = (TableLayout) findViewById(R.id.table);
        //recyclerView = (RecyclerView) findViewById(R.id.fruitlist);

        refresh.setOnClickListener(this);
        logout.setOnClickListener(this);
        save.setOnClickListener(this);

        // Inital table title

        TableRow title = addTextRow("Fruit Name", "Expired date", "Amount");
        table.addView(title, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.MATCH_PARENT));


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.refresh:
                UpdateFruitTask fruitTask = new UpdateFruitTask();
                fruitTask.execute();
                break;
                
            case R.id.savefruit:
                SaveFruitTask saveFruit = new SaveFruitTask();
                saveFruit.execute();
                break;
            default:
                break;
        }
    }

    private TableRow addTextRow(String... titles){

        // title
        TableRow row = new TableRow(this);

        row.setId(++id);

        for(String title: titles){
            TextView text = new TextView(this);
            text.setId(++id);
            text.setText(title);
            text.setPadding(5,5,5,5);
            row.addView(text);
        }
        return row;
    }

    private TableRow addEditRow(String... titles){
        TableRow row = new TableRow(this);

        row.setId(++id);

        for(String title: titles){
            EditText text = new EditText(this);
            text.setId(++id);
            text.setText(title);
            text.setPadding(5,5,5,5);
            row.addView(text);
        }
        return row;
    }

    private class UpdateFruitTask extends AsyncTask<String, Void, String>{
        String fruiturl = "";

        @Override
        protected String doInBackground(String... strings) {
            SharedPreferences settings = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            String email = settings.getString("email", null);
            Map<String, String> map = new HashMap<>();
            map.put("email", email);
            String response = HttpManager.send(fruiturl, map, "GET");

            return response;
        }

        @Override
        protected void onPostExecute(String res) {
            // Get data

            try {
                JSONObject json = new JSONObject(res);
                Iterator<String> machineIds = json.keys();
                table.removeViews(1, table.getChildCount() - 1);

                while(machineIds.hasNext()) {
                    String machineId = machineIds.next();

                    if(json.get(machineId) instanceof JSONObject){
                        JSONObject data = (JSONObject) json.get(machineId);
                        String[] datatext = new String[2];

                        datatext[0] = (String) data.get("fruit_name");
                        datatext[1] = (String) data.get("expire_date");
                        TableRow textrow = addTextRow(datatext);

                        EditText text = new EditText(FruitActivity.this);
                        text.setId(++id);
                        String amount = (String) data.get("amount");
                        text.setText(amount);
                        text.setPadding(10, 5, 5, 5);
                        textrow.addView(text);
                        table.addView(textrow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                                TableLayout.LayoutParams.MATCH_PARENT));

                        amountMap.put(machineId, id);
                    }
                }

            }catch(JSONException e){
                e.printStackTrace();
            }
        }

    }

    private class SaveFruitTask extends AsyncTask<String, Void, String>{
        private String fruiturl = "";

        @Override
        protected String doInBackground(String... strings) {
            SharedPreferences settings = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            String email = settings.getString("email", null);

            Map<String, String> map = new HashMap<>();
            map.put("email", email);

            EditText editText;
            for(String key: amountMap.keySet()){
                editText = (EditText) findViewById(amountMap.get(key));
                String amount = editText.getText().toString();
                map.put(key, amount);
            }
            String response = HttpManager.send(fruiturl, map, "POST");
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s.equals("true")){
                return;
            }else{
                Toast toast = Toast.makeText(FruitActivity.this, "Update Error", Toast.LENGTH_LONG);
                toast.show();
                return;
            }
        }
    }


}
