package com.example.projectbat;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class BeepDetector implements Runnable 
{
	private final short[] buffer = new short[50];
	private final FloatFFT_1D fft = new FloatFFT_1D(buffer.length);
	private final StreamingRecorder recorder = new StreamingRecorder();
	
	BeepDetector()
	{
		recorder.start();
	}
	
	@Override
	public void run()
	{
		recorder.read(buffer, buffer.length);
	}
}
