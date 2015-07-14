# The cWatchTheHamster #
..software project presents a very efficient client-/server backend to stream images from any **webcam** connected to a linux pc to nearly every kind of client.
The server-backend uses [v4l4j](http://code.google.com/p/v4l4j/), client- and server-backend are completely written in java.
There is a **swing** frontend using the client (like hosted in this project) and a beautiful **android** client (+widget) available in the android market.

---

## Finally in the market!!! → [marketlink](https://market.android.com/details?id=com.googlemail.christian667.cWatchTheHamster.Android&feature=search_result) ##
<a href='http://www.youtube.com/watch?feature=player_embedded&v=IRVdIzXmK4w' target='_blank'><img src='http://img.youtube.com/vi/IRVdIzXmK4w/0.jpg' width='425' height=344 /></a>

---

## Server-Features ##
  * Stream images in different FPS to many clients
  * Using JPEG encoding
  * Every client sets its own FPS, the server captures the images in the highest one and sends them to the clients in their individual requested rate
  * RESOURCE SAVING: The server disables the unused webcam completely → NO client connected, NO power wasted
  * Runs on every linux system (Testsystem is a armv7 board running ubuntu → [pandaboard](http://www.pandaboard.org))
  * Connects up to 256 usb webcams, no limit in clients
  * The clients can switch the device WHILE watching!
  * The capturing resolution is also set by the client, the first client connected to a device sets the initial resolution
  * Multithreaded
  * Client timeout
  * Server configuration done by configuration file
  * Multiple useraccounts supported
  * Blacklisting: After a configureable number of failed login attemps the clients ip is blacklisted until the server is restarted

---

## Client-Features ##
  * Save connection to the server
  * Very less ressources needed, depending on requested fps
  * Closes reliable if the connection is closed / broken

---

## cHamster-Authentication (cHA) ##
  * Secure authentication system used by the cWatchTheHamster-Project
  * Multiple MD5-Hashing included
  * Fast + reliable
  * Multi-User-Support, used by the server

---

## Swing-Client-Features ##
  * Runs with open-jdk on every os
  * widget mode with 1 FPS: Click into the displayed image rezises the window, sets it to the upper layer and removes the window decoration, on a intel atom this mode uses <3% CPU
  * instantly change FPS and the device

---

## Android-Client-Features ##
  * Same (!) client-backend for swing / PC and android
  * KISS
  * Swipe to change device
  * WIDGET: Display freqently images on your homescreens, choose you webcam
  * Low resolution supported
  * Even running on HTC Wildfire
  * Best solution seen right now
  * Interested? Take a look in the android market :)

---

## Upcomming-Features ##
  * Windows support for the server-backend
  * Server creates one picture containing every device and streams this as a preview to the clients
  * Statistics for the client backend (AVG FPS, transfered bytes.. etc)

---

  * What is this coding power done for?!?
Easy answer: To watch the hamster of my girlfriend in MY (!) home..
  * What the hell should I need this for???
Mh,
.. watch your car? watch your home? watch your children? watch your crazy cat? watch your stupid neighbour? Watch your girlfriends hamster?...

---

## Starring: Hugo the hamster ##
![http://cwatchthehamster.googlecode.com/svn/trunk/resources/hugo_small.jpg](http://cwatchthehamster.googlecode.com/svn/trunk/resources/hugo_small.jpg)