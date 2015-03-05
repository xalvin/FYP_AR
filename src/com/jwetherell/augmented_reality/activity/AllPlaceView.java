package com.jwetherell.augmented_reality.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

import com.jwetherell.augmented_reality.R;
import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.ui.Marker;

import android.content.Context;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AllPlaceView extends View {

	private static final AtomicBoolean drawing = new AtomicBoolean(false);
	private static final List<Marker> cache = new ArrayList<Marker>();
	private Context context;
	
	public AllPlaceView(Context context) {
		super(context);
		this.context = context;
		LayoutInflater  mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //mInflater.inflate(R.layout.places, this, true);
	}

	protected void onDraw(Canvas canvas) {
        if (canvas == null) return;

        if (drawing.compareAndSet(false, true)) {
        	//Log.v(TAG, "DIRTY flag found, re-populating the cache.");

            // Get all the markers
            List<Marker> leftCollection = ARData.getLeftMarkers();
            List<Marker> rightCollection = ARData.getRightMarkers();
            LinearLayout ll = null;//(LinearLayout) findViewById(R.id.left);
            if(ll == null) return;
            ll.removeAllViews();
            for(Marker ma : leftCollection){
            	TextView child = new TextView(this.context);
            	child.setText(ma.getName());
            	child.setMaxWidth(100);
            	child.setBackgroundResource(R.color.wordsBg);
            	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            	lp.setMargins(0, 0, 0, 10);
            	child.setLayoutParams(lp);
            	ll.addView(child);
            }
            ll = (LinearLayout) findViewById(R.id.right);
            ll.removeAllViews();
            for(Marker ma : rightCollection){
            	TextView child = new TextView(this.context);
            	child.setText(ma.getName());
            	child.setMaxWidth(100);
            	child.setBackgroundResource(R.color.wordsBg);
            	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            	lp.setMargins(0, 0, 0, 10);
            	child.setLayoutParams(lp);
            	ll.addView(child);
            }
            drawing.set(false);
        }
    }
}
