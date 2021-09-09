package com.github.ympavlov.minidoro;

import android.content.Context;
import android.util.Log;

import java.io.*;

/*
 * [5] Utility class to save the state when on pause (the state has no changes when on pause)
 */
class StateSaver
{
	private static File getFile(Context ctx)
	{
		return new File(ctx.getCacheDir().getAbsolutePath() + File.separator + PomodoroState.class.getSimpleName());
	}

	static void saveState(Context ctx, PomodoroState state)
	{
		try (FileOutputStream fileStream = new FileOutputStream(getFile(ctx))) {
			ObjectOutputStream objStream = new ObjectOutputStream(fileStream);
			objStream.writeObject(state);
		} catch (IOException e) {
			Log.w("Minidoro", "Unable to save state: " + e.getMessage());
		}
	}

	static PomodoroState restoreState(Context ctx)
	{
		File f = getFile(ctx);
		if (!f.exists())
			return null;
		try (FileInputStream fileStream = new FileInputStream(getFile(ctx))) {
			ObjectInputStream objStream = new ObjectInputStream(fileStream);
			return (PomodoroState) objStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			Log.w("Minidoro", "Unable to restore state: " + e.getMessage());
			return null;
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	static void dismissState(Context ctx) { getFile(ctx).delete(); }
}
