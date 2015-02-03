package com.jwetherell.augmented_reality.data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import org.json.JSONObject;

public class Account {
	
	private static boolean login;
	private static int userId;
	private static String name;
	private static String message;
	
	public static void reset(){
		//default not login
		login = false;
		name=null;
		message = null;
		userId = Integer.MAX_VALUE;
	}
	
	public static boolean login(String email, String pw){
		if(login)
			return false;
		Pattern p = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)+$");
		if(!p.matcher(email).matches())
			return false;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(pw.getBytes());
			byte[] digest = md.digest();
			StringBuffer sb = new StringBuffer();
			for (byte b : digest) {
				sb.append(String.format("%02x", b & 0xff));
			}
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		try {
			URL url = new URL("http://hkours.com/akFYP/login.php");
			HttpURLConnection conn = (HttpURLConnection)url.openConnection() ;
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestMethod("POST");
			
			OutputStreamWriter request = new OutputStreamWriter(conn.getOutputStream());
			String parameter = "email="+email+"&pw="+pw;
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
            if(!obj.getString("status").equals("OK")){
            	message = obj.getString("message");
            	return false;
            }
            name = obj.getString("name");
            userId = obj.getInt("id");
            login = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			reset();
			return false;
		}
		return true;
	}
	
	public static boolean register(String email, String pw){
		
		return true;
	}
	
	public static boolean getLoginStatus(){
		return login;
	}
	
	public static String getName(){
		return name;
	}
	
	public static int getUserId(){
		return userId;
	}
}
