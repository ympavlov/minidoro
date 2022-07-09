package com.github.ympavlov.minidoro.nofication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("deprecation")
@TargetApi(4) // till 15
public class NotificationFactoryV4 extends NotificationFactory
{
	protected NotificationFactoryV4(Context ctx, Class<? extends Activity> activityClass, ChannelDescriptor ringtoneProvider)
	{
		super(ctx, activityClass, ringtoneProvider);
	}

	@Override
	public Notification createNotification(String tickerText, String title, String text, int icon, boolean highPriority)
	{
		Notification n = new Notification(icon, tickerText, System.currentTimeMillis());

		try { // we need setLatestEventInfo in elder APIs
			@SuppressWarnings("JavaReflectionMemberAccess")
			Method setInfoMethod = Notification.class.getMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
			//n.setLatestEventInfo(context, title, text, pendingIntent);o
			setInfoMethod.invoke(n, context, title, text, getPendingIntent());
		} catch (NoSuchMethodException e) {
			Log.e("Minidoro", "NotificationFactoryV4: no setLatestEventInfo in SDK " + Build.VERSION.SDK_INT);
		} catch (IllegalAccessException e) {
			Log.e("Minidoro", "NotificationFactoryV4: IllegalAccessException with setLatestEventInfo in SDK " + Build.VERSION.SDK_INT);
		} catch (InvocationTargetException e) {
			Log.e("Minidoro", "NotificationFactoryV4: InvocationTargetException with setLatestEventInfo in SDK " + Build.VERSION.SDK_INT);
		}

		n.flags |= Notification.FLAG_AUTO_CANCEL;
		Uri ringtone = ringtoneChannel.getRingtone();
		if (ringtone != null) {
			n.defaults = defaultFlags;
			n.sound = ringtone;
		}
		return n;
	}
}
