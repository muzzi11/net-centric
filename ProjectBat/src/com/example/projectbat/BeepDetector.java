package com.example.projectbat;

import java.nio.ByteBuffer;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class BeepDetector implements Runnable 
{
	private final ByteBuffer buffer = ByteBuffer.allocateDirect(200);
	private final StreamingRecorder recorder = new StreamingRecorder();
	
	BeepDetector()
	{
		recorder.start();
	}
	
	@Override
	public void run()
	{
		final int size = recorder.read(buffer, buffer.capacity() / 2);
		
		FloatFFT_1D fft = new FloatFFT_1D(size);
	}
}
