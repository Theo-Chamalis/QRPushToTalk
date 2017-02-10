QR-PTT PushToTalk
=======

QR-PTT PushToTalk is an easy to use ptt over IP (VoIP) Android client for voice and text communication that uses the [Jumble] (https://github.com/Morlunk/Jumble) protocol implementation and has a GPLv3 license. It can be used by companies that need to be in constant communication with their employees (Lone Workers), to give instructions, make comments, report incidents in real time by voice communication or by sending a text message within the app. It's low cost compared to other competitors like Zello PTT makes it ideal for security companies, cleaning companies, police officers, fire department employees and any other company that needs continuous communication with their employees or customers!

<br/>To try it for free enter "demo" as User ID and leave User PIN field empty.*

Its main features are:
- Very Easy to use
- Lowest cost than the rest PTT applications
- Fast and low latency real time voice communication
- Superior voice compression using CODECS like SILK which is used from Skype
- Works over 2G, 3G, 4G mobile data or Wifi
- Auto-reconnect feature when Internet connection is lost
- Dual-pane channel and chat with swipe left and right gestures
- Channel tree view
- View text comments/description for each user or channel
- Local mute users
- Chat notifications
- Automatic in app secure certificate generation for the server connection
- Opus CODEC support
- Private messaging
- 256 bit AES voice encryption
- Compatible with the latest version of Android 7.1.1 Nugat
- Capacity of 5000 simultaneous users on server and 1000 on each channel
- Automatic channel assignment
- QR-PTT stays active in background so you can use any other app of the phone and listen to your Workers in real time

Screenshots
---------------------
![Alt text](https://lh3.googleusercontent.com/nwebABvFzlRV1xIDKBEeVnnSbZD_dFUfcLice1J3HSJBJ1MXyYTS3GEF_c-_XIiTXFU=h400-rw "Optional title1") ![Alt text](https://lh3.googleusercontent.com/BzedgAWLHVwCtFyB8QI94XLFDq9Ip4RlGJGcBhbZ64U9tH1uWbV0pS14QR1bNvuD3Baf=h400-rw "Optional title2") 
![Alt text](https://lh3.googleusercontent.com/VnlCirO71n56qk9oqKdIHU9145zY00vCVKeGnhuA9x48NeG_LRqwly6eKEWvhFR4g3F0=h400-rw "Optional title3") ![Alt text](https://lh3.googleusercontent.com/Yf2u-e4LhKOoFL0msajz6BLq31Twdi0vODvkl0rZ4NZ7Pj--o95H_42_jfeuaYAI7NQ=h400-rw "Optional title4")
![Alt text](https://lh3.googleusercontent.com/HMOqNl-MK1hNUbYjCtBR-h2nCtyckvcSTa10h6bY3vjZMmvhHQKHncl0Jtv4AMs8meQ=h400-rw "Optional title5")





Downloads
---------------------
<br />
<strong>QR-PTT PushToTalk on Google Play</strong>

<a href="https://play.google.com/store/apps/details?id=com.terracom.qrpttbeta.free&hl=en">
  <img alt="Get it on Google Play" src="https://developer.android.com/images/brand/en_generic_rgb_wo_45.png" />
</a>

<br />


<strong>QR-PTT PushToTalk on Dropbox </strong>

<a href="https://dl.dropboxusercontent.com/u/25024443/QR-PTT PushToTalk.apk">
  <img alt="Get it from my personal Dropbox" src="https://cf.dropboxstatic.com/static/images/index/logo-vflme-Gvg.png" />
</a>

<br/>

*Keep in mind that any communication through the demo channel is public. 


<br/>
Partnership & Services
---------------------
If you are interested in using this application or a customized version of it based on your company's needs, feel free to contact me at <b>theofxam@gmail.com</b> for more information.
<br/>
<br/>
<br/>


Building on GNU/Linux
---------------------

Make sure you have the following installed on your linux machine: Android Studio, Oracle java,
ant, awk, make, git, the Android SDK and the Android NDK. Then fork or download the original Jumble libraries and execute the commands below:

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

Standard FOSS project procedure applies; fork and submit a PR or just email me at theofxam@gmail.com !

Please use Transifex for translations, not pull requests.
