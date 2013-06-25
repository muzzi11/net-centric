package com.example.projectbat;

public class BeepTimer implements BeepInterface
{
	private boolean isSender = true;
	// the time in samples that accounts consecutive onBeep calls to the same beep signal
	private long gracePeriod = BeepGenerator.beepPeriod * 10;
	private long time;
	private boolean awaitFirst = true;
	
	@Override
	public void onBeep(long timeInSamples)
	{
		if(awaitFirst && isSender)
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
				
				MainActivity.beepGenerator.play();
			}
		}
	}
	
	public void start(boolean isSender)
	{	
		this.isSender = isSender;
		awaitFirst = true;
	}
}
