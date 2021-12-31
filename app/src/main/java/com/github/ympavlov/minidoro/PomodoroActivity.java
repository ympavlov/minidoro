package com.github.ympavlov.minidoro;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
////import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import com.github.ympavlov.minidoro.dnd.DndManager;
import com.github.ympavlov.minidoro.dnd.RingerModeManager;

import java.util.Observable;
import java.util.Observer;

/*
 * The main app class
 * Consists of presentation logic and set all interaction with PomodoroState
 */
public class PomodoroActivity extends Activity
{
	public static final int MAX_WAIT_USER_RETURN = TimeTicker.MINUTE * 60 * 3; // 3 hours

	private static final int MENU_PREFERENCES = Menu.FIRST;
	private static final int MENU_DEC_QUOTES = MENU_PREFERENCES + 1;
	private static final int MENU_DEC_DASHES = MENU_DEC_QUOTES + 1;
	private static final int MENU_ABOUT = MENU_DEC_DASHES + 1;
	private static final int MENU_STOP = MENU_ABOUT + 1;

	private static final int pomodoroColor = Color.parseColor("#2e9a36");

	public String QUOTE;
	public String DASH;

	private TimeTicker ticker;
	private PomodoroContext pomodoroContext;
	private PomodoroState pomodoroState; // shortcut to pomodoroContext.pomodoroState
	private Observer stateObserver;

	private Initializer timerServiceConnection;
	private Intent timerServiceIntent;
	private Intent prefsIntent;
	private AlertDialog stopDialog;
	private AppPreferences prefs;
	private NotificationManager notificationManager;

	private boolean openedOnAlarm;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//Log.d("Minidoro", "PomodoroActivity is creating");

		// Try to restore state from service
		timerServiceIntent = new Intent(this, PomodoroService.class);
		timerServiceConnection = new Initializer();
		bindService(timerServiceIntent, timerServiceConnection, Context.BIND_AUTO_CREATE);

		// Create view
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		// Customize view
		updateSizesUponScreenMetrics();
		setButtonTheme();

		prefs = new AppPreferences(getPackageName(), getSharedPreferences(getPackageName() + AppPreferences.PREF_KEY, 0));

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		findViewById(R.id.preferences).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view) { showPreferences(); }
		});

		// [7]
		QUOTE = getString(R.string.quote);
		DASH = getString(R.string.dash);

		Button b = findViewById(R.id.quotes);
		b.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view) { updateCounter(view.getId(), QUOTE, pomodoroState.incrementQuotes()); }
		});
		registerForContextMenu(b); // [7a]
		b = findViewById(R.id.dashes);
		b.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view) { updateCounter(view.getId(), DASH, pomodoroState.incrementDashes()); }
		});
		registerForContextMenu(b); // [7a]
	}

	//
	// WORK WITH VIEW
	//

	@SuppressLint("InlinedApi") // [3] In APIs >= 11 make buttons transparent, but selectable
	private void setButtonTheme()
	{
		TypedValue outValue = new TypedValue();
		getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
		if (outValue.resourceId != 0) {
			findViewById(R.id.start).setBackgroundResource(outValue.resourceId);
			findViewById(R.id.startSmall).setBackgroundResource(outValue.resourceId);
			findViewById(R.id.stop).setBackgroundResource(outValue.resourceId);
			findViewById(R.id.dashes).setBackgroundResource(outValue.resourceId);
			findViewById(R.id.quotes).setBackgroundResource(outValue.resourceId);
			findViewById(R.id.preferences).setBackgroundResource(outValue.resourceId);
		}
	}

	/*
	 * All sizes correlate with screen size
	 */
	static void setTextSizePx(TextView v, int s) { v.setTextSize(TypedValue.COMPLEX_UNIT_PX, s); }
	private void setTextSizePx(int id, int s) { setTextSizePx(findViewById(id), s); }
	@SuppressWarnings("deprecation") // getMetrics is deprecated since 30, hope it will work some time
	private void updateSizesUponScreenMetrics()
	{ // [0]
		DisplayMetrics m = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(m);

		TextView currTime = findViewById(R.id.currTime);
		TextView dashes = findViewById(R.id.dashes);
		TextView quotes = findViewById(R.id.quotes);
		TextView tomatos = findViewById(R.id.tomatos);

		// Limit width of three growing areas
		int maxWidth = (int) (m.widthPixels * 0.45);
		dashes.setMaxWidth(maxWidth);
		quotes.setMaxWidth(maxWidth);
		int maxHeight = (int) (m.heightPixels * 0.45);
		dashes.setMaxHeight(maxHeight);
		quotes.setMaxHeight(maxHeight);
		tomatos.setMaxHeight(maxHeight);

		CountDownView cd = findViewById(R.id.countdown);

		//
		// Central text: size depends on orientation
		//

		int s = Math.min(m.heightPixels / 2, m.widthPixels / 3)
				- 5;   // calibrate font size on very small screens (Wear OS)
		cd.setBreakTextSizePx(s);

		cd.setWorkTextSizePx((s * 3) / 4); // Need some space for stop button
		setTextSizePx(R.id.stop, s  * 4 / 9);

		s = s * getResources().getInteger(R.integer.startBtnRelSize) / 6;
		setTextSizePx(R.id.start, s);
		s = s * 2 / 3;
		setTextSizePx(R.id.startSmall, s);

		//
		// Surrounding text: size doesn't depend on orientation
		//

		s = Math.min(m.heightPixels, m.widthPixels) / 5;

		View prefs = findViewById(R.id.preferences);
		ViewGroup.LayoutParams prefsLp = prefs.getLayoutParams();
		prefsLp.height = prefsLp.width = s & ~15;   // assign 2ᴺ to reduce image blur
		prefs.setLayoutParams(prefsLp);

		s = (s * 2) / 3;

		setTextSizePx(tomatos, s);
		setTextSizePx(dashes, s);
		setTextSizePx(quotes, s);

		setTextSizePx(tomatos, s * getResources().getInteger(R.integer.tomatosRelSize) / 6);

		s /= 2;

		setTextSizePx(currTime, s);

		// Also set padding
		s /= 2;
		prefs.setPadding(s, s, s, s);
		dashes.setPadding(s, s, s, s);
		quotes.setPadding(s, s, 2*s, s);
		currTime.setPadding(0, 2*s, s, 0);
		tomatos.setPadding(s, 0, s, 0);

		tomatos.setMaxWidth(m.widthPixels - 13*s); // correlated with currTime and prefs
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

		updateSizesUponScreenMetrics();
	}

	//
	// THE LOGIC
	//
	private boolean isInitialized() { return ticker != null; }

	private void initActivity()
	{
		CountDownView cd = findViewById(R.id.countdown);

		ticker = new TimeTicker(TimeTicker.SECOND);
		ticker.addObserver(findViewById(R.id.currTime));
		ticker.addObserver(cd);

		cd.timerState = pomodoroState;

		findViewById(R.id.start).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				startWorkBreak(pomodoroState.stage.next(pomodoroState, prefs));
				startTicker();
			}
		});

		findViewById(R.id.startSmall).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				startWorkBreak(Stage.BREAK);
				startTicker();
			}
		});
		// [8]
		findViewById(R.id.stop).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View unused)
			{
				onBackPressed();
			}
		});

		stateObserver = new Observer()
		{
			@Override
			public void update(Observable o, Object arg)
			{
				finishWorkBreak();
				showStartScreen();
			}
		};
		pomodoroState.addObserver(stateObserver);
	}

	private void startTicker()
	{
		findViewById(R.id.start).setVisibility(View.GONE);
		findViewById(R.id.startSmall).setVisibility(View.GONE);
		ticker.start();
	}

	private void showCountDownScreen()
	{
		((CountDownView) findViewById(R.id.countdown)).updateView();

		findViewById(R.id.countdownPanel).setVisibility(View.VISIBLE);

		View stopBtn = findViewById(R.id.stop);
		if (pomodoroState.stage.isWork) {
			stopBtn.setVisibility(View.VISIBLE);
			setDNDView(true);
		} else {
			stopBtn.setVisibility(View.GONE);
		}
	}

	@SuppressLint("DefaultLocale") // HH:MI is assumed to be locale-independent
	private void updateStartBtn(int btnId, Stage s)
	{
		TextView b = findViewById(btnId);
		b.setVisibility(View.VISIBLE);
		b.setText(String.format("▶ % 2d:00", prefs.getDuration(s)));
		b.setTextColor(s.color);
	}

	private void updateStartScreen()
	{
		Stage next = pomodoroState.stage.next(pomodoroState, prefs);
		updateStartBtn(R.id.start, next);

		if (next == Stage.LONG_BREAK) // Add short break option
			updateStartBtn(R.id.startSmall, Stage.BREAK);
		else
			findViewById(R.id.startSmall).setVisibility(View.GONE);
	}

	private void showStartScreen()
	{
		updateStartScreen();
		findViewById(R.id.countdownPanel).setVisibility(View.GONE);
	}

	/*
	 * [9]
	 */
	@SuppressWarnings("deprecation") // FLAG_FULLSCREEN is deprecated since 30, hope it will work some time
	private void setDNDView(boolean dnd)
	{
		if (dnd) {
			// hide panel, but show our clock
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			findViewById(R.id.currTime).setVisibility(View.VISIBLE);
			findViewById(R.id.preferences).setVisibility(View.GONE);
		} else {
			// show panel, hide our clock
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			findViewById(R.id.currTime).setVisibility(View.GONE);
			findViewById(R.id.preferences).setVisibility(View.VISIBLE);
		}
	}

	private void startWorkBreak(Stage next)
	{
		pomodoroState.start(next, System.currentTimeMillis(), prefs.getDuration(next));

		// [4a] End up notification that has overridden silent mode
		pomodoroContext.ringerModeManager.returnUserMode();

		if (pomodoroState.stage.isWork) {
			findViewById(R.id.quotes).setEnabled(true);
			findViewById(R.id.dashes).setEnabled(true);

			if (prefs.isDndModeOn()) { // [6]
				pomodoroContext.dndManager.setDndModeOn();
				// I think there's no need dnd service to be started
				//startService(...);
			}
		}

		showCountDownScreen();

		if (timerServiceConnection.getService() != null)
			timerServiceConnection.getService().init(pomodoroContext, prefs);

		notificationManager.cancel(Bell.NOTIFICATION_ID);
	}

	private void finishWorkBreak()
	{
		ticker.stop();

		if (stopDialog != null)
			stopDialog.dismiss();

		updateWorks();

		if (pomodoroState.stage.isWork) {
			findViewById(R.id.quotes).setEnabled(false);
			findViewById(R.id.dashes).setEnabled(false);

			if (prefs.isDndModeOn()) { // [6]
				pomodoroContext.dndManager.returnUserMode();
			}
			setDNDView(false);

		} else if (timerServiceConnection.getService() != null) {
			timerServiceConnection.getService().stopTimer();
		}
	}

	/**
	 * Put string "pppp" or "5×p" (p c times, N×p if N ≥ maxl) into out
	 * @return prefix length
	 */
	private static int repeated(CharSequence p, StringBuilder out, int c, int maxl)
	{
		int prefix = 0;
		char x = '×';
		if (c < maxl) {
			for (int i = 0; i < c; i++)
				out.append(p);
		} else {
			out.append(c).append(x);
			prefix = out.length();
			out.append(p);
		}
		return prefix;
	}

	/*
	 * In Android (unlike HTML) parts of composite symbols could be stylized separately
	 * Let's draw some tomatos! They look cool like medals (of Tomato Honor :-) So I've decided to use symbols not pictures here
	 */
	private static SpannableString pomodorosRepeated(int c)
	{
		String pattern = "ò\u030c";
		StringBuilder b = new StringBuilder();
		int prefixLen = repeated(pattern, b, c, 5);
		if (prefixLen > 0)
			c = 1;

		SpannableString text = new SpannableString(b.toString());
		for (int i = prefixLen; i < prefixLen + c * pattern.length(); i += pattern.length())
			text.setSpan(new ForegroundColorSpan(pomodoroColor), i + 1, i + pattern.length(), 0);

		return text;
	}

	private void updateWorks()
	{
		((TextView) findViewById(R.id.tomatos)).setText(pomodorosRepeated(pomodoroState.works));
	}

	private void updateCounter(int counterId, CharSequence c, int value)
	{
		StringBuilder s = new StringBuilder();

		if (value > 0)
			repeated(c, s, value, 0);
		else
			s.append(c);

		((TextView) findViewById(counterId)).setText(s);
	}

	private void updateCounters()
	{
		updateCounter(R.id.quotes, QUOTE, pomodoroState.getQuotes());
		updateCounter(R.id.dashes, DASH, pomodoroState.getDashes());
	}

	private void removeQuote() { updateCounter(R.id.quotes, QUOTE, pomodoroState.removeQuote()); }
	private void removeDash() { updateCounter(R.id.dashes, DASH, pomodoroState.removeDash()); }

	private void askStopWork()
	{
		DialogInterface.OnClickListener l = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface d, int i)
			{
				if (i == android.content.DialogInterface.BUTTON_POSITIVE) {
					finishWorkBreak();
					pomodoroState.stop();
					updateCounters();
					showStartScreen();
				}
				d.dismiss();
			}
		};
		if (stopDialog == null) {
			stopDialog = new AlertDialog.Builder(PomodoroActivity.this)
					.setMessage(getString(R.string.msgSureToStop))
					.setPositiveButton(R.string.yes, l)
					.setNegativeButton(R.string.no, l).create();
		}
		stopDialog.show();
	}

	private void stopAndQuit()
	{
		if (timerServiceConnection.getService() != null)
			timerServiceConnection.getService().onTaskRemoved(timerServiceIntent);
		notificationManager.cancel(Bell.NOTIFICATION_ID);
		finish();
	}

	private void askStopAndQuit()
	{
		DialogInterface.OnClickListener l = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface d, int i)
			{
				if (i == android.content.DialogInterface.BUTTON_POSITIVE) {
					stopAndQuit();
				}
				d.dismiss();
			}
		};
		new AlertDialog.Builder(PomodoroActivity.this)
				.setMessage(getString(R.string.msgSureToQuit))
				.setPositiveButton(R.string.yes, l)
				.setNegativeButton(R.string.no, l)
				.create().show();
	}

	//
	// Other event handlers
	//

	@Override
	public void onBackPressed()
	{
		if (!pomodoroState.isTimerOn() && pomodoroState.works > 0)
			askStopAndQuit();
		else if (pomodoroState.isTimerOn() && pomodoroState.stage.isWork)
			askStopWork();
		else
			// Hide the activity, but not destroy. If the activity destroyed its alarm intent will be ignored in API 29+
			moveTaskToBack(true);
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		//Log.d("Minidoro", "Activity on pause");

		if (pomodoroContext != null) { // ignore events before initialization
			if (!pomodoroState.stage.isWork) {
				// TODO: dismissState placed in PomodoroService, I think saveState should be bound with it
				if (pomodoroState.works > 0) // [5]
					StateSaver.saveState(this, pomodoroState);

				if (pomodoroState.isTimerOn()) { // [2]
					// Start main service. Otherwise the service may be reinitialized when bound, but not started
					if (Build.VERSION.SDK_INT >= 26)
						startForegroundService(timerServiceIntent);
					else
						startService(timerServiceIntent);

					if (timerServiceConnection.getService() == null) // rare case (see onServiceDisconnected above)
						bindService(timerServiceIntent, timerServiceConnection, Context.BIND_AUTO_CREATE);
					else
						timerServiceConnection.getService().backgroundTimer();
				}
			}

			// [4a]
			pomodoroContext.ringerModeManager.returnUserMode();
		}
	}

	@Override
	protected void onNewIntent(Intent i)
	{
		super.onNewIntent(i);

		if (i.getExtras() != null && i.getExtras().containsKey(Intent.EXTRA_ALARM_COUNT))
			openedOnAlarm = true;
	}

	// [4a] [6]
	private boolean isDndServiceNeeded() { return prefs.isDndModeOn() || prefs.overrideSilent(); }

	@Override
	protected void onResume()
	{
		super.onResume();

		if (pomodoroState != null) {
			if (!pomodoroState.isTimerOn()) {
				// Case: come back from notification that has overridden silent mode
				if (openedOnAlarm) // stay loud for some time
					openedOnAlarm = false;
				else  // return back [4a]
					pomodoroContext.ringerModeManager.returnUserMode();

				// update after PreferencesActivity changed preferences (possibly)
				updateStartScreen();
			}
			pomodoroContext.dndManager.setNeeded(isDndServiceNeeded());
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		//Log.d("Minidoro", "Main activity is destroying");

		pomodoroState.deleteObserver(stateObserver);
		ticker.stop();

		// If user doesn't want to continue revert some global settings changed by us
		pomodoroContext.dndManager.returnUserMode();
		if (timerServiceConnection.getService().getPomodoroContext() == null)   // WA to unbind service to prevent leaks
			pomodoroContext.dndManager.setNeeded(false);

		unbindService(timerServiceConnection);
	}

	//
	// MENUS & PREFERENCES
	//

	// Menu is optional and is duplicated with other buttons. It may be not shown in some devices
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_PREFERENCES, 1, getString(R.string.preferences))
		    .setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, MENU_ABOUT, 1, getString(R.string.menuAboutPomodoro))
	     	.setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, MENU_STOP, 1, getString(R.string.menuQuit))
		    .setIcon(android.R.drawable.ic_delete);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		if (pomodoroState.noCurrQuotes())
			menu.removeItem(MENU_DEC_QUOTES);
		else if (menu.findItem(MENU_DEC_QUOTES) == null)
			menu.add(0, MENU_DEC_QUOTES, 0, getString(R.string.menuRemoveQ))
					.setIcon(android.R.drawable.ic_menu_revert);

		if (pomodoroState.noCurrDashes())
			menu.removeItem(MENU_DEC_DASHES);
		else if (menu.findItem(MENU_DEC_DASHES) == null)
			menu.add(0, MENU_DEC_DASHES, 0, getString(R.string.menuRemoveD))
					.setIcon(android.R.drawable.ic_menu_revert);

		return super.onPrepareOptionsMenu(menu);
	}

	private void showPreferences()
	{
		pomodoroContext.dndManager.setNeeded(false); // force update strategy on resume
		if (prefsIntent == null)
			prefsIntent = new Intent(this, PreferencesActivity.class);
		startActivityForResult(prefsIntent, 0);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case MENU_DEC_QUOTES: // [7a]
				removeQuote();
				return true;
			case MENU_DEC_DASHES: // [7a]
				removeDash();
				return true;
			case MENU_PREFERENCES:
				showPreferences();
				return true;
			case MENU_STOP:
				stopAndQuit();
				return true;
			case MENU_ABOUT:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.pomodorotechnique.com/")));
				return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	// [7a]
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		int text = 0;
		switch (v.getId()) {
			case R.id.quotes:
				if (pomodoroState.noCurrQuotes())
					return;
				text = R.string.menuRemoveQ;
				break;
			case R.id.dashes:
				if (pomodoroState.noCurrDashes())
					return;
				text = R.string.menuRemoveD;
		}
		if (text != 0)
			menu.add(0, v.getId(), 0, getString(text));
	}

	// [7a]
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.quotes:
				removeQuote();
				return true;
			case R.id.dashes:
				removeDash();
				return true;
		}
		return false;
	}

	//
	// INITIALIZATION [5]
	//
	private class Initializer implements ServiceConnection
	{
		private PomodoroService service;
		private PomodoroService getService() { return service; }

		private void initState()
		{
			// [5]
			//Log.d("Minidoro", "Restoring state from file");
			pomodoroState = StateSaver.restoreState(PomodoroActivity.this);

			// Through away old state (older then half day)
			if (pomodoroState != null && pomodoroState.getUntilMillis() < System.currentTimeMillis() - 2 * MAX_WAIT_USER_RETURN)
				pomodoroState = null;

			if (pomodoroState == null)
				pomodoroState = new PomodoroState();

			// [4]
			pomodoroState.addObserver(new Bell(
					prefs, getApplicationContext(), pomodoroContext.ringerModeManager // may be used after the activity destroyed
			));

			pomodoroContext.pomodoroState = pomodoroState;
		}
		private void initContext()
		{
			pomodoroContext = new PomodoroContext();

			pomodoroContext.ringerModeManager = new RingerModeManager((AudioManager) getSystemService(Context.AUDIO_SERVICE), getContentResolver());

			pomodoroContext.dndManager = new DndManager(getApplicationContext(), new RingerModeManager((AudioManager) getSystemService(Context.AUDIO_SERVICE), getContentResolver()));
			pomodoroContext.dndManager.setNeeded(isDndServiceNeeded());
			pomodoroContext.ringerModeManager.setDndManager(pomodoroContext.dndManager);

			initState();
		}

		@Override
		public void onServiceConnected(ComponentName n, IBinder binder)
		{
			//Log.d("Minidoro", "TimerService connected");
			// That's the local service, we can cast its IBinder to a concrete class and directly access it
			service = ((PomodoroService.TimerBinder) binder).getService();

			// Trying to restore state from service or from persistent
			if (pomodoroContext == null) {
				pomodoroContext = service.getPomodoroContext();
				//Log.d("Minidoro", "Restoring context from service");

				if (pomodoroContext == null)
					initContext();

				// shortcut
				pomodoroState = pomodoroContext.pomodoroState;
			}

			pomodoroState.refresh();

			// almost always would be NOT initialized (unless onServiceDisconnected happened)
			if (!isInitialized()) {
				initActivity();

				if (pomodoroState.isTimerOn()) {
					showCountDownScreen();
					startTicker();
				} else {
					showStartScreen();
				}
				updateCounters();
				updateWorks();
			}

			// in case the state was restored from file
			if (pomodoroState.isTimerOn() && !pomodoroState.stage.isWork) {
				service.init(pomodoroContext, prefs);
			}
		}
		// Rare case or almost never happens
		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			//Log.d("Minidoro", "TimerService DISCONNECTED");
			service = null;
		}
	}
}
