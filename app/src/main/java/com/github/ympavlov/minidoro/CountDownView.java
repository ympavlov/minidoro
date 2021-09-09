package com.github.ympavlov.minidoro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import static com.github.ympavlov.minidoro.TimeTicker.MINUTE;
import static com.github.ympavlov.minidoro.TimeTicker.SECOND;

public class CountDownView extends TextView implements Observer
{
	private int workTextSizePx, breakTextSizePx;

	PomodoroState timerState;

	public CountDownView(Context context, AttributeSet attrs) { super(context, attrs); }

	public void setWorkTextSizePx(int workTextSizePx)
	{
		this.workTextSizePx = workTextSizePx;
		if (timerState == null || timerState.stage.isWork) {
			PomodoroActivity.setTextSizePx(this, workTextSizePx);
		}
	}

	public void setBreakTextSizePx(int breakTextSizePx)
	{
		this.breakTextSizePx = breakTextSizePx;
		if (timerState != null && !timerState.stage.isWork) {
			PomodoroActivity.setTextSizePx(this, breakTextSizePx);
		}
	}

	void updateView()
	{
		update(timerState, System.currentTimeMillis());
		PomodoroActivity.setTextSizePx(this, timerState.stage.isWork ? workTextSizePx : breakTextSizePx);
		setTextColor(timerState.stage.color);
	}

	@SuppressLint("DefaultLocale") // HH:MI is assumed to be locale-independent
	@Override
	public void update(Observable o, Object arg)
	{
		int left = timerState.tick((Long) arg);
		int m = left / MINUTE;
		int s = (left - m * MINUTE) / SECOND;

		setText(String.format("%2d:%02d", m, s));
	}
}
