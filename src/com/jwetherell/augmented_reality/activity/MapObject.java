package com.jwetherell.augmented_reality.activity;

import geo.GeoObj;

import java.util.ArrayList;

import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapObject implements OnMapReadyCallback{
	private ArrayList<ArrayList<GeoObj>> points;
	public static final int[] COLORLIST = new int[]{0x77ff0000,0x7700ff00,0x770000ff,0x7700ffff,0x77ff00ff,0x77000000};
	
	public MapObject(ArrayList<ArrayList<GeoObj>> points){
		this.points= points;
	}
	
	@Override
	public void onMapReady(GoogleMap map) {
		//plot route
		if(map == null || points.size()==0) return;
		int size = points.size();
		GeoObj start = points.get(0).get(0);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(
				new LatLng(start.getLatitude(),start.getLongitude()),15));
		//PolylineOptions pl = new PolylineOptions().geodesic(true);
		for(int i =0; i<size;i++){
			Log.v("map","plot line "+i);
			PolylineOptions pl = new PolylineOptions().geodesic(true);
			ArrayList<GeoObj> objList = points.get(i);
			Log.v("map","size "+objList.size());
			for(GeoObj obj:objList){
				Log.v("map","add GeoObj "+obj);
				pl.add(new LatLng(obj.getLatitude(),obj.getLongitude()));
			}
			try{
				pl.color(COLORLIST[i]);
			}catch(Exception e){
				pl.color((int)(Math.random()*(1<<24)));
			}
			pl.width(10);
			Log.v("map","add to map");
			map.addPolyline(pl);
		}
	}
}