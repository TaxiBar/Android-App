package com.example.gjen.taxibar;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sammy on 2017/6/2.
 */

public class TaxiReviewList {

    int score = 0;

    public enum ReviewType {
        Good, Bad, Neutral;
    }

    public static TaxiReviewList INSTANCE = new TaxiReviewList();

    public static TaxiReviewList getInstance() {
        return INSTANCE;
    }

    public ReviewType getReviewe(String plateNum) {
        ReviewType rt = ReviewType.Neutral;
        getPlateScore(plateNum);
        switch (score){
            case 1:
                rt = ReviewType.Good;
                break;
            case 0:
                rt = ReviewType.Neutral;
                break;
            case -1:
                rt = ReviewType.Bad;
                break;
        }
        return rt;
    }

    private void getPlateScore(final String plateNumber){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;

                try {
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    String strUrl = "http://140.134.26.71:9990/taxibar/webapi/rank/plateNumber/" + plateNumber;
                    URL url = new URL(strUrl);
                    conn = (HttpURLConnection) url.openConnection();

                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("GET");
                    conn.connect();

                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String jsonString = reader.readLine();
                    reader.close();

                    Log.d("abc", "jsonString : " + jsonString);
                    JSONObject jsonObject = new JSONObject(jsonString);
                    score = jsonObject.getInt("score");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        }).start();
    }


}
