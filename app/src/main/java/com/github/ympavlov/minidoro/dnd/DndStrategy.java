package com.github.ympavlov.minidoro.dnd;

public interface DndStrategy
{
	void setDndModeOn();
	void returnUserMode();
	DndMode getDndMode();

	enum DndMode
	{
		SILENT /*only alarms or no sounds at all*/,
		PRIORITY /*important notifications INCLUDING OURS*/,
		NORMAL,
		UNKNOWN
	}
}
