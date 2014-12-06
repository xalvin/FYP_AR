package com.jwetherell.augmented_reality.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jwetherell.augmented_reality.ui.IconMarker;
import com.jwetherell.augmented_reality.ui.Marker;

import geo.Edge;
import geo.GeoObj;
import gl.Color;
import gl.CustomGLSurfaceView;
import gl.GL1Renderer;
import gl.GLCamera;
import gl.GLFactory;
import gl.scenegraph.MeshComponent;
import gui.GuiSetup;
import system.EventManager;
import system.Setup;
import util.Vec;
import worldData.SystemUpdater;
import worldData.World;
import actions.ActionCalcRelativePos;
import actions.ActionMoveCameraBuffered;
import actions.ActionRotateCameraBuffered;
import android.R;
import android.app.Activity;

import commands.Command;
import commands.DebugCommandPositionEvent;
import commands.ui.CommandInUiThread;
import commands.ui.CommandShowToast;

public class RoutingSetup extends Setup {

	protected static final int ZDELTA = 5;
	private final GLCamera camera;
	private final World world;
	private final ActionCalcRelativePos gpsAction;
	private float[] current;
	private float[] destination;
	/*
	private final GeoObj posA;
	private final GeoObj posB;
	private final GeoObj posC;
	private final GeoObj posD;
	private final GeoObj posE;
	*/
	private ArrayList<GeoObj> steps;
	public RoutingSetup() {
		camera = new GLCamera();
		world = new World(camera);
		gpsAction = new ActionCalcRelativePos(world, camera);
		/*
		posA = new GeoObj(50.778922, 6.060461);
		posB = new GeoObj(50.780815, 6.06662);
		posC = new GeoObj(50.780557, 6.06735);
		posD = new GeoObj(50.779892, 6.065955);
		posE = new GeoObj(50.780408, 6.066492);
		*/
		steps = new ArrayList<GeoObj>();
	}

	public RoutingSetup(float[] current, float[] destination) {
		camera = new GLCamera();
		world = new World(camera);
		gpsAction = new ActionCalcRelativePos(world, camera);
		this.current = current;
		this.destination = destination;
		steps = null;
		String JSONStr = null;
		String url = "http://maps.googleapis.com/maps/api/directions/json?origin="+current[0]+","+current[1]+"&destination="+destination[0]+","+destination[1]+"&sensor=false&mode=walking&region=hk&language=en";
		try {
			InputStream stream = (new URL(url)).openConnection().getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream), 8 * 1024);
	        StringBuilder sb = new StringBuilder();

	        try {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                stream.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	        JSONStr = sb.toString();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(JSONStr == null) throw new NullPointerException();
		JSONObject json = null;
		try {
			json = new JSONObject(JSONStr);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (json == null) throw new NullPointerException();
		steps=parse(json);
		if(steps==null){
			CommandShowToast.show(myTargetActivity,"no route found");
		}			
	}
	
	public ArrayList<GeoObj> parse(JSONObject root) {
		if (root == null) throw new NullPointerException();
		JSONObject jo = null;
		JSONArray dataArray = null;
		ArrayList<GeoObj> geoObjs = null;

		try {
			if (root.has("routes")) dataArray = root.getJSONArray("routes");
			if (dataArray == null) return geoObjs;
			jo = dataArray.getJSONObject(0);
			geoObjs = processJSONObject(jo);
			if (geoObjs == null) throw new NullPointerException();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return geoObjs;
	}

	private ArrayList<GeoObj> processJSONObject(JSONObject jo) {
		if (jo == null) throw new NullPointerException();
		if (!jo.has("legs")) return null;
		JSONArray legsArray = null;
		
		ArrayList<GeoObj> result = new ArrayList<GeoObj>();
		try {
			legsArray = jo.getJSONArray("legs");
			JSONObject obj = legsArray.getJSONObject(0);
			if(!obj.has("steps")) return null;
			JSONArray stepsArray = legsArray.getJSONObject(0).getJSONArray("steps");
			int len = stepsArray.length();
			JSONObject first = stepsArray.getJSONObject(0);
			JSONObject start = first.getJSONObject("start_location");
			result.add(new GeoObj(start.getDouble("lat"),start.getDouble("lng")));
			JSONObject end = first.getJSONObject("end_location");
			result.add(new GeoObj(end.getDouble("lat"),end.getDouble("lng")));
			for(int i=1; i<len;i++){
				JSONObject ed = stepsArray.getJSONObject(i).getJSONObject("end_location");
				result.add(new GeoObj(ed.getDouble("lat"),ed.getDouble("lng")));
			}
			if(result.get(0).getLatitude()!=current[0] && result.get(0).getLongitude()!= current[1])
				result.add(0,new GeoObj(current[0],current[1]));
			if(result.get(result.size()-1).getLatitude()!=destination[0] && result.get(result.size()-1).getLongitude()!= destination[1])
				result.add(new GeoObj(destination[0],destination[1]));
		} catch (Exception e) {
			e.printStackTrace();
			result = null;
		}
		return result;
	}
	
	@Override
	public void _a_initFieldsIfNecessary() {

	}

	@Override
	public void _b_addWorldsToRenderer(GL1Renderer renderer,
			GLFactory objectFactory, GeoObj currentPosition) {
/*
		spawnObj(posA, GLFactory.getInstance().newCircle(Color.green()));
		spawnObj(posB, GLFactory.getInstance().newCircle(Color.green()));
		spawnObj(posC, GLFactory.getInstance().newCircle(Color.green()));
		spawnObj(posD, GLFactory.getInstance().newCircle(Color.green()));
		spawnObj(posE, GLFactory.getInstance().newCircle(Color.blue()));
*/
		for(GeoObj obj :this.steps){
			spawnObj(obj,GLFactory.getInstance().newDiamond(Color.greenTransparent()));
		}
		renderer.addRenderElement(world);
	}

	@Override
	public void _c_addActionsToEvents(EventManager eventManager,
			CustomGLSurfaceView arView, SystemUpdater updater) {
		/*
		arView.addOnTouchMoveListener(new ActionMoveCameraBuffered(camera, 5,
				25));
		*/
		ActionRotateCameraBuffered rot = new ActionRotateCameraBuffered(camera);
		updater.addObjectToUpdateCycle(rot);
		eventManager.addOnOrientationChangedAction(rot);

		eventManager.addOnTrackballAction(new ActionMoveCameraBuffered(camera,
				5, 25));
		// eventManager.addOnLocationChangedAction(gpsAction);
	}

	@Override
	public void _d_addElementsToUpdateThread(SystemUpdater updater) {
		updater.addObjectToUpdateCycle(world);
	}

	@Override
	public void _e2_addElementsToGuiSetup(GuiSetup guiSetup, Activity activity) {
/*		guiSetup.setRightViewAllignBottom();

		guiSetup.addImangeButtonToRightView(R.drawable.arrow_up_float,
				new Command() {
					@Override
					public boolean execute() {
						camera.changeZPositionBuffered(+ZDELTA);
						return false;
					}
				});
		guiSetup.addImangeButtonToRightView(R.drawable.arrow_down_float,
				new Command() {
					@Override
					public boolean execute() {
						camera.changeZPositionBuffered(-ZDELTA);
						return false;
					}
				});

		guiSetup.addButtonToBottomView(new Command() {

			@Override
			public boolean execute() {
				gpsAction.resetWorldZeroPositions(camera.getGPSLocation());
				return false;
			}
		}, "Reset world zero pos");

		guiSetup.addButtonToBottomView(new DebugCommandPositionEvent(gpsAction,
				posA), "Go to pos A");
		guiSetup.addButtonToBottomView(new DebugCommandPositionEvent(gpsAction,
				posB), "Go to pos B");
		guiSetup.addButtonToBottomView(new DebugCommandPositionEvent(gpsAction,
				posC), "Go to pos C");
		guiSetup.addButtonToBottomView(new DebugCommandPositionEvent(gpsAction,
				posD), "Go to pos D");
		guiSetup.addButtonToBottomView(new DebugCommandPositionEvent(gpsAction,
				posE), "Go to pos E");

		addSpawnButtonToUI(posA, "Spawn at posA", guiSetup);
		addSpawnButtonToUI(posB, "Spawn at posB", guiSetup);
		addSpawnButtonToUI(posC, "Spawn at posC", guiSetup);
		addSpawnButtonToUI(posD, "Spawn at posD", guiSetup);
		addSpawnButtonToUI(posE, "Spawn at posE", guiSetup);

		addGpsPosOutputButtons(guiSetup);
*/
		for (int i =1; i < steps.size(); i++){
			MeshComponent mesh = GLFactory.getInstance().newDirectedPath(steps.get(i-1), steps.get(i), Color.blueTransparent());
			Edge x = new Edge(steps.get(i-1), steps.get(i),mesh);
			/*
			CommandShowToast.show(myTargetActivity, "Object spawned at "
					+ x.getMySurroundGroup().getPosition());
			*/
			world.add(x);
		}
	}

	private void addGpsPosOutputButtons(GuiSetup guiSetup) {
		guiSetup.addButtonToBottomView(new CommandInUiThread() {

			@Override
			public void executeInUiThread() {
				Vec pos = camera.getGPSPositionVec();
				String text = "latitude=" + pos.y + ", longitude=" + pos.x;
				CommandShowToast.show(myTargetActivity, text);
			}
		}, "Show Camera GPS pos");

		guiSetup.addButtonToBottomView(new CommandInUiThread() {

			@Override
			public void executeInUiThread() {
				GeoObj pos = EventManager.getInstance()
						.getCurrentLocationObject();
				String text = "latitude=" + pos.getLatitude() + ", longitude="
						+ pos.getLongitude();
				CommandShowToast.show(myTargetActivity, text);
			}
		}, "Show real GPS pos");

		guiSetup.addButtonToBottomView(new CommandInUiThread() {

			@Override
			public void executeInUiThread() {
				GeoObj pos = EventManager.getInstance()
						.getZeroPositionLocationObject();
				String text = "latitude=" + pos.getLatitude() + ", longitude="
						+ pos.getLongitude();
				CommandShowToast.show(myTargetActivity, text);
			}
		}, "Show zero GPS pos");
	}

	private void addSpawnButtonToUI(final GeoObj pos, String buttonText,
			GuiSetup guiSetup) {
		guiSetup.addButtonToTopView(new Command() {
			@Override
			public boolean execute() {

				MeshComponent mesh = GLFactory.getInstance().newArrow();
				spawnObj(pos, mesh);
				return true;
			}

		}, buttonText);
	}

	private void spawnObj(final GeoObj pos, MeshComponent mesh) {
		GeoObj x = new GeoObj(pos);

		mesh.setPosition(Vec.getNewRandomPosInXYPlane(new Vec(), 0.1f, 1f));
		x.setComp(mesh);
		/*
		CommandShowToast.show(myTargetActivity, "Object spawned at "
				+ x.getMySurroundGroup().getPosition());
		*/
		world.add(x);
	}
}

