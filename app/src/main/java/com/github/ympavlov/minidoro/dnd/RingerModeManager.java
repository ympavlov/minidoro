package com.github.ympavlov.minidoro.dnd;

import android.content.ContentResolver;
import android.media.AudioManager;
import android.provider.Settings;
//import android.util.Log;

/**
 * @author Yury Pavlov
 */
public class RingerModeManager implements DndStrategy
{
	private int userMode;
	private boolean wasChanged;

	private final AudioManager audioManager;
	private final ContentResolver contentResolver;
	private DndManager dndManager;

	public RingerModeManager(AudioManager audioManager, ContentResolver contentResolver)
	{
		this.audioManager = audioManager;
		this.contentResolver = contentResolver;
	}

	public void setDndManager(DndManager dndManager)
	{
		this.dndManager = dndManager;
	}

	/*
	 * Special method to access zenMode in APIs where it has appeared but is not accessible yet
	 */
	@Override
	public DndMode getDndMode()
	{
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
			int zenMode;
			try {
				zenMode = Settings.Global.getInt(contentResolver, "zen_mode");
			} catch (Settings.SettingNotFoundException e) {
				return DndMode.UNKNOWN;
			}
			//Log.d("Minidoro", "zen_mode="+zenMode);
			switch (zenMode) {
				case 0:
					return DndMode.NORMAL;
				case 1:
					return DndMode.PRIORITY_ALARMS;
			}
			return DndMode.SILENT;
		}
		return DndMode.UNKNOWN;
	}

	public void setLoudModeOn()
	{
		dndManager.returnUserMode();

		DndMode m = dndManager.getDnDMode();

		// There's a very tricky link between DnD mode and ringer mode: getRingerMode may return silent while there's dnd in priority or alarms even if it's in vibrate mode
		// So don't deal with priority mode here to not make user sad
		if (m != null && m != DndMode.PRIORITY_ALARMS) {
			setMode(AudioManager.RINGER_MODE_NORMAL);
		}
	}

	@Override
	public void setDndModeOn()
	{
		setMode(AudioManager.RINGER_MODE_SILENT);
	}

	@Override
	public void returnUserMode()
	{
		if (wasChanged) {
			//Log.d("Minidoro", "RingerMode return: " + audioManager.getRingerMode() + " -> " + userMode);
			audioManager.setRingerMode(userMode);
			wasChanged = false;

			if (dndManager != null) {   // case dndManager = this
				dndManager.returnUserMode();
			}
		}
	}

	private void setMode(int mode)
	{
		int currMode = audioManager.getRingerMode();
		if (currMode != mode) {
			saveUserMode(currMode);
			//Log.d("Minidoro", "RingerMode change: " + currMode + " -> " + mode);
			audioManager.setRingerMode(mode);
		}
	}

	private void saveUserMode(int mode)
	{
		if (!wasChanged) {
			userMode = mode;
			wasChanged = true;
		}
	}
}
