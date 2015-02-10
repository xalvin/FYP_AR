package com.jwetherell.augmented_reality.activity;

import geo.GeoObj;

import java.util.ArrayList;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
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
		map.setMyLocationEnabled(true);
		if(map == null || points == null || points.size()==0) return;
		int size = points.size();
		GeoObj start = points.get(0).get(0);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(
				new LatLng(start.getLatitude(),start.getLongitude()),16));
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
			pl.color(COLORLIST[i%COLORLIST.length]);
			pl.width(10);
			Log.v("map","add to map");
			map.addPolyline(pl);
		}
	}
	
	public void plotRouteOnMap(GoogleMap map, ArrayList<ArrayList<GeoObj>> pts){
		map.clear();
		map.setMyLocationEnabled(true);
		int size = pts.size();
		GeoObj start = pts.get(0).get(0);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(
				new LatLng(start.getLatitude(),start.getLongitude()),16));
		//PolylineOptions pl = new PolylineOptions().geodesic(true);
		for(int i =0; i<size;i++){
			Log.v("map","plot line "+i);
			PolylineOptions pl = new PolylineOptions().geodesic(true);
			ArrayList<GeoObj> objList = pts.get(i);
			Log.v("map","size "+objList.size());
			for(GeoObj obj:objList){
				Log.v("map","add GeoObj "+obj);
				pl.add(new LatLng(obj.getLatitude(),obj.getLongitude()));
			}
			pl.color(COLORLIST[i%COLORLIST.length]);
			pl.width(10);
			Log.v("map","add to map");
			map.addPolyline(pl);
		}
	}
}