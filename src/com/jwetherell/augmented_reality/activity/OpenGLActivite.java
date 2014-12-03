package com.jwetherell.augmented_reality.activity;

import geo.GeoGraph;
import gl.GLCamera;
import util.Vec;
import worldData.World;
import android.app.Activity;
import android.os.Bundle;

public class OpenGLActivite extends Activity{
	private GeoGraph myGraph;
	private World world;
	private GLCamera camera;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myGraph = new GeoGraph();
		camera = new GLCamera(new Vec(0, 0, 1f));
		world.add(myGraph);
	}
}
