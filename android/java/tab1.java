package com.example.iot;

import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link tab1.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link tab1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class tab1 extends Fragment implements ViewStub.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match

    private static int id = ViewCompat.generateViewId();
    private OnFragmentInteractionListener mListener;
    private Button refresh, save, logout;
    private TableLayout table;

    private static Map<String, Integer> amountMap = new HashMap<>();

    public tab1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment tab1.
     */
    // TODO: Rename and change types and number of parameters
    public static tab1 newInstance(String param1, String param2) {
        tab1 fragment = new tab1();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.activity_fruit, container, false);

        refresh = (Button) view.findViewById(R.id.refresh);
        save = (Button) view.findViewById(R.id.savefruit);
        logout = (Button) view.findViewById(R.id.logout);
        table = (TableLayout) view.findViewById(R.id.table);

        refresh.setOnClickListener(this);
        save.setOnClickListener(this);
        logout.setOnClickListener(this);

        TableRow title = addTextRow("Fruit Name", "Expired date", "Amount");
        table.addView(title, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.MATCH_PARENT));

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.refresh:
                tab1.UpdateFruitTask fruitTask = new tab1.UpdateFruitTask(getActivity());
                fruitTask.execute();
                break;
            case R.id.savefruit:
                tab1.SaveFruitTask saveFruit = new tab1.SaveFruitTask();
                saveFruit.execute();
                break;
            case R.id.logout:
                SharedPreferences settings = tab1.this.getActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("login", false);
                editor.putString("email", null);
                editor.putString("password", null);
                editor.commit();
                Toast.makeText(tab1.this.getActivity(), "Logout", Toast.LENGTH_LONG).show();
                Intent i = new Intent(tab1.this.getActivity(), MainActivity.class);
                startActivity(i);
                break;
            default:
                break;
        }
    }

    private class UpdateFruitTask extends AsyncTask<String, Void, String> {
        String fruiturl = "";
        Activity mContext;

        public UpdateFruitTask(Activity activity){
            mContext = activity;
        }

        @Override
        protected String doInBackground(String... strings) {
            SharedPreferences settings = tab1.this.getActivity().getSharedPreferences("LoginPrefs",
                                                                                        Context.MODE_PRIVATE);
            String email = settings.getString("email", null);
            Map<String, String> map = new HashMap<>();
            map.put("email", email);
            String response = HttpManager.send(fruiturl, map, "GET");

            return response;
        }

        @Override
        protected void onPostExecute(String res) {
            // Get data
            if (res.length() > 0) {
                try {
                    JSONObject json = new JSONObject(res);
                    Iterator<String> machineIds = json.keys();
                    if (table.getChildCount() > 1) table.removeViews(1, table.getChildCount() - 1);

                    while (machineIds.hasNext()) {
                        String machineId = machineIds.next();

                        if (json.get(machineId) instanceof JSONObject) {
                            JSONObject data = (JSONObject) json.get(machineId);
                            String[] datatext = new String[2];

                            datatext[0] = (String) data.get("fruit_name");
                            datatext[1] = (String) data.get("expire_date");
                            TableRow textrow = addTextRow(datatext);

                            EditText text = new EditText(tab1.this.getActivity());
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

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private class SaveFruitTask extends AsyncTask<String, Void, String>{
        private String fruiturl = "";

        @Override
        protected String doInBackground(String... strings) {
            SharedPreferences settings = tab1.this.getActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
            String email = settings.getString("email", null);

            Map<String, String> map = new HashMap<>();
            map.put("email", email);

            EditText editText;
            for(String key: amountMap.keySet()){
                editText = (EditText) getView().findViewById(amountMap.get(key));
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
                Toast toast = Toast.makeText(tab1.this.getActivity(), "Update Error", Toast.LENGTH_LONG);
                toast.show();
                return;
            }
        }
    }

    private TableRow addTextRow(String... titles){

        // title
        TableRow row = new TableRow(tab1.this.getActivity());

        row.setId(++id);

        for(String title: titles){
            TextView text = new TextView(tab1.this.getActivity());
            text.setId(++id);
            text.setText(title);
            text.setPadding(5,5,5,5);
            row.addView(text);
        }
        return row;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
