package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {
    public final static String SAVE_IP = "SAVE_IP_ADDRESS";
    public static String ipAddress;
    private Button btn_test;
    private EditText ed_ip;
    private Boolean test_check;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;
    static String IPString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        test_check = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("HTTP_HELPER_PREFS", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        btn_test = (Button)findViewById(R.id.btn_test);
        ed_ip = (EditText) findViewById(R.id.ed_ip);

        ed_ip.setText(sharedPreferences.getString(SAVE_IP,""));

        btn_test.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ipAddress = ed_ip.getText().toString().trim();
                editor.putString(SAVE_IP, ipAddress);
                editor.commit();
                IPString = "test";
                if (ipAddress.length() > 0) {
                    Toast.makeText(MainActivity.this, "傳送請求中", Toast.LENGTH_SHORT).show();
                    new HttpRequestAsyncTask(
                            ipAddress,IPString
                    ).execute();
                }
            }
        });

    }

    public static String sendRequest(String ipAddress, String sendDataString) {
        String serverResponse = "ERROR";
        try {
            HttpClient httpclient = new DefaultHttpClient();
            URI website = new URI("http://"+ipAddress+"/"+sendDataString);
            HttpGet getRequest = new HttpGet();
            getRequest.setURI(website);
            HttpResponse response = httpclient.execute(getRequest);
            InputStream content = null;
            content = response.getEntity().getContent();
            BufferedReader in = new BufferedReader(new InputStreamReader(content));
            serverResponse = in.readLine();
            content.close();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        }catch (URISyntaxException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return serverResponse;
    }


    public class HttpRequestAsyncTask extends AsyncTask<Void, Void, Void> {
        protected String requestReply,ipAddress,sendDataString;
        public HttpRequestAsyncTask (String ipAddress,String sendDataString) {
            this.ipAddress = ipAddress;
            this.sendDataString = sendDataString;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            requestReply = sendRequest(ipAddress, sendDataString);
            if(requestReply.indexOf("Connected") != -1){
                startService(new Intent(MainActivity.this, MyService.class));
                finish();
            }
            return null;
        }
    }
}