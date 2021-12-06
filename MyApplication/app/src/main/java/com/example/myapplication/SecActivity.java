package com.example.myapplication;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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

public class SecActivity extends AppCompatActivity{
    static String LED1,LED2,LED3,LED4;
    static String door1;
    private String Warning_req;
    private Button btn_led1,btn_led2,btn_led3,btn_led4;
    private Button btn_door1;
    private TextView tv_ip;
    private TextView textView13;
    private Boolean flag = false;
    private String ip;

    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sec);


        sharedPreferences = getSharedPreferences("HTTP_HELPER_PREFS", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Warning_req = "0";

        btn_led1 = (Button)findViewById(R.id.btn_led1);
        btn_led2 = (Button)findViewById(R.id.btn_led2);
        btn_led3 = (Button)findViewById(R.id.btn_led3);
        btn_led4 = (Button)findViewById(R.id.btn_led4);

        btn_door1 = (Button)findViewById(R.id.btn_door1);
        tv_ip = (TextView)findViewById(R.id.tv_ip);
        Toast.makeText(SecActivity.this, "連接成功", Toast.LENGTH_SHORT).show();
        ip = MainActivity.ipAddress;
        tv_ip.setText("IP : " + ip);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    new SecActivity.HttpRequestAsyncTask2(ip,"getvalue").execute();
                }
            }
        }).start();

        btn_led1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(LED1.contains("OFF"))
                    new SecActivity.HttpRequestAsyncTask2(ip,"gpio/LED/1/low").execute();
                else
                    new SecActivity.HttpRequestAsyncTask2(ip,"gpio/LED/1/high").execute();
            }
        });
        btn_led2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(LED2.contains("OFF"))
                    new SecActivity.HttpRequestAsyncTask2(ip,"gpio/LED/2/low").execute();
                else
                    new SecActivity.HttpRequestAsyncTask2(ip,"gpio/LED/2/high").execute();
            }
        });
        btn_led3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(LED3.contains("OFF"))
                    new SecActivity.HttpRequestAsyncTask2(ip,"gpio/LED/3/low").execute();
                else
                    new SecActivity.HttpRequestAsyncTask2(ip,"gpio/LED/3/high").execute();
            }
        });
        btn_led4.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(LED4.contains("OFF"))
                    new SecActivity.HttpRequestAsyncTask2(ip,"gpio/LED/4/low").execute();
                else
                    new SecActivity.HttpRequestAsyncTask2(ip,"gpio/LED/4/high").execute();
            }
        });
        btn_door1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(door1.contains("CLOSE"))
                    new SecActivity.HttpRequestAsyncTask2(ip,"gpio/door/1/close").execute();
                else
                    new SecActivity.HttpRequestAsyncTask2(ip,"gpio/door/1/open").execute();
            }
        });
    }
    private void showToast(){
        Toast toast = new Toast(SecActivity.this);
        toast.setGravity(Gravity.TOP,0,50);
        toast.setDuration(Toast.LENGTH_SHORT);
        LayoutInflater inflater = getLayoutInflater();

        View layout = inflater.inflate(R.layout.custom_toast,(ViewGroup)findViewById(R.id.custom_toast_root));
        toast.setView(layout);
        toast.show();
    }
    private void showToast_2(){
        Toast toast = new Toast(SecActivity.this);
        toast.setGravity(Gravity.TOP,0,50);
        toast.setDuration(Toast.LENGTH_SHORT);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast_2,(ViewGroup)findViewById(R.id.custom_toast_root));
        toast.setView(layout);
        toast.show();
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
            // HTTP error
            e.printStackTrace();
        }catch (URISyntaxException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return serverResponse;
    }



    public class HttpRequestAsyncTask2 extends AsyncTask<Void, Void, Void> {
        protected String requestReply,ipAddress,sendDataString;
        public HttpRequestAsyncTask2 (String ipAddress,String sendDataString) {
            this.ipAddress = ipAddress;
            this.sendDataString = sendDataString;
        }
        @Override
        protected Void doInBackground(Void... voids) {

            requestReply = sendRequest(ipAddress, sendDataString);
            try{
                if(requestReply.indexOf("RFIDresponseValid") != -1) {
                    if(requestReply.indexOf("Entry") != -1) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(SecActivity.this, "合法卡進入", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(SecActivity.this, "合法卡離開", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                else if(requestReply.indexOf("RFIDresponseInvalid") != -1) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(SecActivity.this, "不合法卡片嘗試進入", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                if(requestReply.indexOf("WarningResult") != -1){
                    if(requestReply.indexOf("true") != -1) {
                        if(Warning_req.contains("0")){
                            Warning_req = "2";
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable(){
                                public void run(){
                                    showToast();
                                }
                            });
                        }
                    }
                    else{
                        if(Warning_req .contains("2")){
                            Warning_req = "1";
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable(){
                                public void run(){
                                    showToast_2();
                                //    Toast.makeText(SecActivity.this, "人體偵測警報已排除", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else if(Warning_req.contains("1")){
                            Warning_req = "0";
                        }
                    }
                }
                if(requestReply.indexOf("GetValue") != -1){
                    if(requestReply.indexOf("LED1high") != -1)
                        LED1 = "OFF";
                    else
                        LED1 = "ON";
                    if(requestReply.indexOf("LED2high") != -1)
                        LED2 = "OFF";
                    else
                        LED2 = "ON";
                    if(requestReply.indexOf("LED3high") != -1)
                        LED3 = "OFF";
                    else
                        LED3 = "ON";
                    if(requestReply.indexOf("LED4high") != -1)
                        LED4 = "OFF";
                    else
                        LED4 = "ON";
                    if(requestReply.indexOf("dooropen") != -1)
                        door1 = "CLOSE";
                    else
                        door1 = "OPEN";
                }
                btn_led1.setText(LED1);
                btn_led2.setText(LED2);
                btn_led3.setText(LED3);
                btn_led4.setText(LED4);
                btn_door1.setText(door1);
            }catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }
}