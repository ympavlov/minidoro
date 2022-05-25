package com.github.ympavlov.minidoro.nofication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Custom Notification Compat — includes compatibility for APIs 4–31
 */
public abstract class NotificationFactory
{
	private final PendingIntent pendingIntent;

	protected final Context context;
	protected final RingtoneProvider ringtoneProvider;

	protected final int defaultFlags = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS; // [4]

	public static NotificationFactory getFactory(Context context, Class<? extends Activity> activity, RingtoneProvider ringtoneProvider)
	{
		if (Build.VERSION.SDK_INT >= 26) {
			return new NotificationFactoryV26(context, activity, ringtoneProvider);
		}
		if (Build.VERSION.SDK_INT >= 16) {
			return new NotificationFactoryV16(context, activity, ringtoneProvider);
		}
		return new NotificationFactoryV4(context, activity, ringtoneProvider);
	}

	@SuppressLint("InlinedApi")
	protected NotificationFactory(Context ctx, Class<? extends Activity> activity, RingtoneProvider ringtoneProvider)
	{
		context = ctx;
		this.ringtoneProvider = ringtoneProvider;

		Intent i = new Intent(ctx, activity);
		pendingIntent = PendingIntent.getActivity(ctx, 1, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
	}

	protected PendingIntent getPendingIntent() { return pendingIntent; }

	public abstract Notification createNotification(String tickerText, String title, String text, int icon, boolean highPriority);

}
