package com.github.ympavlov.minidoro;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Observable;
import java.util.Observer;

/*
 * [9]
 */
class Clock extends TextView implements Observer
{
	private final DateFormat timeFormat;

	public Clock(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		timeFormat = android.text.format.DateFormat.getTimeFormat(context);
	}

	@Override
	public void update(Observable o, Object timestamp) { setText(timeFormat.format(timestamp)); }
}
