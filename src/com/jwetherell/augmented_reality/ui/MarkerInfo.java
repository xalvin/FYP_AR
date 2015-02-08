package com.jwetherell.augmented_reality.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jwetherell.augmented_reality.R;
import com.jwetherell.augmented_reality.activity.AugmentedReality;
import com.jwetherell.augmented_reality.activity.Demo;
import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.ui.objects.PaintableBox;
import com.jwetherell.augmented_reality.ui.objects.PaintableIcon;
import com.jwetherell.augmented_reality.ui.objects.PaintableObject;
import com.jwetherell.augmented_reality.ui.objects.PaintablePosition;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class MarkerInfo extends Marker{

	private Bitmap add = null;
	private Bitmap remove = null;
	private Bitmap route = null;
	private Bitmap back = null;
	private Resources res = null;
	private float[] locationArray;
	
	public MarkerInfo(String name, double latitude, double longitude,
			double altitude, int color, String imgRef, String detailRef, Resources res) {
		super(name, latitude, longitude, altitude, color, imgRef, detailRef, res.getString(R.string.google_places_api_key));
		// TODO Auto-generated constructor stub
		this.res = res;
		add= BitmapFactory.decodeResource(res,R.drawable.add);
		remove= BitmapFactory.decodeResource(res,R.drawable.remove);
		route= BitmapFactory.decodeResource(res,R.drawable.route);
		back= BitmapFactory.decodeResource(res,R.drawable.back);
	}
	
	public void draw(Canvas canvas) {
		if (canvas == null)
            throw new NullPointerException();

        // If not visible then do nothing
        if (!isOnRadar || !isInView)
            return;

        // Draw the box with information and buttons
        drawInfo(canvas);
	}
	
	public void drawInfo(Canvas canvas){
		if (canvas == null)
            throw new NullPointerException();
		Bitmap img=null;
		try{
			if(!getImgReference().equals("")){
				String url = "https://maps.googleapis.com/maps/api/place/photo?key="+res.getString(R.string.google_places_api_key)+"&sensor=true&maxheight=200&photoreference="+getImgReference();
				InputStream in = new URL(url).openStream();
				img = BitmapFactory.decodeStream(in);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		int width = img.getWidth();
		int height = img.getHeight();
		int buttonWidth = add.getWidth();
		int buttonHeight = add.getHeight();
		PaintableBox objBox = new PaintableBox(width+buttonWidth,Math.max(height, buttonHeight*3));
		
		
		getScreenPosition().get(locationArray);
        float x = locationArray[0];
        float y = locationArray[1];
		
		objBox.setCoordinates(objBox.getWidth()/2, 0);
		PaintableIcon first = new PaintableIcon(route,route.getWidth(),route.getHeight());
		first.setCoordinates((int)(objBox.getX()),(int)(objBox.getY()));
		PaintableIcon second;
		if(checkExists(this.name)){
			second = new PaintableIcon(add,add.getWidth(),add.getHeight());
		}else{
			second = new PaintableIcon(remove,remove.getWidth(),remove.getHeight());
		}
		second.setCoordinates((int)(objBox.getX()),(int)(objBox.getY()+buttonHeight));
		PaintableIcon third = new PaintableIcon(back,back.getWidth(),back.getHeight());
		third.setCoordinates((int)(objBox.getX()),(int)(objBox.getY()+buttonHeight*2));
		PaintableIcon photo = new PaintableIcon(img,img.getWidth(),img.getHeight());
		photo.setCoordinates((int)(objBox.getX()+buttonWidth),(int)(objBox.getY()));
		float currentAngle = 0;
        if (AugmentedReality.useMarkerAutoRotate) {
            currentAngle = ARData.getDeviceOrientationAngle()+90;
            currentAngle = 360 - currentAngle;
        }
        if (textContainer == null)
            textContainer = new PaintablePosition(objBox, x, y, currentAngle, 1);
        else
            textContainer.set(objBox, x, y, currentAngle, 1);
        textContainer.paint(canvas);
        textContainer.set(first, x, y, currentAngle, 1);
        textContainer.paint(canvas);
        textContainer.set(second, x, y, currentAngle, 1);
        textContainer.paint(canvas);
        textContainer.set(third, x, y, currentAngle, 1);
        textContainer.paint(canvas);
        textContainer.set(photo, x, y, currentAngle, 1);
        textContainer.paint(canvas);
	}
	
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
}
