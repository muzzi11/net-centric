package com.example.projectbat;

public interface BeepInterface
{
	/**
	 * Callback function for when a beep occurs.
	 * @param timeInSamples Time in samples since program start.
	 */
	public void onBeep(long timeInSamples);
}
