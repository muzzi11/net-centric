package com.example.projectbat;

import java.nio.ByteBuffer;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class StreamingRecorder
{
	// supposedly supported by all devices
	private final int sampleRate = 44100;
	private AudioRecord record;
	private int offset = 0;
	private int bufferSize;
	
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
		offset = 0;
		try { record.stop(); }
		catch(IllegalStateException e) { Log.e("StreamingRecorder", e.getMessage()); }
	}
	
	/**
	 * IMPORTANT: A single sample takes 2 bytes.
	 * 
	 * Blocks until buffer has at least 'sizeInBytes' elements filled. Buffer must be direct.
	 * @param buffer Must be large enough to contain 2*'sizeInBytes' elements.
	 * @param sizeInBytes Minimum size to be read, might be more.
	 * @return Amount of elements filled in 'buffer'
	 */
	public final int read(ByteBuffer buffer, int sizeInBytes)
	{
		assert(buffer.capacity() >= (2*sizeInBytes));
		
		int currentSize = 0;
		while(currentSize < sizeInBytes)
		{
			buffer.position(currentSize);
			final int shortsRead = record.read(buffer, offset);
			currentSize += shortsRead;
			offset += shortsRead;
			offset %= bufferSize;
		}
		
		return currentSize;
	}
}
