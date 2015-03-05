package com.jwetherell.augmented_reality.activity;

import java.text.DecimalFormat;
import java.util.zip.Inflater;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.jwetherell.augmented_reality.R;
import com.jwetherell.augmented_reality.camera.CameraSurface;
import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.ui.Marker;
import com.jwetherell.augmented_reality.widget.VerticalSeekBar;
import com.jwetherell.augmented_reality.widget.VerticalTextView;

/**
 * This class extends the SensorsActivity and is designed tie the AugmentedView
 * and zoom bar together.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class AugmentedReality extends SensorsActivity implements OnTouchListener {

    private static final String TAG = "AugmentedReality";
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##");
    private static final int ZOOMBAR_BACKGROUND_COLOR = Color.argb(125, 55, 55, 55);
    private static final String END_TEXT = FORMAT.format(AugmentedReality.MAX_ZOOM) + " km";
    private static final int END_TEXT_COLOR = Color.WHITE;

    protected static WakeLock wakeLock = null;
    protected static CameraSurface camScreen = null;
    protected static VerticalSeekBar myZoomBar = null;
    protected static VerticalTextView endLabel = null;
    protected static LinearLayout zoomLayout = null;
    protected static AugmentedView augmentedView = null;
    //protected static View allPlaceView = null;
    protected static ScrollView sv = null;
    protected static LinearLayout allPlaceView = null; 

    public static final float MAX_ZOOM = 5; // in KM
    public static final float ONE_PERCENT = MAX_ZOOM / 100f;
    public static final float TEN_PERCENT = 10f * ONE_PERCENT;
    public static final float TWENTY_PERCENT = 2f * TEN_PERCENT;
    public static final float EIGHTY_PERCENTY = 4f * TWENTY_PERCENT;

    public static boolean ui_portrait = false;  // Defaulted to LANDSCAPE use  
    public static boolean showRadar = true;
    public static boolean showZoomBar = true;
    public static boolean useRadarAutoOrientate = true;
    public static boolean useMarkerAutoRotate = true;
    public static boolean useDataSmoothing = true;
    public static boolean useCollisionDetection = false; // defaulted OFF
    private static TextView place = null;
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        camScreen = new CameraSurface(this);
        setContentView(camScreen);

        augmentedView = new AugmentedView(this);
        augmentedView.setOnTouchListener(this);
        LayoutParams augLayout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //allPlaceView = new AllPlaceView(this);
        //addContentView(allPlaceView,augLayout);
        addContentView(augmentedView, augLayout);
        
        
        zoomLayout = new LinearLayout(this);
        zoomLayout.setVisibility((showZoomBar) ? LinearLayout.VISIBLE : LinearLayout.GONE);
        zoomLayout.setOrientation(LinearLayout.VERTICAL);
        zoomLayout.setPadding(5, 5, 5, 5);
        zoomLayout.setBackgroundColor(ZOOMBAR_BACKGROUND_COLOR);

        endLabel = new VerticalTextView(this);
        endLabel.setText(END_TEXT);
        endLabel.setTextColor(END_TEXT_COLOR);
        LinearLayout.LayoutParams zoomTextParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        zoomTextParams.gravity = Gravity.CENTER;
        zoomLayout.addView(endLabel, zoomTextParams);

        myZoomBar = new VerticalSeekBar(this);
        myZoomBar.setMax(100);
        myZoomBar.setProgress(75);
        myZoomBar.setOnSeekBarChangeListener(myZoomBarOnSeekBarChangeListener);
        LinearLayout.LayoutParams zoomBarParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
        zoomBarParams.gravity = Gravity.CENTER_HORIZONTAL;
        zoomLayout.addView(myZoomBar, zoomBarParams);

        //FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT, Gravity.RIGHT);
        
        RelativeLayout.LayoutParams frameLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        frameLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
        
        addContentView(zoomLayout, frameLayoutParams);

        frameLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.FILL_PARENT);
        frameLayoutParams.addRule(RelativeLayout.LEFT_OF,zoomLayout.getId());
        
        sv = new ScrollView(this);
        
        allPlaceView = new LinearLayout(this);
        allPlaceView.setBackgroundColor(ZOOMBAR_BACKGROUND_COLOR);
        allPlaceView.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        
        sv.addView(allPlaceView,param);
        addContentView(sv, frameLayoutParams);
        
        place = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 10;
        place.setLayoutParams(params);
        place.setMaxWidth(150);
        place.setBackgroundResource(R.color.wordsBg);
        
        updateDataOnZoom();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
    }

    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        wakeLock.acquire();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();

        wakeLock.release();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSensorChanged(SensorEvent evt) {
        super.onSensorChanged(evt);

        if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER || evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            augmentedView.postInvalidate();
            //allPlaceView.postInvalidate();
        }
    }

    private OnSeekBarChangeListener myZoomBarOnSeekBarChangeListener = new OnSeekBarChangeListener() {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            updateDataOnZoom();
            camScreen.invalidate();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            // Ignore
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            updateDataOnZoom();
            camScreen.invalidate();
        }
    };

    private static float calcZoomLevel() {
        int myZoomLevel = myZoomBar.getProgress();
        float myout = 0;

        float percent = 0;
        if (myZoomLevel <= 25) {
            percent = myZoomLevel / 25f;
            myout = ONE_PERCENT * percent;
        } else if (myZoomLevel > 25 && myZoomLevel <= 50) {
            percent = (myZoomLevel - 25f) / 25f;
            myout = ONE_PERCENT + (TEN_PERCENT * percent);
        } else if (myZoomLevel > 50 && myZoomLevel <= 75) {
            percent = (myZoomLevel - 50f) / 25f;
            myout = TEN_PERCENT + (TWENTY_PERCENT * percent);
        } else {
            percent = (myZoomLevel - 75f) / 25f;
            myout = TWENTY_PERCENT + (EIGHTY_PERCENTY * percent);
        }

        return myout;
    }

    /**
     * Called when the zoom bar has changed.
     */
    protected void updateDataOnZoom() {
        float zoomLevel = calcZoomLevel();
        ARData.setRadius(zoomLevel);
        ARData.setZoomLevel(FORMAT.format(zoomLevel));
        ARData.setZoomProgress(myZoomBar.getProgress());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onTouch(View view, MotionEvent me) {
        if (me.getAction() != MotionEvent.ACTION_DOWN) return false;

        // See if the motion event is on a Marker
        for (Marker marker : ARData.getMarkers()) {
            if (marker.handleClick(me.getX(), me.getY())) {
                markerTouched(marker);
                return true;
            }
        }

        return super.onTouchEvent(me);
    }

    protected void markerTouched(Marker marker) {
        Log.w(TAG, "markerTouched() not implemented. marker="+marker.getName());
    }
    
    protected static void updateUI(){
    	allPlaceView.removeAllViews();
    	
    	//sv.removeAllViews();
        for(Marker m : ARData.getMarkers()){
        	double d = m.getDistance();
        	String unit = "m";
        	if(d>1000f){
        		d/=1000f;
        		unit = "km";
        	}
        	place.setText(m.getName()+" "+d+unit);
        	try{
        	((ViewGroup)allPlaceView.getParent()).removeAllViews();
        	}catch(Exception e){
        		
        	}
        	allPlaceView.addView(place);
        }
        //LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        
        //sv.addView(allPlaceView,param);
    }
}
