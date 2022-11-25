package com.github.ympavlov.minidoro.nofication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import com.github.ympavlov.minidoro.PomodoroActivity;
import com.github.ympavlov.minidoro.R;

@TargetApi(26)
public class NotificationFactoryV26 extends NotificationFactory
{
    private boolean isChannelCreated;

	protected NotificationFactoryV26(Context ctx, Class<? extends Activity> activity, ChannelDescriptor ringtoneChannel)
	{
		super(ctx, activity, ringtoneChannel);
	}

	private void updateChannel()
	{
		if (!isChannelCreated) {
			NotificationChannel channel = new NotificationChannel(
			        ringtoneChannel.getChannelInfo().id,
			        ringtoneChannel.getChannelInfo().name,
			        (ringtoneChannel.getRingtone() == null) ? NotificationManager.IMPORTANCE_LOW : NotificationManager.IMPORTANCE_HIGH);

			if (ringtoneChannel.getRingtone() != null) {
				channel.setSound(ringtoneChannel.getRingtone(),
				                 new AudioAttributes.Builder()
					                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
					                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
					                    .build());
			}

			((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

			isChannelCreated = true;
		}
	}

	@Override
	public Notification createNotification(String tickerText, String title, String text, int icon, boolean highPriority)
	{
		updateChannel();

		Notification.Builder b = new Notification.Builder(context, ringtoneChannel.getChannelInfo().id);

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
