/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spot_the_coin.java.productsearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Tasks;
import com.spot_the_coin.java.ClassifierHelper;
import com.spot_the_coin.java.env.Logger;
import com.spot_the_coin.java.objectdetection.DetectedObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.spot_the_coin.java.tflite.Classifier.*;

import org.json.JSONException;
import org.json.JSONObject;

/** A fake search engine to help simulate the complete work flow. */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SearchEngine {

  private static final String TAG = "SearchEngine";
  private static final Logger LOGGER = new Logger();

  private static ClassifierHelper classifierHelper;

  private List<Product> productList = new ArrayList<>();

  private Context context;

  public interface SearchResultListener {
    void onSearchCompleted(DetectedObject object, List<Product> productList);
  }

  private final RequestQueue searchRequestQueue;
  private final ExecutorService requestCreationExecutor;

  public SearchEngine(Context context) {
    searchRequestQueue = Volley.newRequestQueue(context);
    requestCreationExecutor = Executors.newSingleThreadExecutor();
    classifierHelper = new ClassifierHelper(context, Model.QUANTIZED_COIN, Device.CPU, 1);
    this.context = context;
  }

  public void search(DetectedObject object, SearchResultListener listener) {
    // Crops the object image out of the full image is expensive, so do it off the UI thread.
    LOGGER.d("Searching.");
    Tasks.call(requestCreationExecutor, () -> createRequest(object))
        .addOnSuccessListener(results -> {
            LOGGER.d("Results retrieved.");
            listener.onSearchCompleted(object, productList);
        })
        .addOnFailureListener(
            e -> {
              Log.e(TAG, "Failed to create product search request!", e);
        });
  }

  private List<Recognition> createRequest(DetectedObject searchingObject) throws Exception {
    byte[] objectImageData = searchingObject.getImageData();
    if (objectImageData == null) {
      throw new Exception("Failed to get object image data!");
    }

    // Hooks up with your own product search backend here
    try {
        Bitmap capturedBitmap = searchingObject.getBitmap();
        classifierHelper.execute(capturedBitmap, this);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
//    throw new Exception("Hooks up with your own product search backend.");
  }

  public void shutdown() {
    searchRequestQueue.cancelAll(TAG);
    requestCreationExecutor.shutdown();
  }

  public Runnable updateProductList(List<Recognition> results) {
      return new Runnable() {
          @Override
          public void run() {
              productList.clear();
              for (Recognition recognition : results) {
                  if (recognition.getTitle() != null && recognition.getConfidence() != null) {
                      try {
                          JSONObject details = recognition.getDetails(context);
                          String name = details.getString("name");
                          String currency = details.getString("currency");
                          String confidence = String.format("%.2f", (100 * recognition.getConfidence()));
                          productList.add(new Product(/* imageUrl= */ "", name,
                                  currency + "\nConfidence: " + confidence + "%"));
                      } catch (JSONException e) {
                          e.printStackTrace();
                      }
                  }
              }
              LOGGER.d("Update product list.");
          }
      };
  }
}
