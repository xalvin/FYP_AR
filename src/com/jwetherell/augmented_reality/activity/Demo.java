package com.jwetherell.augmented_reality.activity;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
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
import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.data.GooglePlacesDataSource;
//import com.jwetherell.augmented_reality.data.GooglePlacesDataSource;
import com.jwetherell.augmented_reality.data.LocalDataSource;
import com.jwetherell.augmented_reality.data.NetworkDataSource;
import com.jwetherell.augmented_reality.data.TravelDataSource;
import com.jwetherell.augmented_reality.data.WikipediaDataSource;
import com.jwetherell.augmented_reality.ui.Marker;
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
    private static final ThreadPoolExecutor exeService = new ThreadPoolExecutor(1, 1, 20, TimeUnit.SECONDS, queue);
    private static final Map<String, NetworkDataSource> sources = new ConcurrentHashMap<String, NetworkDataSource>();
    private static final int MIN_TIME = 10 * 1000;
    private static final int MIN_DISTANCE = 100;
    
    private static Toast myToast = null;
    private static VerticalTextView text = null;
    private LocationManager locationMgr=null;  

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
        	//GPS
        locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        	//local data source (user added)
        LocalDataSource localData = new LocalDataSource(this.getResources());
        ARData.addMarkers(localData.getMarkers());
        	
        	// network data sources (search by google)
        /*
        NetworkDataSource wikipedia = new WikipediaDataSource(this.getResources());
        sources.put("wiki", wikipedia);
        
        NetworkDataSource googlePlaces = new GooglePlacesDataSource(this.getResources());
        sources.put("googlePlaces", googlePlaces);
        */
        
        NetworkDataSource travel = new TravelDataSource(this.getResources());
        sources.put("travel", travel);
        /**/
    }

    private void spandTimeMethod() {  
        try {  
            Thread.sleep(10000);  
        } catch (InterruptedException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
    } 
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
    	boolean flag = displayGpsStatus();  
    	
    	if (flag) {
    		locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
    		final ProgressDialog pd = ProgressDialog.show(this, "", "loading");
    		final Handler handler = new Handler() {  
    	        @Override  
    	        public void handleMessage(Message msg) {// run when handler receive message  
    	            pd.dismiss();// close ProgressDialog  
    	        }  
    	    };
			/* Thread for loading GPS and network data time*/  
            new Thread(new Runnable() {  
                @Override  
                public void run() {  
                    spandTimeMethod();
                    handler.sendEmptyMessage(0);
                    }  
  
                }).start(); 
            try{
            	Location l = locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            	ARData.setCurrentLocation(l);
            }catch(NullPointerException npe){
            	try{
            		locationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        			/* Thread for loading GPS and network data time*/  
                    new Thread(new Runnable() {  
                        @Override  
                        public void run() {  
                            spandTimeMethod();
                            handler.sendEmptyMessage(0);
                            }  
          
                        }).start(); 
            		Location l= locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            		ARData.setCurrentLocation(l);
            		
            	}catch(Exception e){
	            	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
	        		builder.setMessage("Your location cannot be located.\n"+npe.toString()+"\n"+e.toString());
	        		AlertDialog alert = builder.create();  
	        		alert.show();
        		}
            }
    	} else {  
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);  
    		builder.setMessage("Your Device's GPS is Disable");
    		AlertDialog alert = builder.create();  
    		alert.show();
    	} 
        super.onStart();        
        Location last = ARData.getCurrentLocation();
        updateData(last.getLatitude(), last.getLongitude(), last.getAltitude());
    }

    private Boolean displayGpsStatus() {  
		  ContentResolver contentResolver = getBaseContext()  
		  .getContentResolver();  
		  boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(contentResolver,LocationManager.GPS_PROVIDER);  
		  if (gpsStatus) {  
		   return true;  
		  
		  } else {  
		   return false;  
		  }  
    } 
    
    /**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
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
            case R.id.refresh:
            	/*
            	Location l = ARData.getCurrentLocation();
            	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        		builder.setMessage("lat: "+l.getLatitude()+"\n lon: "+l.getLongitude());
        		AlertDialog alert = builder.create();  
        		alert.show();
        		*/
            	String[] types = new String[]{"str1","str2"};
            	Boolean[] selected = new Boolean[types.length];
            	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            	builder.setSingleChoiceItems(types, 0,
            			new DialogInterface.OnClickListener() {
            				public void onClick(DialogInterface dialog, int which) {
            					dialog.dismiss();
            					Toast.makeText(getApplicationContext(), ""+which, Toast.LENGTH_SHORT).show();
            				}
            			}).setPositiveButton("ok", new DialogInterface.OnClickListener() {
            				public void onClick(DialogInterface dialog, int which) {
            					dialog.dismiss();
            				}
            			}).setNegativeButton("cancel",null);
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
        super.onLocationChanged(location);
        updateData(location.getLatitude(), location.getLongitude(), location.getAltitude());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void markerTouched(Marker marker) {
        text.setText(marker.getName());
        myToast.show();
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
}
