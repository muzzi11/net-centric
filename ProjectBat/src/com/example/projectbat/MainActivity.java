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
	private final String recordFilename = "record"; 
	private static Player player;
	private Recorder recorder;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        player = new Player(this);
        recorder = new Recorder(recordFilename);
        
        Button beepButton = (Button)findViewById(R.id.beep);
        beepButton.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View v)
        	{
        		MainActivity.player.play();
        	}
        });
        
        // Initialize the button to perform device discovery
        Button scanButton = (Button) findViewById(R.id.goBlue);
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
