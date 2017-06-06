package com.example.gjen.taxibar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.File;

public class MemberActivity extends AppCompatActivity {
    TextView tvUsername;
    String userName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);
        getBundle();
        findviews();
    }

    private void findviews(){
        tvUsername = (TextView)findViewById(R.id.textView5);
        tvUsername.setText("Hello " + userName + " !");
    }

    private void getBundle(){
        Bundle bundle = this.getIntent().getExtras();
        userName = bundle.getString("userName");
    }

    public void onLogout(View v){
        File file = new File("/data/data/com.example.gjen.taxibar/shared_prefs","LoginInfo.xml");
        file.delete();
        Intent it = new Intent(MemberActivity.this, LoginActivity.class);
        startActivity(it);
        MemberActivity.this.finish();
    }
}
