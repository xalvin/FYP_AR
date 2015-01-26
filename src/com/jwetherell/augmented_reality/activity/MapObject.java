package com.jwetherell.augmented_reality.activity;

import geo.GeoObj;

import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapObject implements OnMapReadyCallback{
	private ArrayList<ArrayList<GeoObj>> points;
	public static final int[] COLORLIST = new int[]{0xff0000,0x00ff00,0x0000ff,0x00ffff,0xff00ff,0x000000};
	
	public MapObject(ArrayList<ArrayList<GeoObj>> points){
		this.points= points;
	}
	
	@Override
	public void onMapReady(GoogleMap map) {
		//polt route
		if(map == null || points.size()==0) return;
		int size = points.size();
		GeoObj start = points.get(0).get(0);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(
				new LatLng(start.getLatitude(),start.getLongitude()),15));
		//PolylineOptions pl = new PolylineOptions().geodesic(true);
		ArrayList<PolylineOptions> pls = new ArrayList<PolylineOptions>();
		for(int i =0; i<size;i++){
			PolylineOptions pl = new PolylineOptions().geodesic(true);;
			ArrayList<GeoObj> objList = points.get(i);
			for(GeoObj obj:objList)
				pl.add(new LatLng(obj.getLatitude(),obj.getLongitude()));
			try{
				pl.color(COLORLIST[i]);
			}catch(Exception e){
				pl.color((int)(Math.random()*(1<<24)));
			}
			map.addPolyline(pl);
		}
	}
}