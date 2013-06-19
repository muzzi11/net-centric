package com.example.projectbat;

import java.nio.ByteBuffer;
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
	private static BeepGenerator beepGenerator;
	private static StreamingRecorder streamingRecorder;
	private static Timer timer;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);        
        
        beepGenerator = new BeepGenerator();
        streamingRecorder = new StreamingRecorder();
        
        final Button beepButton = (Button)findViewById(R.id.beep);
        beepButton.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View v)
        	{
        		timer = new Timer(true);
        		MainActivity.streamingRecorder.start();
        		MainActivity.beepGenerator.play();
        		
        		timer.schedule(new TimerTask()
        		{
        			@Override
        			public void run()
        			{
        				ByteBuffer buffer = ByteBuffer.allocateDirect(44100);
        				final int samples = MainActivity.streamingRecorder.read(buffer, buffer.capacity() / 2) / 2;
        				MainActivity.streamingRecorder.stop();
            			
        				Log.i("Main", Integer.toString(samples));
        				
            			Bundle extra = new Bundle();
            			extra.putShortArray("data", buffer.asShortBuffer().array());
            			extra.putInt("samples", samples);
            			
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
