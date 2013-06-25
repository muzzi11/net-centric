package com.example.projectbat;

public interface BeepTimerListener
{
	/**
	 * Callback for when the time between two beeps has been measured.
	 * @param timeInSamples The time difference in samples.
	 * @param isListener True if the timer was on the listener side, false if
	 * on the sending side.
	 */
	public void timeMeasured(long timeInSamples, boolean isListener);
}
