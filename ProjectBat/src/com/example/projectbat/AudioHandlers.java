package com.example.projectbat;

import java.util.HashMap;
import java.util.Map;

public class AudioHandlers 
{
	private final BluetoothService btService;
	
	public final Map<String, Handler> handlerMap = new HashMap<String, Handler>();	
	
	public AudioHandlers(BluetoothService btServ)						 
	{
		btService = btServ;
		
		handlerMap.put(btService.BUILDING_DONE, new Handler() 
		{			
			public void handler() 
			{
				int myIndex = btService.addresses.indexOf(btService.btAdapter.getAddress());
				if (myIndex == 0)
					btService.sendToId(btService.addresses.get(1), btService.START_LISTENING);
			}
		});
		
		handlerMap.put(btService.START_LISTENING, new Handler()
		{
			public void handler() 
			{			
				
			}
		});
	}
}

interface Handler
{
	public void handler();
}