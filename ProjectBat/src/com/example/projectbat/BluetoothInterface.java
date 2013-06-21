package com.example.projectbat;

import java.util.ArrayList;

public interface BluetoothInterface
{
	public void addPairedDevice(String address);
	public void updateDevices(ArrayList<String> addresses);
	public void displayMessage(String message);

	public void exit();
}