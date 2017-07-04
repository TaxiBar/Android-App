package com.example.gjen.taxibar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RoutePlanActivity extends AppCompatActivity {

    EditText etEndPlace, etEndLat, etEndLong;
    Button btnStartPlan;

    // 抓位置
    static final int MIN_TIME = 5000;// 位置更新條件：5000 毫秒
    static final float MIN_DIST = 0; // 位置更新條件：5 公尺
    LocationManager mgr;        // 定位總管
    LocationListener myLocListener;
    Location currentLocation;
    LatLng currentLatLng;
    String provider = LocationManager.NETWORK_PROVIDER;

    String plateNum = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_plan);
        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        setMyLocListener();
        findviews();
        getBundle();
        getCurrent();
    }

    private void findviews(){
        etEndPlace = (EditText) findViewById(R.id.editText8);
        etEndLat = (EditText) findViewById(R.id.editText9);
        etEndLong = (EditText) findViewById(R.id.editText10);
        btnStartPlan = (Button) findViewById(R.id.button6);
        btnStartPlan.setEnabled(false);
    }

    private void getCurrent(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        currentLocation = mgr.getLastKnownLocation(provider);
        if(currentLocation != null) {
            currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            btnStartPlan.setEnabled(true);
        }
    }

    private void getBundle(){
        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null){
            plateNum = bundle.getString("PlateNum");
            if(!plateNum.equals("null") || plateNum != null){
                this.setTitle("車號 : " + plateNum);
            }
        }
    }

    private void setMyLocListener() {
        myLocListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("abc", "current : " + location);
                currentLocation = location;
                currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                btnStartPlan.setEnabled(true);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mgr.requestLocationUpdates(provider, MIN_TIME, MIN_DIST, myLocListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mgr.removeUpdates(myLocListener);
    }

    private LatLng geoCorder(String address){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        LatLng latLng = null;
        try {
            List<Address> addressLocation = geocoder.getFromLocationName(address, 1);
            latLng = new LatLng(addressLocation.get(0).getLatitude(), addressLocation.get(0).getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return latLng;
    }

    public void onEndChangeLatLng(View v){
        String address = etEndPlace.getText().toString();
        LatLng latLng = geoCorder(address);
        etEndLat.setText(String.valueOf(latLng.latitude));
        etEndLong.setText(String.valueOf(latLng.longitude));
    }

    public void onStartPlan(View v){
        if(currentLatLng != null && etEndLat != null && etEndLong != null){
            Intent it = new Intent(RoutePlanActivity.this, MapsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("PlateNum", plateNum);
            bundle.putString("startLat", String.valueOf(currentLatLng.latitude));
            bundle.putString("startLong", String.valueOf(currentLatLng.longitude));
            bundle.putString("endLat", etEndLat.getText().toString());
            bundle.putString("endLong", etEndLong.getText().toString());
            it.putExtras(bundle);
            startActivity(it);
        }else{
            Toast.makeText(this, "請輸入經緯度", Toast.LENGTH_SHORT);
        }

    }


    public void onRoutePlanCancel(View v) {
        RoutePlanActivity.this.finish();
    }
}
