package com.example.projectbat;

import android.util.Log;

public class BeepTimer implements BeepInterface
{
	private int time = 0;
	private int grace = 0;
	private final int gracePeriod;
	private boolean timing = true;
	private boolean beeped = false;
	
	private long time2 = 0;
	
	BeepTimer(int gracePeriod)
	{
		this.gracePeriod = gracePeriod;
	}
	
	@Override
	public void update(int samplesRead, boolean beep)
	{
		synchronized(this)
		{
			time += samplesRead;
			if(grace <= 0)
			{
				if(beep && timing)
				{
					timing = false;
					beeped = true;
					
					Log.i("TIMER", Integer.toString(time*1000/44100) + " "+ Long.toString(System.currentTimeMillis() - time2));
				}
			}
			else
			{
				grace -= samplesRead;
			}
		}
	}
	
	public void restart()
	{
		synchronized(this)
		{
			grace = gracePeriod;
			timing = true;
			beeped = false;
			time = 0;
			time2 = System.currentTimeMillis();
		}
	}
	
	public int elapsed()
	{
		synchronized(this)
		{
			return time;
		}
	}
	
	public boolean beepDetected()
	{
		synchronized(this)
		{
			return beeped;
		}
	}
}
