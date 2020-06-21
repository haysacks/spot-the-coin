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

/** Information about a product. */
public class Product {

  final String imageUrl;
  final String title;
  final String subtitle;
  final double value;
  final String currencyId;

  Product(String imageUrl, String title, String subtitle, double value, String currencyId) {
    this.imageUrl = imageUrl;
    this.title = title;
    this.subtitle = subtitle;
    this.value = value;
    this.currencyId = currencyId;
  }

  public Product(Product product) {
      this.imageUrl = product.getImageUrl();
      this.title = product.getTitle();
      this.subtitle = product.getSubtitle();
      this.value = product.getValue();
      this.currencyId = product.getCurrencyId();
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getTitle() {
    return title;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public double getValue() { return value; }

  public String getCurrencyId() { return currencyId; }
}
