package com.example.gjen.taxibar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    String userName = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getBundle();
    }

    private void getBundle(){
        Bundle bundle = this.getIntent().getExtras();
        userName = bundle.getString("userName");
    }

    public void onPlateCheck(View v){
        Intent it = new Intent(MainActivity.this, PlateCheckActivity.class);
        startActivity(it);
    }

    public void onRoutePlan(View v){
        Intent it = new Intent(MainActivity.this, RoutePlanActivity.class);
        startActivity(it);
    }

    public void onComment(View v){
        Intent it = new Intent(MainActivity.this, CommentActivity.class);
        startActivity(it);
    }

    public void onMember(View v){
        Intent it = new Intent(MainActivity.this, MemberActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userName", userName);
        it.putExtras(bundle);
        startActivity(it);
    }
}
