package com.example.gjen.taxibar;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gjen.taxibar.camera.GraphicOverlay;
import com.example.gjen.taxibar.camera.OcrGraphic;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    int checkDisSmallestDis = 1;

    EditText etAddr;
    ImageButton btnMic;
    TextView tvMoney;
    LinearLayout linearLayout;

    // Permission request codes need to be < 256
    private static final int PERM_LOCATION_CODE = 2;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    private GoogleMap mMap;
    String data = "";
    Marker itemMarker, currentMarker, destiMarker;
    String plateNum = null;
    String userName = null;

    // 抓位置
    static final int MIN_TIME = 1100;// 位置更新條件：5000 毫秒
    static final float MIN_DIST = 0; // 位置更新條件：5 公尺
    LocationManager mgr;        // 定位總管
    LocationListener myLocListener;
    Location currentLocation;
    LatLng currentLatLng, destiLatLng;
    String GPSProvider = LocationManager.GPS_PROVIDER;
    String NetProvider = LocationManager.NETWORK_PROVIDER;
    String bestProvider;

    // 路經中的轉角點
    ArrayList<LatLng> points = null;
    int distance = 0;
    int money;

    // 通知
    NotificationManager ntfMgr;
    final int NTF_ID = 1000;
    int notiFlag = 0;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        permissionCheck();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        findviews();
        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        ntfMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        getBestProvider();
        setMyLocListener();
        getBundle();
    }

    private void permissionCheck(){
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permission != PackageManager.PERMISSION_GRANTED){
            // 未取得權限
            requestLocationPermission();
        }else {
            // 取得權限
        }
    }

    private void requestLocationPermission(){
        final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, permissions, PERM_LOCATION_CODE);
            return;
        }

        ActivityCompat.requestPermissions(this, permissions, PERM_LOCATION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode != PERM_LOCATION_CODE){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        if(grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            onMapReady(mMap);
        }
    }

    private void findviews() {
        etAddr = (EditText) findViewById(R.id.editText5);
        tvMoney = (TextView) findViewById(R.id.textView);
        btnMic = (ImageButton) findViewById(R.id.imageButton);
        linearLayout = (LinearLayout) findViewById(R.id.mapsLinearLayout);
        PackageManager pm = getPackageManager();
        List activities = pm.queryIntentActivities( new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if( activities.size() == 0 )
            btnMic.setEnabled( false );
    }

    private void getBundle() {
        Bundle bundle = this.getIntent().getExtras();
        plateNum = bundle.getString("PlateNum");
        userName = bundle.getString("userName");
        if(null != bundle.getString("destination") && !"".equals(bundle.getString("destination"))){
            etAddr.setText(bundle.getString("destination"));
        }
        MapsActivity.this.setTitle("車號 : " + plateNum);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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
        currentLocation = mgr.getLastKnownLocation(bestProvider);
        if(currentLocation != null){
            mMap.setMyLocationEnabled(true);
            currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            currentMarker = mMap.addMarker(new MarkerOptions()
                    .position(currentLatLng)
                    .title("Current")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi)));
            moveMap(currentLatLng);
            if(!"".equals(etAddr.getText().toString()) && null != etAddr.getText().toString()){
                setPlanLatLng();
            }
        }
    }

    private void getBestProvider(){
//        Criteria criteria = new Criteria();
//        bestProvider = mgr.getBestProvider(criteria, false);
        bestProvider = NetProvider;
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "%2C"
                + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "%2C" + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Travelling Mode
        String mode = "mode=driving";

        //waypoints,116.32885,40.036675
        String waypointLatLng = "waypoints=" + "40.036675" + "%" + "116.32885";

        // Building the parameters to the web service
//        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&"
//                + mode+"&"+waypointLatLng;
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&"
                + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + parameters;
        System.out.println("getDerectionsURL--->: " + url);
        return url;
    }

    private String downloadUrl(final String strUrl) throws IOException {
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("abc", "data in downloadUrl : " + data);

            br.close();

        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            try {
                iStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            urlConnection.disconnect();
        }

        System.out.println("url:" + strUrl + "---->   downloadurl:" + data);
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {
        String dirUrl = "";

        public DownloadTask(String dirUrl) {
            this.dirUrl = dirUrl;
        }

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(dirUrl);
                Log.d("abc", "data : " + data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
                distance = parser.getDistance(jObject);
                Log.d("abc", "distance : " + distance);
                money = calMoney();
                handler.post(runnable);
                Log.d("abc", "money : " + String.valueOf(money));
                System.out.println("do in background:" + routes);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(15);

                // Changing the color polyline according to the mode
                lineOptions.color(Color.BLUE);
            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null){
                mMap.addPolyline(lineOptions);
            }else{
                Toast.makeText(MapsActivity.this, "搜尋不到此經緯度", Toast.LENGTH_SHORT);
            }
        }
    }

    private void moveMap(LatLng place) {
        // 建立地圖攝影機的位置物件
        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(place)
                        .zoom(15)
                        .build();

        // 使用動畫的效果移動地圖
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        if (itemMarker != null) {
                            itemMarker.showInfoWindow();
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    private void moveMap(LatLng place, int zoomNum) {
        // 建立地圖攝影機的位置物件
        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(place)
                        .zoom(zoomNum)
                        .build();

        // 使用動畫的效果移動地圖
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        if (itemMarker != null) {
                            itemMarker.showInfoWindow();
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    private void setMyLocListener() {
        myLocListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("abc", "current : " + location);
                currentLocation = location;
                currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                if(currentMarker != null){
                    currentMarker.remove();
                    currentMarker = mMap.addMarker(new MarkerOptions()
                            .position(currentLatLng)
                            .title("Current")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi)));
                }else {
                    currentMarker = mMap.addMarker(new MarkerOptions()
                            .position(currentLatLng)
                            .title("Current")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi)));
                }
//                moveMap(currentLatLng);
                if(points != null){
                    checkDis();
                }
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
        mgr.requestLocationUpdates(bestProvider, MIN_TIME, MIN_DIST, myLocListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mgr.removeUpdates(myLocListener);
    }

    public void onMapsMic(View v){
        // 不知道從哪個版本之後, 語音辨識需要【網路有通】才能進行
        // 判斷有無連接網路, 若沒有, 則不允許進行語音輸入
        ConnectivityManager cm = (ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if( info == null || !info.isConnected() )
        {
            return;
        }

        try {
            // ACTION_RECOGNIZE_SPEECH: 透過 Android 內建語音辨識
            // ACTION_WEB_SEARCH: 透過外掛的語音辨識 App
            Intent intent = new Intent( RecognizerIntent.ACTION_RECOGNIZE_SPEECH );

            intent.putExtra( RecognizerIntent.EXTRA_LANGUAGE_MODEL
                    , RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            );

            // 設定語音辨識的語系
            intent.putExtra( RecognizerIntent.EXTRA_LANGUAGE, Locale.TRADITIONAL_CHINESE.toString() );

            // 回傳語音辨識有多少結果段落 (沒有設定, 回傳全部段落)
            //intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

            // 語音提示文字 (替換成您喜歡的文字)
            intent.putExtra( RecognizerIntent.EXTRA_PROMPT
                    , "請說 ..."
            );

            startActivityForResult( intent, 0 );
        }
        catch (ActivityNotFoundException e) {
            // 如果沒有安裝具有語音辨識 Activity 的時候，顯示錯誤訊息
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == 0 && resultCode == RESULT_OK )
        {
            // 取得 STT 語音辨識的結果段落
            ArrayList results = data.getStringArrayListExtra( RecognizerIntent.EXTRA_RESULTS );
            Log.d("abc", "results : " + results);

            etAddr.setText(results.get(0).toString());
        }
    }

    public void onMapsWriteComment(View v){
        Intent it = new Intent(MapsActivity.this, CommentWriteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("PlateNum", plateNum);
        it.putExtras(bundle);
        startActivity(it);
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

    public void onMapsPlan(View v) {
        setPlanLatLng();
    }

    private void setPlanLatLng(){
        String address = etAddr.getText().toString();
        notiFlag = 0;
        if(address != null && !"".equals(address)) {
            if(destiMarker != null){
                mMap.clear();
            }
            destiLatLng = geoCorder(address);
            if(destiLatLng != null){
                currentMarker = mMap.addMarker(new MarkerOptions()
                        .position(currentLatLng)
                        .title("Current")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi)));
                destiMarker = mMap.addMarker(new MarkerOptions()
                        .position(destiLatLng)
                        .title("Destination"));
                startPlan();
            }else {
                Toast.makeText(this, "搜尋不到經緯度", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, "請輸入地址", Toast.LENGTH_SHORT).show();
        }
    }

    private void startPlan(){
        String dirUrl = getDirectionsUrl(currentLatLng, destiLatLng);
        Log.d("abc", "dirUrl : " + dirUrl);
        DownloadTask downloadTask = new DownloadTask(dirUrl);
        downloadTask.execute();
        double distance = calDistance(currentLatLng, destiLatLng);
        int zoomNum = calZoomNum(distance);
        moveMap(currentLatLng, zoomNum);
    }

    private double calDistance(LatLng current, LatLng point){
        final double EARTH_RADIUS = 6378137.0;

        double ALat = current.latitude * Math.PI / 180.0;
        double BLat = point.latitude * Math.PI / 180.0;
        double a = ALat - BLat;
        double b = (current.longitude - point.longitude) * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(ALat) * Math.cos(BLat) * Math.pow(Math.sin(b / 2),2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;

        return s;
    }

    private void checkDis(){
        double smallestDis = 0.0;
        int flag = 0;
        for(LatLng point : points){
            double distance = calDistance(currentLatLng, point);
            Log.d("abc", "distance : " + distance);
            if(flag == 0){
                smallestDis = distance;
                flag++;
                continue;
            }
            if(distance < smallestDis) {
                smallestDis = distance;
            }
        }
        Log.d("abc", "smallest : " + smallestDis);
        if(smallestDis >= checkDisSmallestDis){
            if(notiFlag == 0){
                notification();
            }
        }else {
            notiFlag = 0;
        }
    }

    private void notification(){
        notiFlag = 1;

        Intent it = new Intent(this, MapsActivity.class);
        it.putExtra("PlateNum", plateNum);
        it.putExtra("userName", userName);
        it.putExtra("destination", etAddr.getText().toString());
        it.putExtra("Lat", currentLatLng.latitude);
        it.putExtra("Long", currentLatLng.longitude);

        PendingIntent pIntent = PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.noti)
                .setContentTitle("TaxiBar")
                .setContentText("超出原先預定路徑")
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build();
        ntfMgr.notify(NTF_ID, notification);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        };

        Snackbar.make(linearLayout, "路徑偏移",
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    // 預估車資
    private int calMoney(){
        // 單位 ： 公尺
        int money = 70;
        int startDis = 1250;

        int calDis = distance - startDis;
        if(calDis>0){
            int temp = calDis / 200;
            money += temp * 5;
        }

        return money;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            tvMoney.setText("車資 : "+String.valueOf(money));
        }
    };

    private int calZoomNum(double distance){
        int zoomNum = 0;
        if(distance<5000){
            zoomNum = 15;
        }else if(distance<10000){
            zoomNum = 13;
        }else if(distance >10000 || distance <25000){
            zoomNum = 11;
        }else {
            zoomNum = 9;
        }
        return zoomNum;
    }
}
