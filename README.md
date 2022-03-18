# G-Core Labs Demo – Stream Live Video Online on Android
# Introduction
Set up live streaming in 15 minutes in your Android project instead of 7 days of work and setting wild parameters of codecs, network, etc. This demo project is a quick tutorial how to stream live video from your own mobile app to an audience of 1 000 000+ viewers like Instagram, Youtube, etc.
# Features
1. Authorization 
2. HLS & MPEG-DASH playback
3. RTMP streaming
5. Creating a new live stream
6. Camera switching
7. Mute mode
8. Video:
    * Network adaptive bitrate mechanism
    * Source: front and back cameras
    * Orientation: portrait
    * Codec: AVC/H.264
    * Configurable bitrate, resolution, iFrameInterval, encoder profile
9. Audio:
    * Codec: AAC
    * Configurable bitrate, sample rate, stereo/mono
    * Processing: noise suppressor, echo cancellation
# Testing the app with your G-Core Labs account
1. You can clone this project and run it in Android Studio, where you can test it either by connecting a real device or through an Android emulator
2. Sign in with your G-Core-Labs account email and password.
3. On the Streams screen, you can watch the available streams
4. On the Select stream screen, you can select/create a stream to start streaming
5. On the Start Broadcast screen, you can start broadcasting, set the video quality, and select push url or backup push url for streaming
## Screenshots
<img src = "https://user-images.githubusercontent.com/100352152/156735600-33638b7b-6c15-4c53-ba66-08a11afff1f1.png" width=240> <img 
src = "https://user-images.githubusercontent.com/100352152/156735626-2ae9de13-8568-46a1-91af-6e5bb29db891.png" width=240> <img 
src = "https://user-images.githubusercontent.com/100352152/156735678-e5849855-5a19-4df9-ad60-c2dd533c8eb3.png" width=240>
<img src = "https://user-images.githubusercontent.com/100352152/156735655-5346523e-da0d-43e6-91b4-87869c9ddd6e.png" width=728>

# Quick Start
### 1. Compile
You will need a third party library to implement RTMP streaming. In this project, [rtmp-rtsp-stream-client-java](https://github.com/pedroSG94/rtmp-rtsp-stream-client-java) was used. To use it, you need to specify this in your build.gradle:
``` gradle
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
dependencies {
  implementation 'com.github.pedroSG94.rtmp-rtsp-stream-client-java:rtplibrary:2.1.7'
}
```
### 2. Permissions
In order to broadcast, you will also need an internet connection, a camera and a microphone. To use them, add the following permissions to your app's AndroidManifest.xml file:
``` xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
```
### 3. Layout
To display the stream from the camera on the smartphone screen, you will need OpenGlView, which is implemented in the library used.
``` xml
<com.pedro.rtplibrary.view.OpenGlView
        android:id="@+id/openGlView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:keepScreenOn="true"
        android:visibility="gone"

        app:keepAspectRatio="true"
        app:aspectRatioMode="adjust"/>
```
### 4. Start streaming
Streaming with default params:
``` kotlin
val rtmpCamera2 = RtmpCamera2(openGlview, connectCheckerRtmp)

if (rtmpCamera2.prepareAudio() && rtmpCamera2.prepareVideo()) {
   rtmpCamera2.startStream("rtmpUrl")
}
rtmpCamera2.stopStream()
```
Streaming with custom params:
``` kotlin
val rtmpCamera2 = RtmpCamera2(openGlview, connectCheckerRtmp)

val audioIsReady = rtmpCamera2.prepareAudio(
   bitrate: Int,
   sampleRate: Int,
   isStereo: Boolean,
   echoCanceler: Boolean,
   noiseSuppressor: Boolean
)

val videoIsReady = rtmpCamera2.prepareVideo(
   width: Int,
   height: Int,
   fps: Int,
   bitrate: Int,
   iFrameInterval: Int,
   rotation: Int
)

if (audioIsReady && videoIsReady) {
   rtmpCamera2.startStream("rtmpUrl")
}
rtmpCamera2.stopStream()
```
You can find out how to get rtmpUrl for streaming in the G-Core-Labs API [documentation](https://apidocs.gcorelabs.com/streaming#tag/Streams).
# Requirements
* The presence of an Internet connection on the device
* The presence of a camera and microphone on the device.
* Anroid min API 23 
# License
    Copyright 2022 G-Core Labs

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
