package com.example.projectbat;

import java.util.ArrayList;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class BluetoothActivity extends Activity implements BluetoothInterface
{
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_DISCOVERABLE = 2;
	
	private BluetoothAdapter btAdapter;	
	private BluetoothService btService;
	
	private final ArrayList<BluetoothDevice> foundDevices = new ArrayList<BluetoothDevice>();
	private ArrayList<String> addresses = new ArrayList<String>();
	private final ArrayList<String> pairedData = new ArrayList<String>();
	
	private ArrayAdapter<String> pairedAdapter;
	private ArrayAdapter<String> addressAdapter;
	
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        final String action = intent.getAction();

	        if (BluetoothDevice.ACTION_FOUND.equals(action))
	        {
	            // Get the BluetoothDevice object from the Intent
	            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            final String name = device.getName();
	            final String address = device.getAddress();
	            
	            //if( name.equals("projectThunder") && !foundDevices.contains(device) ) foundDevices.add(device);            	
	            if (name.equals("projectThunder") && !addresses.contains(address) )
	            {
	            	foundDevices.add(device);
	            	btAdapter.cancelDiscovery();
	            }
	        }	        
	        else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
	        {	        	
	        	Log.i("Bluetooth", "Discovery finished");
	        	
	        	if (foundDevices.isEmpty())	        	
	        		btAdapter.startDiscovery();	        	
	        	else	        	      
	        		if ( !btService.connect( foundDevices.get(0) ) )
	        		{
	        			foundDevices.clear();
	        			btAdapter.startDiscovery();
	        		}
	        }
	    }
	};
	
	// Interface functions	
	public void addPairedDevice(final String address)
	{
		BluetoothActivity.this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				pairedData.add(address);
    			pairedAdapter.notifyDataSetChanged();
			}
		});
	}
	public void updateDevices(final ArrayList<String> updatedList)
	{
		BluetoothActivity.this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				addresses.clear();
				addresses.addAll(updatedList);				
    			addressAdapter.notifyDataSetChanged();
			}
		});	
	}
	public void displayMessage(final String message)
	{
		BluetoothActivity.this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				TextView textView = (TextView) findViewById(R.id.receivedMsg);
				textView.setText(message);
			}
		});		
	}	
	public void exit()
	{
		finish();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth);
		
		addressAdapter = new ArrayAdapter<String>(this, R.layout.device_name, addresses);
		ListView addressView = (ListView)findViewById(R.id.networkList);
		addressView.setAdapter(addressAdapter);
		
		pairedAdapter = new ArrayAdapter<String>(this, R.layout.device_name, pairedData);
		ListView conView = (ListView)findViewById(R.id.connectionList);
		conView.setAdapter(pairedAdapter);
		conView.setVisibility(View.VISIBLE);
		
		btService = new BluetoothService(this);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if(!btAdapter.isEnabled())
		{
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		
		TextView macAddress = (TextView)findViewById(R.id.macAddress);
		macAddress.setText("Own: " + btAdapter.getAddress());
		
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
		startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);
				
		// Register the BroadcastReceiver
		final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiver, filter);
		
		final Button discoveryButton = (Button) findViewById(R.id.startDiscovery);
		discoveryButton.setOnClickListener(new OnClickListener() 
        {       	
            public void onClick(View v) 
            {            	
        		discoveryButton.setText("Discovery Started");
        		btService.linkBuilding();
            }
        });      
	}	
	
	@Override
	protected void onStart() 
	{
		super.onStart();
		
		btService.start();		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == REQUEST_ENABLE_BT && resultCode != RESULT_OK)
		{
			Log.e("Bluetooth", "Request to enable BT has been denied.");
			finish();
		}
		if(REQUEST_DISCOVERABLE == requestCode && resultCode == RESULT_CANCELED)
		{
			Log.e("Bluetooth", "Request to activate discoverability has been denied.");
			finish();
		}
	}
	
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		
		unregisterReceiver(receiver);
		
		btService.stop();	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bluetooth, menu);
		return true;
	}
}