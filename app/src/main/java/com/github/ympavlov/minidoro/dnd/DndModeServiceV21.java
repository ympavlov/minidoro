package com.github.ympavlov.minidoro.dnd;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.util.Log;

import static com.github.ympavlov.minidoro.dnd.DndStrategy.DndMode.*;

/**
 * DnD mode introduced in API 21 is very tricky
 * Service will always be running once enabled till disabled
 * It's not a quite API-change-tolerant code. isEnabled may not work in APIs >= 23
 * Replaced by DnDModeServiceV23 for APIs >= 23, but can be fallen back to if user has denied access for DnDModeServiceV23
 * @author Yury Pavlov
 */
@TargetApi(21)
public class DndModeServiceV21 extends NotificationListenerService implements DndModeService
{
	private final IBinder binder = new DndBinder(this);
	private boolean listenerConnected;
	private int userFilter;

	@Override
	public IBinder onBind(Intent intent)
	{
		Log.d("Minidoro", "DnDv21 service is bound by " + intent.getAction());
		if (SERVICE_INTERFACE.equals(intent.getAction())) {
			if (Build.VERSION.SDK_INT >= 23 && DndModeServiceV23.isEnabled(this)) { // As I could see: DndModeServiceV23.isEnabled is always TRUE here
				return null;
			}
			return super.onBind(intent);
		} else {
			if (Build.VERSION.SDK_INT >= 23) {
				Log.w("Minidoro", "DnDv21 service may not be started by app with API>=23"); // As noticed before: possibly this never happens
			}
			return binder;
		}
	}

	static boolean isEnabled(Context context)
	{
		String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
		return flat != null && flat.contains((new ComponentName(context, DndModeServiceV21.class)).flattenToString());
	}

	@Override
	public boolean isEnabled()
	{
		if (!listenerConnected) {
			Log.i("Minidoro", "STRANGE: DnDModeServiceV21 is requested but it's not connected yet");
			return false;
		}

		boolean isEnabled = isEnabled(this);
		if (!isEnabled) {
			Log.w("Minidoro", "Permissions for DnDModeServiceV21 have disappeared. DnD fallback needed");
		}
		return isEnabled;
	}

	@Override
	public void onListenerConnected()
	{
		Log.i("Minidoro", "Listener connected");
		listenerConnected = true;
	}

	@Override
	public void priorityModeOn()
	{
		if (isEnabled() && getCurrentInterruptionFilter() == INTERRUPTION_FILTER_ALL) {
			saveUserMode();

			Log.d("Minidoro", "Request filter: " + INTERRUPTION_FILTER_PRIORITY);
			requestInterruptionFilter(INTERRUPTION_FILTER_PRIORITY);
		}
	}

	@Override
	public void returnUserMode()
	{
		if (isEnabled() && userFilter != 0) {
			Log.d("Minidoro", "Return user's filter: " + userFilter);
			requestInterruptionFilter(userFilter);
			userFilter = 0; // INTERRUPTION_FILTER_UNKNOWN
		}
	}

	private void saveUserMode()
	{
		if (isEnabled()) {
			userFilter = getCurrentInterruptionFilter();
			Log.d("Minidoro", "Save user's filter: " + userFilter);
		}
	}

	@Override
	public DndStrategy.DndMode getDnDMode()
	{
		if (!isEnabled())
			return UNKNOWN;
		switch (getCurrentInterruptionFilter()) {
			case INTERRUPTION_FILTER_ALL:
				return NORMAL;
			case INTERRUPTION_FILTER_PRIORITY:
				return PRIORITY;
		}
		return SILENT;
	}

	@Override
	public String accessSettings() { return "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"; }
}
