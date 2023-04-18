package com.github.ympavlov.minidoro;

import java.io.Serializable;
import java.util.Observable;

/*
 * This class encapsulates all the pomodoro logic state
 * Other app classes' states can be recreated from this state
 *
 * State dependent classes should be subscribed from this
 */
public class PomodoroState extends Observable implements Serializable
{
	Stage stage;
	int works;

	private int quotes, dashes; // current work counters [7]
	private int allQuotes, allDashes; // all works counters
	private boolean isTimerOn;
	private long untilMillis;
	private int lastLongBreak; // work number last long break happened after

	public PomodoroState() { this.stage = Stage.BREAK; }

	public boolean noCurrQuotes() { return quotes <= 0; }
	public boolean noCurrDashes() { return dashes <= 0; }

	public int getQuotes() { return quotes + allQuotes; }
	public int getDashes() { return dashes + allDashes; }

	public int incrementQuotes() { return ++quotes + allQuotes; }
	public int incrementDashes() { return ++dashes + allDashes; }

	public int removeQuote()
	{
		if (quotes > 0)
			return --quotes + allQuotes;
		return allQuotes;
	}

	public int removeDash()
	{
		if (dashes > 0)
			return --dashes + allDashes;
		return allDashes;
	}

	public boolean isTimerOn() { return isTimerOn; }

	/*
	 * Get count of works happened after last long break, <b>including</b> current (if it's going on)
	 */
	public int getWorksSinceLastLongBreak() { return works + (isTimerOn() ? 1 : 0) - lastLongBreak; }

	/*
	 * Change status (work to pause and pause to work) and start period
	 */
	@SuppressWarnings("IntegerMultiplicationImplicitCastToLong")
	public void start(Stage next, long startTime, int duration, int longBreakVariance)
	{
		if (next == Stage.WORK && (startTime - untilMillis >= TimeTicker.MINUTE * longBreakVariance)) {
			lastLongBreak = works;
		}
		stage = next;
		untilMillis = startTime + duration * TimeTicker.MINUTE;
		isTimerOn = true;
	}

	long getUntilMillis() { return untilMillis; }

	/*
	 * [8] Stop current work and cancel current counters
	 */
	public void stopWork()
	{
		if (stage.isWork) {
			stage = Stage.BREAK; // break ended, no matter long or short
			isTimerOn = false;

			quotes = 0;
			dashes = 0;

			// do not notify observers
		}
	}

	/**
	 * Update state in time
	 * @return time left in millis
	 */
	public int refresh()
	{
		return tick(System.currentTimeMillis());
	}

	/**
	 * Update state in time
	 * @return time left in millis
	 */
	int tick(long currTime)
	{
		if (untilMillis <= currTime) {
			if (isTimerOn)
				ended();
			return 0;
		}
		return (int) (untilMillis - currTime);
	}

	private void ended()
	{
		isTimerOn = false;

		if (stage.isWork)
			works++;

		allQuotes += quotes;
		quotes = 0;
		allDashes += dashes;
		dashes = 0;

		setChanged();
		notifyObservers();
	}
}
