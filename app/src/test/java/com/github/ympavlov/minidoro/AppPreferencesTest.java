package com.github.ympavlov.minidoro;

import android.content.SharedPreferences;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class AppPreferencesTest
{
	private void testRestoreIncorrectValue(String value)
	{
		Stage s = Stage.WORK;

		SharedPreferences p = Mockito.mock(SharedPreferences.class);

		when(p.getString(s.durationPref, Integer.toString(s.defaultDuration))).thenReturn(value);

		AppPreferences ap = new AppPreferences(AppPreferencesTest.class.getPackage().getName(), p);

		assertEquals(ap.getDuration(s), s.defaultDuration);
	}

	@Test
	public void testRestoreZero()
	{
		testRestoreIncorrectValue("0");
	}

	@Test
	public void testRestoreIncorrectNumberFloat()
	{
		testRestoreIncorrectValue(".0");
	}
}
