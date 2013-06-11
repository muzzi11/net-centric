package com.example.projectbat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BluetoothService 
{
	private BluetoothAdapter bluetoothAdapter;
	
	 // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothSecure";

    // Unique UUID for this application
    private static final UUID MY_UUID =
        UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	
	public BluetoothService() 
	{        
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}    
	
	/**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */	
	private class AcceptThread extends Thread
	{
	    private final BluetoothServerSocket serverSocket;	    
	    
	    public AcceptThread() {
	    	 BluetoothServerSocket tmp = null;	    	

            // Create a new listening server socket
            try 
            {                
                    tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);            
            }
            catch (IOException e) {
                Log.e("Bluetooth", "listen() failed", e);
            }
            serverSocket = tmp;
	    }
	 
	    public void run() 
	    {
	        BluetoothSocket socket = null;
	        // Keep listening until exception occurs or a socket is returned
	        while (true) 
	        {
	            try 
	            {
	                socket = serverSocket.accept();
	            } catch (IOException e) 
	            {
	            	Log.e("Bluetooth", "accept() failed", e);
                    break;
	            }
	            // If a connection was accepted
	            if (socket != null) 
	            {
	                // Do work to manage the connection (in a separate thread)	                
	            	try 
	    	    	{
	                    serverSocket.close();
	                } catch (IOException e) 
	                {
	                    Log.e("Bluetooth", "close() of server failed", e);
	                }
	                break;
	            }
	        }
	    }
	 
	    /** Will cancel the listening socket, and cause the thread to finish */
	    public void cancel() 
	    {
	    	try 
	    	{
                serverSocket.close();
            } catch (IOException e) 
            {
                Log.e("Bluetooth", "close() of server failed", e);
            }
	    }	    
	}

	 /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
	private class ConnectThread extends Thread 
	{
	    private final BluetoothSocket clientSocket;
	    private final BluetoothDevice device;	    
	 
	    public ConnectThread(BluetoothDevice dev) 
	    {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        device = dev;
	 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try 
	        {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
	        }
	        catch (IOException e)
	        { 
	        	Log.e("Bluetooth", "failed to connect to server", e);	        	
	        }
	        clientSocket = tmp;
	    }
	 
	    public void run() 
	    {
	        // Cancel discovery because it will slow down the connection
	        bluetoothAdapter.cancelDiscovery();
	 
	        try 
	        {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	        	clientSocket.connect();
	        } 
	        catch (IOException e) 
	        {
	            // Unable to connect; close the socket and get out
	        	Log.e("Bluetooth", "unable to connect", e);
	            try 
	            {
	            	clientSocket.close();
	            }
	            catch (IOException closeError)
	            { 
	            	Log.e("Bluetooth", "failed to close clientsocket", closeError);
	            }
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread)
	        // DO SOMETHING
	    }
	 
	    /** Will cancel an in-progress connection, and close the socket */
	    public void cancel() 
	    {
	        try 
	        {
	            clientSocket.close();
	        } 
	        catch (IOException e) 
	        {
	        	Log.e("Bluetooth", "failed to close clientsocket", e);	        
	        }
	    }
	}

	/**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
	private class ConnectedThread extends Thread
	{
		private final BluetoothSocket clientSocket;
		private final InputStream inStream;
		private final OutputStream outStream;
		
		public ConnectedThread(BluetoothSocket socket) {            
			clientSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	
	        // Get the BluetoothSocket input and output streams
	        try 
	        {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        }
	        catch (IOException e) 
	        {
	            Log.e("Bluetooth", "temp sockets not created", e);
	        }
	
	        inStream = tmpIn;
	        outStream = tmpOut;
	    }
	
	
		public void run() 
		{
	        byte[] buffer = new byte[1024];  // buffer store for the stream
	        int bytes; // bytes returned from read()
	 
	        // Keep listening to the InputStream until an exception occurs
	        while (true) 
	        {
	            try 
	            {
	                // Read from the InputStream
	                bytes = inStream.read(buffer);                
	            }
	            catch (IOException e) 
	            {
	                break;
	            }
	        }
	    }
	 
	    /* Call this from the main activity to send data to the remote device */
	    public void write(byte[] bytes) 
	    {
	        try 
	        {
	            outStream.write(bytes);
	        } 
	        catch (IOException e) 
	        {
	        	
	        }
	    }
	 
	    /* Call this from the main activity to shutdown the connection */
	    public void cancel() 
	    {
	        try 
	        {
	            clientSocket.close();
	        } 
	        catch (IOException e) 
	        { 
	        	
	        }
	    }
	}
}