package com.jwetherell.augmented_reality.activity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.maps.MapActivity;
import com.jwetherell.augmented_reality.R;

import geo.Edge;
import geo.GeoGraph;
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
import util.IO;
import util.Vec;
import worldData.SystemUpdater;
import worldData.World;
import actions.ActionCalcRelativePos;
import actions.ActionRotateCameraBuffered;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import commands.Command;
import commands.ui.CommandInUiThread;
import commands.ui.CommandShowToast;

public class RoutingSetup extends Setup {

	private final String TAG = "Routing Setup";
	protected static final int ZDELTA = 7;
	private final GLCamera camera;
	private final World world;
	private final ActionCalcRelativePos gpsAction;
	private float[] current;
	private float[] destination;
	private String JSONStr;
	//private Fragment map;
	/*
	private final GeoObj posA;
	private final GeoObj posB;
	private final GeoObj posC;
	private final GeoObj posD;
	private final GeoObj posE;
	*/
	private ArrayList<ArrayList<GeoObj>> steps;
	private ArrayList<String> instructions;

	static int firstPlotBit;
	private MapObject map;
	public RoutingSetup() {
		camera = new GLCamera();
		world = new World(camera);
		gpsAction = new ActionCalcRelativePos(world, camera){
			
			public boolean onLocationChanged(Location location){
				/*
				appendLog("Update camera location");
				Log.i(TAG, "Update camera location");
				camera.setGpsPos(new GeoObj(location.getLatitude(),location.getLongitude()));
				*/
				appendLog("active super.onLocationChanged");
				Log.i(TAG, "active super.onLocationChanged");
				super.onLocationChanged(location);
				return true;
			}
		};
		/*
		posA = new GeoObj(50.778922, 6.060461);
		posB = new GeoObj(50.780815, 6.06662);
		posC = new GeoObj(50.780557, 6.06735);
		posD = new GeoObj(50.779892, 6.065955);
		posE = new GeoObj(50.780408, 6.066492);
		*/
		steps = new ArrayList<ArrayList<GeoObj>>();
		instructions = new ArrayList<String>();
		map = null;
		firstPlotBit = 0;
	}

	public RoutingSetup(float[] destination) {
		camera = new GLCamera();
		world = new World(camera);
		gpsAction = new ActionCalcRelativePos(world, camera);
		this.destination = destination;
		steps = new ArrayList<ArrayList<GeoObj>>();;
		JSONStr = null;
		instructions = new ArrayList<String>();
		map = null;
		firstPlotBit = 0;
	}
	
	public void parse(JSONObject root) {
		if (root == null) throw new NullPointerException();
		JSONObject jo = null;
		JSONArray dataArray = null;
		ArrayList<GeoObj> geoObjs = null;

		try {
			if (root.has("routes")) dataArray = root.getJSONArray("routes");
			if (dataArray == null) {
				//steps=null;
				return;
			}
			int size = dataArray.length();
			for(int i =0; i<size;i++){
				jo = dataArray.getJSONObject(i);
				geoObjs = processJSONGeoObject(jo);
				//instructions = processJSONInstruction(jo);
				if (geoObjs == null) throw new NullPointerException();
				Log.v("parse",i+" passed");
				steps.add(geoObjs);
			}	
		} catch (JSONException e) {
			e.printStackTrace();
		}
		//steps = geoObjs;
	}

	private ArrayList<GeoObj> processJSONGeoObject(JSONObject jo) {
		if (jo == null) throw new NullPointerException();
		if (!jo.has("legs")) return null;
		JSONArray legsArray = null;
		
		ArrayList<GeoObj> result = new ArrayList<GeoObj>();
		StringBuilder inst = new StringBuilder();
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
			inst.append(first.getString("html_instructions"));
			for(int i=1; i<len;i++){
				JSONObject next = stepsArray.getJSONObject(i);
				JSONObject ed = next.getJSONObject("end_location");
				result.add(new GeoObj(ed.getDouble("lat"),ed.getDouble("lng")));
				//blank line on each instructions
				inst.append("<br/>");
				try{
					Log.v("parse",next.getString("html_instructions"));
					inst.append(next.getString("html_instructions"));
					inst.append("<br/>distance : ");
					inst.append(next.getJSONObject("distance").getString("text"));
					inst.append("<br/>");
				}catch(Exception e){
					Log.e("parse","fail here");
				}
			}
			if(result.get(0).getLatitude()!=current[0] && result.get(0).getLongitude()!= current[1])
				result.add(0,new GeoObj(current[0],current[1]));
			if(result.get(result.size()-1).getLatitude()!=destination[0] && result.get(result.size()-1).getLongitude()!= destination[1])
				result.add(new GeoObj(destination[0],destination[1]));
		} catch (Exception e) {
			Log.e("parse","fail within try block");
			e.printStackTrace();
			result = null;
			inst=null;
		}
		if (inst!=null)
			instructions.add(inst.toString());
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
		this.current = new float[] {(float) currentPosition.getLatitude(),(float) currentPosition.getLongitude(),(float) currentPosition.getAltitude()};
		//add points to world
		String url = "http://maps.googleapis.com/maps/api/directions/json?origin="+current[0]+","+current[1]+"&destination="+destination[0]+","+destination[1]+"&sensor=true&mode=walking&region=hk&language=en&alternatives=false";
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
		//process JSONObject
		JSONObject json = null;
		try {
			json = new JSONObject(JSONStr);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (json == null) throw new NullPointerException();
		parse(json);
		try{
			renderer.addRenderElement(world);
		}catch(Exception ex){
			CommandShowToast.show(myTargetActivity,"no JSON");
		}
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

		/*
		eventManager.addOnTrackballAction(new ActionMoveCameraBuffered(camera,
				5, 25));
		*/
		eventManager.addOnLocationChangedAction(gpsAction);
	}

	@Override
	public void _d_addElementsToUpdateThread(SystemUpdater updater) {
		updater.addObjectToUpdateCycle(world);
	}

	public void _e1_addElementsToOverlay(FrameLayout overlayView,
			Activity activity) {
		// the main.xml layout is loaded and the guiSetup is created for
		// customization. then the customized view is added to overlayView
		View sourceView = View.inflate(activity, R.layout.maplayout, null);
		MapFragment mapFragment = (MapFragment) myTargetActivity.getFragmentManager()
                .findFragmentById(R.id.map);
		map = new MapObject(steps);
        mapFragment.getMapAsync(map);
		TextView instructionView = (TextView) (sourceView.findViewById(R.id.instructions));
		try{
			instructionView.setText(Html.fromHtml(instructions.get(0)));
		}catch(Exception e){
			// do nothing
		}
		instructionView.setBackgroundColor(0xAA000000);
		Button showInst = (Button)(sourceView.findViewById(R.id.showInst));
		showInst.setVisibility(View.GONE);
		showInst.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				((Button)(myTargetActivity.findViewById(R.id.showInst))).setVisibility(View.GONE);
				((Button)(myTargetActivity.findViewById(R.id.hideInst))).setVisibility(View.VISIBLE);
				((TextView)(myTargetActivity.findViewById(R.id.instructions))).setVisibility(View.VISIBLE);
			}
			
		});
		((Button)(sourceView.findViewById(R.id.hideInst))).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				((Button)(myTargetActivity.findViewById(R.id.showInst))).setVisibility(View.VISIBLE);
				((Button)(myTargetActivity.findViewById(R.id.hideInst))).setVisibility(View.GONE);
				((TextView)(myTargetActivity.findViewById(R.id.instructions))).setVisibility(View.GONE);
			}
			
		});
		Button showMap = (Button)(sourceView.findViewById(R.id.showMap));
		showMap.setVisibility(View.GONE);
		showMap.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				((Button)(myTargetActivity.findViewById(R.id.showMap))).setVisibility(View.GONE);
				((Button)(myTargetActivity.findViewById(R.id.hideMap))).setVisibility(View.VISIBLE);
				((LinearLayout)(myTargetActivity.findViewById(R.id.LinLay_bottomRight))).setVisibility(View.VISIBLE);
			}
			
		});
		((Button)(sourceView.findViewById(R.id.hideMap))).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				((Button)(myTargetActivity.findViewById(R.id.showMap))).setVisibility(View.VISIBLE);
				((Button)(myTargetActivity.findViewById(R.id.hideMap))).setVisibility(View.GONE);
				((LinearLayout)(myTargetActivity.findViewById(R.id.LinLay_bottomRight))).setVisibility(View.GONE);
			}
			
		});
		((Button)(sourceView.findViewById(R.id.alternative))).setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				new DownloadFromURL(){
					@Override
					protected void onPostExecute(Void result){
						// get size of routes
						int size = steps.size();
						// place to add routes
						LinearLayout targetLayout = (LinearLayout)myTargetActivity.findViewById(R.id.LinLay_left);
						// reset view content
						targetLayout.removeAllViews();
						for(int j =0;j<size;j++){
							Button b = new Button(targetLayout.getContext());
							b.setText("Route "+(j+1));
							final int e = j;
							b.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									world.clear();
									ArrayList<GeoObj> stepList = steps.get(e);
									int len = stepList.size();
									
									
									for(int i=1; i< len;i++){
										MeshComponent diamond = GLFactory.getInstance().newDiamond(new Color(MapObject.COLORLIST[e]));
										final String text = "connection on point "+(i-1)+" to point "+i;
										GeoObj pre = stepList.get(i-1);
										GeoObj pos = stepList.get(i);
										final String text1 = "point number "+i;
										diamond.setOnClickCommand(new Command(){

											@Override
											public boolean execute() {
												// TODO Auto-generated method stub
												CommandShowToast.show(myTargetActivity,text1);
												return true;
											}
											
										});
										spawnObj(pos,diamond);
										
										MeshComponent mesh = GLFactory.getInstance().newDirectedPath(pre,pos,Color.blueTransparent());
										mesh.setOnClickCommand(new Command(){
							
												@Override
												public boolean execute() {
													// TODO Auto-generated method stub
													CommandShowToast.show(myTargetActivity,text);
													return true;
												}
												
											});
										Edge x = new Edge(pre,pos,mesh);
										/*
										x.setMyAltitude(x.getAltitude()-ZDELTA);
										x.refreshVirtualPosition();
										*/
										/*
										CommandShowToast.show(myTargetActivity, "Object spawned at "
												+ x.getMySurroundGroup().getPosition());
										*/
										world.add(x);
										((TextView)(myTargetActivity.findViewById(R.id.instructions))).setText(Html.fromHtml(instructions.get(e)));
										((ScrollView) (myTargetActivity.findViewById(R.id.sRight))).scrollTo(0,0);
									}
								}
							});
							targetLayout.addView(b);
						}
						MapFragment mapFragment = (MapFragment) myTargetActivity.getFragmentManager()
				                .findFragmentById(R.id.map);
				        map.plotRouteOnMap(mapFragment.getMap(), steps);
						pd.dismiss();
					}
				}.execute(true,EventManager.getInstance().getCurrentLocation());	
			}
			
		});
		//add location listener to map
		final GoogleMap gm = mapFragment.getMap();
		gm.setOnMyLocationChangeListener(new OnMyLocationChangeListener(){

				@Override
				public void onMyLocationChange(Location l) {
					LatLng loc = new LatLng(l.getLatitude(),l.getLongitude());
					gm.animateCamera(CameraUpdateFactory.newLatLng(loc));
					//gpsAction.onLocationChanged(l);
					//EventManager.getInstance().onLocationChanged(l);
					current = new float[]{(float) l.getLatitude(),(float) l.getLongitude(),(float) l.getAltitude()};
					firstPlot(l);
				}
				
			});
		guiSetup = new GuiSetup(this, sourceView);
		_e2_addElementsToGuiSetup(getGuiSetup(), activity);
		overlayView.addView(sourceView);
	}
	
	private void firstPlot(Location l){
		if(firstPlotBit>0)
			return;
		gpsAction.resetWorldZeroPositions(l);
		world.clear();
		new DownloadFromURL(){
			@Override
			protected void onPostExecute(Void result){
				try{
					// default show first route found
					ArrayList<GeoObj> first = steps.get(0);
					int size = first.size();
					
					for(int i=1; i< size;i++){
						MeshComponent diamond = GLFactory.getInstance().newDiamond(new Color(MapObject.COLORLIST[0]));
						final String text1 = "point number "+i;
						final String text = "connection on point "+(i-1)+" to point "+i;
						GeoObj pre = first.get(i-1);
						GeoObj pos = first.get(i);
						diamond.setOnClickCommand(new Command(){

							@Override
							public boolean execute() {
								// TODO Auto-generated method stub
								CommandShowToast.show(myTargetActivity,text1);
								return true;
							}
							
						});
						spawnObj(pos,diamond);
						MeshComponent mesh = GLFactory.getInstance().newDirectedPath(pre,pos,Color.blueTransparent());
						mesh.setOnClickCommand(new Command(){
			
								@Override
								public boolean execute() {
									// TODO Auto-generated method stub
									CommandShowToast.show(myTargetActivity,text);
									return true;
								}
								
							});
						Edge x = new Edge(pre,pos,mesh);
						/*
						CommandShowToast.show(myTargetActivity, "Object spawned at "
								+ x.getMySurroundGroup().getPosition());
						*/
						world.add(x);
						((TextView)(myTargetActivity.findViewById(R.id.instructions))).setText(Html.fromHtml(instructions.get(0)));
						MapFragment mapFragment = (MapFragment) myTargetActivity.getFragmentManager()
				                .findFragmentById(R.id.map);
				        map.plotRouteOnMap(mapFragment.getMap(), steps);
					}
				}catch(NullPointerException npe){
					//CommandShowToast.show(myTargetActivity,"no route found");
					npe.printStackTrace();
				}
				pd.dismiss();
			}
		}.execute(false,l);
		firstPlotBit+=1;
		return;
	}
	
	private class DownloadFromURL extends AsyncTask<Object, Void, Void> {
		ProgressDialog pd;
		
		protected void onPreExecute(){
			pd = ProgressDialog.show(myTargetActivity, "", "loading");
			steps.clear();
			instructions.clear();
		}
		
		@Override
		protected Void doInBackground(Object... params) {
			// TODO Auto-generated method stub
			//"http://maps.googleapis.com/maps/api/directions/json?sensor=true&mode=walking&region=hk&language=en&alternatives=true
			//				&origin="+current[0]+","+current[1]+"&destination="+destination[0]+","+destination[1]+"";
			boolean alternative = (Boolean) params[0];
			Location l = (Location) params[1];
			if(l!=null){
				current= new float[]{(float) l.getLatitude(),(float) l.getLongitude(),(float) l.getAltitude()};
			}
			String url ="http://maps.googleapis.com/maps/api/directions/json?origin="+current[0]+","+current[1]+"&destination="+destination[0]+","+destination[1]+"&sensor=true&mode=walking&region=hk&language=en&alternatives="+alternative;

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
			//process JSONObject
			JSONObject json = null;
			try {
				json = new JSONObject(JSONStr);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (json == null) throw new NullPointerException();
			parse(json);
			return null;
		}
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
*/


		camera.changeZPositionBuffered(+ZDELTA);
		/*
		int len = steps.size();
		for(int j =0;j<len;j++){
			final int e = j;
			guiSetup.addButtonToBottomView(new CommandInUiThread() {
	
				@Override
				public void executeInUiThread() {
					world.clear();
					ArrayList<GeoObj> stepList = steps.get(e);
					int len = stepList.size();
					
					
					for(int i=1; i< len;i++){
						MeshComponent diamond = GLFactory.getInstance().newDiamond(new Color(MapObject.COLORLIST[e]));
						final String text = "connection on point "+(i-1)+" to point "+i;
						GeoObj pre = stepList.get(i-1);
						GeoObj pos = stepList.get(i);
						final String text1 = "point number "+i;
						diamond.setOnClickCommand(new Command(){

							@Override
							public boolean execute() {
								// TODO Auto-generated method stub
								CommandShowToast.show(myTargetActivity,text1);
								return true;
							}
							
						});
						spawnObj(pos,diamond);
						
						MeshComponent mesh = GLFactory.getInstance().newDirectedPath(pre,pos,Color.blueTransparent());
						mesh.setOnClickCommand(new Command(){
			
								@Override
								public boolean execute() {
									// TODO Auto-generated method stub
									CommandShowToast.show(myTargetActivity,text);
									return true;
								}
								
							});
						Edge x = new Edge(pre,pos,mesh);
						
						
						//CommandShowToast.show(myTargetActivity, "Object spawned at "
						//		+ x.getMySurroundGroup().getPosition());
						
						world.add(x);
						((TextView)(myTargetActivity.findViewById(R.id.instructions))).setText(Html.fromHtml(instructions.get(e)));
						((ScrollView) (myTargetActivity.findViewById(R.id.scrollText))).scrollTo(0,0);
					}
				}
			}, "Route "+(e+1));
		}
		*/
		try{
			// default show first route found
			ArrayList<GeoObj> first = steps.get(0);
			int size = first.size();
			
			for(int i=1; i< size;i++){
				MeshComponent diamond = GLFactory.getInstance().newDiamond(new Color(MapObject.COLORLIST[0]));
				final String text1 = "point number "+i;
				final String text = "connection on point "+(i-1)+" to point "+i;
				GeoObj pre = first.get(i-1);
				GeoObj pos = first.get(i);
				diamond.setOnClickCommand(new Command(){

					@Override
					public boolean execute() {
						// TODO Auto-generated method stub
						CommandShowToast.show(myTargetActivity,text1);
						return true;
					}
					
				});
				spawnObj(pos,diamond);
				MeshComponent mesh = GLFactory.getInstance().newDirectedPath(pre,pos,Color.blueTransparent());
				mesh.setOnClickCommand(new Command(){
					
					@Override
					public boolean execute() {
						// TODO Auto-generated method stub
						CommandShowToast.show(myTargetActivity,text);
						return true;
					}
					
				});
				Edge x = new Edge(pre,pos,mesh);
				/*
				CommandShowToast.show(myTargetActivity, "Object spawned at "
						+ x.getMySurroundGroup().getPosition());
				*/
				world.add(x);
			}
		}catch(NullPointerException npe){
			//CommandShowToast.show(myTargetActivity,"no route found");
			npe.printStackTrace();
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
	
    public static void appendLog(String text){       
       File logFile = new File("sdcard/","RoutingLog.txt");
       if (!logFile.exists())
       {
          try
          {
        	  logFile.createNewFile();
          } 
          catch (IOException e)
          {
             // TODO Auto-generated catch block
             e.printStackTrace();
          }
       }
       try
       {
          //BufferedWriter for performance, true to set append to file flag
          BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
          buf.append("[\n\t");
          String currentDateandTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
          buf.append(currentDateandTime);
          buf.newLine();
          buf.append("\t\t"+text);
          buf.newLine();
          buf.flush();
          buf.close();
       }
       catch (IOException e)
       {
          // TODO Auto-generated catch block
          e.printStackTrace();
       }
    }
}

