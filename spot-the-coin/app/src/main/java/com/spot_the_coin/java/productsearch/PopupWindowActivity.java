package com.spot_the_coin.java.productsearch;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.spot_the_coin.R;
import com.spot_the_coin.java.AmountHelper;

import org.json.JSONException;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;

public class PopupWindowActivity extends Activity {

    private int imageSize;
    private int coinValueSpinnerCurrentSelection;
    private int totalValueSpinnerCurrentSelection;
//    private RequestQueue requestQueue;
//    private boolean isRequestSuccess = false;

    private static final int COIN_VALUE = 0;
    private static final int TOTAL_VALUE = 1;
    private static final int ADD_TOTAL_VALUE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_window);
        this.setFinishOnTouchOutside(true);

        imageSize = getApplicationContext().getResources().getDimensionPixelOffset(R.dimen.preview_card_image_size);

//        // Instantiate the cache
//        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
//        // Set up the network to use HttpURLConnection as the HTTP client.
//        Network network = new BasicNetwork(new HurlStack());
//        // Instantiate the RequestQueue with the cache and network.
//        requestQueue = new RequestQueue(cache, network);
//        // Start the queue
//        requestQueue.start();

        Bundle extras = getIntent().getExtras();
        String title = null;
        String subtitle = null;
        String imageUrl = null;
        String currencyId = null;
        double value = 0;

        if (extras != null) {
            title = extras.getString("title");
            subtitle = extras.getString("subtitle");
            imageUrl = extras.getString("imageUrl");
            currencyId = extras.getString("currencyId");
            value = extras.getDouble("value");
        }

        TextView coinTitle = findViewById(R.id.coin_title);
        coinTitle.setText(title);

        TextView coinSubtitle = findViewById(R.id.coin_subtitle);
        coinSubtitle.setText(subtitle);

        ImageView imageView = findViewById(R.id.coin_image);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageDrawable(null);
        if (!TextUtils.isEmpty(imageUrl)) {
            new ImageDownloadTask(imageView, imageSize).execute(imageUrl);
        } else {
            imageView.setImageResource(R.drawable.logo_google_cloud);
        }

        AmountHelper.setCoinValue(value);
        setCoinValueText(value);

        Spinner coinValueSpinner = findViewById(R.id.coin_value_dropdown);
        ArrayAdapter<CharSequence> coinValueAdapter = ArrayAdapter.createFromResource(this,
                R.array.currency_id_array, R.layout.dropdown_menu_popup_item);
        coinValueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        coinValueSpinner.setAdapter(coinValueAdapter);

        List<String> spinnerOptions = Arrays.asList((getResources().getStringArray(R.array.currency_id_array)));
        int currencyIdIndex = spinnerOptions.indexOf(currencyId);
        coinValueSpinnerCurrentSelection = currencyIdIndex;
        coinValueSpinner.setSelection(currencyIdIndex);
        AmountHelper.setCurrencyId(currencyId);

        coinValueSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i != coinValueSpinnerCurrentSelection) {
                    String newCurrencyId = spinnerOptions.get(i);

                    if(newCurrencyId.equals(AmountHelper.getCurrencyId())) {
                        setCoinValueText(AmountHelper.getCoinValue());
                    } else {
                        try {
                            double exchangeRate = AmountHelper.getExchangeRate(AmountHelper.getCurrencyId(), newCurrencyId);
                            updateCoinExchangeRate(exchangeRate);
                            coinValueSpinnerCurrentSelection = i;
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    "Failed to fetch latest exchange rates",
                                    Toast.LENGTH_LONG).show();
                            coinValueSpinner.setSelection(coinValueSpinnerCurrentSelection);
                        }
//                        convertExchangeRate(AmountHelper.getCurrencyId(), newCurrencyId, COIN_VALUE);
//                        if(isRequestSuccess)
//                            coinValueSpinnerCurrentSelection = i;
//                        else
//                            coinValueSpinner.setSelection(coinValueSpinnerCurrentSelection);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Spinner totalValueSpinner = findViewById(R.id.total_value_dropdown);
        ArrayAdapter<CharSequence> totalValueAdapter = ArrayAdapter.createFromResource(this,
                R.array.currency_id_array, R.layout.dropdown_menu_popup_item);
        totalValueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        totalValueSpinner.setAdapter(totalValueAdapter);

        if(AmountHelper.getTotalBaseValue() > 0) {
            String totalBaseCurrencyId = AmountHelper.getTotalBaseCurrencyId();
            String totalCurrencyId = AmountHelper.getTotalCurrencyId();
            if(totalBaseCurrencyId.equals(totalCurrencyId))
                setTotalValueText(AmountHelper.getTotalBaseValue());
            else {
                try {
                    double exchangeRate = AmountHelper.getExchangeRate(AmountHelper.getTotalBaseCurrencyId(),
                            AmountHelper.getTotalCurrencyId());
                    updateTotalExchangeRate(exchangeRate);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "Failed to fetch latest exchange rates",
                            Toast.LENGTH_LONG).show();
                }
//                convertExchangeRate(AmountHelper.getTotalBaseCurrencyId(), AmountHelper.getTotalCurrencyId(), TOTAL_VALUE);
            }
        }
        else {
            setTotalValueText(AmountHelper.getTotalBaseValue());
        }

        int totalIdIndex = spinnerOptions.indexOf(AmountHelper.getTotalCurrencyId());
        totalValueSpinnerCurrentSelection = totalIdIndex;
        totalValueSpinner.setSelection(totalIdIndex);

        totalValueSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i != totalValueSpinnerCurrentSelection) {
                    String newCurrencyId = spinnerOptions.get(i);

                    if(newCurrencyId.equals(AmountHelper.getTotalBaseCurrencyId())) {
                        setTotalValueText(AmountHelper.getTotalBaseValue());
                        AmountHelper.setTotalCurrencyId(newCurrencyId);
                    } else {
                        try {
                            double exchangeRate = AmountHelper.getExchangeRate(AmountHelper.getTotalBaseCurrencyId(), newCurrencyId);
                            updateTotalExchangeRate(exchangeRate);
                            totalValueSpinnerCurrentSelection = i;
                            AmountHelper.setTotalCurrencyId(newCurrencyId);
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    "Failed to fetch latest exchange rates",
                                    Toast.LENGTH_LONG).show();
                            totalValueSpinner.setSelection(totalValueSpinnerCurrentSelection);
                        }
//                        convertExchangeRate(AmountHelper.getTotalBaseCurrencyId(), newCurrencyId, TOTAL_VALUE);
//
//                        if(isRequestSuccess) {
//                            totalValueSpinnerCurrentSelection = i;
//                            AmountHelper.setTotalCurrencyId(newCurrencyId);
//                        } else {
//                            totalValueSpinner.setSelection(totalValueSpinnerCurrentSelection);
//                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void onClickAddButton(View view) {
        String currencyId = AmountHelper.getCurrencyId();
        String totalBaseCurrencyId = AmountHelper.getTotalBaseCurrencyId();
        String totalCurrencyId = AmountHelper.getTotalCurrencyId();
        if(currencyId.equals(totalBaseCurrencyId) && totalBaseCurrencyId.equals(totalCurrencyId)) {
            AmountHelper.addTotal(AmountHelper.getCoinValue());
            setTotalValueText(AmountHelper.getTotalBaseValue());
        } else {
            try {
                double exchangeRate = AmountHelper.getExchangeRate(currencyId, AmountHelper.getTotalBaseCurrencyId());
                addTotalExchangeRate(exchangeRate);
                exchangeRate = AmountHelper.getExchangeRate(totalBaseCurrencyId, totalCurrencyId);
                updateTotalExchangeRate(exchangeRate);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        "Failed to fetch latest exchange rates",
                        Toast.LENGTH_LONG).show();
            }
//            convertExchangeRate(currencyId, totalBaseCurrencyId, ADD_TOTAL_VALUE);
//            convertExchangeRate(totalBaseCurrencyId, totalCurrencyId, TOTAL_VALUE);
        }
    }

    public void onClickResetButton(View view) {
        AmountHelper.resetTotal();
        setTotalValueText(AmountHelper.getTotalBaseValue());
    }

    public void onClickRefreshButton(View view) {
        AmountHelper.startLoadExchangeRateJSON(this);
    }

    private void setCoinValueText(double value) {
        TextView coinValueText = findViewById(R.id.coin_value);
        coinValueText.setText(String.format("%,.2f", value));
    }

    private void setTotalValueText(double value) {
        TextView totalValueText = findViewById(R.id.total_value);
        totalValueText.setText(String.format("%,.2f", value));
    }

//    private void convertExchangeRate(String oldCurrencyId, String newCurrencyId, int valueToUpdate) {
//        String query = oldCurrencyId + "_" + newCurrencyId;
//        String url = "https://free.currconv.com/api/v7/convert?q=" + query + "&compact=y&apiKey=API_KEY";
//
//        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            JSONObject jsonObject = response.getJSONObject(query);
//                            exchangeRate = jsonObject.getDouble("val");
//                            isRequestSuccess = true;
//                            switch(valueToUpdate) {
//                                case COIN_VALUE: updateCoinExchangeRate(exchangeRate); break;
//                                case TOTAL_VALUE: updateTotalExchangeRate(exchangeRate); break;
//                                case ADD_TOTAL_VALUE: addTotalExchangeRate(exchangeRate); break;
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(getApplicationContext(),
//                                "Failed to fetch latest exchange rates",
//                                Toast.LENGTH_LONG).show();
//                        isRequestSuccess = false;
//                    }
//                });
//        requestQueue.add(req);
//    }

    private void updateCoinExchangeRate(double exchangeRate) {
        double newValue = AmountHelper.getCoinValue() * exchangeRate;
        setCoinValueText(newValue);
    }

    private void updateTotalExchangeRate(double exchangeRate) {
        double newValue = AmountHelper.getTotalBaseValue() * exchangeRate;
        setTotalValueText(newValue);
    }

    private void addTotalExchangeRate(double exchangeRate) {
        double newValue = AmountHelper.getCoinValue() * exchangeRate;
        AmountHelper.addTotal(newValue);
    }
}
