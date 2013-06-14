package com.example.projectbat;

import java.util.Arrays;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class StreamingRecorder
{
	// supposedly supported by all devices
	private final int sampleRate = 44100;
	private final AudioRecord record;
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
		
		record = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer.length);
	}
	
	public void start()
	{
		record.startRecording();
	}
	
	public void stop()
	{
		bytesRead = record.read(buffer, 0, buffer.length); 
		record.stop();
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
