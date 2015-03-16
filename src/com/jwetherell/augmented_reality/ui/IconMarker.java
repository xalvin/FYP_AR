package com.jwetherell.augmented_reality.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import com.jwetherell.augmented_reality.R;
import com.jwetherell.augmented_reality.ui.objects.PaintableIcon;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

/**
 * This class extends Marker and draws an icon instead of a circle for it's
 * visual representation.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class IconMarker extends Marker {

    private Bitmap bitmap = null;

    public IconMarker(String name, double latitude, double longitude, double altitude, int color, Bitmap bitmap) {
        super(name, latitude, longitude, altitude, color);
        this.bitmap = bitmap;
    }

    public IconMarker(String name, double latitude, double longitude, double altitude, int color, Bitmap bitmap,String imgReference, String detailRef, String key) {
    	super(name, latitude, longitude, altitude, color, imgReference, detailRef, key);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void drawIcon(Canvas canvas) {
        if (canvas == null /*|| bitmap == null*/) throw new NullPointerException();

        // gpsSymbol is defined in Marker
        if(bitmap!=null){
	        gpsSymbol = new PaintableIcon(bitmap, 96, 96);
	        super.drawIcon(canvas);
        }else{
        	File img = new File("sdcard/com.jwetherell.augmented_reality/imgs/",this.getImgReference()+"-s");
        	if(img.exists()){
        		try{
        			FileInputStream in = new FileInputStream(img);
        			Bitmap mIcon11 = BitmapFactory.decodeStream(in);
        			in.close();
        			this.bitmap = mIcon11;
        			gpsSymbol = new PaintableIcon(mIcon11,96,96);
    	            super.drawIcon(canvas);
    	            return;
        		}catch(Exception e){
        			Log.e("IconMarker","Error on opening image");
        		}
        	}
	        String url = "https://maps.googleapis.com/maps/api/place/photo?sensor=true&maxwidth=96&maxheight=96&photoreference="+this.getImgReference()+"&key="+this.getkey();
	        Log.v("IconMarker","download from url "+url);
	        try {
	        	InputStream stream = (new URL(url)).openConnection().getInputStream();
	        	Bitmap mIcon11 = BitmapFactory.decodeStream(stream);
	        	this.bitmap = mIcon11;
	        	
	        	Log.v("IconMarker","saving image to storage");
	        	try {
	        		FileOutputStream out = new FileOutputStream(img);
	        		mIcon11.compress(Bitmap.CompressFormat.JPEG, 90, out);
	        		out.close();
	        	}catch(Exception e){
	        		Log.e("IconMarker","Error on saving image to storage");
	        	}	        	
	            Log.v("IconMarker","finish");
	            gpsSymbol = new PaintableIcon(mIcon11,96,96);
	            super.drawIcon(canvas);
	        }catch(Exception e){
	        	Log.e("IconMarker", "Error on downloading image");
	        }
	        //new DownloadImageTask(this,canvas).execute(url);
        }
    }
/*    
    public void callSuperDrawIcon(Canvas canvas){
    	super.drawIcon(canvas);
    }
    
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    	IconMarker im;
    	Canvas canvas;
	    public DownloadImageTask(IconMarker im,Canvas canvas) {
	    	this.im = im;
	    	this.canvas = canvas;
	    }

	    protected Bitmap doInBackground(String... urls) {
	        String urldisplay = urls[0];
	        Bitmap mIcon11 = null;
	        try {
	        	Log.v("IconMarker","try download");
	            InputStream in = new java.net.URL(urldisplay).openStream();
	            mIcon11 = BitmapFactory.decodeStream(in);
	            Log.v("IconMarker","finish");
	        } catch (Exception e) {
	            Log.e("Error", e.getMessage());
	            e.printStackTrace();
	        }
	        return mIcon11;
	    }

	    protected void onPostExecute(Bitmap result) {
	    	Log.v("IconMarker","create new symbol");
	        im.gpsSymbol = new PaintableIcon(result,96,96);
	        Log.v("IconMarker","draw symbol");
	        im.callSuperDrawIcon(canvas);
	    }
	}
*/
}
