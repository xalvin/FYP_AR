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
	private static String status;
	private static String message;
	
	public static void reset(){
		//default not login
		login = false;
		name=null;
		userId = Integer.MAX_VALUE;
	}
	
	private static boolean validateEmail(String email){
		Pattern p = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)+$");
		if(!p.matcher(email).matches()){
			message= "Wrong email format";
			return false;
		}
		return true;
	}
	
	private static String encryptPw(String pw){
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(pw.getBytes());
			byte[] digest = md.digest();
			StringBuffer sb = new StringBuffer();
			for (byte b : digest) {
				sb.append(String.format("%02x", b & 0xff));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			message= "Password encryption error";
			return null;
		}
	}
	
	public static boolean login(String email, String pw){
		if(login){
			message = "Login already";
			return false;
		}
		boolean x = validateEmail(email);
		if(!x){
			return false;
		}
		String epw = encryptPw(pw);
		try {
			URL url = new URL("http://hkours.com/akFYP/login.php");
			HttpURLConnection conn = (HttpURLConnection)url.openConnection() ;
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestMethod("POST");
			
			OutputStreamWriter request = new OutputStreamWriter(conn.getOutputStream());
			String parameter = "email="+email+"&pw="+epw;
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
	
	public static boolean register(String email, String pw, String name){
		if(login){
			message = "Login already";
			return false;
		}
		boolean x = validateEmail(email);
		if(!x){
			return false;
		}
		String epw = encryptPw(pw);
		try {
			URL url = new URL("http://hkours.com/akFYP/register.php");
			HttpURLConnection conn = (HttpURLConnection)url.openConnection() ;
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestMethod("POST");
			
			OutputStreamWriter request = new OutputStreamWriter(conn.getOutputStream());
			String parameter = "email="+email+"&pw="+epw+"&name="+name;
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
			reset();
			return false;
		}
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
	
	public static String getMessage(){
		return message;
	}
	
	public static String getStatus(){
		return status;
	}
}
