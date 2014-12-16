QRPushToTalk
=======

QRPushToTalk is a robust GPLv3 Mumble client for Android that uses the [Jumble](https://github.com/terracom/Jumble) protocol implementation.
It is intended to be used by lone workers of companies (i.e. cleaning companies, companies in industrialized areas) and mainly for security
companies that need to monitor and communicate with their patrolling guards. It will be used alongside with the above app:
Kerveros QR-Patrol guard tour


<a href="https://play.google.com/store/apps/details?id=com.terracom.gr.kerverosqrpatrol">
  <img alt="Get it on Google Play" src="https://developer.android.com/images/brand/en_generic_rgb_wo_45.png" />
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
