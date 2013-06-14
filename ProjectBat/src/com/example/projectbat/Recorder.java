package com.example.projectbat;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;


public class Recorder
{
	private MediaRecorder recorder = new MediaRecorder();
	private final File file;
	
	public Recorder(Context context, String filename)
	{
		file = new File(context.getFilesDir() + File.separator + filename);
		file.setReadable(true, false);
	}
	
	public boolean start()
	{
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(file.getAbsolutePath());
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
