package com.github.ympavlov.minidoro.dnd;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
//import android.util.Log;

public class DndServiceStrategy implements ServiceConnection, DndStrategy
{
	private DndModeService service;

	public static Class<?> getServiceClass()
	{
		if (Build.VERSION.SDK_INT >= 23)
			return DndModeServiceV23.class;
		else if (Build.VERSION.SDK_INT >= 21) {
			try {
				return Class.forName(DndServiceStrategy.class.getPackage().getName()+".DndModeServiceV21"); // avoid insignificant but unpleasant E/dalvikvm: Could not find class
			} catch (ClassNotFoundException e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public void onServiceConnected(ComponentName n, IBinder binder)
	{
		//Log.d("Minidoro", "DND Service connected");
		// That's the local service, we can cast its IBinder to a concrete class and directly access it
		service = ((DndModeService.DndBinder) binder).getService();
	}

	public boolean isReady() { return service != null; }

	// Rare case or almost never happens
	@Override
	public void onServiceDisconnected(ComponentName componentName)
	{
		//Log.d("Minidoro", "DND Service DISCONNECTED");
		service = null;
	}

	@Override
	public void setDndModeOn()
	{
		if (service != null)    // not connected yet
			service.priorityModeOn();
	}

	@Override
	public void returnUserMode()
	{
		if (service != null)    // not connected yet
			service.returnUserMode();
	}

	@Override
	public DndMode getDndMode()
	{
		if (service == null)    // not connected yet
			return null;
		return service.getDnDMode();
	}

	public String accessSettings() { return service.accessSettings(); }

	public boolean isEnabled() {
		if (service == null)    // not connected yet
			return false;
		return service.isEnabled();
	}
}
