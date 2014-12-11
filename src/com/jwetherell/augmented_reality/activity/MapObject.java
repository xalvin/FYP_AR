package com.jwetherell.augmented_reality.activity;

import java.util.ArrayList;

import geo.GeoObj;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapObject  implements OnMapReadyCallback{
	private ArrayList<GeoObj> points;
	
	public MapObject(ArrayList<GeoObj> points){
		this.points= points;
	}
	
	@Override
	public void onMapReady(GoogleMap map) {
		//polt route
		if(map == null || points.size()==0) return;
		int size = points.size();
		GeoObj start = points.get(0);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(
				new LatLng(start.getLatitude(),start.getLongitude()),14));
		PolylineOptions pl = new PolylineOptions().geodesic(true);
		for(int i =0; i<size;i++){
			GeoObj obj = points.get(i);
			pl.add(new LatLng(obj.getLatitude(),obj.getLongitude()));
		}
		map.addPolyline(pl);
	}
}
