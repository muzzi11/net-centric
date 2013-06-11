package com.example.projectbat;

import java.io.IOException;

import android.media.MediaRecorder;
import android.util.Log;

public class Recorder
{
	private MediaRecorder recorder = new MediaRecorder();
	private final String filename; 
	
	public Recorder(String filename)
	{
		this.filename = filename;
	}
	
	public boolean start()
	{
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(filename);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        
        try
        {
        	recorder.prepare();
        }
        catch(IOException e)
        {
        	Log.e("Recorder", e.getMessage());
        	return false;
        }
        
		recorder.start();
		
		return true;
	}
	
	public void stop()
	{
		recorder.stop();
		recorder.reset();
	}
}
