package com.jwetherell.augmented_reality.activity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;  
import android.location.LocationManager; 
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jwetherell.augmented_reality.R;
import com.jwetherell.augmented_reality.common.Vector;
import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.data.GooglePlacesDataSource;
import com.jwetherell.augmented_reality.data.LocalDataSource;
import com.jwetherell.augmented_reality.data.NetworkDataSource;
import com.jwetherell.augmented_reality.data.TravelDataSource;
import com.jwetherell.augmented_reality.data.WikipediaDataSource;
import com.jwetherell.augmented_reality.ui.Marker;
import com.jwetherell.augmented_reality.ui.MarkerInfo;
import com.jwetherell.augmented_reality.widget.VerticalTextView;


/**
 * This class extends the AugmentedReality and is designed to be an example on
 * how to extends the AugmentedReality class to show multiple data sources.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class Demo extends AugmentedReality {
	
    private static final String TAG = "Demo";
    private static final String locale = Locale.getDefault().getLanguage();
    private static final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(1);
    private static final ThreadPoolExecutor exeService = new ThreadPoolExecutor(1, 4, 20, TimeUnit.SECONDS, queue);
    private static final Map<String, NetworkDataSource> sources = new ConcurrentHashMap<String, NetworkDataSource>();
    private static final int MIN_TIME = 30000 * 1000;
    private static final int MIN_DISTANCE = 100;
    private static final int CONTROL=1;
    
    private static LocalDataSource localData = null;
    private static Toast myToast = null;
    private static VerticalTextView text = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create toast
        myToast = new Toast(getApplicationContext());
        myToast.setGravity(Gravity.CENTER, 0, 0);
        // Creating our custom text view, and setting text/rotation
        text = new VerticalTextView(getApplicationContext());
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        text.setLayoutParams(params);
        text.setBackgroundResource(android.R.drawable.toast_frame);
        text.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Small);
        text.setShadowLayer(2.75f, 0f, 0f, Color.parseColor("#BB000000"));
        myToast.setView(text);
        // Setting duration and displaying the toast
        myToast.setDuration(Toast.LENGTH_SHORT);

        // Local
        
        	//local data source (user added)
        
        	
        	// network data sources (search by google)
        /*
        NetworkDataSource wikipedia = new WikipediaDataSource(this.getResources());
        sources.put("wiki", wikipedia);
        
        NetworkDataSource googlePlaces = new GooglePlacesDataSource(this.getResources());
        sources.put("googlePlaces", googlePlaces);
        */
        
        localData = new LocalDataSource(this.getResources());
        ARData.addMarkers(localData.getMarkers());
        
        NetworkDataSource travel = new TravelDataSource(this.getResources());
        sources.put("travel", travel);      
        /**/
    }

    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
		/*
		locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
		final ProgressDialog pd = ProgressDialog.show(this, "", "loading");
		final Handler handler = new Handler() {  
	        @Override  
	        public void handleMessage(Message msg) {// run when handler receive message  
	            pd.dismiss();// close ProgressDialog  
	        }  
	    };
		// Thread for loading GPS and network data time  
            new Thread(new Runnable() {  
                @Override  
                public void run() {  
                    spandTimeMethod();
                    handler.sendEmptyMessage(0);
                    }  
  
                }).start(); 
        try{
        	Location l = locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        	if(l == null) throw new NullPointerException();
        }catch(NullPointerException npe){
        	try{
        		locationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
    			// Thread for loading GPS and network data time  
            new Thread(new Runnable() {  
                @Override  
                public void run() {  
                    spandTimeMethod();
                    handler.sendEmptyMessage(0);
                    }  
  
                }).start(); 
    		Location l= locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    		if(l == null) throw new NullPointerException();
    		
        	}catch(Exception e){
            	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        		builder.setMessage("Your location cannot be located.\n"+npe.toString()+"\n"+e.toString());
        		AlertDialog alert = builder.create();  
        		alert.show();
    		}
        }*/
        super.onStart();        
        //Location last = ARData.getCurrentLocation();
        //updateData(last.getLatitude(), last.getLongitude(), last.getAltitude());
          
    }

    /**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.demomenu, menu);
	    return true;
	}
	
	/**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(TAG, "onOptionsItemSelected() item=" + item);
        switch (item.getItemId()) {
            case R.id.showRadar:
                showRadar = !showRadar;
                item.setTitle(((showRadar) ? "Hide" : "Show") + " Radar");
                break;
            case R.id.showZoomBar:
                showZoomBar = !showZoomBar;
                item.setTitle(((showZoomBar) ? "Hide" : "Show") + " Zoom Bar");
                zoomLayout.setVisibility((showZoomBar) ? LinearLayout.VISIBLE : LinearLayout.GONE);
                break;
            case R.id.filter:
            	/*
            	Location l = ARData.getCurrentLocation();
            	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        		builder.setMessage("lat: "+l.getLatitude()+"\n lon: "+l.getLongitude());
        		AlertDialog alert = builder.create();  
        		alert.show();
        		*/
            	final String[] types = new String[]{"Accommodation","Entertatment","Food","Sightseeing","Shopping","Transport"};
            	final String[] forSearch = new String[]{
            			"campground|lodging",
            			"amusement_park|aquarium|art_gallery|night_club|park",
            			"bar|cafe|food|meal_delivery|meal_takeaway|restaurant",
            			"church|city_hall|hindu_temple|mosque|museum|place_of_worship|stadium|synagogue|zoo",
            			"clothing_store|convenience_store|department_store|electronics_store|florist|furniture_store|grocery_or_supermarket|home_goods_store|jewelry_store|liquor_store|shoe_store|shopping_mall|store",
            			"bus_station|subway_station|taxi_stand|train_station"
            		};
            	final Map<String,String> translator = new ConcurrentHashMap<String,String>();
            	for(int i =0;i<types.length;i++){
            		translator.put(types[i], forSearch[i]);
            	}
            	final boolean[] selected = new boolean[types.length];
            	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            	builder.setMultiChoiceItems(types, null,
            			new DialogInterface.OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface d, int indexSelected,
									boolean isChecked) {
								// TODO Auto-generated method stub
								if(isChecked){
									selected[indexSelected] = true;
								}else{
									selected[indexSelected] = false;
								}
							}
            			}).setPositiveButton("ok", new DialogInterface.OnClickListener() {
            				public void onClick(DialogInterface dialog, int id) {
            					dialog.dismiss();
            					//fixing text format
            					String st = "";
            					for(int i =0;i<types.length;i++){
            						if(selected[i]){
            							if(st.equals("")){
            								st = translator.get(types[i]);
            							}else{
            								st += "|"+translator.get(types[i]);
            							}
            						}
            					}
            					//st = st.toLowerCase().replaceAll(" ", "_");
            					appendLog("types for searching " +st);
            					//update types user selected
            					TravelDataSource t = (TravelDataSource) sources.get("travel");
            					t.setCustomTypes(st);
            					//update display data
            					ARData.resetMarkers();
            					ARData.addMarkers(localData.getMarkers());
            					Location last = ARData.getCurrentLocation();
            			        updateData(last.getLatitude(), last.getLongitude(), last.getAltitude());
            				}
            			}).setNegativeButton("cancel",null);
            	AlertDialog alert = builder.create();  
        		alert.show();
                break;
            case R.id.exit:
                finish();
                break;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLocationChanged(Location location) {
    	Log.i(TAG,"changing location "+location.getLatitude()+","+location.getLongitude());
        super.onLocationChanged(location);
        updateData(location.getLatitude(), location.getLongitude(), location.getAltitude());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void markerTouched(final Marker marker) {

		Vector dest = marker.getPhysicalLocation();
    	final float[] destination = {dest.getX(),dest.getY(),dest.getZ()};
        Bundle bundle = new Bundle();
        bundle.putFloatArray("destination", destination);
        bundle.putString("name", marker.getName());
        bundle.putString("imgRef", marker.getImgReference());
        bundle.putString("detailRef", marker.getDetailRef());
        Intent i = new Intent();
        i.setClass(this,LocationInfoActivity.class);
        i.putExtras(bundle);
        startActivityForResult(i,CONTROL);

    	/*
    	Vector dest = marker.getPhysicalLocation();
    	MarkerInfo mi = new MarkerInfo(marker.getName(),dest.getX(),dest.getY(),dest.getZ(), marker.getColor(),marker.getImgReference() , this.getResources());
    	ARData.addMarkers(mi);
    	*/
    	/*
        text.setText(marker.getName());
        myToast.show();
        */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    switch(requestCode){
	    	case CONTROL:
	    		
			    if(data.getExtras().getBoolean("dirtyBit")){
				    ARData.resetMarkers();
				    localData = new LocalDataSource(this.getResources());
					ARData.addMarkers(localData.getMarkers());
			    }
				Location last = ARData.getCurrentLocation();
				updateData(last.getLatitude(), last.getLongitude(), last.getAltitude());
				break;
	    }
	}
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateDataOnZoom() {
        super.updateDataOnZoom();
        Location last = ARData.getCurrentLocation();
        updateData(last.getLatitude(), last.getLongitude(), last.getAltitude());
    }

    private void updateData(final double lat, final double lon, final double alt) {
        try {
        	Log.i(TAG, "try download.");
            exeService.execute(new Runnable() {
                @Override
                public void run() {
                    for (NetworkDataSource source : sources.values())
                        download(source, lat, lon, alt);
                }
            });
        } catch (RejectedExecutionException rej) {
            Log.w(TAG, "Not running new download Runnable, queue is full.");
        } catch (Exception e) {
            Log.e(TAG, "Exception running download Runnable.", e);
        }
    }

    private static boolean download(NetworkDataSource source, double lat, double lon, double alt) {
        if (source == null) return false;

        String url = null;
        try {
            url = source.createRequestURL(lat, lon, alt, ARData.getRadius(), locale);
        } catch (NullPointerException e) {
            return false;
        }

        List<Marker> markers = null;
        try {
            markers = source.parse(url);
        } catch (NullPointerException e) {
            return false;
        }

        ARData.addMarkers(markers);
        return true;
    }
    
    public static void appendLog(String text)
    {       
       File logFile = new File("sdcard/ARlog.txt");
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
