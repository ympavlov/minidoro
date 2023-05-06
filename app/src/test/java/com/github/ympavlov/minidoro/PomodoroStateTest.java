package com.github.ympavlov.minidoro;

import org.junit.Test;

import java.util.Observable;
import java.util.Observer;

import static com.github.ympavlov.minidoro.Stage.*;
import static com.github.ympavlov.minidoro.TimeTicker.*;
import static org.junit.Assert.*;

public class PomodoroStateTest
{
	private final long START_TIME = 100000000L;
	private boolean observerWasNotified;

	@Test
	public void startFinishWork()
	{
		final int workDuration = 1;

		PomodoroState s = new PomodoroState();

		assertFalse(s.isTimerOn());

		assertEquals(s.getDashes(), 0);
		assertEquals(s.getQuotes(), 0);
		assertTrue(s.noCurrDashes());
		assertTrue(s.noCurrQuotes());
		assertEquals(s.works, 0);
		assertEquals(s.getWorksSinceLastLongBreak(), 0);

		s.addObserver(new Observer()
		{
			@Override
			public void update(Observable observable, Object o) { observerWasNotified = true; }
		});

		s.start(WORK, START_TIME, workDuration, 0);

		assertTrue(s.isTimerOn());
		assertFalse(observerWasNotified);
		assertEquals(s.works, 0);

		s.tick(START_TIME + SECOND);

		assertTrue(s.isTimerOn());
		assertFalse(observerWasNotified);
		assertEquals(s.works, 0);

		s.tick(START_TIME + workDuration * MINUTE + SECOND);

		assertFalse(s.isTimerOn());
		assertTrue(observerWasNotified);
		assertEquals(s.works, 1);
		assertEquals(s.getWorksSinceLastLongBreak(), 1);
	}

	private PomodoroState doWorkBreakWork(Stage breakType, int breakActually)
	{
		//Using default period values
		final int longBreakVariance = LONG_BREAK.defaultDuration - BREAK.defaultDuration;

		long currTime = START_TIME;
		PomodoroState s = new PomodoroState();

		s.start(WORK, currTime, WORK.defaultDuration, longBreakVariance);
		s.tick(currTime += (long) WORK.defaultDuration * MINUTE + 1);

		s.start(breakType, currTime, breakType.defaultDuration, longBreakVariance);
		s.tick(currTime += (long) breakActually * MINUTE + 1);

		s.start(WORK, currTime, WORK.defaultDuration, longBreakVariance);

		return s;
	}
	@Test
	public void worksSinceLastLongBreak_usual()
	{
		PomodoroState s = doWorkBreakWork(BREAK, BREAK.defaultDuration);

		assertEquals(s.getWorksSinceLastLongBreak(), 2);
	}
	@Test
	public void worksSinceLastLongBreak_long()
	{
		PomodoroState s = doWorkBreakWork(LONG_BREAK, LONG_BREAK.defaultDuration);

		assertEquals(s.getWorksSinceLastLongBreak(), 1);
	}
	@Test
	public void worksSinceLastLongBreak_shortWasActuallyLong()
	{
		PomodoroState s = doWorkBreakWork(BREAK, LONG_BREAK.defaultDuration);

		assertEquals(s.getWorksSinceLastLongBreak(), 1);
	}
}
