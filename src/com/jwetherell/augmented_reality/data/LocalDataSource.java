package com.jwetherell.augmented_reality.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.jwetherell.augmented_reality.R;
import com.jwetherell.augmented_reality.ui.IconMarker;
import com.jwetherell.augmented_reality.ui.Marker;

/**
 * This class should be used as a example local data source. It is an example of
 * how to add data programatically. You can add data either programatically,
 * SQLite or through any other source.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class LocalDataSource extends DataSource {

    private List<Marker> cachedMarkers = new ArrayList<Marker>();
    private static Bitmap icon = null;
    private static String key = null;

    public LocalDataSource(Resources res) {
        if (res == null) throw new NullPointerException();
        key = res.getString(R.string.google_places_api_key);
        createIcon(res);
    }

    protected void createIcon(Resources res) {
        if (res == null) throw new NullPointerException();
        icon = BitmapFactory.decodeResource(res, R.drawable.travel);
    }

    public List<Marker> getMarkers() {
    	// user cached marker is being added to here
    	
        /* e.g.
        Marker atl = new IconMarker("ATL ICON", 39.931268, -75.051262, 0, Color.DKGRAY, icon);
        cachedMarkers.add(atl);

        Marker home = new Marker("ATL CIRCLE", 39.931269, -75.051231, 0, Color.YELLOW);
        cachedMarkers.add(home);
        */
    	/*
    	Marker ouhk = new IconMarker("Open University of Hong Kong", 22.3152108, 114.1802943, 0, Color.YELLOW, icon);
        cachedMarkers.add(ouhk);
        Marker testPt1 = new Marker("Avenue of stars", 22.2945141,114.1708274, 0, Color.YELLOW);
        cachedMarkers.add(testPt1);
        Marker testPt2 = new Marker("The Peak Tram", 22.2771122,114.1570673, 0, Color.YELLOW);
        cachedMarkers.add(testPt2);
        */
    	/*
    	Marker hku = new Marker("The University of Hong Kong", 22.282999,114.137085,0,Color.YELLOW);
    	cachedMarkers.add(hku);
    	Marker philipDental = new Marker("Prince Philip Dental Hospital", 22.2862335,114.14393,0,Color.YELLOW);
    	cachedMarkers.add(philipDental);
    	*/
    	File jsonFile = new File("sdcard/localData.json");
        String jsonStr = null;
        if (jsonFile.exists()){
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
				a = root.getJSONArray("data");
				for(int i = 0;i<a.length();i++){
					JSONObject obj = a.getJSONObject(i);
					JSONObject dest = obj.getJSONObject("destination");
					IconMarker temp = new IconMarker(obj.getString("name"), dest.getDouble("x"),dest.getDouble("y"),dest.getDouble("z"),Color.YELLOW,icon,obj.getString("imgRef"),obj.getString("detailRef"),key);
					cachedMarkers.add(temp);
				}
			} catch(Exception e){
				e.printStackTrace();
			}
        }
        /*
         * Marker lon = new IconMarker(
         * "I am a really really long string which should wrap a number of times on the screen."
         * , 39.95335, -74.9223445, 0, Color.MAGENTA, icon);
         * cachedMarkers.add(lon); Marker lon2 = new IconMarker(
         * "2: I am a really really long string which should wrap a number of times on the screen."
         * , 39.95334, -74.9223446, 0, Color.MAGENTA, icon);
         * cachedMarkers.add(lon2);
         */

        /*
         * float max = 10; for (float i=0; i<max; i++) { Marker marker = null;
         * float decimal = i/max; if (i%2==0) marker = new Marker("Test-"+i,
         * 39.99, -75.33+decimal, 0, Color.LTGRAY); marker = new
         * IconMarker("Test-"+i, 39.99+decimal, -75.33, 0, Color.LTGRAY, icon);
         * cachedMarkers.add(marker); }
         */

        return cachedMarkers;
    }
}
