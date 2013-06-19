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
	private final short buffer[];
	private int bytesRead = 0;
	
	StreamingRecorder()
	{
		int bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		// Record for a minimum of half a second
		if(sampleRate / 2 > bufferSize) bufferSize = sampleRate / 2;
		buffer = new short[bufferSize];
		
		Log.i("StreamingRecorder", Integer.toString(bufferSize));
		
		try
		{
			record = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
					AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer.length);
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
		bytesRead = record.read(buffer, 0, buffer.length);
		try { record.stop(); }
		catch(IllegalStateException e) { Log.e("StreamingRecorder", e.getMessage()); }
	}
	
	public int numBytesRead()
	{
		return bytesRead;
	}
	
	public final short[] getBuffer()
	{
		return buffer;
	}
}
