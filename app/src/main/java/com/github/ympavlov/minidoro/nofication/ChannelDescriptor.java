package com.github.ympavlov.minidoro.nofication;

import android.net.Uri;

public interface ChannelDescriptor
{
	Uri getRingtone();

    ChannelInfo getChannelInfo();

	class ChannelInfo
    {
        final String id;
        final String name;

        public ChannelInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}