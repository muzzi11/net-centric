package com.example.projectbat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

public class BluetoothService 
{
	private final BluetoothAdapter bluetoothAdapter;	
	private AcceptThread acceptThread = null;
	private ConnectThread connectThread = null;
	private ConnectedThread connectedThread = null;
	private int state;
	
	// Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device	
	
	 // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothInsecure";

    // Unique UUID for this application
    private static final UUID MY_UUID =
        UUID.fromString("d29fd110-d754-11e2-8b8b-0800200c9a66");
	
	public BluetoothService(Context context) 
	{        
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}    
		
	/**
     * Start the service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() 
    {
    	Log.d("Bluetooth", "Service started");
    	
    	// Cancel any thread attempting to make a connection
        if (connectThread != null) 
        {
        	connectThread.cancel(); 
        	connectThread = null;
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null)
        {
        	connectedThread.cancel(); 
        	connectedThread = null;
        }
        
        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
        
        bluetoothAdapter.startDiscovery();
    }
    
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device)
    {    	
    	Log.d("Bluetooth", "connect to: " + device);
        // Cancel any thread attempting to make a connection
        if (state == STATE_CONNECTING) 
        {
            if (connectThread != null) {connectThread.cancel(); connectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {connectedThread.cancel(); connectedThread = null;}

        // Start the thread to connect with the given device
        connectThread = new ConnectThread(device);
        connectThread.start();
        setState(STATE_CONNECTING);
    }    
       
    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device) 
    {
        // Cancel the thread that completed the connection
        if (connectThread != null) {connectThread.cancel(); connectThread = null;}

        // Cancel any thread currently running a connection
        if (connectedThread != null) {connectedThread.cancel(); connectedThread = null;}
        
        // Cancel the accept thread because we only want to connect to one device
        if (acceptThread != null) {
        	acceptThread.cancel();
        	acceptThread = null;
        }        

        // Start the thread to manage the connection and perform transmissions
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
        
        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() 
    {   	
    	if (connectThread != null) 
    	{
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) 
        {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (acceptThread != null) 
        {
        	acceptThread.cancel();
        	acceptThread = null;
        }
       
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) 
    {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (state != STATE_CONNECTED) return;
            r = connectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() 
    {      
        // Start the service over to restart listening mode     	
        this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() 
    {
    	// Start the service over to restart listening mode    	
        this.start();
    }

    
    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int s) 
    {        
        state = s;        
    }
		
	/**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */	
 	private class AcceptThread extends Thread
	{
	    private final BluetoothServerSocket serverSocket;	    
	    
	    public AcceptThread() 
	    {
	    	 BluetoothServerSocket tmp = null;	    	

            // Create a new listening server socket
	    	 try
	    	 {	               
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                        NAME, MY_UUID);           
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
	            } 
	            catch (IOException e) 
	            {
	            	Log.e("Bluetooth", "accept() failed", e);
                    break;
	            }
	            // If a connection was accepted
	            if (socket != null) 
	            {
	            	Log.d("Bluetooth", "Connection accepted");
	            	synchronized (BluetoothService.this) 
	            	{
                        switch (state) 
                        {
	                        case STATE_LISTEN:
	                        case STATE_CONNECTING:
	                            // Situation normal. Start the connected thread.
	                            connected(socket, socket.getRemoteDevice());
	                            break;
	                        case STATE_NONE:
	                        case STATE_CONNECTED:
	                            // Either not ready or already connected. Terminate new socket.
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
	        
	        try 
	        {       
                tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID);     
            }
	        catch (IOException e) 
	        {
                Log.e("Bluetooth", "create client socket failed", e);
            }
        	
	        clientSocket = tmp;
	    }
	 
	    public void run() 
	    {
	        // Cancel discovery because it will slow down the connection
	    	
	        bluetoothAdapter.cancelDiscovery();
	        
	        while(bluetoothAdapter.isDiscovering()){}
	        
	        try 
	        {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	        	
	        
        		Log.d("Bluetooth", "baaaaaaaaaaaaaaaaaa");
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
	            connectionFailed();
	            return;	            
	        }	 
    		Log.d("Bluetooth", "CONNECTED");
	        // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) 
            {
                connectThread = null;
            }

            // Start the connected thread
            connected(clientSocket, device);
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
	            	connectionLost();
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
	        	connectionLost();
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