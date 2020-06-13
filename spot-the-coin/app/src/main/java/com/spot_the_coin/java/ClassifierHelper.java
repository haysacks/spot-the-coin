/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spot_the_coin.java;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.SystemClock;
import android.util.Size;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.List;

import com.spot_the_coin.java.env.Logger;
import com.spot_the_coin.java.productsearch.SearchEngine;
import com.spot_the_coin.java.tflite.Classifier;
import com.spot_the_coin.java.tflite.Classifier.*;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ClassifierHelper extends AppCompatActivity {
  private static final Logger LOGGER = new Logger();
  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
  private static final float TEXT_SIZE_DIP = 10;
  private Bitmap rgbFrameBitmap = null;
  private long lastProcessingTimeMs;
  private Integer sensorOrientation;
  private Classifier classifier;
  /** Input image size of the model along x axis. */
  private int imageSizeX = 299;
  /** Input image size of the model along y axis. */
  private int imageSizeY = 299;

  private Context context;
  private Model model;
  private Device device;
  private int numThreads;

  public ClassifierHelper(Context context, Model model, Device device, int numThreads) {
    this.context = context;
    this.model = model;
    this.device = device;
    this.numThreads = numThreads;
  }

  public void execute(Bitmap bitmap, SearchEngine searchEngine) throws IllegalStateException {
      try {
          createClassifier();
          processImage(bitmap, searchEngine);
      } catch(IllegalStateException e) {
          e.printStackTrace();
      }
  }

  private void createClassifier() {
      if(classifier != null)
          return;
      try {
          LOGGER.d("Creating classifier");
          classifier = Classifier.create(context, model, device, numThreads);
      } catch (IOException e) {
          LOGGER.e("Failed to create classifier: $e");
      }
  }

  private void processImage(Bitmap bitmap, SearchEngine searchEngine) throws IllegalStateException {
    if (classifier != null) {
      final long startTime = SystemClock.uptimeMillis();
      LOGGER.d("Processing image.");
      final List<Recognition> results = classifier.recognizeImage(bitmap, 0);
      lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
      LOGGER.d("Detect: %s", results);
      searchEngine.updateProductList(results).run();
    }
    else {
        throw new IllegalStateException("Classifier not ready");
    }
  }
}
