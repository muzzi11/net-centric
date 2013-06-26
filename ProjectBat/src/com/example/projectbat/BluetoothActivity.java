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
	private final int MAX_DISCOVERIES = 0;
	private final int MAX_CONNECTION_ATTEMPTS = 1;
	
	private BluetoothAdapter btAdapter;	
	private BluetoothService btService;

	private final ArrayList<BluetoothDevice> foundDevices = new ArrayList<BluetoothDevice>();
	private ArrayList<String> addresses = new ArrayList<String>();
	private final ArrayList<String> pairedData = new ArrayList<String>();

	private ArrayAdapter<String> pairedAdapter;
	private ArrayAdapter<String> addressAdapter;
	
	private int discoveryCounter = 0;
	private int connectionAttempt = 0;

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if ( BluetoothDevice.ACTION_FOUND.equals(action) )
			{
				// Get the BluetoothDevice object from the Intent
				final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				final String name = device.getName() == null ? "" : device.getName(); // derp
				final String address = device.getAddress();

				if ( name.equals("projectThunder") && !addresses.contains(address) )
				{
					foundDevices.add(device);
					btAdapter.cancelDiscovery();
				}
			}	        
			else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
			{	        	
				Log.i("Bluetooth", "Discovery finished");

				if ( foundDevices.isEmpty() )
				{
					if (discoveryCounter < MAX_DISCOVERIES)
					{
						btAdapter.startDiscovery();
						++discoveryCounter;
						displayMessage("Service discovery attempt: " + Integer.toString(discoveryCounter));
					}
					else
					{
						btService.linkBuildingHandlers.linkBuildingFinished();
						requestDiscoverable(1);
					}
				}
				else if ( !btService.connect( foundDevices.get(0) ) )
				{
					foundDevices.clear();
					if(++connectionAttempt < MAX_CONNECTION_ATTEMPTS) btAdapter.startDiscovery();
					else btService.linkBuildingHandlers.linkBuildingFinished();
				}
				else
				{
					// connection has been established, turn off discoverability
					requestDiscoverable(1);
				}
			}
		}
	};
	
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

		if(btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
		{
			requestDiscoverable(0);
		}

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
				discoveryButton.setEnabled(false);
				
				final Button measureButton = (Button)findViewById(R.id.measure);
				measureButton.setEnabled(true);
				
				btService.linkBuildingHandlers.linkBuilding();
			}
		});
		
		final Button measureButton = (Button)findViewById(R.id.measure);
        measureButton.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				btService.sendToId(btService.addresses.get(1), "", btService.START_LISTENING);
			}
        });
	}	

	private void requestDiscoverable(final int duration)
	{
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
		startActivity(discoverableIntent);
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
		if(requestCode == REQUEST_DISCOVERABLE && resultCode == RESULT_CANCELED)
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