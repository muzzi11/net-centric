package com.example.projectbat;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class BeepGenerator
{
	public static final int beepPeriod = 5;
	private final short buffer[] = new short[200 * beepPeriod];
	private final int sampleRate = 44100;
	private final AudioTrack track;
	
	BeepGenerator()
	{	
		//sampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
		
		Log.i("BeepGenerator", Integer.toString(sampleRate));
		
		track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT, buffer.length, AudioTrack.MODE_STATIC);
		
		for(int i = 0; i < buffer.length; ++i)
		{
			buffer[i] = (short) (Math.sin((i+1) * 2.0 * Math.PI / beepPeriod) * Short.MAX_VALUE);
		}
		track.write(buffer, 0, buffer.length);
	}
	
	public void play()
	{
		track.stop();
		
		if(AudioTrack.SUCCESS != track.reloadStaticData())
		{
			Log.e("BeepGenerator", "reload static data failed.");
		}
		
		track.play();
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
		track.stop();
	}
}
