package com.example.projectbat;

import android.util.Log;
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class BeepDetector implements Runnable 
{
	private final int fftIndex;
	private final short[] buffer = new short[5 * BeepGenerator.beepPeriod];
	private final float[] signal = new float[2 * buffer.length];
	private final FloatFFT_1D fft = new FloatFFT_1D(buffer.length);
	
	private final BeepInterface beepInterface;
	
	private final StreamingRecorder recorder;
	
	BeepDetector(StreamingRecorder recorder, BeepInterface beepInterface)
	{
		this.recorder = recorder;
		this.beepInterface = beepInterface;
		recorder.start();
		
		fftIndex = 2 * buffer.length / BeepGenerator.beepPeriod + 1;
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			recorder.read(buffer, buffer.length);
			
			for(int i = 0; i < buffer.length; ++i)
			{
				signal[i] = buffer[i] / (float) Short.MAX_VALUE;
			}
			
			fft.realForwardFull(signal);
			
			final double strength = signal[fftIndex]*signal[fftIndex] + signal[fftIndex+1]*signal[fftIndex+1];
			boolean beep = strength > 1.0;
			if(beep)
			{
				Log.i("Detector", "BEEP! "+Double.toString(strength));
				beepInterface.onBeep(recorder.elapsed());
			}
		}
	}
}
