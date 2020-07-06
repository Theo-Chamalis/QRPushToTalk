<h1 align="center">EVO PTT - Walkie Talkie Solution for Businesses</h1>
<br/>

![Alt text](https://github.com/Theofilos-Chamalis/QR-PTT-PushToTalk/blob/master/EVO%20PTT-feature-graphic.png "EVO PTT - The Evolution in PTT communication")
<br/>
<br/>

Designed with Businesses in mind, EVO PTT is a complete Walkie Talkie (Push To Talk) solution that provides:

► Safe communication of users and supervisors with the EVO PTT Android App.
<br/>
► Easy administration and management of users and companies with the EVO PTT Web Panel.
<br/>
<br/>

Demo testing
---------------------
• To try the EVO PTT Android App as a User for free, open the Android Application, accept the required permissions and tap on the DEMO LOGIN button! 
<br/>
• To try the EVO PTT Web Panel as a Company Manager for free, go to https://evoptt.ddns.net and click on the DEMO LOGIN button!
<br/>
<br/>

Main Features
---------------------
- Easy to understand and use UI.
- Fast and low latency real time voice communication,
- Seamless geolocation monitoring of EVO PTT Users location.
- Superior voice compression CODECs and low data consumption.
- High security voice encryption.
- Works over any network condition (2G, 3G, 4G or Wifi) with Network Type Switching mode to switch seamlessly between Data and WiFi without disconnecting.
- Native integration with Android Walkie Talkie devices (F22, F22+, F25 and more).
- Ability to work with screen off in Android Walkie Talkie devices.
- SOS button function with location reporting.
- Real time location tracking of Mobile Users to the EVO PTT Android App and the EVO PTT Web Panel.
- Easy User management through the EVO PTT Web Panel.
- Mobile Users log in on a per-company basis for better user isolation and security (using secure docker containers).
- Start on boot, Auto-login, Remember Login Credentials and Auto-reconnect when Internet connection is lost.
- Stay Awake mode to keep the screen on.
- EVO PTT stays active in background so you can use other apps while listening other Users in real time.
- Compatible with the latest version of Android 10, Android Studio IDE and Gradle tools.
- Ideal for security companies, cleaning companies, taxi drivers, police or fire departments, lorry drivers and more!
- Competitive subscription and one off pay pricing plans!
- Ability for source code purchase and app rebranding!
<br/>

EVO PTT Android App Screenshots
-------------------------------
![Alt text](https://lh3.googleusercontent.com/QHKcGjB1coyKFnoMMlMDKFZsxb5b2QIPDGkVRa7l2B1iYPpyqAqKp7EwNM47eivWmtQ=w720-h380-rw "Login Screen") 
![Alt text](https://lh3.googleusercontent.com/jPr6XrHdUqLu-cHzY_Dx4Rx1y7EyP2j-VIMmVrLAYwCVcMu8icfUCZI4-ebSyZIl11U=w720-h380-rw "Server Screen") 
![Alt text](https://lh3.googleusercontent.com/p_BwjT4Qvb3m_a8J7jW8HLPeI4jSLh84FT5__dJQUP6oIiObLpujtze7YMP9B-QOHwA=w720-h380-rw "Mapview Screen") 
![Alt text](https://lh3.googleusercontent.com/K5ey4JjmU2UO9Vph4RSq7qnqSWjkw9zt0Cg9S8-wRQjmGXVLg2IRvzN8LgcKLS5oh1lu=w720-h380-rw "Chat Screen")
![Alt text](https://lh3.googleusercontent.com/GEb4GqhKVz7NjIKZygA6qggeA-WA_7B-_ESqHLKJohQA7YZoTD--JAkXrQeQPR0JU5I=w720-h380-rw "Side Menu")
![Alt text](https://lh3.googleusercontent.com/Xp-WIpbpKCMPK6vYzgBwXYDGJ5w9-wMfikgopJXRl26KfXUaeK5d9a2LEgk6z1rqjhH6=w720-h380-rw "General Settings")
![Alt text](https://lh3.googleusercontent.com/UH4IizfoTs5-GmJkqnJaASVTWIDOCAydbu007FH27y2RhjYlRtBQGlzMqJGDDJZFAtY=w720-h380-rw "Audio Settings")
<br/>
<br/>

Downloads
---------------------

<strong>EVO PTT on Google Play</strong>
<br /><br />
<a href="https://play.google.com/store/apps/details?id=com.theofilos.chamalis.evoptt">
  <img alt="Get it on Google Play" src="https://developer.android.com/images/brand/en_generic_rgb_wo_45.png" />
</a>

<br/>

\*  Any communication through the demo channel is public. <br/>
*\* For a data consumption comparison of EVO PTT with our competitors click <a target="_blank" href="https://www.dropbox.com/s/39qpqhx88bqj5nl/EVO%20PTT%20Benchmark.pdf?dl=0"> HERE</a>.

<br/>

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
