package com.github.ympavlov.minidoro.prefs;

import android.app.AlertDialog;
import android.content.*;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.Editable;
import android.text.TextWatcher;
import com.github.ympavlov.minidoro.R;
import com.github.ympavlov.minidoro.Stage;
import com.github.ympavlov.minidoro.dnd.DndServiceStrategy;
import com.github.ympavlov.minidoro.nofication.NotificationFactory;

@SuppressWarnings("deprecation") // Using PreferenceActivity for compatibility with APIs 4–11. Not PreferenceFragment. It's too small to Fragment would be useful
public class PreferencesActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
	private SharedPreferences prefs;
    private AppPreferences appPrefs;
    private DndServiceStrategy dndServiceConnection;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.timer_preferences);
        addPreferencesFromResource(R.xml.notification_preferences);

		prefs = getPreferenceScreen().getSharedPreferences();
		appPrefs = new AppPreferences(prefs);
        NotificationPreferences notificationPreferences = NotificationFactory.getChannelRingtoneProvider(this, appPrefs.getNotificationPreferences(getPackageName()));

        if (!notificationPreferences.isDirectChangeAvailable()) {
            CheckBoxPreference minidoroRingtonePref = (CheckBoxPreference) findPreference(appPrefs.USE_MINIDORO_RINGTONE_KEY);
            minidoroRingtonePref.setEnabled(false);
            minidoroRingtonePref.setPersistent(false);
            minidoroRingtonePref.setChecked(notificationPreferences.isRingtoneDefault());
            findPreference(appPrefs.RINGTONE_KEY).setEnabled(false);
        } else {
            Preference chanelPrefs = findPreference(appPrefs.CHANNEL_KEY);
            if (chanelPrefs != null) {
                chanelPrefs.setEnabled(false);
            }
        }

		TrimLeadingZerosTextWatcher w = new TrimLeadingZerosTextWatcher();
		w.assignToPreference(Stage.WORK.durationPref);
		w.assignToPreference(Stage.BREAK.durationPref);
		w.assignToPreference(Stage.LONG_BREAK.durationPref);
		w.assignToPreference(appPrefs.LONG_BREAK_PERIODICITY_KEY);

		Class<?> dndServiceClass = DndServiceStrategy.getServiceClass();
		if (dndServiceClass != null) {
			dndServiceConnection = new DndServiceStrategy();
			bindService(new Intent(this, dndServiceClass), dndServiceConnection, Context.BIND_AUTO_CREATE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		prefs.registerOnSharedPreferenceChangeListener(this);

		onSharedPreferenceChanged(prefs, appPrefs.USE_MINIDORO_RINGTONE_KEY + appPrefs.OVERRIDE_SILENT_MODE_KEY);
	}

	@Override
	protected void onPause() {
		super.onPause();
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	private void checkAndSetDurationSummary(Stage stage) {
		int d = appPrefs.getDuration(stage);
		findPreference(stage.durationPref).setSummary(getResources().getQuantityString(R.plurals.prefMin, d, d));
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String keys)
	{
        if (keys.contains(appPrefs.USE_MINIDORO_RINGTONE_KEY)) {
            Preference minidoroRingtonePref = findPreference(appPrefs.USE_MINIDORO_RINGTONE_KEY);
            if (minidoroRingtonePref.isEnabled()) {
                boolean minidoroRingtone = prefs.getBoolean(appPrefs.USE_MINIDORO_RINGTONE_KEY, true);

                minidoroRingtonePref.setSummary(
                        getString(minidoroRingtone ?
                                  R.string.prefMinidoroRingtoneOn : R.string.prefMinidoroRingtoneOff));

                findPreference(appPrefs.RINGTONE_KEY).setEnabled(!minidoroRingtone);
            }
        }

		// [4a]
		if (keys.contains(appPrefs.OVERRIDE_SILENT_MODE_KEY)) {
			findPreference(appPrefs.OVERRIDE_SILENT_MODE_KEY).setSummary(
					getString(appPrefs.overrideSilent() ?
					          R.string.prefOverrideSilentOn : R.string.prefOverrideSilentOff));
		}

		// Update always. For simplicity

		checkAndSetDurationSummary(Stage.WORK);
		checkAndSetDurationSummary(Stage.BREAK);
		checkAndSetDurationSummary(Stage.LONG_BREAK);

		Preference longBreakPref = findPreference(appPrefs.LONG_BREAK_PERIODICITY_KEY);

		if (appPrefs.isLongBreaksOn()) {
			longBreakPref.setEnabled(true);
			int p = appPrefs.getLongBreaksPeriodicity();
			longBreakPref.setSummary(getResources().getQuantityString(R.plurals.prefPomodoros, p, p));
		} else {
			longBreakPref.setEnabled(false);
		}

		if (dndServiceConnection != null){
			String summary = getString(R.string.prefDndModeComment);

			if (dndServiceConnection.isReady()) {
				if (dndServiceConnection.isEnabled()) {
					summary = "";
				} else if (appPrefs.DND_MODE_KEY.contains(keys) && appPrefs.isDndModeOn()) {
					Intent intent = new Intent(dndServiceConnection.accessSettings());
					try {
						startActivity(intent);
					} catch (ActivityNotFoundException e) {
						new AlertDialog.Builder(this)
								.setMessage(getString(R.string.msgUseAdbForDnDAccess))
								.setPositiveButton("OK", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface d, int id) { d.dismiss(); }
								})
								.create().show();
					}
				}
			}

			findPreference(appPrefs.DND_MODE_KEY).setSummary(summary + '(' + Build.VERSION.SDK_INT + ')');
		}
	}

	private class TrimLeadingZerosTextWatcher implements TextWatcher
	{
		private void assignToPreference(String key)
		{
			((EditTextPreference) findPreference(key)).getEditText().addTextChangedListener(this);
		}

		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

		@Override
		public void afterTextChanged(Editable editable)
		{
			// Replace leading zeros
			String s = editable.toString();
			if (s.length() > 0 && s.charAt(0) == '0') {
				int i = 0;
				while (++i < s.length() && s.charAt(i) == '0');
				editable.replace(0, i, "");
			}
			// Limit value to 2 digits
			s = editable.toString();
			if (s.length() > 2) {
				editable.replace(2, s.length(), "");
			}
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if (dndServiceConnection != null) {
			unbindService(dndServiceConnection);
		}
	}
}