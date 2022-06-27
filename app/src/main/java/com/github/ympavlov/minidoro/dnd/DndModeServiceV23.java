package com.github.ympavlov.minidoro.dnd;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import java.util.Objects;

import static android.app.NotificationManager.*;
import static com.github.ympavlov.minidoro.dnd.DndStrategy.DndMode;

/**
 * @author Yury Pavlov
 */
@TargetApi(23)
public class DndModeServiceV23 extends Service implements DndModeService
{
	private final IBinder binder = new DndBinder(this);
	private NotificationManager notificationManager;
	private int userFilter = INTERRUPTION_FILTER_UNKNOWN;

	@Override
	public IBinder onBind(Intent intent)
	{
		if (notificationManager == null) {
			notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		}
		return binder;
	}

	public static boolean isEnabled(Context context)
	{
		return ((NotificationManager) Objects.requireNonNull(context.getSystemService(NOTIFICATION_SERVICE))).isNotificationPolicyAccessGranted();
	}

	public boolean isEnabled()
	{
		boolean isEnabled = notificationManager.isNotificationPolicyAccessGranted();
		if (!isEnabled) {
			Log.w("Minidoro", "Permissions for DnDModeServiceV23 have disappeared. DnD fallback needed");
		}
		return isEnabled;
	}

	@Override
	public void setPriorityModeOn()
	{
		if (isEnabled() && (getDnDMode() == DndMode.NORMAL || getDnDMode() == DndMode.UNKNOWN)) {

			saveUserMode();

			//Log.d("Minidoro", "Request filter: " + INTERRUPTION_FILTER_PRIORITY);
			notificationManager.setInterruptionFilter(INTERRUPTION_FILTER_PRIORITY);
		}
	}

	@Override
	public void returnUserMode()
	{
		if (isEnabled() && userFilter != INTERRUPTION_FILTER_UNKNOWN) {
			//Log.d("Minidoro", "Return user's filter: " + userFilter);
			notificationManager.setInterruptionFilter(userFilter);
			userFilter = INTERRUPTION_FILTER_UNKNOWN;
		}
	}

	private void saveUserMode() { userFilter = notificationManager.getCurrentInterruptionFilter(); }

	@SuppressLint("SwitchIntDef")
	@Override
	public DndMode getDnDMode()
	{
		switch (notificationManager.getCurrentInterruptionFilter()) {
			case INTERRUPTION_FILTER_ALL:
				return DndMode.NORMAL;
            case INTERRUPTION_FILTER_PRIORITY:
            case INTERRUPTION_FILTER_ALARMS:
				return DndMode.PRIORITY_ALARMS;
			case INTERRUPTION_FILTER_UNKNOWN:
				return DndMode.UNKNOWN;
		}
		return DndMode.SILENT;
	}

	@Override
	public String accessSettings() { return Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS; }
}
