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

<a href="https://db.tt/F5tck1qI">
  <img alt="Get it from my personal Dropbox" src="https://dt8kf6553cww8.cloudfront.net/static/images/icons/blue_dropbox_glyph-vflJ8-C5d.png" />
</a>




Building on GNU/Linux
---------------------

    git submodule update --init --recursive
    ndk-build -C libraries/Jumble/src/main/jni/
    ./gradlew assembleDebug

It's that simple!


Contributing
============

Coding
------

Standard FOSS project procedure applies; fork and submit a PR!

Please use Transifex for translations, not pull requests.
