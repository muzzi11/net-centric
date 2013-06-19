package com.example.projectbat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BluetoothActivity extends Activity
{
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_DISCOVERABLE = 2;
	
	private BluetoothAdapter btAdapter;
	private BroadcastReceiver receiver;
	private BluetoothServerSocket btServerSocket;
	
	private final Map<String, BluetoothSocket> sockets = new HashMap<String, BluetoothSocket>();
	private final Map<String, BluetoothSocket> redundantSockets = new HashMap<String, BluetoothSocket>();
	
	private final ArrayList<String> pairedData = new ArrayList<String>();
	private ArrayAdapter<String> pairedAdapters;
	
	private final UUID uuid = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
	
	private long time = System.currentTimeMillis();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth);
		
		pairedAdapters = new ArrayAdapter<String>(this, R.layout.device_name, pairedData);
		ListView listView = (ListView)findViewById(R.id.connectionList);
		listView.setAdapter(pairedAdapters);
		
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if(!btAdapter.isEnabled())
		{
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		
		TextView macAddress = (TextView)findViewById(R.id.macAddress);
		macAddress.setText("Own: "+btAdapter.getAddress());
		
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
		startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);
		
		receiver = new BroadcastReceiver() {
		    public void onReceive(Context context, Intent intent) {
		        final String action = intent.getAction();

		        if (BluetoothDevice.ACTION_FOUND.equals(action))
		        {
		            // Get the BluetoothDevice object from the Intent
		            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		            final String address = device.getAddress();

		            synchronized(sockets)
        			{
			            if(device.getName().equals("projectThunder") && !sockets.containsKey(address))
			            {
			            	Log.i("Bluetooth", "Found projectThunder device: "+address);
			            	btAdapter.cancelDiscovery();
			            	
			            	BluetoothSocket btSocket = null;
			            	try
			            	{
								btSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
							}
			            	catch (IOException e)
							{
								Log.e("Bluetooth", e.getMessage());
							}
			            	
			            	if(btSocket != null)
			            	{
			            		try
			            		{
									btSocket.connect();
								}
			            		catch (IOException e)
			            		{
			            			try{ btSocket.close(); }
			            			catch(IOException e1){ Log.e("Bluetooth", e1.getMessage()); }
			            			
			            			Log.e("Bluetooth", e.getMessage());
								}
			            		
			            		if(btSocket.isConnected())
			            		{
			            			Log.i("Bluetooth", "Connected DJECKO");
			            			
			            			
		            				if( !sockets.containsKey(address) )
		            				{
		            					sockets.put(address, btSocket);
		            					
		            					pairedData.add(address);
				            			pairedAdapters.notifyDataSetChanged();
		            				}
		            			}		            			
			            	}
			            	else
			            	{
			            		Log.e("Bluetooth", "Failed to get socket.");
			            	}
			            }
        			}
		        }
		        else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
		        {
		        	//if(System.currentTimeMillis() - time < 30000)
		        	{
		        		btAdapter.startDiscovery();
		        		Log.i("Bluetooth", "Restarting discovery");
		        	}
		        }
		    }
		};
		// Register the BroadcastReceiver
		final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiver, filter);
		
		try
		{
			btServerSocket = btAdapter.listenUsingInsecureRfcommWithServiceRecord("THUNDER", uuid);
		}
		catch (IOException e)
		{
			Log.e("Bluetooth", e.getMessage());
			finish();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		new Thread(new Runnable()
		{
			public void run()
			{
				BluetoothSocket btSocket = null;
				
				Log.i("Bluetooth", "Accepting sheezy my neezy");
				try{ btSocket = btServerSocket.accept(); }
				catch(IOException e){ Log.e("Bluetooth", e.getMessage()); }
				
				if(btSocket != null)
				{
					Log.i("Bluetooth", "CONNECTED GEEZY");
					
					final BluetoothDevice remoteDevice = btSocket.getRemoteDevice();
					final String address = remoteDevice.getAddress();
					
					synchronized(sockets)
					{
						if( !sockets.containsKey(address) )
						{
							sockets.put(address, btSocket);
							
							BluetoothActivity.this.runOnUiThread(new Runnable()
							{
								public void run()
								{
									pairedData.add(address);
			            			pairedAdapters.notifyDataSetChanged();
								}
							});
						}
						else if( !redundantSockets.containsKey(address))
						{
							redundantSockets.put(address, btSocket);
						}
					}
				}
			}
		}).start();
		
		Log.i("Bluetooth", "Start discovering");
		btAdapter.startDiscovery();
		Log.i("Bluetooth", "Stahp discovering");
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
	protected void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(receiver);
		
		try{ btServerSocket.close(); }
		catch(IOException e){ Log.e("Bluetooth", e.getMessage()); }
		
		for(BluetoothSocket btSocket : sockets.values())
		{
			try{ btSocket.close(); }
			catch(IOException e){ Log.e("Bluetooth", e.getMessage()); }
		}
		for(BluetoothSocket btSocket : redundantSockets.values())
		{
			try{ btSocket.close(); }
			catch(IOException e){ Log.e("Bluetooth", e.getMessage()); }
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bluetooth, menu);
		return true;
	}

}
