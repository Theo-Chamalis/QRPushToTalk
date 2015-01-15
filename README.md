QRPushToTalk
=======

QRPushToTalk is a robust GPLv3 Mumble client for Android that uses the [Jumble](https://github.com/Morlunk/Jumble) protocol implementation.
It is intended to be used by lone workers of companies (i.e. cleaning companies, companies in industrialized areas) and mainly for security
companies that need to monitor and communicate with their patrolling guards. It will be used alongside with the app below:

<br />
<strong>Kerveros QR-Patrol guard tour</strong>

<a href="https://play.google.com/store/apps/details?id=com.terracom.gr.kerverosqrpatrol">
  <img alt="Get it on Google Play" src="https://developer.android.com/images/brand/en_generic_rgb_wo_45.png" />
</a>  

<br />
And here is QRPushToTalk on my personal dropbox account if you want to test it:
<br />

<strong>QR PushToTalk</strong>

<a href="https://dl.dropboxusercontent.com/u/25024443/QRPushToTalk.apk">
  <img alt="Get it from my personal Dropbox" src="https://dt8kf6553cww8.cloudfront.net/static/images/icons/blue_dropbox_glyph-vflJ8-C5d.png" />
</a>




Building on GNU/Linux
---------------------

Make sure you have the following installed on your linux machine: Android Studio, Oracle java,
ant, awk, make, git, the Android SDK and the Android NDK. Then fork or download the original github project created by Andrew Comminos at https://github.com/Morlunk/Plumble and execute the commands below: 

    git submodule update --init --recursive
    ndk-build -C libraries/Jumble/src/main/jni/
    ./gradlew assembleDebug

This is necessary in order to download the libraries that the initial application uses.
Keep in mind that throughout the proccess you should use chmod command on the whole project file
to give it the right permissions. Finally, download the files of my project and overwrite the old
ones. If you encounter any problem throughout the procedure feel free to contact me.


Contributing	
============

Coding
------

Standard FOSS project procedure applies; fork and submit a PR or just write me a pm!

Please use Transifex for translations, not pull requests.
