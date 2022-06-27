package com.github.ympavlov.minidoro.dnd;

public interface DndStrategy
{
	void setDndModeOn();
	void returnUserMode();
	DndMode getDndMode();

	enum DndMode
	{
		SILENT /*no sounds at all*/,
		PRIORITY_ALARMS /*important notifications or alarms only (ours is alarm!)*/,
		NORMAL,
		UNKNOWN
	}
}
