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
	private final String recordFilename = "record.3gp";
	private static Player player;
	private static Recorder recorder;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        player = new Player(this);
        recorder = new Recorder(this, recordFilename);
        
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
        		}
        	}
        });
        
        final Button beepButton = (Button)findViewById(R.id.beep);
        beepButton.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View v)
        	{
        		MainActivity.player.play();
        	}
        });
        
        // Initialize the button to perform device discovery
        final Button scanButton = (Button) findViewById(R.id.goBlue);
        scanButton.setOnClickListener(new OnClickListener() 
        {       	
            public void onClick(View v) 
            {
            	Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
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
