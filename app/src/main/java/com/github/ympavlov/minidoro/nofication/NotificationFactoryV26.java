package com.github.ympavlov.minidoro.nofication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import com.github.ympavlov.minidoro.PomodoroActivity;
import com.github.ympavlov.minidoro.R;

@TargetApi(26)
public class NotificationFactoryV26 extends NotificationFactory
{
	protected static String channelId;  // FIXME WA static

	protected NotificationFactoryV26(Context ctx, Class<? extends Activity> activity, RingtoneProvider ringtoneProvider)
	{
		super(ctx, activity, ringtoneProvider);
	}

	private String getChannelId()
	{
		return (ringtoneProvider == null) ? "MinidoroReminder" : "MinidoroBell:"+ringtoneProvider.getRingtone();
	}

	// TODO replace this WA, use proper ringtone settings instead of creating new channel for each ringtone
	private void createChannel(String channelId)
	{
		NotificationChannel channel = new NotificationChannel(
				channelId,
				context.getString(R.string.notifChannelName),
				(ringtoneProvider == null) ? NotificationManager.IMPORTANCE_LOW : NotificationManager.IMPORTANCE_HIGH);

		if (ringtoneProvider != null) {
			channel.setSound(Uri.parse(ringtoneProvider.getRingtone()),
					new AudioAttributes.Builder()
							.setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
							.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
							.build());
		}

		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
	}

	private void deleteChannel(String channelId)
	{
		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).deleteNotificationChannel(channelId);
	}

	@Override
	public Notification createNotification(String tickerText, String title, String text, int icon, boolean highPriority)
	{
		String currentChannelId = getChannelId();
		if (!currentChannelId.equals(channelId)) {
			if (channelId != null) {
				deleteChannel(channelId);
			}
			createChannel(currentChannelId);
			channelId = currentChannelId;
		}

		Notification.Builder b = new Notification.Builder(context, channelId);

		b.setSmallIcon(icon);
		b.setTicker(tickerText);
		b.setContentTitle(title);
		b.setContentText(text);
		b.setColor(context.getResources().getColor(R.color.accent, null));

		b.setAutoCancel(true);
		b.setContentIntent(getPendingIntent());

		if (highPriority) {
			b.setCategory(Notification.CATEGORY_ALARM);
		}

		b.setColorized(true);
		b.setTimeoutAfter(PomodoroActivity.MAX_WAIT_USER_RETURN);

		return b.build();
	}
}
