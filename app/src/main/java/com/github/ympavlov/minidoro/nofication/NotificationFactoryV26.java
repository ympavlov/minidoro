package com.github.ympavlov.minidoro.nofication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import com.github.ympavlov.minidoro.PomodoroActivity;

@TargetApi(26)
public class NotificationFactoryV26 extends NotificationFactoryV16
{
	protected NotificationFactoryV26(Context ctx, Class<? extends Activity> activity, RingtoneProvider ringtoneProvider)
	{
		super(ctx, activity, ringtoneProvider);
	}

	@Override
	public Notification createNotification(String tickerText, String title, String text, int icon, boolean highPriority)
	{
		// TODO new separate implementation with channels
		Notification.Builder b = createNotificationBuilder(tickerText, title, text, icon, highPriority);

		b.setColorized(true);
		b.setTimeoutAfter(PomodoroActivity.MAX_WAIT_USER_RETURN);

		return b.build();
	}
}
