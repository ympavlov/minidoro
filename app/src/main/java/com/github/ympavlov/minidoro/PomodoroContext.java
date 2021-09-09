package com.github.ympavlov.minidoro;

import com.github.ympavlov.minidoro.dnd.DndManager;
import com.github.ympavlov.minidoro.dnd.RingerModeManager;

/**
 * This class encapsulates all the app state (logic and auxiliary things)
 */
public class PomodoroContext
{
	PomodoroState pomodoroState;
	RingerModeManager ringerModeManager;
	DndManager dndManager;
}
