package com.github.ympavlov.minidoro;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.github.ympavlov.minidoro.nofication.NotificationFactory;
import com.github.ympavlov.minidoro.nofication.NotificationIcons;

/**
 * [2a] Updates notification icon on status bar and lock screen. Uses alarms to operate in doze mode
 */
public class BarIconUpdater extends BroadcastReceiver
{
	// We need BroadcastReceiver to store state. TODO: think out how to make w/o static
	private static long until;
	private static int periodMillis;
	private static PendingIntent pIntent;

	static void setDuration(int durationMinutes)
	{
		periodMillis = TimeTicker.MINUTE * durationMinutes / NotificationIcons.NPARTS;
	}

	/**
	 *
	 * @param leftMillis time left in minutes
	 * @return number less than NotificationIcons.NPARTS (might be negative)
	 */
	static int getIconsLeft(long leftMillis)
	{
		return Math.min((int) Math.ceil((float) leftMillis / periodMillis), NotificationIcons.NPARTS);
	}

	static void setupNextAlarm(Context ctx, long untilMillis, int durationMinutes)
	{
		if (pIntent != null)
			return;

		setDuration(durationMinutes);
		until = untilMillis;
		long leftMillis = untilMillis - System.currentTimeMillis();
		int n = getIconsLeft(leftMillis)
				- 1; // Since last icon update will be made by Bell

		if (n > 0) {
			AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
			if (alarmManager != null) {
				//Log.d("Minidoro", "BarIconUpdater: planning " + n + " alarms");
				Intent i = new Intent(ctx, BarIconUpdater.class);

				pIntent = PendingIntent.getBroadcast(ctx, 1, i, 0);

				// I think we've no need for exact alarm
				// Min alarm interval is 1 minute. So we don't need repeating if we have less than 2 minutes
				if (leftMillis > 2 * TimeTicker.MINUTE && n > 1)
					alarmManager.setRepeating(AlarmManager.RTC, untilMillis - periodMillis * n, Math.max(periodMillis, TimeTicker.MINUTE), pIntent);
				else	// We need to make only one alarm. Let's choose proper time
					alarmManager.set(AlarmManager.RTC, untilMillis - periodMillis * (int) Math.ceil((float) n/2), pIntent);
			}
		}
	}

	static void stop(Context ctx)
	{
		if (pIntent != null) {
			//Log.d("Minidoro", "BarIconUpdater: stopping alarms");
			AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
			if (alarmManager != null)
				alarmManager.cancel(pIntent);
			pIntent = null;
		}
	}

	private static int millisToMinutes(float millis) { return Math.round(millis / TimeTicker.MINUTE); }

	static Notification createForegroundNotification(Context ctx, NotificationFactory notificationFactory, String ticker, long leftMillis, int icon)
	{
		int leftMinutes = millisToMinutes(leftMillis);
		String title = ctx.getResources().getQuantityString(R.plurals.barLeftMinutes, leftMinutes, leftMinutes);
		return notificationFactory.createNotification(
				ticker != null ? ticker : title,
				title,
				ctx.getString(R.string.barBreakWish),
				NotificationIcons.getBreakIcon(icon),
				false
		);
	}

	@Override
	public void onReceive(Context ctx, Intent i)
	{
		//Log.d("Minidoro", "BarIconUpdater: updating notification at " + System.currentTimeMillis());

		long left = until - System.currentTimeMillis();

		int n = getIconsLeft(left);
		if (n > 0) {
			NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
			NotificationFactory notificationFactory = NotificationFactory.getFactory(ctx, PomodoroActivity.class, null);
			if (notificationManager != null)
				notificationManager.notify(Bell.NOTIFICATION_ID, createForegroundNotification(ctx, notificationFactory, null, left, n));
		}
		if (n <= 1) {
			stop(ctx);
			//Log.d("Minidoro", "BarIconUpdater: notification updates ended");
		}
	}
}