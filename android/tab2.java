package com.example.iot;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link tab2.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link tab2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class tab2 extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText recommendDays;
    private Button recommendRefresh;

    private TextView vitaminA;
    private TextView vitaminB1;
    private TextView vitaminB2;
    private TextView vitaminB6;
    private TextView vitaminC;

    private TextView recommendFruit;

    private String[] vitaminname = new String[]{"A", "B1", "B2", "B6", "C"};

    private OnFragmentInteractionListener mListener;

    public tab2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment tab2.
     */
    // TODO: Rename and change types and number of parameters
    public static tab2 newInstance(String param1, String param2){
        tab2 fragment = new tab2();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab2, container, false);

        recommendDays = (EditText) view.findViewById(R.id.day);
        recommendRefresh = (Button) view.findViewById(R.id.recommendRefresh);

        recommendRefresh.setOnClickListener(this);

        vitaminA = (TextView) view.findViewById(R.id.vitaminA);
        vitaminB1 = (TextView) view.findViewById(R.id.vitaminB1);
        vitaminB2 = (TextView) view.findViewById(R.id.vitaminB2);
        vitaminB6 = (TextView) view.findViewById(R.id.vitaminB6);
        vitaminC = (TextView) view.findViewById(R.id.vitaminC);

        recommendFruit = (TextView) view.findViewById(R.id.recommendlist);

        return view;
    }

    @Override
    public void onClick(View v) {
        if(recommendDays.getText().toString().length() != 0){
            tab2.UpdateRecommend updateRecommend = new UpdateRecommend();
            updateRecommend.execute();
        }else{
            Toast toast = Toast.makeText(tab2.this.getActivity(), "Please input days", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
    }

    private class UpdateRecommend extends AsyncTask<String, Void, String>{
        String url = "";
        @Override
        protected String doInBackground(String... strings) {
            SharedPreferences settings = tab2.this.getActivity().getSharedPreferences("LoginPrefs",
                    Context.MODE_PRIVATE);
            String email = settings.getString("email", null);
            String day = recommendDays.getText().toString();
            Map<String, String> map = new HashMap<>();
            map.put("email", email);
            map.put("day", day);
            String response = HttpManager.send(url, map, "POST");
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s.length() > 0){
                try {
                    JSONObject json = new JSONObject(s);
                    String recommend = json.getString("recommend");
                    recommendFruit.setText(recommend);

                    vitaminA.setText(json.getString("A"));
                    vitaminB1.setText(json.getString("B1"));
                    vitaminB2.setText(json.getString("B2"));
                    vitaminB6.setText(json.getString("B6"));
                    vitaminC.setText(json.getString("C"));

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
