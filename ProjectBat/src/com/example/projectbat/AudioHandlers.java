package com.example.projectbat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AudioHandlers 
{
	class Listener implements BeepTimerListener
	{
		String sender = null;
		private long time = -1, measurement = -1;
		
		@Override
		public void timeMeasured(long timeInSamples, boolean isListener)
		{
			//btService.btInterface.displayMessage(Boolean.toString(isListener)+" "+Long.toString(time)+" "+Long.toString(measurement));
			if(sender != null && isListener)
			{
				btService.sendToId(sender, Long.toString(timeInSamples), btService.TIME_MEASUREMENT);
				btService.btInterface.displayMessage("Measurement: "+Long.toString(timeInSamples));
			}
			time = timeInSamples;
			if(measurement >= 0 && time >= 0) onAllReceived();
		}
		
		private void onAllReceived()
		{
			double dist = 170.14 * (time - measurement) / 44100.0;
			btService.btInterface.displayMessage("Time: "+Long.toString(time)+" "+
					Long.toString(measurement)+" "+Double.toString(dist));
			//btService.sendToId(btService.addresses.get(1), "", btService.START_LISTENING);
			reset();
		}
		
		public void reset()
		{
			sender = null;
			measurement = time = -1;
		}
		
		public void setMeasurement(long measurement)
		{
			this.measurement = measurement;
			if(time >= 0) onAllReceived();
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
				
				//int myIndex = btService.addresses.indexOf(btService.btAdapter.getAddress());
				//if (myIndex == beeperTurn)
				//	btService.sendToId(btService.addresses.get(1), "", btService.START_LISTENING);
			}
		});
		
		handlerMap.put(btService.START_LISTENING, new Handler()
		{
			public void handler(ArrayList<String> data)
			{	
				String sender = data.get(1);

				listener.reset();
				listener.sender = sender;
				MainActivity.beepTimer.start(true);
						
				btService.sendToId(sender, "", btService.ACK_LISTENING);
			}
		});
		
		handlerMap.put(btService.ACK_LISTENING, new Handler()
		{
			public void handler(ArrayList<String> data) 
			{
				listener.reset();
				MainActivity.beepTimer.start(false);
				MainActivity.beepGenerator.play();
			}
		});
		
		handlerMap.put(btService.TIME_MEASUREMENT, new Handler()
		{
			public void handler(ArrayList<String> data) 
			{
				Long time = Long.parseLong(data.get(2));
				listener.setMeasurement(time);
			}
		});
	}
}

interface Handler
{
	public void handler(ArrayList<String> data);
}