package com.github.ympavlov.minidoro.dnd;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
//import android.util.Log;

/**
 * [6] DnD mode handler. It chooses right strategy according to API version and granted app privileges
 *
 * NOTE: this class depends on activity context. It wouldn't be so ugly if it wouldn't be used outside of the main activity
 * DndManager is mainly used in work time when activity has never be destroyed (when activity are being destroying work period stops)
 * But it's also used in RingerModeManager to finish up DnD mode (not needed after break time) and get current system DnD mode
 * to avoid ringtone volume change if we're not able to return back DnD mode
 * It seems safe till Android doesn't kill the main activity without services (never seen this yet:
 * Androids 5+ devices where dnd service is used usually have enough memory to not kill activities apart)
 * TODO to think and do something with this mess
 */
public class DndManager
{
	private final Context context;
	private final RingerModeManager ringerModeManager;
	private DndStrategy writeStrategy;
	private DndStrategy readStrategy;

	private boolean needed;

	public DndManager(Context context, RingerModeManager ringerModeManager)
	{
		this.context = context;
		this.ringerModeManager = ringerModeManager;
	}

	private void updateStrategy()
	{
		Class<? extends Service> serviceReadClass = null;
		writeStrategy = null;
		readStrategy = null;

		// See also DndServiceStrategy.getServiceClass
		if (Build.VERSION.SDK_INT >= 23 && DndModeServiceV23.isEnabled(context)) {
			serviceReadClass = DndModeServiceV23.class;
		} else {
			if (Build.VERSION.SDK_INT >= 21 && DndModeServiceV21.isEnabled(context)) {
				serviceReadClass = DndModeServiceV21.class;
			} else if (Build.VERSION.SDK_INT >= 23) {
				serviceReadClass = DndModeServiceV23.class;
				writeStrategy = ringerModeManager;
			}
		}

		if (serviceReadClass != null) {
			readStrategy = new DndServiceStrategy();
			context.bindService(new Intent(context, serviceReadClass), (ServiceConnection) readStrategy, Context.BIND_AUTO_CREATE);

			if (writeStrategy == null) {
				writeStrategy = readStrategy;
			}
		} else {
			writeStrategy = readStrategy = ringerModeManager;
		}
		//Log.d("Minidoro", "Dnd write strategy chosen: " + writeStrategy);
		//Log.d("Minidoro", "Dnd read strategy chosen: " + readStrategy);
		//Log.d("Minidoro", "Dnd service class name: " + serviceReadClass);
	}

	public void setNeeded(boolean needed)
	{
		if (this.needed == needed)
			return;

		if (needed) {
			updateStrategy();
		}
		else {
			if (readStrategy != writeStrategy) {
				context.unbindService((ServiceConnection) readStrategy);
			}
			if (writeStrategy instanceof ServiceConnection) {
				context.unbindService((ServiceConnection) writeStrategy);
			}
		}

		this.needed = needed;
	}

	public void setDndModeOn() { writeStrategy.setDndModeOn(); }

	public void returnUserMode()
	{
		if (writeStrategy != null) { // returnUserMode may be called from various places when not initialized yet
			writeStrategy.returnUserMode();
		}
	}

	DndStrategy.DndMode getDnDMode() { return readStrategy.getDndMode(); }
}
