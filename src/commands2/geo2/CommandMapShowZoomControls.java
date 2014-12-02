package commands2.geo2;

import geo2.GMap;

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
