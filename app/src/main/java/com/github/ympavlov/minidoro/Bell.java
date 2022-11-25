package com.github.ympavlov.minidoro;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
//import android.util.Log;
import com.github.ympavlov.minidoro.dnd.RingerModeManager;
import com.github.ympavlov.minidoro.nofication.NotificationFactory;
import com.github.ympavlov.minidoro.nofication.NotificationIcons;
import com.github.ympavlov.minidoro.prefs.AppPreferences;

import java.util.Observable;
import java.util.Observer;

/**
 * Makes notification appears when time ends up
 */
public class Bell implements Observer
{
	public final static int NOTIFICATION_ID = 1; // Use single notification for the whole app

	private final AppPreferences prefs;
	private final RingerModeManager ringerModeManager;
	private final NotificationManager notificationManager;
	private final NotificationFactory notificationFactory;
	private final Context ctx;
	private final Handler handler;

	public Bell(AppPreferences prefs, Context ctx, RingerModeManager ringerModeManager)
	{
		this.prefs = prefs;
		this.ctx = ctx;
		handler = new Handler(ctx.getMainLooper());
		this.ringerModeManager = ringerModeManager;
		notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationFactory = NotificationFactory.getFactory(ctx, PomodoroActivity.class, NotificationFactory.getChannelRingtoneProvider(ctx, prefs.getNotificationPreferences(ctx.getPackageName())));
	}

	@Override
	public void update(Observable observable, Object data)
	{
		// XOR: returnUserMode may delay setLoudModeOn effect
		if (prefs.overrideSilent()) {
			ringerModeManager.setLoudModeOn();
		} else {
			ringerModeManager.returnUserMode();
		}

		// Delay notification to make sure DnD be off
	handler.post(new Runnable() {
		@Override
			public void run()
			{
				final PomodoroState state = (PomodoroState) observable;
				final int title, msg;
				switch (state.stage.next(state, prefs)) {
					case BREAK:
						title = R.string.msgHaveBreakHead;
						msg = R.string.msgHaveBreakBody;
						break;
					case LONG_BREAK:
						title = R.string.msgHaveLongBreakHead;
						msg = R.string.msgHaveLongBreakBody;
						break;
					default: //WORK
						title = R.string.msgHaveWorkHead;
						msg = R.string.msgHaveWorkBody;
				}

				//Log.d("Minidoro", "Notify");
				notificationManager.notify(NOTIFICATION_ID, notificationFactory.createNotification(
				       ctx.getString(msg), ctx.getString(title), ctx.getString(msg),
				       NotificationIcons.getBreakIcon(state.stage.isWork ? NotificationIcons.N_SLICES : 0),
				       true
				));

				// immediate sound off causes the ring to be suppressed
				//if (prefs.overrideSilent()) ringerModeManager.returnUserMode();
			}
		});
	}
}
