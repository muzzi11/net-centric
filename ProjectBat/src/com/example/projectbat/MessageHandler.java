package com.example.projectbat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.bluetooth.BluetoothSocket;

public class MessageHandler 
{
	class Listener implements BeepTimerListener
	{
		String sender = null;
		int counter = 0;
		private long time = -1, measurement = -1;
		public double dist;
		
		@Override
		public synchronized void timeMeasured(long timeInSamples, boolean isListener)
		{
			if(sender != null && isListener)
			{
				btService.sendToId(sender, Long.toString(timeInSamples), btService.TIME_MEASUREMENT);
				btService.btInterface.displayMessage("Measurement: "+Long.toString(timeInSamples));
			}
			time = timeInSamples;
			if(time >= measurement && measurement >= 0)
			{
				onAllReceived();
				gotMeasurement = true;
			}
		}
		
		private void onAllReceived()
		{
			dist = 170.14 * (time - measurement) / 44100.0;
			String d = String.format("%.4f", dist);
			btService.btInterface.displayMessage(Integer.toString(++counter)+" : "+
					Long.toString(time - measurement)+" : "+d+"m");
			//btService.sendToId(btService.addresses.get(1), "", btService.START_LISTENING);
			reset();
		}
		
		public synchronized void reset()
		{
			sender = null;
			measurement = time = -1;
		}
		
		public synchronized void setMeasurement(long measurement)
		{
			this.measurement = measurement;
			if(time >= measurement && measurement >= 0)
			{
				onAllReceived();
				gotMeasurement = true;
			}
		}
	}
	
	private final BluetoothService btService;
	private final Listener listener = new Listener();
	private final Timer timer = new Timer();
	private boolean gotMeasurement = false;
	private String targetAddress;
	private Map<String, Double> measurements = new HashMap<String, Double>();
	
	public MessageHandler(final BluetoothService btServ)						 
	{
		btService = btServ;
		MainActivity.beepTimer.registerListener(listener);
	}
	
	private String SerializeMeasurements()
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String strOut = new String();
		try
		{
			ObjectOutputStream objOut = new ObjectOutputStream(out);
	        objOut.writeObject(measurements);
	        objOut.close();
	        strOut += objOut.toString();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
        
        return strOut;
	}
	
	@SuppressWarnings("unchecked")
	private void DeserializeMeasurements(String str)
	{
		try
		{
			ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(str.getBytes(), 0, str.length()));
			measurements = (Map<String, Double>) objIn.readObject();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	private void StartMeasurement(String address)
	{
		targetAddress = address;
		
		timer.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				//btService.btInterface.displayMessage(Boolean.toString(gotMeasurement));
				if(!gotMeasurement) btService.sendToId(targetAddress, "", btService.START_LISTENING);
				else
				{
					cancel();
				}
			}
		}, 1000, 1000);
		
		gotMeasurement = false;
		btService.sendToId(address, "", btService.START_LISTENING);
	}
	
	private class DoMeasurements implements Runnable 
	{
		final ArrayList<String> addresses = btService.addresses;
		final String ownAddress = btService.btAdapter.getAddress();
		
		public void run()
		{
			int next = 0;
			
			for(int i = 0; i < addresses.size(); ++i)
			{
				final String address = addresses.get(i);
				
				if(address.equals(ownAddress))
				{
					next = (i + 1) % addresses.size();
					continue;
				}
				
				StartMeasurement(address);
				
				while(!gotMeasurement) {}
				
				measurements.put(address, listener.dist);
			}
			
			btService.sendToId(addresses.get(next), SerializeMeasurements(), btService.NEXT_BOSS);
		}
	}
	
	private void BuildingDone(ArrayList<String> data)
	{
		String sender = data.get(1);				
		btService.btInterface.displayMessage("Received from: " + sender);
		
		if(btService.parent == null)
		{	
			DoMeasurements task = new DoMeasurements();
			Thread t = new Thread(task);
			t.setDaemon(true);
			t.start();
		}
	}
	
	private void StartListening(ArrayList<String> data)
	{
		String sender = data.get(1);

		listener.reset();
		listener.sender = sender;
		MainActivity.beepTimer.start(true);
				
		btService.sendToId(sender, "", btService.ACK_LISTENING);
	}
	
	private void AckListening()
	{
		listener.reset();
		MainActivity.beepTimer.start(false);
		MainActivity.beepGenerator.play();
	}
	
	private void TimeMeasurement(ArrayList<String> data)
	{
		Long time = Long.parseLong(data.get(2));
		listener.setMeasurement(time);
	}
	
	public void Handle(final int id, ArrayList<String> data)
	{
		if(id == btService.BUILDING_DONE)
		{
			BuildingDone(data);
		}
		else if(id == btService.START_LISTENING)
		{
			StartListening(data);
		}
		else if(id == btService.ACK_LISTENING)
		{
			AckListening();
		}
		else if(id == btService.TIME_MEASUREMENT)
		{
			TimeMeasurement(data);
		}
		else if(id == btService.NEXT_BOSS)
		{
			DeserializeMeasurements(data.get(2));

			DoMeasurements task = new DoMeasurements();
			Thread t = new Thread(task);
			t.setDaemon(true);
			t.start();
		}
	}
}

interface Handler
{
	public void handler(ArrayList<String> data);
}