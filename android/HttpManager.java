package com.example.iot;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class HttpManager {

    public static String send(String urladdress, Map<String, String> map, String method){
        try {
            URL url = new URL(urladdress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // Send message to server
            OutputStream outPutStream = conn.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outPutStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            JSONObject json = new JSONObject();
            for(Map.Entry item: map.entrySet()){
                try{
                json.put((String) item.getKey(), item.getValue());
                }catch(JSONException e){
                    e.printStackTrace();
                    return null;
                }
            }
            Log.d("HttpSend", "Send json");

            bufferedWriter.write("PostData="+json.toString());
            bufferedWriter.flush();
            bufferedWriter.close();
            outPutStream.close();

            // receive response from server
            InputStream inputStream = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String response = "";
            String line = "";
            while((line = bufferedReader.readLine()) != null){
                response += line;
            }
            bufferedReader.close();
            inputStream.close();
            conn.disconnect();

            Log.d("HttpRev", response);

            return response;

        }catch (MalformedURLException e){
            e.printStackTrace();
            return null;
        }catch (ProtocolException e){
            e.printStackTrace();
            return null;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
