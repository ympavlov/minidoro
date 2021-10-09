package com.github.ympavlov.minidoro;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewAssertion;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.*;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;

@Ignore("It runs up the service so breaks other tests. Should be run separately") // TODO
@RunWith(AndroidJUnit4.class)
public class BreakEndedRestoreFromFileTest
{
	@Rule
	public ActivityScenarioRule<PomodoroActivity> activityActivityTestRule = new ActivityScenarioRule<>(PomodoroActivity.class);

	@Before
	public void prepareFile()
	{
		Context ctx = ApplicationProvider.getApplicationContext();

		PomodoroState s = new PomodoroState();
		int dur = 1;
		// Should be ended already
		s.start(Stage.BREAK, System.currentTimeMillis() - dur * TimeTicker.MINUTE - 100, dur);
		s.works = 2;

		StateSaver.saveState(ctx, s);
	}

	@Test
	public void restoreState()
	{
		final ViewAssertion DISPLAYED = matches(isDisplayed());
		final ViewAssertion NOT_DISPLAYED = matches(not(isDisplayed()));

		onView(withId(R.id.countdownPanel)).check(NOT_DISPLAYED);
		onView(withId(R.id.countdown)).check(NOT_DISPLAYED);
		onView(withId(R.id.stop)).check(NOT_DISPLAYED);
		onView(withId(R.id.start)).check(DISPLAYED);
		onView(withId(R.id.startSmall)).check(NOT_DISPLAYED);
	}

	@After
	public void dismiss()
	{
		Context ctx = ApplicationProvider.getApplicationContext();
		StateSaver.dismissState(ctx);
	}
}