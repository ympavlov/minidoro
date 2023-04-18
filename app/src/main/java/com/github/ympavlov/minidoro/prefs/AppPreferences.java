package com.github.ympavlov.minidoro.prefs;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.net.Uri;
import com.github.ympavlov.minidoro.R;
import com.github.ympavlov.minidoro.Stage;

import static com.github.ympavlov.minidoro.Stage.BREAK;
import static com.github.ympavlov.minidoro.Stage.LONG_BREAK;

/**
 * Wrapper for SharePreferences. It's needed because SharePreferences stores user-friendly list
 * Also this class includes calculated prefs getters and default values for stored prefs
 */
public class AppPreferences
{
	public static final String PREF_KEY = "_preferences";

	static final String LONG_BREAK_PERIODICITY_KEY = "longBreakPeriodicity";
	static final String DND_MODE_KEY = "dndMode";
	static final String USE_MINIDORO_RINGTONE_KEY = "minidoroRingtone";
	static final String RINGTONE_KEY = "ringtone";
	static final String CHANNEL_KEY = "chanelPreferences";
	static final String OVERRIDE_SILENT_MODE_KEY = "overrideSilent";

	private final SharedPreferences sharedPreferences;

	public AppPreferences(SharedPreferences p)
	{
		this.sharedPreferences = p;
	}

	private static int parsePositive(String s, int def)
	{
		try {
			int i = Integer.parseInt(s);
			return i > 0 ? i : def;
		} catch (NumberFormatException e) {
			return def;
		}
	}

	public int getDuration(Stage s)
	{
		return parsePositive(
				sharedPreferences.getString(s.durationPref, ""),
				s.defaultDuration
		);
	}

	public int getLongBreakVariance() { return getDuration(LONG_BREAK) - getDuration(BREAK); }
	public boolean isLongBreaksOn() { return getLongBreakVariance() > 0; }

	public int getLongBreaksPeriodicity()
	{
		return parsePositive(sharedPreferences.getString(LONG_BREAK_PERIODICITY_KEY, "4"), 4);
	}

	public boolean isDndModeOn() { return sharedPreferences.getBoolean(DND_MODE_KEY, false); }

	// [4a]
	public boolean overrideSilent() { return sharedPreferences.getBoolean(OVERRIDE_SILENT_MODE_KEY, false); }

	public RingtoneSharedPreferences getNotificationPreferences(String packageName)
	{
		return new RingtoneSharedPreferences(packageName);
	}

	/*
	 * Plain old mutable ringtone preferences before notification channels
	 */
	public class RingtoneSharedPreferences implements NotificationPreferences
	{
		public final String minidoroRingtoneStr;
		public final Uri minidoroRingtone;

		private RingtoneSharedPreferences(String packageName)
		{
		    // The Minidoro sound is quieter than regular sounds
		    minidoroRingtoneStr = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.darkjazz;
		    minidoroRingtone = Uri.parse(minidoroRingtoneStr);
		}

		@Override
		public ChannelInfo getChannelInfo() { return null; }

		@Override
		public Uri getRingtone()
		{
		    return isRingtoneDefault() ?
		           minidoroRingtone :
		           Uri.parse(sharedPreferences.getString(AppPreferences.RINGTONE_KEY, minidoroRingtoneStr));
		}

		@Override
		public boolean isRingtoneDefault() { return sharedPreferences.getBoolean(AppPreferences.USE_MINIDORO_RINGTONE_KEY, true); }

		@Override
		public boolean isDirectChangeAvailable() { return true; }
	}
}
