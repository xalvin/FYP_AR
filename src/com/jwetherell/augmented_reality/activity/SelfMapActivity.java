package com.jwetherell.augmented_reality.activity;

import java.util.ArrayList;

import geo.GeoObj;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jwetherell.augmented_reality.R;


public class SelfMapActivity extends Activity implements OnMapReadyCallback{
	private ArrayList<GeoObj> points;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try{
			Bundle b = this.getIntent().getExtras();
			if(b != null){
				Log.v("MapActivity","bundle get not null");
				ArrayList<Float> p =(ArrayList<Float>) b.getSerializable("points");
				try{
					points=new ArrayList<GeoObj>();
					//keep getting points from p, lat,lon pairs
					while(true){
						float lat = p.remove(0);
						float lon = p.remove(0);
						points.add(new GeoObj(lat,lon));
					}
				}catch(Exception e){
					//ignore
				}
			}else
				Log.v("MapActivity","bundle get is null");
		}catch(Exception e){
			Log.v("MapActivity","error in parsing points");
			e.printStackTrace();
		}
		if(points == null) {
			Log.v("MapActivity","points get is null");
			throw new NullPointerException();
		}
		setContentView(R.layout.maplayout);
		MapFragment smf = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
		
		if(smf != null) smf.getMapAsync(this);
	}
	
	@Override
	public void onMapReady(GoogleMap map) {
		//Plot route
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
