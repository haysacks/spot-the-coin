# Spot The Coin (World Coin Classification)

This repository makes use of the image dataset provided in: https://www.kaggle.com/wanderdust/coin-images and acts as completion for the Final Assignment of Bangkit Academy from Group JKT2-A.

We use the images provided in the dataset to train a CNN to classify coin images based on their currency and values.

## Dependencies
This project makes use of the following libraries:
* NumPy
* Matplotlib
* pandas
* json
* zipfile
* Tensorflow
* Keras
* Jupyter Notebook
* Google Colab

## About the project
This project aims to improve the accuracy of a MobileNetV2 model through knowledge distillation, where the class probabilites from a pre-trained larger model (teacher model) are used to train a smaller model (student model) [1]. The teacher model used here is an ensemble taking the average prediction of 3 models, namely:
* Xception model
* Xception model with bilinear pooling
* InceptionV3 model

The bilinear Xception model is an implementation of a bilinear model, which consists of 2 feature extractors whose outputs are multiplied using outer product at each location of the image and pooled to obtain an image descriptor [2]. The features here are extracted from a single Xception model, unlike the usual implementation extracting features from 2 CNN models. The bilinear CNN, with its orderless pooling, is said to perform well for fine-grained visual recognition where intra-category differences are small; this description fits our dataset quite well. However, our bilinear Xception model did not outperform the normal Xception model, but we still included it in the ensemble, as there are some results from the testing dataset where the former predicted correctly when the latter failed to do so, and vice versa.

Using the ensemble model, we manage to obtain a higher accuracy than each of the above models, as illustrated in the following table:
| Model             | Training Accuracy | Validation Accuracy | Testing Accuracy |
| :---------------: | :---------------: | :-----------------: | :--------------: |
| Xception          | 0.982             | 0.843               | 0.859            |
| Bilinear Xception | 0.981             | 0.831               | 0.859            |
| InceptionV3       | 0.970             | 0.834               | 0.857            |
| Ensemble          | 0.985             | 0.853               | 0.880            |

Knowledge distillation involves "softening" the logits (class probabilities) of the teacher model by dividing them with a temperature ![formula](https://render.githubusercontent.com/render/math?math=T), usually ranging from 1 to 20. A higher temperature will magnify the incorrect class probability values, which serves as additional information for the student model to identify which incorrect classes are similar or different from the correct class predicted by the teacher model. This information is also dubbed as "dark knowledge". For our student model training, we experiment with ![formula](https://render.githubusercontent.com/render/math?math=T%20\in%20(3,%205,%2010)) and find ![formula](https://render.githubusercontent.com/render/math?math=T=5) to yield the best results, which are illustrated below:
| Model                              | Training Accuracy | Validation Accuracy | Testing Accuracy |
| :--------------------------------: | :---------------: | :-----------------: | :--------------: |
| Teacher - Ensemble                 | 0.985             | 0.853               | 0.880            |
| Student - MobileNetV2 (without KD) | 0.971             | 0.805               | 0.836            |
| Student - MobileNetV2 (with KD)    | 0.975             | 0.822               | 0.857            |

The improved MobileNetV2 model from knowledge distillation has the same testing accuracy as the InceptionV3 model used in our ensemble model. The upsides of the smaller MobileNetV2 model are its smaller size as well as faster prediction times, which are especially desirable for mobile and web implementations.  We compare the model sizes and average prediction times of their TFLite models when used in our Android application below:
| Model                 | Model (.h5) Size | Average Prediction Time |
| :-------------------: | :--------------: | :---------------------: |
| Xception              | 252 MB           | 3840 ms                 |
| Teacher (ensemble)    | 385 MB           | 9022 ms                 |
| Student (MobileNetV2) | 11.9 MB          | 345 ms                  |

## Usage
### Training the models
#### Ensemble learning
First, train the 3 models which form the ensemble by running `xception_model.ipynb`, `bilinear_xception_model.ipynb` and `inception_model.ipynb` in the folder `models`.

The ensemble model that is based on the average prediction of these 3 models can be built by running `ensemble_model.ipynb` in the folder `models`. The variable `model_path` is to be defined as the path of the folder which contains the 3 models above.

#### Knowledge distillation
The performance of the base MobileNetV2 model without knowledge distillation can be asssessed by running `mobilenet_model.ipynb` in the folder `knowledge_distillation`, which will train a MobileNetV2 model from scratch.

To perform knowledge distillation to train a student MobileNetV2 model, run `knowledge_distillation.ipynb` in the folder `knowledge_distillation`. The variable `teacher_model_path` is to be defined as the path of the pre-trained ensemble model from before.

### Running the Android application
Our Android application is a modification of the repos [ML Kit Showcase App with Material Design](https://github.com/firebase/mlkit-material-android) and [Bangkit Image Classifier Example](https://github.com/esafirm/bangkit-image-classifier-example). These are the steps to build and run the app:
* Clone this repo locally
  ```
  git clone https://github.com/haysacks/spot-the-coin
  ```
* [Create a Firebase project in the Firebase console, if you don't already have one](https://firebase.google.com/docs/android/setup)
* Add a new Android app into your Firebase project with package name com.spot_the_coin
* Download the config file (google-services.json) from the new added app and move it into the module folder (i.e. [app/](./spot-the-coin/app/))
* Build and run it on an Android device

The APK build of the app can also be found in [Releases](https://github.com/haysacks/spot-the-coin/releases).

### Running the website
TBA

## Model and TFLite files
The files for each of the Keras models (in .h5 and .tflite) can be found in this Drive folder:
https://drive.google.com/drive/folders/1ObSUM7Yn11DGDR3IHNIZiFvY04LSnXns?usp=sharing

## References
[1] Geoffrey Hinton, Oriol Vinyals and Jeff Dean. Distilling the Knowledge in a Neural Network. [arxiv:1503.02531](https://arxiv.org/abs/1503.02531)

[2] Tsung-Yu Lin, Aruni RoyChowdhury, and Subhransu Maji. Bilinear CNNs for Fine-grained Visual Recognition. [arxiv:1504.07889](https://arxiv.org/abs/1504.07889)