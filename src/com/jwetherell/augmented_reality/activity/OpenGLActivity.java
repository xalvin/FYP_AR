package com.jwetherell.augmented_reality.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import system.ArActivity;

import com.jwetherell.augmented_reality.R;

import de.rwth.setups.PositionTestsSetup;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class OpenGLActivity extends Activity{
	private float[] destination;
	private float[] current;
	private String name;
	private String imgRef;
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
			
			//this.current = new Vector(curr[0],curr[1],curr[2]);
			this.current = data.getFloatArray("current");
			//this.destination = new Vector(dest[0],dest[1],dest[2]);
			this.destination = data.getFloatArray("destination");
			this.name = data.getString("name");
			if (data.getString("imgRef")!=null)
				this.imgRef = data.getString("imgRef");
		} catch(NullPointerException npe){
			npe.printStackTrace();
		}
		
		setContentView(R.layout.defaultlistitemview);
		if (current == null) throw new NullPointerException("current location cannot be loaded");
		if (destination == null) throw new NullPointerException("destination location cannot be loaded");
		
		//((TextView) findViewById(R.id.lat)).setText("current: x "+current[0]+" y "+current[1]+" z "+current[2]+"\n");
		try{
			if(this.imgRef!=null){
				String url = "https://maps.googleapis.com/maps/api/place/photo?key="+this.getResources().getString(R.string.google_places_api_key)+"&sensor=true&maxwidth=300&photoreference="+this.imgRef;
				new DownloadImageTask((ImageView) findViewById(R.id.markerImg)).execute(url);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		((TextView) findViewById(R.id.markerName)).setText(this.name);
		((TextView) findViewById(R.id.location)).setText("destination: x "+destination[0]+"\n\t\t\ty "+destination[1]+"\n\t\t\tz "+destination[2]);
		((Button) findViewById(R.id.routeMeThere)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*
				Bundle bundle = new Bundle();
	            bundle.putFloatArray("destination", destination);
	            bundle.putFloatArray("current", current);
	            */
	            Activity theCurrentActivity = OpenGLActivity.this;
				ArActivity.startWithSetup(theCurrentActivity,
						new RoutingSetup(current,destination));
	            //ARActivityPlusMaps.startWithSetup(OpenGLActivity.this,new ARNavigatorSetup(),bundle);
			}
		});
		((Button) findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
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
}
