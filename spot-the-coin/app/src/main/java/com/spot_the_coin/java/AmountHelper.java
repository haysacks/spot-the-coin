package com.spot_the_coin.java;

import android.app.Activity;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class AmountHelper {

    private static RequestQueue requestQueue;

    private static double COIN_VALUE = 0.0;
    private static String CURRENCY_ID = "";

    private static double TOTAL_BASE_VALUE = 0.0;
    private static final String TOTAL_BASE_CURRENCY_ID = "IDR";

    private static String TOTAL_CURRENCY_ID = "IDR";

    private static JSONObject EXCHANGE_RATE_JSON;

    public static void addTotal(double convertedAmount) {
        TOTAL_BASE_VALUE += convertedAmount;
    }

    public static void resetTotal() {
        TOTAL_BASE_VALUE = 0;
    }

    public static double getTotalBaseValue() {
        return TOTAL_BASE_VALUE;
    }

    public static String getTotalBaseCurrencyId() {
        return TOTAL_BASE_CURRENCY_ID;
    }

    public static String getTotalCurrencyId() {
        return TOTAL_CURRENCY_ID;
    }

    public static void setTotalCurrencyId(String totalCurrencyId) {
        TOTAL_CURRENCY_ID = totalCurrencyId;
    }

    public static void setCoinValue(double coinValue) {
        COIN_VALUE = coinValue;
    }

    public static void setCurrencyId(String currencyId) {
        CURRENCY_ID = currencyId;
    }

    public static double getCoinValue() {
        return COIN_VALUE;
    }

    public static String getCurrencyId() {
        return CURRENCY_ID;
    }

    public static void setExchangeRateJson(JSONObject exchangeRateJson) {
        EXCHANGE_RATE_JSON = exchangeRateJson;
    }

    public static double getExchangeRate(String sourceCurrencyId, String targetCurrencyId) throws Exception {
        return EXCHANGE_RATE_JSON.getDouble(targetCurrencyId) / EXCHANGE_RATE_JSON.getDouble(sourceCurrencyId);
    }

    public static void startLoadExchangeRateJSON(Activity activity) {
        // Instantiate the cache
        Cache cache = new DiskBasedCache(activity.getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);
        // Start the queue
        requestQueue.start();

        loadExchangeRateJSON(activity);
    }

    private static void loadExchangeRateJSON(Activity activity) {
        String url = "https://openexchangerates.org/api/latest.json?app_id=7f0b6b5123c046febe8c1b8d5325f9b7";

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject jsonObject = response.getJSONObject("rates");
                            setExchangeRateJson(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(activity.getApplicationContext(),
                                "Failed to fetch latest exchange rates",
                                Toast.LENGTH_LONG).show();
                    }
                });
        requestQueue.add(req);
    }
}
