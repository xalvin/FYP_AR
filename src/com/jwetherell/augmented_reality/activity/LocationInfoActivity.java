package com.jwetherell.augmented_reality.activity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import system.ArActivity;

import com.jwetherell.augmented_reality.R;
import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.data.LocalDataSource;

import de.rwth.setups.PositionTestsSetup;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class LocationInfoActivity extends Activity{
	private float[] destination;
	private String name;
	private String imgRef;
	private boolean dirtyBit;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			System.setErr(new PrintStream(new FileOutputStream(new File("sdcard/ARErrLog.txt"), true)));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try{
			Bundle data = this.getIntent().getExtras();
			
			//this.destination = new Vector(dest[0],dest[1],dest[2]);
			this.destination = data.getFloatArray("destination");
			this.name = data.getString("name");
			if (data.getString("imgRef")!=null)
				this.imgRef = data.getString("imgRef");
			else
				this.imgRef = "";
		} catch(NullPointerException npe){
			npe.printStackTrace();
		}
		dirtyBit=false;
		
		setContentView(R.layout.defaultlistitemview);
		if (destination == null) throw new NullPointerException("destination location cannot be loaded");
		
		//((TextView) findViewById(R.id.lat)).setText("current: x "+current[0]+" y "+current[1]+" z "+current[2]+"\n");
		try{
			if(!this.imgRef.equals("")){
				String url = "https://maps.googleapis.com/maps/api/place/photo?key="+this.getResources().getString(R.string.google_places_api_key)+"&sensor=true&maxwidth=300&photoreference="+this.imgRef;
				new DownloadImageTask((ImageView) findViewById(R.id.markerImg)).execute(url);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		((TextView) findViewById(R.id.markerName)).setText(this.name);
		//((TextView) findViewById(R.id.location)).setText("destination: x "+destination[0]+"\n\t\t\ty "+destination[1]+"\n\t\t\tz "+destination[2]);
		((ImageButton) findViewById(R.id.routeMeThere)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*
				Bundle bundle = new Bundle();
	            bundle.putFloatArray("destination", destination);
	            bundle.putFloatArray("current", current);
	            */
	            Activity theCurrentActivity = LocationInfoActivity.this;
				RoutingActivity.startWithSetup(theCurrentActivity,
						new RoutingSetup(destination));
	            //ARActivityPlusMaps.startWithSetup(OpenGLActivity.this,new ARNavigatorSetup(),bundle);
			}
		});
		final ImageButton add = (ImageButton) findViewById(R.id.addFavourite);
		final ImageButton remove = (ImageButton) findViewById(R.id.removeFavourite);
		remove.setOnClickListener(new View.OnClickListener(){
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
					removeFromJson(name);
					add.setVisibility(ImageButton.VISIBLE);
					remove.setVisibility(ImageButton.GONE);
					dirtyBit=true;
			}
		});
		add.setOnClickListener(new View.OnClickListener(){
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
					appendToJson(destination,name,imgRef);
					remove.setVisibility(ImageButton.VISIBLE);
					add.setVisibility(ImageButton.GONE);
					dirtyBit=true;
			}
		});
		
		if(checkExists(this.name)){
			remove.setVisibility(ImageButton.VISIBLE);
			add.setVisibility(ImageButton.GONE);
		} else{
			add.setVisibility(ImageButton.VISIBLE);
			remove.setVisibility(ImageButton.GONE);
		}
		((ImageButton) findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i=new Intent();
				Bundle b=new Bundle();
				b.putBoolean("dirtyBit", dirtyBit);
				i.putExtras(b);
				setResult(RESULT_OK,i);
				finish();
			}
		});
	}
	
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	    ImageView bmImage;

	    public DownloadImageTask(ImageView bmImage) {
	        this.bmImage = bmImage;
	    }

	    protected Bitmap doInBackground(String... urls) {
	        String urldisplay = urls[0];
	        Bitmap mIcon11 = null;
	        try {
	            InputStream in = new java.net.URL(urldisplay).openStream();
	            mIcon11 = BitmapFactory.decodeStream(in);
	        } catch (Exception e) {
	            Log.e("Error", e.getMessage());
	            e.printStackTrace();
	        }
	        return mIcon11;
	    }

	    protected void onPostExecute(Bitmap result) {
	        bmImage.setImageBitmap(result);
	    }
	}
	
	//append new favourite destination to JSON file
	/* JSON format:
		{
			"data":
			[
				{
					"name" : "location_name"
					"destination":
					{
						"x" : (float)lat
						"y" : (float)lon
						"z" : (float)alt
					}
					"imgRef" : "image_reference"
				}...
			]
		}
				
	
	*/
	public static void appendToJson(float[] destination,String name,String imgRef){       
       File jsonFile = new File("sdcard/localData.json");
       String jsonStr = null;
       if (!jsonFile.exists())
       {
          try
          {
        	  jsonFile.createNewFile();
          } 
          catch (IOException e)
          {
             // TODO Auto-generated catch block
             e.printStackTrace();
          }
       }else{
    	   FileInputStream in = null;
	       try{
	    	   // read exist json string
	    	   in = new FileInputStream(jsonFile);
	    	   FileChannel fc = in.getChannel();
               MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
               jsonStr = Charset.defaultCharset().decode(bb).toString();
	       }catch(Exception e){
	    	   e.printStackTrace();
	       }finally{
	    	  try {
	    		   in.close();
	    	  } catch (IOException e) {
					e.printStackTrace();
	    	  }
	       }
       }
		try
		{
			JSONObject root = new JSONObject();
			if(jsonStr!=null){
				try {
					root = new JSONObject(jsonStr);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			JSONArray a = null;
			try {
				try{
					a = root.getJSONArray("data");
				} catch(Exception e){
					a = new JSONArray();
				}
				JSONObject obj = new JSONObject();
				obj.put("name", name);
				JSONObject dest = new JSONObject();
				dest.put("x", destination[0]);
				dest.put("y", destination[1]);
				dest.put("z", destination[2]);
				obj.put("destination",dest);
				obj.put("imgRef", imgRef);
				a.put(obj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if(a!=null){
				root = new JSONObject();
				root.put("data",a);
			}
			try{
				FileWriter out = new FileWriter(jsonFile,false);
				out.write(root.toString());
				out.flush();
				out.close();
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
       catch (Exception e)
       {
          // TODO Auto-generated catch block
          e.printStackTrace();
       }
    }
	
	//check if destination exists in data file already
	public static boolean checkExists(String name){
	       File jsonFile = new File("sdcard/localData.json");
	       String jsonStr = null;
	       if (!jsonFile.exists())
	       {
	          return false;
	       }else{
	    	   FileInputStream in = null;
		       try{
		    	   // read exist json string
		    	   in = new FileInputStream(jsonFile);
		    	   FileChannel fc = in.getChannel();
	               MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
	               jsonStr = Charset.defaultCharset().decode(bb).toString();
		       }catch(Exception e){
		    	   e.printStackTrace();
		    	   return false;
		       }finally{
		    	  try {
		    		   in.close();		    		   
		    	  } catch (IOException e) {
						e.printStackTrace();
						return false;
		    	  }
		       }
	       }
	       try
			{
	    	   
				if(jsonStr==null) return false;
				JSONObject root=null;
				try {
					root = new JSONObject(jsonStr);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				try {
					JSONArray a = root.getJSONArray("data");
					for(int i = 0;i<a.length();i++){
						if(a.getJSONObject(i).getString("name").equals(name))
							return true;
					}
					return false;
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				
			}
	       catch (Exception e)
	       {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
	          return false;
	       }
	}
	
	//check if destination exists in data file already
	public static void removeFromJson(String name){       
	       File jsonFile = new File("sdcard/localData.json");
	       String jsonStr = null;
	       if (!jsonFile.exists())
	       {
	          return;
	       }else{
	    	   FileInputStream in = null;
		       try{
		    	   // read exist json string
		    	   in = new FileInputStream(jsonFile);
		    	   FileChannel fc = in.getChannel();
	               MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
	               jsonStr = Charset.defaultCharset().decode(bb).toString();
		       }catch(Exception e){
		    	   e.printStackTrace();
		    	   return;
		       }finally{
		    	  try {
		    		   in.close();
		    	  } catch (IOException e) {
						e.printStackTrace();
						return;
		    	  }
		       }
	       }
	       try
			{
	    	   
				if(jsonStr==null) return;
				JSONObject root=null;
				try {
					root = new JSONObject(jsonStr);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				JSONArray a = null;
				try {
					try{
						a = root.getJSONArray("data");
					} catch(Exception e){
						return;
					}
					JSONArray temp = new JSONArray();
					int i;
					for(i = 0;i<a.length();i++){
						if(!a.getJSONObject(i).getString("name").equals(name))
							temp.put(a.getJSONObject(i));
					}
					a = temp;
				} catch (JSONException e) {
					e.printStackTrace();
					return;
				}
				
				if(a!=null){
					root = new JSONObject();
					root.put("data",a);
				}
				try{
					FileWriter out = new FileWriter(jsonFile,false);
					out.write(root.toString());
					out.flush();
					out.close();
				}catch(IOException ex){
					ex.printStackTrace();
				}				
			}
	       catch (Exception e)
	       {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
	          return;
	       }
	}
	
	public void finish(){
		Intent i=new Intent();
		Bundle b=new Bundle();
		b.putBoolean("dirtyBit", dirtyBit);
		i.putExtras(b);
		setResult(RESULT_OK,i);
		super.finish();
	}
}
