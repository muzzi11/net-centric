package com.example.projectbat;

import java.util.ArrayList;

import android.bluetooth.BluetoothDevice;

public class LinkBuildingHandlers 
{
	private final BluetoothService btService;	
	
	public LinkBuildingHandlers(final BluetoothService btServ)							
	{
		btService = btServ;				
	}
	
	public void linkBuilding()
	{	
		btService.btInterface.displayMessage("Starting discovery");
		
		btService.addresses.add(btService.btAdapter.getAddress());		
		btService.btAdapter.startDiscovery();
	}	
	
	public void linkBuildingFinished()
	{
		btService.btInterface.displayMessage("Link building finished.");
		
		btService.broadcastMessage(btService.BUILDING_DONE);
	}
	
	public void connectingSucceeded(final BluetoothDevice device)
	{		
		btService.btInterface.displayMessage("Connecting succeeded, sending addresses");
		
		String address = device.getAddress();		

		// Notify parent of new node in the network
		if (btService.parent != null)			
			btService.parent.sendString(address, btService.addressMsgID);

		// Send the child the current known addresses in the network
		btService.addresses.add(address);		
		btService.child.sendObject(btService.addresses, btService.addressesMsgID);		
		
		btService.btInterface.updateDevices(btService.addresses);
		btService.btInterface.addPairedDevice(address);
	}

	public void acceptingSucceeded(final BluetoothDevice device)
	{
		btService.btInterface.displayMessage("Accepting succeeded, adding address");
		
		String address = device.getAddress();		
		
		btService.btInterface.addPairedDevice(address);
		btService.btInterface.updateDevices(btService.addresses);
	}	
	
	public void propagateAddress(final String address)
	{
		btService.btInterface.displayMessage("Propagating message: " + address);
		
		btService.addresses.add(address);            	
		btService.btInterface.updateDevices(btService.addresses);
		
		// Send new address to parent if applicable
		if (btService.parent != null)
			btService.parent.sendString(address, btService.addressMsgID);      
	}
	
	public void processReceivedAddresses(final ArrayList<String> adresses)
	{
		btService.btInterface.displayMessage("Received addresses, starting discovery");
		
		btService.addresses.addAll(adresses);		
		btService.btInterface.updateDevices(btService.addresses);		
		btService.btAdapter.startDiscovery();
	}
}