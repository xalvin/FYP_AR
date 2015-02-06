package com.jwetherell.augmented_reality.activity;

import com.jwetherell.augmented_reality.R;
import com.jwetherell.augmented_reality.data.Account;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StartActivity extends Activity{
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.startinterface);
        ((Button)findViewById(R.id.navigation)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent();
			    i.setClass(StartActivity.this,Demo.class);
				startActivity(i);
			}
        });
        ((Button)findViewById(R.id.social)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!Account.getLoginStatus()){
					//To-do add login code here
				}				
				Intent i = new Intent();
			    i.setClass(StartActivity.this,SocialActivity.class);
				startActivity(i);
			}
        });
        ((Button)findViewById(R.id.exit)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
        });
	}

	
	@Override
    public void onStart() {
		super.onStart();
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
	    //check login status
	    if(Account.getLoginStatus()){
	    	menu.findItem(R.id.login).setVisible(false);
	    	menu.findItem(R.id.register).setVisible(false);
	    	menu.findItem(R.id.logout).setVisible(true);
	    }else{
	    	menu.findItem(R.id.logout).setVisible(false);
	    	menu.findItem(R.id.login).setVisible(true);
	    	menu.findItem(R.id.register).setVisible(true);
	    }
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.startmenu, menu);	    
	    return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.register:
        		//To-do add register function here
        		// get prompts.xml view
				LayoutInflater li = LayoutInflater.from(this);
				View promptsView = li.inflate(R.layout.register, null);
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						this);
				
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
							Toast.makeText(StartActivity.this, Account.getMessage(), Toast.LENGTH_SHORT).show();
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
 
        		break;
        	case R.id.login:
        		//To-do add login function here
        		
        		LayoutInflater l = LayoutInflater.from(this);
				View prompt = l.inflate(R.layout.login, null);
				AlertDialog.Builder builder = new AlertDialog.Builder(
						this);
				
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
							Toast.makeText(StartActivity.this, Account.getMessage(), Toast.LENGTH_SHORT).show();
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
 
        		break;
        	case R.id.logout:
        		//To-do add logout function here
        		Toast.makeText(StartActivity.this, "Goodbye "+Account.getName(), Toast.LENGTH_SHORT).show();
        		Account.reset();
        		break;
        }
        return true;
	}
	
}
