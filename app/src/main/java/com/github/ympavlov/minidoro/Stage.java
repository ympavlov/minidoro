package com.github.ympavlov.minidoro;

public enum Stage
{
	// The colors are assumed to be colorblind-friendly. Also they are chosen so to be bright on the e-book "paper" screen
	WORK("workDuration", 25, 0xfff29268, true),
	BREAK("breakDuration", 5, 0xff79f0dc, false),
	LONG_BREAK("longBreakDuration", 15, 0xff79f0dc, false);

	public final String durationPref;
	public final int defaultDuration;
	public final int color;
	public final boolean isWork;

	Stage(String durationPref, int defaultDuration, int color, boolean isWork)
	{
		this.durationPref = durationPref;
		this.defaultDuration = defaultDuration;
		this.color = color;
		this.isWork = isWork;
	}

	public Stage next(PomodoroState s, AppPreferences prefs)
	{
		if (!isWork)
			return WORK;
		if (prefs.isLongBreaksOn() && s.getWorksSinceLastLongBreak() >= prefs.getLongBreaksPeriodicity())
			return LONG_BREAK;
		return BREAK;
	}

}
