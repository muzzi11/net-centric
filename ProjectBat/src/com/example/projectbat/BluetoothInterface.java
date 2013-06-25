package com.example.projectbat;

import java.util.ArrayList;

public interface BluetoothInterface
{
	public void addPairedDevice(final String address);
	public void updateDevices(final ArrayList<String> addresses);
	public void displayMessage(final String message);

	public void exit();
}