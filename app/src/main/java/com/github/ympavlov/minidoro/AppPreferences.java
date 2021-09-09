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

	public AppPreferences(Context context, SharedPreferences p)
	{
		this.p = p;
		// The Minidoro sound is quieter than regular sounds
		minidoroRingtone = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ context.getPackageName() + "/" + R.raw.darkjazz;
	}

	public int getDuration(Stage s)
	{
		String val = p.getString(s.durationPref, Integer.toString(s.defaultDuration));
		if ("".equals(val) || "0".equals(val))
			return s.defaultDuration;
		return Integer.parseInt(val);
	}

	public boolean isLongBreaksOn() { return getDuration(BREAK) != getDuration(LONG_BREAK); }

	public int getLongBreaksPeriodicity()
	{
		String val = p.getString(LONG_BREAK_PERIODICITY_KEY, "4");
		if ("".equals(val) || "0".equals(val))
			return 4;
		return Integer.parseInt(val);
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
