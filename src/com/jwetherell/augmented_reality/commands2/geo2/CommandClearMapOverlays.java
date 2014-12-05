package com.jwetherell.augmented_reality.commands2.geo2;

import com.jwetherell.augmented_reality.geo2.GMap;

import android.util.Log;

import commands.Command;

public class CommandClearMapOverlays extends Command {

	private GMap myMap;

	public CommandClearMapOverlays(GMap map) {
		myMap = map;
	}

	@Override
	public boolean execute() {
		if (myMap != null && myMap.getOverlays() != null) {
			Log.d("Commands", "Clearing map");
			myMap.getOverlays().clear();
			return true;
		}
		if (myMap == null)
			Log.w("Commands",
					"CommandClearMapOverlays command: myMap was null!");
		return false;
	}

}
