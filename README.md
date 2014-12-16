QRPushToTalk
=======

QRPushToTalk is a robust GPLv3 Mumble client for Android that uses the [Jumble](https://github.com/terracom/Jumble) protocol implementation.

<a href="https://play.google.com/store/apps/details?id=com.terracom.mumbleclient">
  <img alt="Get it on Google Play" src="https://developer.android.com/images/brand/en_generic_rgb_wo_45.png" />
</a>
<a href="https://f-droid.org/repository/browse/?fdid=com.terracom.mumbleclient">
  <img alt="Get it on F-Droid" src="https://f-droid.org/wiki/images/c/c4/F-Droid-button_available-on.png" height="45" />
</a>

Building on GNU/Linux
---------------------

    git submodule update --init --recursive
    ndk-build -C libraries/Jumble/src/main/jni/
    ./gradlew assembleDebug

It's that simple!

Inter-process communication
---------------------------

Documentation on integrating your app with QRPushToTalk's IPC features [here](https://github.com/terracom/QRPushToTalk/wiki/Inter-process-communication).

Contributing
============

Coding
------

Standard FOSS project procedure applies; fork and submit a PR!

Please use Transifex for translations, not pull requests.

Testing
-------

[Help test the latest QRPushToTalk nightly builds here.](https://www.terracom.com/jenkins/) File issue reports with Nightly version number.

Translation
-----------

Contribute translations to QRPushToTalk using [Transifex](https://www.transifex.com/projects/p/qrptt/)!

Donate
------

QRPushToTalk is a lot of work to develop! All donations are appreciated, via the paid version on Google Play or here.

[Paypal](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=ALTS7G56K2CGS)

[Bitcoin](bitcoin:1ySD4UzFDtPLq9agRg9eiFtWmz6DJ7bBf?label=QRPushToTalk%20Donations) (1ySD4UzFDtPLq9agRg9eiFtWmz6DJ7bBf)
