package com.example.projectbat;

import java.util.Timer;
import java.util.TimerTask;

import com.example.projectbat.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity
{
	private final String recordFilename = "record.3gp";
	//private static Player player;
	private static Recorder recorder;
	
	private static BeepGenerator beepGenerator;
	private static StreamingRecorder streamingRecorder;
	private static Timer timer;
	private long time = 0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //player = new Player(this);
        recorder = new Recorder(this, recordFilename);
        beepGenerator = new BeepGenerator();
        streamingRecorder = new StreamingRecorder();
        
        
        final Button recButton = (Button)findViewById(R.id.record);
        recButton.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View v)
        	{
        		final Button button = (Button)v;
        		if(button.getText().equals("Rec"))
        		{
        			button.setText("Stop");
        			MainActivity.recorder.start();
        		}
        		else
        		{
        			button.setText("Rec");
        			MainActivity.recorder.stop();
        			Intent intent = new Intent(MainActivity.this, HistogramActivity.class);
        			startActivity(intent);
        		}
        	}
        });
        
        final Button beepButton = (Button)findViewById(R.id.beep);
        beepButton.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View v)
        	{
        		timer = new Timer(true);
        		MainActivity.streamingRecorder.start();
        		time = System.currentTimeMillis();
        		MainActivity.beepGenerator.play();
        		
        		timer.schedule(new TimerTask()
        		{
        			@Override
        			public void run()
        			{
        				Log.i("generator time", Long.toString(System.currentTimeMillis() - time));
        				MainActivity.streamingRecorder.stop();
        				short[] buffer = MainActivity.streamingRecorder.getBuffer();
        				int size = MainActivity.streamingRecorder.numBytesRead();
            			
        				Log.i("Main", Integer.toString(size));
        				
            			Bundle extra = new Bundle();
            			extra.putShortArray("data", buffer);
            			extra.putInt("size", size);
            			
            			Intent intent = new Intent(MainActivity.this, HistogramActivity.class);
            			intent.putExtras(extra);
            			startActivity(intent);
        			}
        		}, 100);
        	}
        });
        
        // Initialize the button to perform device discovery
        final Button scanButton = (Button) findViewById(R.id.goBlue);
        scanButton.setOnClickListener(new OnClickListener() 
        {       	
            public void onClick(View v) 
            {
            	Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
            	startActivity(intent);
            }
        });      
    }    
        
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
