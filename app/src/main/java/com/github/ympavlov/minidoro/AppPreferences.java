package com.github.ympavlov.minidoro;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import com.github.ympavlov.minidoro.nofication.RingtoneProvider;

import static com.github.ympavlov.minidoro.Stage.*;

/**
 * Wrapper for SharePreferences. It's needed because SharePreferences stores user-friendly list
 * Also this class includes calculated prefs getters and default values for stored prefs
 */
public class AppPreferences implements RingtoneProvider
{
	public static final String PREF_KEY = "_preferences";

	public static final String LONG_BREAK_PERIODICITY_KEY = "longBreakPeriodicity";
	public static final String DND_MODE_KEY = "dndMode";
	public static final String USE_MINIDORO_RINGTONE_KEY = "minidoroRingtone";
	public static final String RINGTONE_KEY = "ringtone";
	public static final String OVERRIDE_SILENT_MODE_KEY = "overrideSilent";

	private final SharedPreferences p;
	private final String minidoroRingtone;

	public AppPreferences(String packageName, SharedPreferences p)
	{
		this.p = p;
		// The Minidoro sound is quieter than regular sounds
		minidoroRingtone = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ packageName + "/" + R.raw.darkjazz;
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
				p.getString(s.durationPref, ""),
				s.defaultDuration
		);
	}

	public boolean isLongBreaksOn() { return getDuration(BREAK) != getDuration(LONG_BREAK); }

	public int getLongBreaksPeriodicity()
	{
		return parsePositive(p.getString(LONG_BREAK_PERIODICITY_KEY, "4"), 4);
	}

	public boolean isDndModeOn() { return p.getBoolean(DND_MODE_KEY, false); }

	@Override
	public String getRingtone()
	{
		return p.getBoolean(USE_MINIDORO_RINGTONE_KEY, true) ?
		            minidoroRingtone :
		            p.getString(RINGTONE_KEY, minidoroRingtone);
	}

	// [4a]
	public boolean overrideSilent() { return p.getBoolean(OVERRIDE_SILENT_MODE_KEY, false); }
}
