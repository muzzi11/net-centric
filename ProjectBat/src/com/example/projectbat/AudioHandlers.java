package com.example.projectbat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AudioHandlers 
{
	class Listener implements BeepTimerListener
	{
		String sender = null;
		long time = 0;
		boolean measurementReceived = false;
		
		@Override
		public void timeMeasured(long timeInSamples, boolean isListener)
		{
			if(sender != null && isListener)
			{
				btService.sendToId(sender, Long.toString(timeInSamples), btService.TIME_MEASUREMENT);
			}
			
			if(measurementReceived)
			{
				btService.btInterface.displayMessage("Time: "+Long.toString(timeInSamples - time));
			}
			
			if(!isListener) time = timeInSamples;
		}
	}
	
	private final BluetoothService btService;
	
	public final Map<Integer, Handler> handlerMap = new HashMap<Integer, Handler>();
	private final Listener listener = new Listener();
	
	private int listenerTurn = 0;
	private int beeperTurn = 0;
	
	public AudioHandlers(final BluetoothService btServ)						 
	{
		btService = btServ;
		
		MainActivity.beepTimer.registerListener(listener);
		
		handlerMap.put(btService.BUILDING_DONE, new Handler() 
		{			
			public void handler(ArrayList<String> data) 
			{
				String sender = data.get(1);				
				btService.btInterface.displayMessage("Received from: " + sender);
				
				int myIndex = btService.addresses.indexOf(btService.btAdapter.getAddress());
				if (myIndex == beeperTurn)
					btService.sendToId(btService.addresses.get(1), "", btService.START_LISTENING);
			}
		});
		
		handlerMap.put(btService.START_LISTENING, new Handler()
		{
			public void handler(ArrayList<String> data) 
			{	
				String sender = data.get(1);
				
				btService.btInterface.displayMessage("Starting listening");

				MainActivity.beepTimer.start(true);
				listener.sender = sender;
						
				btService.sendToId(sender, "", btService.ACK_LISTENING);
			}
		});
		
		handlerMap.put(btService.ACK_LISTENING, new Handler()
		{
			public void handler(ArrayList<String> data) 
			{			
				btService.btInterface.displayMessage("Received ack listening.");
				
				MainActivity.beepGenerator.play();
				listener.measurementReceived = false;
				listener.time = 0;
				
				String sender, msg;
				sender = data.get(1);
				msg = data.get(2);
			}
		});
		
		handlerMap.put(btService.TIME_MEASUREMENT, new Handler()
		{
			public void handler(ArrayList<String> data) 
			{
				Long time = Long.parseLong(data.get(2));
				
				listener.measurementReceived = true;
				
				if(listener.time != 0)
				{
					time = listener.time - time;
					btService.btInterface.displayMessage("Time: "+Long.toString(time));
				}
				else
				{
					listener.time = time;
				}
			}
		});
	}
}

interface Handler
{
	public void handler(ArrayList<String> data);
}