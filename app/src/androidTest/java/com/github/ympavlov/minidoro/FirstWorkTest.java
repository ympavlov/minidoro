package com.github.ympavlov.minidoro;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewAssertion;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.CoreMatchers.not;

@RunWith(AndroidJUnit4.class)
public class FirstWorkTest
{
	@Rule
	public ActivityScenarioRule<PomodoroActivity> activityActivityTestRule = new ActivityScenarioRule<>(PomodoroActivity.class);

	@Before
	public void prepareFile()
	{
		Context ctx = ApplicationProvider.getApplicationContext();
		StateSaver.dismissState(ctx);
	}

	@Test
	public void firstWorkStart()
	{
		final ViewAssertion DISPLAYED = matches(isDisplayed());
		final ViewAssertion NOT_DISPLAYED = matches(not(isDisplayed()));

		onView(withId(R.id.countdownPanel)).check(NOT_DISPLAYED);
		onView(withId(R.id.countdown)).check(NOT_DISPLAYED);
		onView(withId(R.id.stop)).check(NOT_DISPLAYED);
		onView(withId(R.id.start)).check(DISPLAYED);
		onView(withId(R.id.startSmall)).check(NOT_DISPLAYED);

		onView(withId(R.id.start)).check(matches(isEnabled()));

		onView(withId(R.id.start)).perform(click());

		onView(withId(R.id.countdownPanel)).check(DISPLAYED);
		onView(withId(R.id.countdown)).check(DISPLAYED);
		onView(withId(R.id.stop)).check(DISPLAYED);
		onView(withId(R.id.start)).check(NOT_DISPLAYED);
		onView(withId(R.id.startSmall)).check(NOT_DISPLAYED);
	}
}