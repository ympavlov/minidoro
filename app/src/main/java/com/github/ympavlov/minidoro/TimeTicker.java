package com.github.ympavlov.minidoro;

import android.os.Handler;
import android.os.Looper;

import java.util.Observable;

public class TimeTicker extends Observable
{
	public static final int SECOND = 1000;
	public static final int MINUTE = 60 * SECOND;

	private final Handler handler;
	private final Runnable callback;

	private boolean isStopped;

	public TimeTicker(final int period)
	{
		// Single looper to change state from different places by single thread
		handler = new Handler(Looper.getMainLooper());
		callback = new Runnable() {
			@Override
			public void run()
			{
				if (!isStopped) {
					setChanged();
					notifyObservers(System.currentTimeMillis());
					handler.postDelayed(callback, period);
				}
			}
		};
	}

	public void start()
	{
		isStopped = false;
		callback.run();
	}

	public void stop() { isStopped = true; }
}
