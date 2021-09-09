package com.github.ympavlov.minidoro;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
//import android.util.Log;
import com.github.ympavlov.minidoro.nofication.NotificationFactory;

/**
 * TimerService [1]
 * maintains tray icon when activity became invisible
 * keeps state and context in case activity will be killed (when pause)
 * creates alert in case activity and this service both will be killed
 */
public class PomodoroService extends Service
{
	private final IBinder mBinder = new TimerBinder();

	private PomodoroContext pomodoroContext;

	private boolean startedForeground;
	private PendingIntent alarmIntent;

	private AppPreferences prefs;
	private NotificationManager notificationManager;
	private NotificationFactory notificationFactory;
	private AlarmManager alarmManager;

	PomodoroContext getPomodoroContext() { return pomodoroContext; }

	void init(PomodoroContext pomodoroContext, AppPreferences prefs)
	{
		this.pomodoroContext = pomodoroContext;
		this.prefs = prefs;

		if (notificationManager == null) {
			notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationFactory = NotificationFactory.getFactory(getApplicationContext(), PomodoroActivity.class, null);
		}
		if (alarmManager == null)
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		if (pomodoroContext.pomodoroState.stage.isWork) {
			if (startedForeground) {
				//Log.d("Minidoro", "Stopping Timer Service. It's not needed when work");
				if (Build.VERSION.SDK_INT > 5)
					stopForeground(true);
				stopSelf();
				startedForeground = false;
			}
		}
	}

	void stopTimer()
	{
		//alarmManager.cancel(alarmIntent); // IS IT NEEDED ??
		alarmIntent = null;
		BarIconUpdater.stop(this);
	}

	/*
	 * Starts background timer when the activity came background
	 */
	void backgroundTimer()
	{
		if (!startedForeground) {
			long left = pomodoroContext.pomodoroState.refresh();
			BarIconUpdater.setDuration(prefs.getDuration(pomodoroContext.pomodoroState.stage));
			Notification n = BarIconUpdater.createForegroundNotification(this,
					notificationFactory,
					getString(R.string.barMinidoroNotifies),
					left,
					BarIconUpdater.getIconsLeft(left)
			);

			if (Build.VERSION.SDK_INT > 5)
				startForeground(Bell.NOTIFICATION_ID, n);
			else
				notificationManager.notify(Bell.NOTIFICATION_ID, n);

			startedForeground = true;
		}
		long until = pomodoroContext.pomodoroState.getUntilMillis();
		//Log.d("Minidoro", "Setting alarms up to " + until);
		// [2a]
		BarIconUpdater.setupNextAlarm(this, until, prefs.getDuration(pomodoroContext.pomodoroState.stage));
		// [5]
		if (alarmIntent == null) {
			Intent i = new Intent(this, PomodoroActivity.class);
			alarmIntent = PendingIntent.getActivity(this, 1, i, 0);
			// We've no need for exact alarm because we do only one alarm, it'll not be batched (see doc)
			if (Build.VERSION.SDK_INT < 23)
				alarmManager.set(AlarmManager.RTC_WAKEUP, until, alarmIntent);
			else // but then in case if maintenance window is really long we need AllowWhileIdle
				alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, until, alarmIntent);
		}
	}

	@SuppressLint("WrongConstant")
	@Override
	public int onStartCommand(Intent i, int flags, int startId)
	{
		return 2; // START_NOT_STICKY since service doesn't restore state by himself
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		//Log.d("Minidoro", "TimerService is destroying");

		if (alarmManager != null && alarmIntent != null)
			alarmManager.cancel(alarmIntent);

		BarIconUpdater.stop(this);

		// to unbind service
		if (pomodoroContext != null)
			pomodoroContext.dndManager.setNeeded(false);
	}

	// [5a] If user prefers to stop app by himself, stop
	@Override
	public void onTaskRemoved(Intent rootIntent)
	{
		//Log.d("Minidoro", "TimerService TaskRemoved");

		if (pomodoroContext != null) {
			pomodoroContext.dndManager.returnUserMode();
			pomodoroContext.ringerModeManager.returnUserMode();
		}

		StateSaver.dismissState(this);

		stopSelf();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		//Log.d("Minidoro", "TimerService bound");
		return mBinder;
	}

	class TimerBinder extends Binder { PomodoroService getService() { return PomodoroService.this; }}
}
