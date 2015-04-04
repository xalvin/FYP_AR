package com.jwetherell.augmented_reality.activity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.SensorEvent;
import android.location.Location;
import android.location.LocationListener;  
import android.location.LocationManager; 
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jwetherell.augmented_reality.R;
import com.jwetherell.augmented_reality.common.Vector;
import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.data.GooglePlacesDataSource;
import com.jwetherell.augmented_reality.data.LocalDataSource;
import com.jwetherell.augmented_reality.data.NetworkDataSource;
import com.jwetherell.augmented_reality.data.TravelDataSource;
import com.jwetherell.augmented_reality.data.WikipediaDataSource;
import com.jwetherell.augmented_reality.ui.IconMarker;
import com.jwetherell.augmented_reality.ui.Marker;
import com.jwetherell.augmented_reality.widget.VerticalTextView;


/**
 * This class extends the AugmentedReality and is designed to be an example on
 * how to extends the AugmentedReality class to show multiple data sources.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class Demo extends AugmentedReality {
	
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("@#");
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

    private final String[] types = new String[]{"Accommodation","Entertatment","Food","Sightseeing","Shopping","Transport"};
	private final String[] forSearch = new String[]{
			"campground|lodging",
			"amusement_park|aquarium|art_gallery|night_club|park",
			"bar|cafe|food|meal_delivery|meal_takeaway|restaurant",
			"church|city_hall|hindu_temple|mosque|museum|place_of_worship|stadium|synagogue|zoo",
			"clothing_store|convenience_store|department_store|electronics_store|florist|furniture_store|grocery_or_supermarket|home_goods_store|jewelry_store|liquor_store|shoe_store|shopping_mall|store",
			"bus_station|subway_station|taxi_stand|train_station"
		};
	private Map<String,String> translator = null;
	
	private boolean[] selected = new boolean[types.length];
	private boolean preset = false;

	protected static View allPlaceView = null;
	protected static LinearLayout leftPlaces = null;
	protected static LinearLayout rightPlaces = null;
	
	private ArrayList<String> pool = new ArrayList<String>();
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add my view to UI
        LayoutParams augLayout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        allPlaceView = this.getLayoutInflater().inflate(R.layout.places, null);
        addContentView(allPlaceView,augLayout);
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


        leftPlaces = (LinearLayout) findViewById(R.id.left);
        rightPlaces = (LinearLayout) findViewById(R.id.right);
        /*
        place = new TextView(this);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.bottomMargin = 10;
        place.setLayoutParams(param);
        place.setMaxWidth(150);
        place.setBackgroundResource(R.color.wordsBg);
		*/
        
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
        translator = new ConcurrentHashMap<String,String>();
        int len = types.length;
        for(int i =0;i<len;i++){
    		translator.put(types[i], forSearch[i]);
    	}
        selected = new boolean[len];
        File folder = new File(Environment.getExternalStorageDirectory().getPath()+"/com.jwetherell.augmented_reality/data/");
        folder.mkdirs();
        File jsonFile = new File(folder,"ARTypeSelected.json");
        if (!jsonFile.exists())
        {
        	for(int i =0;i<len;i++){
            	selected[i]=true;
            }
        }else{
        	preset = true;
        	FileInputStream in = null;
        	String jsonStr = null;
        	try {
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
        	/*
        	{
        		"types":
        			[
	        			typeIndex(int),...
	        		]
        	}
        	*/
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
				a = root.getJSONArray("types");
				for(int i = 0;i<a.length();i++){
					int index = a.getInt(i);
					selected[index] = true;
				}
			} catch(Exception e){
				e.printStackTrace();
			}
        }
        
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
        if(!preset){
        	Log.i(TAG,""+preset);
        	typeSearch();
        }
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
	
	private void typeSearch(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
    	builder.setMultiChoiceItems(types, selected,
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
    					//fixing text format, saving into file
    					String st = "";
    					File folder = new File(Environment.getExternalStorageDirectory().getPath()+"/com.jwetherell.augmented_reality/data/");
    			        folder.mkdirs();
    			        File jsonFile = new File(folder,"ARTypeSelected.json");
    			        if (!jsonFile.exists())
    			        {
    			        	try {
								jsonFile.createNewFile();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
    			        }
    			        FileWriter out = null;
    			        try {
    			        	out = new FileWriter(jsonFile,false);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    			        JSONObject root = new JSONObject();
    			        JSONArray array = new JSONArray();
    					for(int i =0;i<types.length;i++){
    						if(selected[i]){
    							array.put(i);
    							if(st.equals("")){
    								st = translator.get(types[i]);
    							}else{
    								st += "|"+translator.get(types[i]);
    							}
    						}
    					}
    					try {
							root.put("types",array);
							out.write(root.toString());
							out.flush();
							out.close();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
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
    			        
    			        Log.i(TAG, "update left places.");
    			        leftPlaces.post(new Runnable(){
    			        	public void run(){
    			        		leftPlaces.removeAllViews();
    			        		updateView(ARData.getLeftMarkers(),leftPlaces);
    			        	}
    			        });
    			        Log.i(TAG, "update right places.");
    					rightPlaces.post(new Runnable(){
    			        	public void run(){
    			        		rightPlaces.removeAllViews();
    			        		updateView(ARData.getRightMarkers(),rightPlaces);
    			        	}
    			        });
    				}
    			}).setNegativeButton("cancel",null);
    	AlertDialog alert = builder.create();  
		alert.show();
	}
	
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
            	typeSearch();
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
        
        Log.i(TAG, "update left places.");
        leftPlaces.post(new Runnable(){
        	public void run(){
        		leftPlaces.removeAllViews();
        		updateView(ARData.getLeftMarkers(),leftPlaces);
        	}
        });
        Log.i(TAG, "update right places.");
		rightPlaces.post(new Runnable(){
        	public void run(){
        		rightPlaces.removeAllViews();
        		updateView(ARData.getRightMarkers(),rightPlaces);
        	}
        });
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
    
    public void onSensorChanged(SensorEvent evt) {
        super.onSensorChanged(evt);
        //update list in ARData        
        ARData.updateList();
        //update view
        Log.i(TAG, "update left places.");
        leftPlaces.post(new Runnable(){
        	public void run(){
        		leftPlaces.removeAllViews();
        		updateView(ARData.getLeftMarkers(),leftPlaces);
        	}
        });
        Log.i(TAG, "update right places.");
		rightPlaces.post(new Runnable(){
        	public void run(){
        		rightPlaces.removeAllViews();
        		updateView(ARData.getRightMarkers(),rightPlaces);
        	}
        });        
    }
    
    

    private synchronized void updateView(List<Marker> mList, LinearLayout viewToAdd){
    	for(Marker m : mList){
    		Log.i(TAG, "marker in, name = "+m.getName());
    		Log.i(TAG, "marker img reference = "+m.getImgReference());
    		if(m.getImgReference()==null)
    			continue;
	    	File folder = new File(Environment.getExternalStorageDirectory().getPath()+"/com.jwetherell.augmented_reality/imgs/");
	    	folder.mkdirs();
	    	File img = new File(folder,fileName(m.getImgReference())+"-s");
	    	LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	        param.bottomMargin = 5;
	        ImageView place = new ImageView(getBaseContext());
	        place.setLayoutParams(param);
	        place.setMaxWidth(50);
	        place.setMaxHeight(50);
	        Log.i(TAG, "add image view to layout");
	        viewToAdd.addView(place);
	    	if(img.exists()){
	    		try{
	    			FileInputStream in = new FileInputStream(img);
	    			Bitmap mIcon11 = BitmapFactory.decodeStream(in);
	    			in.close();
	    			place.setImageBitmap(mIcon11);
	    		}catch(Exception e){}
	    	}else{
	    		//while(true){
	    			try{
	    				synchronized (pool){
		    				if(!pool.contains(m.getImgReference())){
		    					pool.add(m.getImgReference());
		    					new DownloadImageTask(place).execute(m.getImgReference(),m.getkey());
		    				}
	    				}
	    				//break;
	    			}catch(RejectedExecutionException e){
	    				Log.i(TAG,"Thread Pool is full, retrying");
	    			}
	    		//}
	    		
	    	}
	    	
    	}
    }
    
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>{
    	ImageView bmImage;
    	
    	public DownloadImageTask(ImageView i){
    		bmImage = i;
    	}
    	
    	@Override
		protected Bitmap doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			String ref = arg0[0];
			String key = arg0[1];
			String url = "https://maps.googleapis.com/maps/api/place/photo?sensor=true&maxwidth=96&maxheight=96&photoreference="+ref+"&key="+key;
			Bitmap mIcon11 = null;
			try {
	            InputStream in = new java.net.URL(url).openStream();
	            mIcon11 = BitmapFactory.decodeStream(in);
	            Log.i(TAG, "download success");
	        } catch (Exception e) {
	        	Log.e(TAG, "Fail to download image");
	            Log.e(TAG, e.getMessage());
	            synchronized (pool){
	            	pool.remove(ref);
	            }
	        }
			//save image to storage
			Log.i(TAG, "saving image file");
	        File folder = new File(Environment.getExternalStorageDirectory().getPath()+"/com.jwetherell.augmented_reality/imgs/");
	        folder.mkdirs();
	        File img = new File(folder,fileName(ref)+"-s");
	        try {
        		FileOutputStream out = new FileOutputStream(img);
        		mIcon11.compress(Bitmap.CompressFormat.JPEG, 90, out);
        		out.close();
        	}catch(Exception e){
        		Log.e(TAG,"Error on saving image to storage");
        		Log.e(TAG,e.getLocalizedMessage());
        	}	   
	        return mIcon11;
		}
		
		protected void onPostExecute(Bitmap result) {
			Log.i(TAG, "setting image to view");
			bmImage.setImageBitmap(result);
	    }
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
				
				Log.i(TAG, "update left places.");
		        leftPlaces.post(new Runnable(){
		        	public void run(){
		        		leftPlaces.removeAllViews();
		        		updateView(ARData.getLeftMarkers(),leftPlaces);
		        	}
		        });
		        Log.i(TAG, "update right places.");
				rightPlaces.post(new Runnable(){
		        	public void run(){
		        		rightPlaces.removeAllViews();
		        		updateView(ARData.getRightMarkers(),rightPlaces);
		        	}
		        });
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
        Log.i(TAG, "update left places.");
        /*
        leftPlaces.post(new Runnable(){
        	public void run(){
        		leftPlaces.removeAllViews();
        		updateView(ARData.getLeftMarkers(),leftPlaces);
        	}
        });
        Log.i(TAG, "update right places.");
		rightPlaces.post(new Runnable(){
        	public void run(){
        		rightPlaces.removeAllViews();
        		updateView(ARData.getRightMarkers(),rightPlaces);
        	}
        });
        */
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

    private boolean download(NetworkDataSource source, double lat, double lon, double alt) {
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
        Log.i(TAG, "update left places.");
        leftPlaces.post(new Runnable(){
        	public void run(){
        		leftPlaces.removeAllViews();
        		updateView(ARData.getLeftMarkers(),leftPlaces);
        	}
        });
        Log.i(TAG, "update right places.");
		rightPlaces.post(new Runnable(){
        	public void run(){
        		rightPlaces.removeAllViews();
        		updateView(ARData.getRightMarkers(),rightPlaces);
        	}
        });
        return true;
    }
    
    
    public static void appendLog(String text)
    {       
       File folder = new File(Environment.getExternalStorageDirectory().getPath()+"/com.jwetherell.augmented_reality/log/");
       folder.mkdirs();
       File logFile = new File(folder,"ARlog.txt");
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
    
    private static String fileName(String ref){
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(ref.getBytes());
			byte[] digest = md.digest();
			StringBuffer sb = new StringBuffer();
			for (byte b : digest) {
				sb.append(String.format("%02x", b & 0xff));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			return null;
		}
	}
}
