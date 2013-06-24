package com.example.projectbat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.example.projectbat.BluetoothService.Connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class LinkBuildingHandlers 
{
	private final BluetoothAdapter btAdapter;
	private final BluetoothInterface btInterface;
	private final ArrayList<String> addresses;
	private final Map<String, Connection> connections;
	
	private static final String BUILDING_DONE = "BuildingDone";
	private static final String START_LISTENING = "StartListening";
	
	private Connection parent = null;
	private Connection child = null;
	
	private final byte addressesListMsgID = 0x2;
	private final byte addressMsgID = 0x4;
	private final byte broadcastMsgID = 0x6;
	private final byte sendtoMsgID = 0x8;
	
	public final Map<String, Handler> handlerMap = new HashMap<String, Handler>();
	
	public LinkBuildingHandlers(final BluetoothInterface ie, 
								final ArrayList<String> addresses, 
								final Map<String, Connection> connections)
	{		
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		btInterface = ie;
		
		this.addresses =  addresses;
		this.connections = connections;
		
		handlerMap.put(BUILDING_DONE, new Handler() 
		{			
			public void handler() 
			{
				int myIndex = addresses.indexOf(btAdapter.getAddress());
				if (myIndex == 0)
					sendToId(addresses.get(1), START_LISTENING);
			}
		});
		
		handlerMap.put(START_LISTENING, new Handler()
		{
			public void handler() 
			{			
				
			}
		});
	}
	
	private void broadcastMessage(String message)
	{
		for (Connection con : connections.values())					
			con.sendString(message, broadcastMsgID);		
	}
	
	private void sendToId(String address, String msg)
	{
		int myIndex = addresses.indexOf(btAdapter.getAddress());
		int targetIndex = addresses.indexOf(address);
		
		ArrayList<String> data = new ArrayList<String>();
		data.add(address);
		data.add(msg);
		
		if (myIndex < targetIndex)
			parent.sendObject(data, sendtoMsgID);
		else
			child.sendObject(data, sendtoMsgID);
	}
	
	public void linkBuilding()
	{	
		btInterface.displayMessage("Starting discovery");
		addresses.add(btAdapter.getAddress());		
		btAdapter.startDiscovery();
	}	
	
	public void linkBuildingFinished()
	{
		btInterface.displayMessage("Link building finished.");
		
		broadcastMessage(BUILDING_DONE);
	}
	
	public void connectingSucceeded(BluetoothDevice device)
	{		
		String address = device.getAddress();		

		// Notify parent of new node in the network
		if (parent != null)			
			parent.sendString(address, addressMsgID);

		// Send the child the current known addresses in the network
		addresses.add(address);
		child = connections.get(device.getAddress());
		child.sendObject(addresses, addressesListMsgID);
		
		btInterface.displayMessage("Connecting succeeded, sending addresses");
		btInterface.updateDevices(addresses);		
		btInterface.addPairedDevice(address);
	}

	public void acceptingSucceeded(BluetoothDevice device)
	{
		String address = device.getAddress();		
		parent = connections.get(address);	
		
		btInterface.displayMessage("Accepting succeeded, adding address");
		btInterface.addPairedDevice(address);
		btInterface.updateDevices(addresses);
	}	
	
	public void propagateAddress(String address)
	{
		addresses.add(address);            	
		btInterface.updateDevices(addresses);
		
		// Send new address to parent if applicable
		if (parent != null)
			parent.sendString(address, addressMsgID);      
	}
	
	public void processReceivedAddresses(ArrayList<String> adresses)
	{
		this.addresses.addAll(adresses);
		
		btInterface.updateDevices(addresses);
		btInterface.displayMessage("Starting discovery");
		btAdapter.startDiscovery();
	}
}

interface Handler
{
	public void handler();
}