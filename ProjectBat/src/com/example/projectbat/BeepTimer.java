package com.example.projectbat;

import android.util.Log;

public class BeepTimer implements BeepInterface
{
	// the time in samples that accounts consecutive onBeep calls to the same beep signal
	private long gracePeriod = BeepGenerator.beepPeriod * 10;
	private long time;
	private boolean awaitFirst = true;
	
	@Override
	public void onBeep(long timeInSamples)
	{
		if(awaitFirst)
		{
			awaitFirst = false;
			time = timeInSamples;
		}
		else
		{
			if(timeInSamples - time > gracePeriod)
			{
				awaitFirst = true;
				time = timeInSamples - time;
				
				Log.i("Timer", Long.toString(time));
			}
		}
	}
	
	public void start()
	{
		awaitFirst = true;
	}
}
