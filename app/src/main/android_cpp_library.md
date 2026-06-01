DEPLOYMENTS
C++ library
Run C++ library on Android
Documentation Index
Fetch the complete documentation index at: https://docs.edgeimpulse.com/llms.txt

Use this file to discover all available pages before exploring further.

Impulses exported as a Android Library variant of the C++ library can be integrated into Android applications to run locally on-device as an Android distributable binary (APK).
Android library (C++)
Deploy as Android Library (C++)

This option now comes with a preconfigured CMake build system tailored for Android projects. The provided CMakeLists.txt automatically links the Edge Impulse SDK, your model, and TensorFlow Lite runtime libraries—making it easier to build and run inference without manual configuration.
This deployment path is built on top of the Android NDK, which enables native C++ code execution inside Android applications.
Our sample repository works with Object detection, Image classification, Audio classification, and Sensor data projects. It also includes a WearOS example for motion data.
Here is a sample of the GMM Cracks demo project running on an Android device, using the camera to detect cracks in concrete.

Android Studio - FOMO-AD - live debugging

​
Try the example above on your Android device:
To try out an example, we have created an application that you can download and run on your Android device. The APK contains our GMM Cracks demo project to detect cracks in concrete.
Android GMM Cracks APK
Download the GMM Test APK

Or continue reading to build your own project as an Android application. This document will guide you through the high level process of building an Android application. See the example-android-inferencing README for more details, and any latest updates.
​
Prerequisites
Make sure you followed the Visual anomaly detection (FOMO-AD) tutorial, and have a trained impulse, or clone Visual GMM cracks.
Also install the following software:
Android Studio
Android NDK
example-android-inferencing repository
​
Clone the base repository
We created an example repository which contains a sample application for Android, and wearOS which you can use to build on, and experiment using your own impulse. Download the application as a .zip, or import this repository using Git:
git clone https://github.com/edgeimpulse/example-android-inferencing
WearOS - example_motion_WearOS
Android - example_camera_inference
Static Buffer - example_static_buffer Open Android Studio.
​
Deploy your C++ project
Make sure you have exported your impulse as a C++ library. If you haven’t done this yet, follow the steps in the C++ Library documentation. Ensure that TensorFlow lite is selected, before the C++ library is generated.

Deploy C++ Library

​
Import your C++ project
Depending on the example you want to use, import the project into Android Studio:
​
Static buffer
The Static Buffer example is a simple application that uses a static buffer to run the impulse on the device. The application will show the result of the inference on the screen.
To get inference to work, we need to add raw data from one of our samples to native-lib.cpp. Head back to the studio and click on Live classification. Then load a validation sample, and click on a row under ‘Detailed result’. Make a note of the classification results, as we want our local application to produce the same numbers from inference.

Selecting the row with timestamp '320' under 'Detailed result'.

Here we replace the raw_features array in native-lib.cpp with the raw data from the sample.

Static Buffer Inference

The application will show the result of the inference on the screen.
​
Android
The Android example is a simple application that uses the camera to collect data, and run the impulse on the device. The application will show the result of the inference on the screen.

Android GMM Cracks

​
WearOS
The WearOS example is a simple application that uses the accelerometer sensor to collect data, and run the impulse on the device. The application will show the result of the inference on the screen.

WearOS Motion Inference

​
Add Edge Impulse C++ files
Unzip your Edge Impulse C++ Library export. Copy these folders into your project’s app/src/main/cpp/ directory:
edge-impulse-sdk/
model-parameters/
tflite-model/
​
Download the TFLite libraries
Run the Windows / Linux / OSX script to fetch resources
cd example-android-inferencing/example_static_buffer/app/src/main/cpp/tflite
sh download_tflite_libs.bat # download_tflite_libs.sh for OSX and Linux
Now you can build the application.
​
Building the application
To build the application, open the project in Android Studio, and click on the ‘Run’ button. This will build the application and deploy it to your Android device.
​
Adding additional sensors
If you want to integrate additional sensors, such as a Gyroscope or Heart Rate Sensor, follow these steps:
Enable the Sensor in the Code In MainActivity.kt, locate the sensor initialization section and uncomment the corresponding lines:
(kotlin)
// Uncomment to add Gyroscope support
private var gyroscope: Sensor? = null

// Uncomment to add Heart Rate sensor support
private var heartRateSensor: Sensor? = null
Initialize the Sensor in onCreate Inside onCreate(), uncomment and initialize the sensor:
(kotlin)
 gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
// heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
Register the Sensor in onResume To start collecting sensor data when the app is active, uncomment the registration logic:
(kotlin)
 gyroscope?.also {
     sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
 }

// heartRateSensor?.also {
//     sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
// }
Handle Sensor Data in onSensorChanged Modify the onSensorChanged() function to collect new sensor data:
(kotlin)

 Gyroscope data
 Sensor.TYPE_GYROSCOPE -> {
     ringBuffer[ringBufferIndex++] = event.values[0] // X rotation
     ringBuffer[ringBufferIndex++] = event.values[1] // Y rotation
     ringBuffer[ringBufferIndex++] = event.values[2] // Z rotation
 }

// Heart Rate data
// Sensor.TYPE_HEART_RATE -> {
//     ringBuffer[ringBufferIndex++] = event.values[0] // Heart rate BPM
// }
Unregister the Sensor in onPause To save battery and improve performance, ensure sensors stop when the app is paused:
(kotlin)

sensorManager.unregisterListener(this)
​
Update the CMakeLists.txt for additional libraries or options (optional step for entire projects e.g. HRV)
This is the where you can add additional libraries or options to the CMakeLists.txt file. For example, to enable the full TFLite library, you can add the following line to the CMakeLists.txt file:
add_definitions(-DEI_CLASSIFIER_ENABLE_DETECTION_POSTPROCESS_OP=1
    -DEI_DSP_ENABLE_RUNTIME_HR == 1
    -DEI_CLASSIFIER_USE_FULL_TFLITE=1
    -DNDEBUG
)
​
Hardware Acceleration (Coming soon)
To further optimize inference on Android, future updates will include:
GPU acceleration with LiteRT delegate: Improves performance for TFLite models.
Refer to Google’s LiteRT documentation for details, or contact sales for more information.
​
Conclusion
You now have a working Android application that runs your impulse on the device. You can use this as a starting point to build your own application, or integrate it into an existing application.
Android opens up the ability to distribute Android APKs for a wide range of platforms, including WearOS, Automotive, Television, Unity, and eXtended Reality (XR). This makes it straightforward to deploy your impulse on a wide range of devices.
We hope this tutorial has been helpful. If you have any questions, or need further assistance, please reach out to us on the Edge Impulse forum.