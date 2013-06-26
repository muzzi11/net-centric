package com.example.projectbat;

public class BeepTimer implements BeepInterface
{
	private boolean isListener = false, timing = false;
	// the time in samples that accounts consecutive onBeep calls to the same beep signal
	private long gracePeriod = BeepGenerator.beepPeriod * 350;
	private long time;
	private boolean awaitFirst = true;
	private BeepTimerListener listener = null;
	
	@Override
	public void onBeep(long timeInSamples)
	{
		if(!timing) return;
		
		if(isListener && awaitFirst) MainActivity.beepGenerator.play();
		
		if(awaitFirst)
		{
			awaitFirst = false;
			time = timeInSamples;
		}
		else
		{
			if(timeInSamples - time > gracePeriod)
			{
				timing = false;
				awaitFirst = true;
				time = timeInSamples - time;
				
				if(listener != null) listener.timeMeasured(time, isListener);
			}
		}
	}
	
	public void start(boolean isListener)
	{	
		this.isListener = isListener;
		awaitFirst = true;
		timing = true;
	}
	
	public void registerListener(BeepTimerListener listener)
	{
		this.listener = listener;
	}
}
