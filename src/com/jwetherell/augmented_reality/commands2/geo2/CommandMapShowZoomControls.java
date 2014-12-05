package com.jwetherell.augmented_reality.commands2.geo2;

import com.jwetherell.augmented_reality.geo2.GMap;

import commands.Command;

public class CommandMapShowZoomControls extends Command {

	private GMap myMap;
	private boolean showControls;

	public CommandMapShowZoomControls(GMap map, boolean showZoomControls) {
		myMap = map;
		showControls = showZoomControls;
	}

	@Override
	public boolean execute() {
		myMap.enableZoomButtons(showControls);
		return false;
	}

}
