package com.example.projectbat;

import java.util.ArrayList;
import java.util.Set;

import com.example.projectbat.R;

import android.os.Bundle;
import android.preference.PreferenceManager.OnActivityResultListener;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class DeviceListActivity extends Activity 
{	
	private static final String TARGET_DEVICE = "projectThunder";
	        
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_DISCOVERABLE = 2;

	private BluetoothService bluetoothService;
	private BluetoothAdapter bluetoothAdapter;	
	private ArrayAdapter<String> pairedDevicesArrayAdapter;
	private ArrayAdapter<BluetoothDevice> newDevicesArrayAdapter;
	
	private ArrayList<String> pairedDevicesData = new ArrayList<String>();
	private ArrayList<BluetoothDevice> newDevices = new ArrayList<BluetoothDevice>();
	
	private Set<BluetoothDevice> pairedDevices;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);
        
        // Initialize BluetoothService
        bluetoothService = new BluetoothService(this);        
              
        //Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name, pairedDevicesData);
        newDevicesArrayAdapter = new ArrayAdapter<BluetoothDevice>(this, R.layout.device_name, newDevices);
        
        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(newDevicesArrayAdapter);
        
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(receiver, filter);
              
        // Get the BluetootheAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) 
        {
            // Device does not support Bluetooth
        	// DO SOMETHING
        }
        
        pairedDevices = bluetoothAdapter.getBondedDevices();
        
        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) 
        {    
        	findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) 
            {            	
            	pairedDevicesData.add(device.getName() + "\n" + device.getAddress());
            }
        } 
        
        pairedDevicesArrayAdapter.notifyDataSetChanged();
    }
    
    @Override
    public void onStart() {
        super.onStart();    

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        // Enable Bluetooth
        if (!bluetoothAdapter.isEnabled()) 
        {        	
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);            
        }
             		
        // Ensure discoverable
        Intent discoverableIntent = new	Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);
		
		// Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle("Scanning");

        // Turn on sub-title for new devices
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
		
    	// Start AcceptThread        
        bluetoothService.start();        
    }
    
    public void OnActivityResultListener(int requestCode, int resultCode, Intent data)
    {
    	if (requestCode == REQUEST_ENABLE_BT && resultCode != RESULT_OK)
    		finish();
    	
    	if (requestCode == REQUEST_DISCOVERABLE && resultCode == RESULT_CANCELED)
    		finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (bluetoothAdapter != null) {
        	bluetoothAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(receiver);
    }
          
    private void updatePairedDevices()
    {
    	pairedDevices = bluetoothAdapter.getBondedDevices();          		
			            		
        for (BluetoothDevice dev : pairedDevices) 
        {            	
        	String deviceDescription = dev.getName() + "\n" + dev.getAddress();
        	if (!pairedDevicesData.contains(deviceDescription))
        	{
        		pairedDevicesData.add(deviceDescription);
        	}
        }
        pairedDevicesArrayAdapter.notifyDataSetChanged();    	
    }
    
    // 	Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver receiver = new BroadcastReceiver() 
    {
        public void onReceive(Context context, Intent intent) 
        {
            String action = intent.getAction();      
            
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) 
            {
            	TextView text = (TextView) findViewById(R.id.textView1);
            	text.setText("Discovering");
            	
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);               
                    
                if (device.getName().equals(TARGET_DEVICE) && !newDevices.contains(device))
            	{            	      		
            		newDevices.add(device);
            		newDevicesArrayAdapter.notifyDataSetChanged();
            	}	            
            }
            // Restart discovery when finished
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
            	TextView text = (TextView) findViewById(R.id.textView1);
            	text.setText("Connecting");
            	
            	for (BluetoothDevice dev : newDevices)
            		bluetoothService.connect(dev);
            	
            	updatePairedDevices();            	
            }
        }
    };
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
