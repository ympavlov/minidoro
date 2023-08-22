[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid"
    height="80">](https://f-droid.org/packages/com.github.ympavlov.minidoro)

Minidoro is a minimalistic Pomodoro® Technique timer app

[Pomodoro® Technique](http://www.pomodorotechnique.com/) is an extremely simple but efficient time management technique

The main thing this app does is show how much time left for work or break and take care you are not distracted while you work (by notifications from the device)

Minidoro is gained to be simple, reliable and easy to use and run on 100% of Android devices: smart phones, smart watches (on Wear OS), tablets, e-books (including quite old devices, i.e. my old Motorola Milestone)

Also it's assumed to be colorblind friendly

<img src="https://github.com/ympavlov/minidoro/blob/main/metadata/en-US/images/phoneScreenshots/1.png?raw=true" alt="phone screenshot 1" style="height: 15em;"/>
<img src="https://github.com/ympavlov/minidoro/blob/main/metadata/en-US/images/phoneScreenshots/2.png?raw=true" alt="phone screenshot 2" style="width: 15em;"/>
<img src="https://github.com/ympavlov/minidoro/blob/main/metadata/en-US/images/wearScreenshots/2.png?raw=true" alt="wear screenshot" style="height: 15em;"/>

# Sailfish OS/Аврора ОС port
Exists: https://openrepos.net/content/ichthyosaurus/minidoro

Sources: https://github.com/ichthyosaurus/harbour-minidoro/tree/main

# Build requirements
Use Gradle 4.6–5.6.4 to build app for any Android version (or you can raise up com.android.tools.build:gradle version by yourself before build). Android.support/AndroidX is not used (in runtime) since it was almost useless here

# Wear OS notice
Firstly it was assumed to run Minidoro on Wear OS would be easy as pie, a few modification would be required… But Minidoro optimization for Wear OS is still in progress. Probably, the preference activity  should be rewritten to fragment, still

## Do-not-disturb mode in Wear OS
It would be a great feature that Minidoro running on Wear OS turns DnD mode on both Wear OS and Android device. It should be generally possible since Wear OS may turn DnD on Android devices. But there's one nuisance. It's not possible to allow any app to operate DnD Mode in Wear OS from Wear OS UI. The only way to allow Minidoro to operate DnD in Wear OS is ADB command (no root should be needed).

For Wear OS 2.2 (based on Android 9) and above run this:

    adb shell cmd notification allow_listener com.github.ympavlov.minidoro/com.github.ympavlov.minidoro.dnd.DndModeServiceV21

For any other Wear OS version below (based on Android 8.1 and below) use this:

    adb shell settings put secure enabled_notification_listeners com.google.android.wearable.app/com.google.android.clockwork.stream.NotificationCollectorService:com.github.ympavlov.minidoro/com.github.ympavlov.minidoro.dnd.DndModeServiceV21