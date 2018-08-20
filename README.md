![Alt text](https://github.com/Theofilos-Chamalis/QR-PTT-PushToTalk/blob/master/EVO%20PTT-feature-graphic.png "EVO PTT - The Evolution in PTT communication")

<h1 align="center">EVO PTT - The Evolution in PTT communication</h1>
<br/>

EVO PTT is the evolution of the highly successful QR-PTT Push To Talk android application that is used by a wide range of security and other companies worldwide. Designed to be a substantial upgrade over the old application, we listened to our customers needs and suggestions to improve EVO PTT in key areas and provide new innovative features. It is ideal for companies that need to be in constant communication with their employees (Lone Workers), to give instructions, make comments, report incidents in real time by voice communication, by sending a text message within the app or for responding quick in emergency situations. Its low cost and flexible pricing compared to other competitors like Zello PTT as well as it's ability to rebrand it and adapt it into existing systems makes it the best option in the market, from small businesses to large organizations!

<br/> To try EVO PTT for free enter any Username of your choice and "demo" as Password.* 
<br/> To try the EVO PTT Admin Web Panel for free, go to http://evoptt.ddns.net and enter the Email: "demo@demo.com"  and "demo" as Password.*\*

<br/>Main features:
- Easy to understand and use
- Fast and low latency real time voice communication
- Lowest data consumption PTT application
- Superior voice compression using CODECS like Skype's SILK and CELT
- Opus codec support
- Clever mobile data management resulting in lowest data consumption (see comparison with the competition)*\*\* 
- Works over any network condition (2G, 3G, 4G or Wifi)
- High security voice encryption
- Native integration with Android Walkie Talkie devices (F22, F22+, F25 and more)
- Ability to work with screen off in Android Walkie Talkie devices
- SOS button function with location reporting
- Auto location reporting of workers to EVO PTT Admin Web Panel*\*
- Start on boot feature
- Remember Login Credentials
- Auto-login
- Auto-reconnect feature when Internet connection is lost
- Stay Awake feature to keep the screen on
- Distinctive sounds for voice, text and SOS
- Hide or adjust PTT button height in lower resolution screens
- Chat notifications
- Private messaging
- Compatible with the latest version of Android 9 (Pie)
- Compatible with the latest version of the Android Studio IDE and Gradle
- EVO PTT stays active in background so you can use other apps while listening other users in real time
- Competitive subscription and one off pay pricing plans
- Ability for source code purchase and app rebranding
<br />


Screenshots
---------------------
![Alt text](https://lh3.googleusercontent.com/Y6MHbEta2KJGCu55hPDnROV8WPLU8xQ6DE7w2ApKvhWkoJiD0H3Jm9iRDuBfHz0RAw=h380-rw "Login Screen") ![Alt text](https://lh3.googleusercontent.com/pbWTFvjpTW6RYKsCPURtUHH7x0CvyejuDd3pPTJE2A15xU-xNoFQDVE5RM4fs2NK2w=h380-rw "Server Screen") 
![Alt text](https://lh3.googleusercontent.com/a-WRsl_Rt7ccWa-yco9W7-X8EbYLDRWMHn5OikSTAn_mdDtbKhoE0efRPbbQ0Uh5ErLU=h380-rw "Chat message") ![Alt text](https://lh3.googleusercontent.com/_Sw7q4nVN1nFnD7O48eQdsMI2sff7ErL2FExfsdiJ0T7sDgYCCqNAgy8j7pAqduhZA=h380-rw "SOS Function")
![Alt text](https://lh3.googleusercontent.com/jnHH8rFJACwj5rm_p0R8LmdLP2RBKaza1W_I2brY63vMlAWqFlPtXWdDpEch6wTAJNc=h380-rw "Side Menu")
![Alt text](https://lh3.googleusercontent.com/G0yoXMKCHgPJJF_U44ye5i2WER4SbFRew9JHjI1hPajtfLqmtNbOwUSUicVyrsmt8dgq=h380-rw "General Settings")
![Alt text](https://lh3.googleusercontent.com/QOroqhL8IWU57Ldd-pdWEF0VjcfcuAVknlFNIfMWdkl6la51JAVV-OL5xDs3KdomL-26=h380-rw "Audio Settings")
![Alt text](https://lh3.googleusercontent.com/VVONnN-ZYBtILuRNIZzjpdjeQeld2Is7i4ku6-2d09uDZo-ucDjvdlfz9nBWZ_o7hQ=h380-rw "Appearance Settings")




<br/><br/>
Downloads
---------------------

<strong>EVO PTT on Google Play</strong>
<br /><br />
<a href="https://play.google.com/store/apps/details?id=com.theofilos.chamalis.evoptt">
  <img alt="Get it on Google Play" src="https://developer.android.com/images/brand/en_generic_rgb_wo_45.png" />
</a>

<br/><br/>
*  Any communication through the demo channel is public.
*\* The free version of EVO PTT Admin Web Panel does not provide geolocation tracking and personal data of test users for privacy reasons

*\*\* For a data consumption comparison of EVO PTT with our competitors click <a target="_blank" href="https://www.dropbox.com/s/39qpqhx88bqj5nl/EVO%20PTT%20Benchmark.pdf?dl=0"> HERE </a>

<br/><br/>
Partnership, Services & Pricing
-----------------------
Our services include plans with customized and private (per customer) servers based on the amount of users as well as the ability to buy the full source (front end and back end) alongside with redistributing and rebranding rights. If you are interested in using EVO PTT or a customized version of it based on your company's needs, feel free to contact us at <b>theofxam@gmail.com</b> for more information.
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
ones. If you encounter any problem throughout the procedure feel free to contact 
.


Contributing	
============

Coding
------

Standard FOSS project procedure applies; fork and submit a PR or just email us at theofxam@gmail.com !

Please use Transifex for translations, not pull requests.
