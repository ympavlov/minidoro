package com.github.ympavlov.minidoro.nofication;

import android.os.Build;
import com.github.ympavlov.minidoro.R;

public class NotificationIcons
{
	public static final int NPARTS = 5;

	public static int getBreakIcon(int n)
	{
		if (Build.VERSION.SDK_INT < 21) {
			switch (n) {
				case 0:
					return R.drawable.break0;
				case 1:
					return R.drawable.break1;
				case 2:
					return R.drawable.break2;
				case 3:
					return R.drawable.break3;
				case 4:
					return R.drawable.break4;
			}
			return R.drawable.break5;
		} else {
			switch (n) {
				case 0:
					return R.drawable.break0w;
				case 1:
					return R.drawable.break1w;
				case 2:
					return R.drawable.break2w;
				case 3:
					return R.drawable.break3w;
				case 4:
					return R.drawable.break4w;
			}
			return R.drawable.break5w;
		}
	}
}
