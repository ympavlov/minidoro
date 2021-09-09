package com.github.ympavlov.minidoro;

import org.junit.Test;

import java.util.Observable;
import java.util.Observer;

import static org.junit.Assert.*;

public class PomodoroStateTest
{
	private boolean observerWasNotified;

	@Test
	public void startFirstWork()
	{
		final long startTime = System.currentTimeMillis();
		final int workDuration = 1;

		PomodoroState s = new PomodoroState();

		assertFalse(s.isTimerOn());

		assertEquals(s.getDashes(), 0);
		assertEquals(s.getQuotes(), 0);
		assertTrue(s.noCurrDashes());
		assertTrue(s.noCurrQuotes());
		assertEquals(s.works, 0);
		assertEquals(s.getWorksSinceLastLongBreak(), 0);

		s.addObserver(new PomodoroStateTestObserver());

		s.start(Stage.WORK, startTime, workDuration);

		assertTrue(s.isTimerOn());
		assertFalse(observerWasNotified);
		assertEquals(s.works, 0);

		s.tick(startTime + workDuration * TimeTicker.SECOND);

		assertTrue(s.isTimerOn());
		assertFalse(observerWasNotified);
		assertEquals(s.works, 0);

		s.tick(startTime + TimeTicker.MINUTE);

		assertFalse(s.isTimerOn());
		assertTrue(observerWasNotified);
		assertEquals(s.works, 1);
		assertEquals(s.getWorksSinceLastLongBreak(), 1);
	}

	private class PomodoroStateTestObserver implements Observer
	{
		@Override
		public void update(Observable observable, Object o) { observerWasNotified = true; }
	}
}
