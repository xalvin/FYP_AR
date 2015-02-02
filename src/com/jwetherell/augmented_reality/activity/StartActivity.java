package com.jwetherell.augmented_reality.activity;

import com.jwetherell.augmented_reality.R;
import com.jwetherell.augmented_reality.data.Account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

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
				if(!Account.login){
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
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.startmenu, menu);
	    //check login status
	    if(Account.login){
	    	menu.findItem(R.id.login).setVisible(false);
	    	menu.findItem(R.id.logout).setVisible(true);
	    }else{
	    	menu.findItem(R.id.logout).setVisible(false);
	    	menu.findItem(R.id.login).setVisible(true);
	    }
	    return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.login:
        		//To-do add login function here
        		break;
        	case R.id.logout:
        		//To-do add logout function here
        		break;
        }
        return true;
	}
	
}
