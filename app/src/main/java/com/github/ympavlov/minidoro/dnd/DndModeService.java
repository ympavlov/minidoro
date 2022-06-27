package com.github.ympavlov.minidoro.dnd;

import android.os.Binder;

public interface DndModeService
{
	void setPriorityModeOn();
	void returnUserMode();
	DndStrategy.DndMode getDnDMode();
	String accessSettings();
	boolean isEnabled();

	class DndBinder extends Binder
	{
		private final DndModeService service;
		protected DndBinder(DndModeService s) { service = s; }
		public DndModeService getService() { return service; }
	}
}
