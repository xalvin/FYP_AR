package com.jwetherell.augmented_reality.activity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import system.ArActivity;

import com.jwetherell.augmented_reality.R;
import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.data.Account;
import com.jwetherell.augmented_reality.data.LocalDataSource;
import com.jwetherell.augmented_reality.ui.objects.PaintableIcon;
import com.jwetherell.augmented_reality.widget.VerticalTextView;

import de.rwth.setups.PositionTestsSetup;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class LocationInfoActivity extends Activity{
	private float[] destination;
	private String name;
	private String imgRef;
	private String detailRef;
	private boolean dirtyBit;
	private static Toast myToast = null;
	private static VerticalTextView text = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			File folder = new File(Environment.getExternalStorageDirectory().getPath()+"/com.jwetherell.augmented_reality/log/");
			folder.mkdirs();
			System.setErr(new PrintStream(new FileOutputStream(new File(folder,"ARErrLog.txt"), true)));
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
			this.detailRef = data.getString("detailRef");
		} catch(NullPointerException npe){
			npe.printStackTrace();
		}
		dirtyBit=false;
		
		setContentView(R.layout.defaultlistitemview);
		if(Account.getLoginStatus()){
			((LinearLayout)findViewById(R.id.loginLayout)).setVisibility(View.GONE);
		}else{
			((LinearLayout)findViewById(R.id.commentLayout)).setVisibility(View.GONE);
			((LinearLayout)findViewById(R.id.register)).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					LayoutInflater li = LayoutInflater.from(LocationInfoActivity.this);
					View promptsView = li.inflate(R.layout.register, null);
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							LocationInfoActivity.this);
					
					// set prompts.xml to alertdialog builder
					alertDialogBuilder.setView(promptsView);
					final EditText email = (EditText) promptsView
							.findViewById(R.id.email);
					final EditText pw = (EditText) promptsView
							.findViewById(R.id.pw);
					final EditText name = (EditText) promptsView
							.findViewById(R.id.name);
					
					// set dialog message
					alertDialogBuilder
						.setCancelable(false)
						.setPositiveButton("OK",
						  new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog,int id) {
								// get user input and set it to result
								// edit text
								String mail = email.getText().toString();
								String pass = pw.getText().toString();
								String userName = name.getText().toString();
								boolean success = Account.register(mail, pass, userName);
								if(success)
									Account.login(mail, pass);
								Toast.makeText(LocationInfoActivity.this, Account.getMessage(), Toast.LENGTH_SHORT).show();
								if(Account.getLoginStatus()){
									((LinearLayout)findViewById(R.id.commentLayout)).setVisibility(View.VISIBLE);
									((LinearLayout)findViewById(R.id.loginLayout)).setVisibility(View.GONE);
								}
						    }
						  })
						.setNegativeButton("Cancel",
						  new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog,int id) {
							dialog.cancel();
						    }
						  });
	 
					// create alert dialog
					AlertDialog alertDialog = alertDialogBuilder.create();
	 
					// show it
					alertDialog.show();
				}
				
			});
			((LinearLayout)findViewById(R.id.login)).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					LayoutInflater l = LayoutInflater.from(LocationInfoActivity.this);
					View prompt = l.inflate(R.layout.login, null);
					AlertDialog.Builder builder = new AlertDialog.Builder(
							LocationInfoActivity.this);
					
					// set prompts.xml to alertdialog builder
					builder.setView(prompt);
					final EditText mail = (EditText) prompt
							.findViewById(R.id.email);
					final EditText pass = (EditText) prompt
							.findViewById(R.id.pw);
					
					// set dialog message
					builder
						.setCancelable(false)
						.setPositiveButton("OK",
						  new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog,int id) {
								// get user input and set it to result
								// edit text
								String email = mail.getText().toString();
								String pw = pass.getText().toString();
								Account.login(email, pw);
								Toast.makeText(LocationInfoActivity.this, Account.getMessage(), Toast.LENGTH_SHORT).show();
								if(Account.getLoginStatus()){
									((LinearLayout)findViewById(R.id.commentLayout)).setVisibility(View.VISIBLE);
									((LinearLayout)findViewById(R.id.loginLayout)).setVisibility(View.GONE);
								}
						    }
						  })
						.setNegativeButton("Cancel",
						  new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog,int id) {
							dialog.cancel();
						    }
						  });
	 
					// create alert dialog
					AlertDialog ad = builder.create();
	 
					// show it
					ad.show();
				}
				
			});
		}
		if (destination == null) throw new NullPointerException("destination location cannot be loaded");
		
		//((TextView) findViewById(R.id.lat)).setText("current: x "+current[0]+" y "+current[1]+" z "+current[2]+"\n");
		try{
			if(!this.imgRef.equals("")){
				File folder = new File("sdcard/com.jwetherell.augmented_reality/imgs/");
				folder.mkdirs();
				File img = new File(folder,fileName(this.imgRef)+"-L");
	        	if(img.exists()){
	        		try{
	        			FileInputStream in = new FileInputStream(img);
	        			Bitmap mIcon11 = BitmapFactory.decodeStream(in);
	        			in.close();
	        			ImageView iv = (ImageView) findViewById(R.id.markerImg);
	        			iv.setImageBitmap(mIcon11);
	        		}catch(Exception e){
	        			Log.e("LocationInfo","Error on opening image");
	        		}
	        	}else{
					String url = "https://maps.googleapis.com/maps/api/place/photo?key="+this.getResources().getString(R.string.google_places_api_key)+"&sensor=true&maxwidth=300&photoreference="+this.imgRef;
					new DownloadImageTask((ImageView) findViewById(R.id.markerImg)).execute(url);
	        	}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			if(!this.detailRef.equals("")){
				File folder = new File(Environment.getExternalStorageDirectory().getPath()+"/com.jwetherell.augmented_reality/data/");
				folder.mkdirs();
				File jsonFile = new File(folder,"locationDeatils.json");
				if (jsonFile.exists()){
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
						int len = a.length();
						boolean flag = true;
						for(int i = 0;i<len;i++){
							JSONObject obj = a.getJSONObject(i);
							if(obj.getString("detailRef").equals(this.detailRef)){
								JSONObject jo = obj.getJSONObject("details");
								StringBuilder textToSet = new StringBuilder();
								textToSet.append("Address :\n\t");
								textToSet.append(jo.getString("formatted_address"));
								textToSet.append("\n");
								textToSet.append("Phone number : ");
								textToSet.append(jo.getString("international_phone_number"));
								textToSet.append("\n");
								textToSet.append("Website :\n\t");
								textToSet.append(jo.getString("website"));
								textToSet.append("\n\n\t");
								textToSet.append(jo.getString("url"));
								TextView targetView = (TextView) findViewById(R.id.description);
								targetView.setText(textToSet.toString());
								flag = false;
								break;
							}							
						}
						if(flag){
							String url = "https://maps.googleapis.com/maps/api/place/details/json?key="+this.getResources().getString(R.string.google_places_api_key)+"&sensor=true&reference="+this.detailRef;
							new DownloadDetailTask((TextView) findViewById(R.id.description)).execute(url);
						}
					} catch(Exception e){
						e.printStackTrace();
						String url = "https://maps.googleapis.com/maps/api/place/details/json?key="+this.getResources().getString(R.string.google_places_api_key)+"&sensor=true&reference="+this.detailRef;
						new DownloadDetailTask((TextView) findViewById(R.id.description)).execute(url);
					}
				}else{
					String url = "https://maps.googleapis.com/maps/api/place/details/json?key="+this.getResources().getString(R.string.google_places_api_key)+"&sensor=true&reference="+this.detailRef;
					new DownloadDetailTask((TextView) findViewById(R.id.description)).execute(url);
				}
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
					appendToJson(destination,name,imgRef,detailRef);
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
		((ImageButton) findViewById(R.id.send)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String msg = ((EditText) findViewById(R.id.enterComment)).getText().toString();
				new UploadComment().execute(msg);
			}
			
		});
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
	}

	private class DownloadDetailTask extends AsyncTask<String, Void, String>{
		TextView targetView;
		
		public DownloadDetailTask(TextView target){
			this.targetView = target;
		}
		
		protected String doInBackground(String... urls) {
	        String urldisplay = urls[0];
	        StringBuilder textToSet = new StringBuilder();
	        String JSONStr = null;
	        try {
	            InputStream stream = new java.net.URL(urldisplay).openConnection().getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream), 8 * 1024);
		        StringBuilder sb = new StringBuilder();

		        try {
		            String line;
		            while ((line = reader.readLine()) != null) {
		                sb.append(line + "\n");
		            }
		        } catch (IOException e) {
		            e.printStackTrace();
		        } finally {
		            try {
		                stream.close();
		            } catch (IOException e) {
		                e.printStackTrace();
		            }
		        }
		        JSONStr = sb.toString();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
	            Log.e("Error", e.getMessage());
	            e.printStackTrace();
	        }
			//process JSONObject
			JSONObject json = null;
			try {
				json = new JSONObject(JSONStr);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (json == null) throw new NullPointerException();
			JSONObject jo = null;
			try {
				if (json.has("result")) jo = json.getJSONObject("result");
				if (jo == null) {
					//steps=null;
					return null;
				}
				
				//save json to storage
				File folder = new File(Environment.getExternalStorageDirectory().getPath()+"/com.jwetherell.augmented_reality/data/");
				folder.mkdirs();
				File jsonFile = new File(folder,"locationDeatils.json");
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
			       /* format
					{"data":
						[{"detailRef":"detailRef....",
						  "details":{obj details}
						  },{...}]					
					}
					*/
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
							obj.put("detailRef", detailRef);
							obj.put("details",jo);
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
					
				
				textToSet.append("Address :\n\t");
				textToSet.append(jo.getString("formatted_address"));
				textToSet.append("\n");
				textToSet.append("Phone number : ");
				textToSet.append(jo.getString("international_phone_number"));
				textToSet.append("\n");
				textToSet.append("Website :\n\t");
				textToSet.append(jo.getString("website"));
				textToSet.append("\n\n\t");
				textToSet.append(jo.getString("url"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        return textToSet.toString();
	    }

	    protected void onPostExecute(String result) {
	    	targetView.setText(result);
	    }
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
	        //save image to storage
	        File folder = new File(Environment.getExternalStorageDirectory()+"/com.jwetherell.augmented_reality/imgs/");
	        folder.mkdirs();
	        File img = new File(folder,fileName(imgRef)+"-L");
	        try {
        		FileOutputStream out = new FileOutputStream(img);
        		mIcon11.compress(Bitmap.CompressFormat.JPEG, 90, out);
        		out.close();
        	}catch(Exception e){
        		Log.e("LocationInfo","Error on saving image to storage");
        	}	   
	        return mIcon11;
	    }

	    protected void onPostExecute(Bitmap result) {
	        bmImage.setImageBitmap(result);
	    }
	}
	
	private class DownloadCommentTask extends AsyncTask<Void, Void, Void> {
		LinearLayout ll;

	    public DownloadCommentTask(LinearLayout ll) {
	        this.ll = ll;
	    }

	    protected Void doInBackground(Void... params) {
	        URL url = new URL("http://hkours.com/akFYP/retriveMsg.php");
	        try {
	        	HttpURLConnection conn = (HttpURLConnection)url.openConnection() ;
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				conn.setRequestMethod("POST");
				
				OutputStreamWriter request = new OutputStreamWriter(conn.getOutputStream());
				String parameter = "id="+Account.getUserId()+
						"&lName="+name+
						"&lan="+destination[0]+
						"&lon="+destination[1]+
						"&imgRef="+imgRef+
						"&detailRef="+detailRef+
						"&msg="+msg;
	            request.write(parameter);
	            request.flush();
	            request.close(); 

				BufferedReader myReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	            StringBuilder sb = new StringBuilder();
	            sb.append(myReader.readLine() + "\n");
	            String line="";
	            while ((line = myReader.readLine()) != null) {
	                sb.append(line);
	            }
	            String result = sb.toString();
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
	
	private class UploadComment extends AsyncTask<String, Void, Boolean> {
		private final ProgressDialog dialog = new ProgressDialog(LocationInfoActivity.this);
		String status;
		String message;

	    protected void onPreExecute() {
	        this.dialog.setMessage("Loading...");
	        this.dialog.setCancelable(false);
	        this.dialog.show();
	    }

	    @Override
		protected Boolean doInBackground(String... arg0) {
			// TODO Auto-generated method stub
	    	String msg = arg0[0];
	    	URL url;
			try {
				url = new URL("http://hkours.com/akFYP/addMsg.php");
				
				HttpURLConnection conn = (HttpURLConnection)url.openConnection() ;
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				conn.setRequestMethod("POST");
				
				OutputStreamWriter request = new OutputStreamWriter(conn.getOutputStream());
				String parameter = "id="+Account.getUserId()+
						"&lName="+name+
						"&lan="+destination[0]+
						"&lon="+destination[1]+
						"&imgRef="+imgRef+
						"&detailRef="+detailRef+
						"&msg="+msg;
	            request.write(parameter);
	            request.flush();
	            request.close(); 

				BufferedReader myReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	            StringBuilder sb = new StringBuilder();
	            sb.append(myReader.readLine() + "\n");
	            String line="";
	            while ((line = myReader.readLine()) != null) {
	                sb.append(line);
	            }
	            String result = sb.toString();
	            JSONObject obj = new JSONObject(result);
	            status = obj.getString("status");
	            message = obj.getString("message");
	            if(!status.equals("OK")){
	            	return false;
	            }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}

        protected void onPostExecute(Boolean result) {

            // Here if you wish to do future process for ex. move to another activity do here

            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            text.setText(message);
            myToast.show();
        }
	}
	
	//append new favourite destination to JSON file
	/* JSON format:
		{
			"data":
			[
				{
					"name" : "location_name",
					"destination":
					{
						"x" : (float)lat
						"y" : (float)lon
						"z" : (float)alt
					},
					"imgRef" : "image_reference",
					"detailRef" : "detail_reference"
				}...
			]
		}
				
	
	*/
	public static void appendToJson(float[] destination,String name,String imgRef,String detailRef){  
		File folder = new File(Environment.getExternalStorageDirectory().getPath()+"/com.jwetherell.augmented_reality/data/");
		folder.mkdirs();
       File jsonFile = new File(folder,"localData.json");
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
				obj.put("detailRef", detailRef);
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
			File folder = new File(Environment.getExternalStorageDirectory().getPath()+"/com.jwetherell.augmented_reality/data/");
			folder.mkdirs();
	       File jsonFile = new File(folder,"localData.json");
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
			File folder = new File(Environment.getExternalStorageDirectory().getPath()+"/com.jwetherell.augmented_reality/data/");
			folder.mkdirs();
	       File jsonFile = new File(folder,"localData.json");
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
