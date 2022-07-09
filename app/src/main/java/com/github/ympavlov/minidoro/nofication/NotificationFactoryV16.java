package com.github.ympavlov.minidoro.nofication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import com.github.ympavlov.minidoro.R;

@SuppressWarnings("deprecation")
@TargetApi(16) // till 29
public class NotificationFactoryV16 extends NotificationFactory
{
	protected NotificationFactoryV16(Context ctx, Class<? extends Activity> activityClass, ChannelDescriptor ringtoneProvider)
	{
		super(ctx, activityClass, ringtoneProvider);
	}

	protected Notification.Builder createNotificationBuilder(String tickerText, String title, String text, int icon, boolean highPriority)
	{
		Notification.Builder b = new Notification.Builder(context);

		b.setSmallIcon(icon);
		b.setTicker(tickerText);
		b.setContentTitle(title);
		b.setContentText(text);
        Uri ringtone = ringtoneChannel.getRingtone();
		if (ringtone != null) {
			b.setDefaults(defaultFlags);
			b.setSound(ringtone);
		}
		b.setAutoCancel(true);
		b.setContentIntent(getPendingIntent());

		if (Build.VERSION.SDK_INT >= 21)
			b.setColor(context.getResources().getColor(R.color.accent));

		if (highPriority) {
			b.setPriority(Notification.PRIORITY_HIGH);
			if (Build.VERSION.SDK_INT >= 21)
				b.setCategory(Notification.CATEGORY_ALARM);
		}

		return b;
	}

	@Override
	public Notification createNotification(String tickerText, String title, String text, int icon, boolean highPriority)
	{
		return createNotificationBuilder(tickerText, title, text, icon, highPriority).build();
	}
}
