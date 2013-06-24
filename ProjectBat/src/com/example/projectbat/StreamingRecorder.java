package com.example.projectbat;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class StreamingRecorder
{
	// supposedly supported by all devices
	private final int sampleRate = 44100;
	private AudioRecord record;
	private int bufferSize;
	
	private long samples = 0;
	
	StreamingRecorder()
	{
		bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		// Hold recording data for a minimum of half a second
		if(sampleRate / 2 > bufferSize) bufferSize = sampleRate / 2;
		
		Log.i("StreamingRecorder", Integer.toString(bufferSize));
		
		try
		{
			record = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
					AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
		}
		catch(IllegalArgumentException e)
		{
			Log.e("StreamingRecorder", e.getMessage());
		}
	}
	
	public void start()
	{
		try { record.startRecording(); }
		catch(IllegalStateException e) { Log.e("StreamingRecorder", e.getMessage()); }
	}
	
	public void stop()
	{
		try { record.stop(); }
		catch(IllegalStateException e) { Log.e("StreamingRecorder", e.getMessage()); }
	}
	
	/**
	 * @return The amount of samples that have been read since construction.
	 */
	public long elapsed()
	{
		synchronized(this)
		{
			return samples;
		}
	}
	
	/**
	 * Blocks until buffer is filled with sizeInShorts elements.
	 * @param buffer Needs to be larger than or equal to sizeInShorts.
	 * @param sizeInShorts Amount of samples to be read.
	 */
	public final void read(short[] buffer, int sizeInShorts)
	{
		assert(buffer.length >= sizeInShorts);
		
		int currentSize = 0;
		while(currentSize < sizeInShorts)
		{
			final int shortsRead = record.read(buffer, currentSize, sizeInShorts - currentSize);
			currentSize += shortsRead;
		}
		
		synchronized(this)
		{
			samples += currentSize;
		}
	}
}
