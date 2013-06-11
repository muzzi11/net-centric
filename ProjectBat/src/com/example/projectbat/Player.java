package com.example.projectbat;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class Player
{
	private MediaPlayer player;
	
	public Player(Context context)
	{
		player = MediaPlayer.create(context, R.raw.beep);
		if(player == null) Log.e("Player", "Failed to create player.");
	}
	
	public void play()
	{
		player.start();
	}
}
