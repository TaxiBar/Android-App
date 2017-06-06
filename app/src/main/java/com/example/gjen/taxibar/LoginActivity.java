package com.example.gjen.taxibar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class LoginActivity extends AppCompatActivity {
    private static String strValidateUrl = "http://140.134.26.71:9990/taxibar/webapi/user/validate";
    private EditText etAcc, etPwd;
    private static File file;
    private SharedPreferences setting;
    private String userNameInSP;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findviews();
        setSharePref();
    }

    private void findviews(){
        etAcc = (EditText)findViewById(R.id.editText);
        etPwd = (EditText)findViewById(R.id.editText2);
    }

    public void onEnter(View v){
        String userName = etAcc.getText().toString();
        String password = etPwd.getText().toString();
//        userNameInSP = "GJen123";
//        setUserName();
//        SendIntent();
        validate(userName, password);
    }

    public void onMemberAdd(View v){
        Intent it = new Intent(LoginActivity.this, MemberAddActivity.class);
        startActivity(it);
    }

    private void validate(final String userName, final String password){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient client = new DefaultHttpClient();
                try {
                    HttpPost post = new HttpPost(strValidateUrl);

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
                        Log.d("abc", "result : " + result);
                        if(result.equals("true")){
                            userNameInSP = userName;
                            setUserName();
                            SendIntent();
                        }else {
                            handler.post(runnable);
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

    private void setSharePref(){
        file = new File("/data/data/com.example.gjen.taxibar/shared_prefs","LoginInfo.xml");
        if(file.exists()){
            ReadValue();
            if(!"".equals(userNameInSP)){
                SendIntent();
            }
        }
    }

    private void setUserName(){
        setting = getSharedPreferences("LoginInfo", 0);
        setting.edit().putString("userName", userNameInSP).commit();
    }

    private void ReadValue(){
        setting = getSharedPreferences("LoginInfo", 0);
        userNameInSP = setting.getString("userName", "");
    }

    private void SendIntent(){
        Intent it = new Intent(LoginActivity.this, PlateCheckActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userName", userNameInSP);
        it.putExtras(bundle);
        startActivity(it);
        LoginActivity.this.finish();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(LoginActivity.this, "登入失敗", Toast.LENGTH_SHORT);
        }
    };
}
