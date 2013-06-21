package com.example.projectbat;

import android.util.Log;
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class BeepDetector implements Runnable 
{
	private final int fftIndex;
	private final short[] buffer = new short[50];
	private final float[] signal = new float[2 * buffer.length];
	private final float[] beepFFT;
	private final FloatFFT_1D fft = new FloatFFT_1D(buffer.length);
	private final StreamingRecorder recorder;
	
	BeepDetector(StreamingRecorder recorder)
	{
		this.recorder= recorder; 
		
		fftIndex = 2 * buffer.length / BeepGenerator.beepPeriod + 1;
		
		beepFFT = BeepGenerator.generateSignal(signal.length);
		fft.realForward(beepFFT);
		
		recorder.start();
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
			
			double strength = Math.sqrt(signal[fftIndex]*signal[fftIndex] + signal[fftIndex+1]*signal[fftIndex+1]);
			if(strength > 2.0)
			{
				Log.i("Detector", "BEEP! "+Double.toString(strength));
			}
		}
	}
}
