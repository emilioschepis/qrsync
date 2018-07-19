# QR Sync

QR Sync is a QR code / barcode scanning application for Android.

I originally published the first version in November of 2016 as QReader (com.esapp.qreader).

In an effort to improve my coding skills I'm open sourcing the code that I started rewriting from scratch on June 16, 2018.

<a href='https://play.google.com/store/apps/details?id=com.emilioschepis.qrsync&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_gb/badges/images/generic/en_badge_web_generic.png' width="240" height="93"/></a>

## About the application
QR Sync makes it easy to save QR codes you scan harnessing the power of cloud databases.

All of your codes are automatically kept in sync across all of your devices, so you can access them wherever you need them.

For each code, the application provides a contextual action based on its type, so that you can interact with the code in a quicker and more efficient way.

The application is completely free to use, it has no ads and no in-app purchases. 

<img src="https://github.com/emilioschepis/QRSync/blob/master/screenshots/Screenshot_1530261416.png?raw=true" width="162" height="288">  <img src="https://github.com/emilioschepis/QRSync/blob/master/screenshots/Screenshot_1530262501.png?raw=true" width="162" height="288">  <img src="https://github.com/emilioschepis/QRSync/blob/master/screenshots/Screenshot_1530262545.png?raw=true" width="162" height="288">  <img src="https://github.com/emilioschepis/QRSync/blob/master/screenshots/Screenshot_1530262563.png?raw=true" width="162" height="288">

## Technical details
The app is based on the MVVM presentation layer architecture, is completely written in Kotlin and follows the 
[recommended app architecture](https://developer.android.com/jetpack/docs/guide#recommended_app_architecture) provided 
by Google.

### Libraries
* [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/): 
used for the communication between layers. It fully supports the activities' lifecycle in order 
to prevent memory leaks and unnecessary network requests.
* [Firebase](http://firebase.google.com): the backend of this application.
  * Auth: used to authenticate users
  * Firestore: NoSQL Document DB used to store all the codes
  * Vision (MLKit): machine learning library used to scan all kinds of codes
* [Koin](https://github.com/InsertKoinIO/koin): easy to use and flexible dependency injection library for Kotlin.
* [Gson](https://github.com/google/gson): serialization/deserialization library for JSON objects.
* [Arrow](https://github.com/arrow-kt/arrow): Typed Functional Programming library for Kotlin.
* [CameraView](https://github.com/natario1/CameraView): optimized and lightweight Camera2API library.
