![Alt text](https://github.com/Theofilos-Chamalis/QR-PTT-PushToTalk/blob/master/EVO%20PTT-feature-graphic.png "EVO PTT - The Evolution in PTT communication")

<h1 align="center">EVO PTT - The Evolution in PTT communication</h1>
<br/>

Designed for professional lone workers, EVO PTT is the Evolution in Push to Talk over IP communication. Based on the highly successful QR-PTT Push To Talk android application, we listened to our customers needs and suggestions and we have redesigned EVO PTT to offer a better user experience, new innovative features and improved performance. It is easy to understand and use and it is ideal for companies that need to be in continuous real time communication with their employees or customers via voice or text messaging! Using seamless geolocation monitoring, you are always aware of the EVO PTT users location through the EVO PTT Android App or the EVO PTT User Management Panel where you can also register new EVO PTT users and check their current status. Our customers include security companies, cleaning companies, taxi drivers, police & fire departments, lorry drivers and more and the low cost and flexible pricing of our service compared to other competitors like Zello PTT as well as it's rebranding options and adaption into existing systems make it the best solution in the market, from small businesses to large organizations!


Demo testing
---------------------
<br/>~ To try EVO PTT Android App for free enter any Username of your choice and "demo" as Password. 
<br/>~ To try the EVO PTT User Management Panel for free, go to https://evoptt.ddns.net and enter:<br/> 
       &nbsp;&nbsp;&nbsp;Email: "demo@demo.com",  Password: "demo".


Main Features
---------------------
- Easy to understand and use
- Fast and low latency real time voice communication
- Lowest data consumption PTT application
- Seamless geolocation monitoring of EVO PTT users location
- Superior voice compression including Skype's SILK and CELT codecs (on x32 & x64 devices)
- Opus codec support (on x32 & x64 devices)
- Clever mobile data management resulting in lowest data consumption (see comparison with the competition) *\*\* 
- Works over any network condition (2G, 3G, 4G or Wifi)
- Network Type Switching mode to switch seamlessly between Data and WiFi without disconnecting
- High security voice encryption
- Native integration with Android Walkie Talkie devices (F22, F22+, F25 and more)
- Ability to work with screen off in Android Walkie Talkie devices
- SOS button function with location reporting
- Auto location reporting of users to EVO PTT User Management Panel *\*
- Easy user management through the EVO PTT User Management Panel
- EVO PTT users log in on a per-company basis for better user isolation and security (using secure docker containers)
- Busy Channel Lockout option to prevent users from talking in a busy channel
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
- EVO PTT stays active in background so you can use other apps*\*\*\* while listening other users in real time
- Competitive subscription and one off pay pricing plans
- Ability for source code purchase and app rebranding
<br />


Screenshots
---------------------
![Alt text](https://lh3.googleusercontent.com/QSwd7eHGxs6FcQ5vR8dkBnkMHtriAKHo5punNTWTFbxDNKzLZuqNrqpAyYNHrI3L1NAM=w720-h380-rw "Login Screen") ![Alt text](https://lh3.googleusercontent.com/jPr6XrHdUqLu-cHzY_Dx4Rx1y7EyP2j-VIMmVrLAYwCVcMu8icfUCZI4-ebSyZIl11U=w720-h380-rw "Server Screen") 
![Alt text](https://lh3.googleusercontent.com/WP132tAw0IAZslRDpLWl0uk-SGBWgOpQMnuFRwJ4vHNdaKctEXzc-Q2yOgKbfb78BA=w720-h380-rw "Mapview Screen") ![Alt text](https://lh3.googleusercontent.com/K5ey4JjmU2UO9Vph4RSq7qnqSWjkw9zt0Cg9S8-wRQjmGXVLg2IRvzN8LgcKLS5oh1lu=w720-h380-rw "Chat Screen")
![Alt text](https://lh3.googleusercontent.com/GEb4GqhKVz7NjIKZygA6qggeA-WA_7B-_ESqHLKJohQA7YZoTD--JAkXrQeQPR0JU5I=w720-h380-rw "Side Menu")
![Alt text](https://lh3.googleusercontent.com/Xp-WIpbpKCMPK6vYzgBwXYDGJ5w9-wMfikgopJXRl26KfXUaeK5d9a2LEgk6z1rqjhH6=w720-h380-rw "General Settings")
![Alt text](https://lh3.googleusercontent.com/UH4IizfoTs5-GmJkqnJaASVTWIDOCAydbu007FH27y2RhjYlRtBQGlzMqJGDDJZFAtY=w720-h380-rw "Audio Settings")




<br/><br/>
Downloads
---------------------

<strong>EVO PTT on Google Play</strong>
<br /><br />
<a href="https://play.google.com/store/apps/details?id=com.theofilos.chamalis.evoptt">
  <img alt="Get it on Google Play" src="https://developer.android.com/images/brand/en_generic_rgb_wo_45.png" />
</a>

<br/><br/>
\*  Any communication through the demo channel is public.
<br/>
*\* The free version of EVO PTT geolocalization monitoring is not based on real user location, in order to ensure the privacy and protect the personal data of our test users.
<br/>
*\*\* For a data consumption comparison of EVO PTT with our competitors click <a target="_blank" href="https://www.dropbox.com/s/39qpqhx88bqj5nl/EVO%20PTT%20Benchmark.pdf?dl=0"> HERE</a>.
<br/>
*\*\* Applications that do not occupy the voice recording channel.

<br/><br/>
Partnership, Services & Pricing
-----------------------
Our services include plans with customized and private (per customer) servers based on the amount of users as well as the ability to buy the full source (front end and back end) alongside with redistribution and rebranding rights. If you are interested in using EVO PTT or a customized version of it based on your company's needs, feel free to contact us at <b>theofxam@gmail.com</b> for more information.
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
