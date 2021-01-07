# Skydroid-USB-Video-to-USB

# WHAT

This application is capable of streaming UDP H264 video to [QGroundControl](http://qgroundcontrol.com/) from USB Video of the Skydroid T10 Remote Controller connected to an Android phone.

# WHY

Currently QGroundControl doesn't support USB Video of this particular remote controller due to a Qt bug.

# HOW IT WORKS

This application use the SDK provided by Skydroid to receive H264 video packets.

A UDP H264 video stream is established (port 5600, ip localhost) with [GStreamer](https://gitlab.freedesktop.org/gstreamer/) libraries.

# HOW TO USE
* This app doesn't have an user interface

- Use QGroundControl daily version and enable UDP H264 video streaming in general settings.
- Connect USB of the remote controller to the phone. A notification will appear.
- It is possible to stop the stream by clicking on the notification button.
- Disconnect USB to stop the video stream.

