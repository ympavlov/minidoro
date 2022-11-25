package com.github.ympavlov.minidoro.nofication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.github.ympavlov.minidoro.prefs.AppPreferences;
import com.github.ympavlov.minidoro.prefs.NotificationPreferences;

/**
 * Custom Notification Compat — includes compatibility for APIs 4–33
 */
public abstract class NotificationFactory
{
	private final PendingIntent pendingIntent;

	protected final Context context;
	protected final ChannelDescriptor ringtoneChannel;

	protected final int defaultFlags = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS; // [4]

	private static final int USE_V16_SINCE = 16;
	//private static final int USE_V26_SINCE = 26; FIXME test this impl only on latter versions first, use old styled if possible
	private static final int USE_V26_SINCE = 30;

	public static NotificationPreferences getChannelRingtoneProvider(Context ctx, AppPreferences.RingtoneSharedPreferences p)
	{
		return (Build.VERSION.SDK_INT >= USE_V26_SINCE) ? new RingtoneNotificationChannel(ctx, p) : p;
	}

	public static NotificationFactory getFactory(Context context, Class<? extends Activity> activity, ChannelDescriptor ringtoneChannel)
	{
		if (Build.VERSION.SDK_INT >= USE_V26_SINCE) {
			return new NotificationFactoryV26(context, activity, ringtoneChannel);
		}
		if (Build.VERSION.SDK_INT >= USE_V16_SINCE) {
			return new NotificationFactoryV16(context, activity, ringtoneChannel);
		}
		return new NotificationFactoryV4(context, activity, ringtoneChannel);
	}

	@SuppressLint("InlinedApi")
	protected NotificationFactory(Context ctx, Class<? extends Activity> activity, ChannelDescriptor ringtoneChannel)
	{
		context = ctx;
		this.ringtoneChannel = ringtoneChannel;

		Intent i = new Intent(ctx, activity);
		pendingIntent = PendingIntent.getActivity(ctx, 1, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
	}

	protected PendingIntent getPendingIntent() { return pendingIntent; }

	public abstract Notification createNotification(String tickerText, String title, String text, int icon, boolean highPriority);

}
