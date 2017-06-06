package com.example.gjen.taxibar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MemberAddActivity extends AppCompatActivity {
    private static String strRegisterUrl = "http://140.134.26.71:9990/taxibar/webapi/user/register";
    private EditText etAcc, etPwd;
    private static File file;
    private SharedPreferences setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_add);
        findviews();
    }

    private void findviews(){
        etAcc = (EditText)findViewById(R.id.editText3);
        etPwd = (EditText)findViewById(R.id.editText4);
    }

    public void onRegOk(View v){
        String userName = etAcc.getText().toString();
        String password = etPwd.getText().toString();
        register(userName, password);
    }

    public void onRegCancel(View v){
        MemberAddActivity.this.finish();
    }

    private void register(final String userName, final String password){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient client = new DefaultHttpClient();
                try {
                    HttpPost post = new HttpPost(strRegisterUrl);
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("userName",userName));
                    params.add(new BasicNameValuePair("password",password));

                    UrlEncodedFormEntity ent = null;
                    ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                    post.setEntity(ent);

                    HttpResponse responsePOST = client.execute(post);
                    HttpEntity resEntity = responsePOST.getEntity();

                    String result = null;
                    if(resEntity != null){
                        result = EntityUtils.toString(resEntity);
                        if(result.equals("true")){
                            setSharePref(userName);
                            SendIntent();
                        }else {
                            Toast.makeText(MemberAddActivity.this, "註冊失敗", Toast.LENGTH_SHORT);
                        }
                    }else{

                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setSharePref(String userName){
        file = new File("/data/data/com.example.gjen.taxibar/shared_prefs","LoginInfo.xml");
        if(file.exists()){
            setting = getSharedPreferences("LoginInfo", 0);
            setting.edit().putString("userName", userName).commit();
        }
    }

    private void SendIntent(){
        Intent it = new Intent(MemberAddActivity.this, MainActivity.class);
        startActivity(it);
        MemberAddActivity.this.finish();
    }
}
