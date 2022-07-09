package com.github.ympavlov.minidoro.prefs;

import com.github.ympavlov.minidoro.nofication.ChannelDescriptor;

public interface NotificationPreferences extends ChannelDescriptor
{
    boolean isRingtoneDefault();
    boolean isDirectChangeAvailable();
}