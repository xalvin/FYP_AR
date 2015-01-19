package com.jwetherell.augmented_reality.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import com.jwetherell.augmented_reality.R;
import com.jwetherell.augmented_reality.ui.IconMarker;
import com.jwetherell.augmented_reality.ui.Marker;

public class TravelDataSource extends NetworkDataSource {
	private static final String URL = "https://maps.googleapis.com/maps/api/place/search/json?";
	//private static final String URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?language=en";
	private static String TYPES = "airport|amusement_park|aquarium|art_gallery|bus_station|campground|car_rental|city_hall|embassy|establishment|hindu_temple|local_governemnt_office|mosque|museum|night_club|park|place_of_worship|police|post_office|stadium|spa|subway_station|synagogue|taxi_stand|train_station|travel_agency|University|zoo";

	//default given all types
	private String keyword = "";
	private String customTypes = "";
	private static String key = null;
	//private static Bitmap amusementPark = null;
	private static Bitmap defaultIcon = null;
	//private static Bitmap wikipedia = null;

	public TravelDataSource(Resources res) {
		if (res == null) throw new NullPointerException();

		key = res.getString(R.string.google_places_api_key);

		createIcon(res);
	}

	protected void createIcon(Resources res) {
		if (res == null) throw new NullPointerException();
		
		//amusementPark = BitmapFactory.decodeResource(res, R.drawable.ferriswheel);
		defaultIcon = BitmapFactory.decodeResource(res, R.drawable.travel);
		//wikipedia = BitmapFactory.decodeResource(res, R.drawable.wikipedia);
	}

	public void setKeyword(String st){
		this.keyword = st;
	}
	
	public String getKeyword(){
		return this.keyword;
	}
	
	public void setCustomTypes(String st){
		this.customTypes = st;
	}
	
	public String getCustomTypes(){
		return this.customTypes;
	}
	@Override
	public String createRequestURL(double lat, double lon, double alt, float radius, String locale) {
		try {
			if (customTypes.equals(""))
				return URL + "&location="+lat+","+lon+"&radius="+(radius*1000.0f)+"&types="+TYPES+"&sensor=true&key="+key;
			else{
				return URL + "&location="+lat+","+lon+"&radius="+(radius*1000.0f)+"&types="+customTypes+"&sensor=true&key="+key;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */

	@Override
	public List<Marker> parse(String URL) {
		if (URL == null) throw new NullPointerException();

		InputStream stream = null;
		stream = getHttpGETInputStream(URL);
		if (stream == null) {
			Log.e("TravelDataSource","no input stream received from network");
			throw new NullPointerException();
		}

		String string = null;
		string = getHttpInputString(stream);
		if (string == null){
			Log.e("TravelDataSource","no string received from network");
			throw new NullPointerException();
		}

		JSONObject json = null;
		try {
			json = new JSONObject(string);
		} catch (JSONException e) {
			Log.e("TravelDataSource","json object creation error");
			e.printStackTrace();
		}
		if (json == null) throw new NullPointerException();

		return parse(json);
	}

	@Override
	public List<Marker> parse(JSONObject root) {
		if (root == null) throw new NullPointerException();
		appendLog("json received "+root.toString()+"\n\n\n\n");
		JSONObject jo = null;
		JSONArray dataArray = null;
		List<Marker> markers = new ArrayList<Marker>();

		try {
			if (root.has("results")) dataArray = root.getJSONArray("results");
			if (dataArray == null) {
				Log.e("TravelDataSource","empty data source");
				return markers;
			}
			int top = Math.min(MAX, dataArray.length());
			for (int i = 0; i < top; i++) {
				Log.v("TravelDataSource","creating json object"+ i);
				jo = dataArray.getJSONObject(i);
				//Log.v("TravelDataSource","creating marker"+ i);
				Marker ma = processJSONObject(jo);
				if (ma != null) {
					markers.add(ma);
				}else{
					Log.v("TravelDataSource","no marker created, json object no "+ i);
				}
			}
		} catch (JSONException e) {
			Log.e("TravelDataSource","JSONException thrown");
			e.printStackTrace();
		}
		return markers;
	}

	private Marker processJSONObject(JSONObject jo) {
		if (jo == null) throw new NullPointerException();
		//if (!jo.has("photos")) throw new NullPointerException();
		if (!jo.has("geometry")) throw new NullPointerException();

		Marker ma = null;
		try {
			String ref = null;
			if (!jo.isNull("photos")){
				try{
					JSONArray imgReference = jo.getJSONArray("photos");
					ref = imgReference.getJSONObject(0).getString("photo_reference");
					Log.v("TravelDataSource","ref "+ref);
				}catch(Exception e){
					Log.e("TravelDataSource","parse image reference error");
				}
			}
			Double lat = null, lon = null;

			if (!jo.isNull("geometry")) {
				try{
					JSONObject geo = jo.getJSONObject("geometry");
					JSONObject coordinates = geo.getJSONObject("location");
					lat = Double.parseDouble(coordinates.getString("lat"));
					lon = Double.parseDouble(coordinates.getString("lng"));
				}catch(Exception e){
					Log.e("TravelDataSource","parse geometry error");
				}
			}
			if (lat != null) {
				String user=null;
				try{
					user = jo.getString("name");
				}catch(Exception e){
					Log.e("TravelDataSource","parse name error");
					user = "";
				}
				
				JSONArray temp = jo.getJSONArray("types");
				int len = temp.length();
				String log = "";
				for(int i=0; i<len;i++){
					log+= temp.getString(i)+"\n";
				}
				if (ma==null){
					Log.v("TravelDataSource","creating marker");
					if(ref!=null)
						ma = new IconMarker(user, lat, lon, 0, Color.RED, defaultIcon,ref+"&key="+key);
					else
						ma = new IconMarker(user, lat, lon, 0, Color.RED, defaultIcon);
				}
				appendLog(log);
			}
		} catch (Exception e) {
			Log.e("TravelDataSource","error in processing JSONObject");
			e.printStackTrace();
		}
		return ma;
	}
	
	public static void appendLog(String text)
    {       
       File logFile = new File("sdcard/","ARlog.txt");
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
          String currentDateandTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
          buf.append(currentDateandTime);
          buf.append("\n");
          buf.append(text);
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
