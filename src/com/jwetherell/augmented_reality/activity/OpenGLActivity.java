package com.jwetherell.augmented_reality.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import com.jwetherell.augmented_reality.R;
import com.jwetherell.augmented_reality.common.Vector;

import geo.GeoGraph;
import gl.GLCamera;
import util.Vec;
import worldData.World;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class OpenGLActivity extends Activity{
	private GeoGraph myGraph;
	private World world;
	private GLCamera camera;
	private Vector destination;
	private Vector current;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			System.setErr(new PrintStream(new FileOutputStream(new File("sdcard/ARErrLog.txt"), true)));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		float[] curr = null;
		float[] dest = null;
		try{
			Bundle data = this.getIntent().getExtras();
			curr = data.getFloatArray("current");
			this.current = new Vector(curr[0],curr[1],curr[2]);
			dest = data.getFloatArray("destination");
			this.destination = new Vector(dest[0],dest[1],dest[2]);
		} catch(NullPointerException npe){
			npe.printStackTrace();
		}/*
		try{
			myGraph = new GeoGraph();
			camera = new GLCamera(new Vec(0, 0, 1f));
			world.add(myGraph);
		}catch(Exception e){
			e.printStackTrace();
		}*/
		setContentView(R.layout.defaultlistitemview);
		if (curr == null) throw new NullPointerException("current location cannot be loaded");
		if (dest == null) throw new NullPointerException("destination location cannot be loaded");
		
		((TextView) findViewById(R.id.lat)).setText("current: x "+curr[0]+" y "+curr[1]+" z "+curr[2]+"\n");
		((TextView) findViewById(R.id.lon)).setText("destination: x "+dest[0]+" y "+dest[1]+" z "+dest[2]+"\n");
	}
}
