package com.example.projectbat;

import com.example.projectbat.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity
{	
	private static BeepGenerator beepGenerator;
	private static StreamingRecorder streamingRecorder;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);        
        
        beepGenerator = new BeepGenerator();
        streamingRecorder = new StreamingRecorder();
        Thread t = new Thread(new BeepDetector(streamingRecorder));
        t.setDaemon(true);
        t.start();
        
        final Button beepButton = (Button)findViewById(R.id.beep);
        beepButton.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View v)
        	{
        		//MainActivity.streamingRecorder.start();
        		MainActivity.beepGenerator.play();
        		/*
        		short[] buffer = new short[11025];
				MainActivity.streamingRecorder.read(buffer, buffer.length);
				MainActivity.streamingRecorder.stop();
				
    			Bundle extra = new Bundle();
    			extra.putShortArray("data", buffer);
    			
    			Intent intent = new Intent(MainActivity.this, HistogramActivity.class);
    			intent.putExtras(extra);
    			startActivity(intent);
    			*/
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
    protected void onDestroy()
    {
    	super.onDestroy();
    	streamingRecorder.stop();
    }
        
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
