package com.ct.armsk;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ct.armsk.jni.Processor;
import com.opencv.camera.NativePreviewer;
import com.opencv.camera.NativeProcessor;
import com.opencv.camera.NativeProcessor.PoolCallback;
import com.opencv.jni.image_pool;
import com.opencv.opengl.GL2CameraViewer;



public class armskdemo extends Activity {
    /** Called when the activity is first created. */
	private NativePreviewer mPreview;
	private GL2CameraViewer glview;
	final Processor processor = new Processor();
	private SharedPreferences detectPref;
	private int detection_method;
	boolean settingsChanged = false;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Settings");
		menu.add("Add template");
		menu.add("Start AR");
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		LinkedList<PoolCallback> defaultcallbackstack = new LinkedList<PoolCallback>();
		defaultcallbackstack.addFirst(glview.getDrawCallback());
		if (item.getTitle().equals("Start AR")) {

			//defaultcallbackstack.addFirst(new FastProcessor());
			defaultcallbackstack.addFirst(new ARProcessor());
			Toast.makeText(this, "Initiate Augmented Reality",
					Toast.LENGTH_LONG).show();
		}
		if (item.getTitle().equals("Settings")) {

		Intent intent = new Intent(this, EditPreferences.class);
		settingsChanged=true;
		
		startActivity(intent);
		return (true);
		
		}
		if (item.getTitle().equals("Add template")) {

			Toast.makeText(this, "Template features added to the training set",
					Toast.LENGTH_LONG).show();
			
		}
		
		

		mPreview.addCallbackStack(defaultcallbackstack);
		return true;
	}
	
	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		super.onOptionsMenuClosed(menu);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	
    	detectPref = PreferenceManager.getDefaultSharedPreferences(this);
    	
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		FrameLayout frame = new FrameLayout(this);

		// Create our Preview view and set it as the content of our activity.
		mPreview = new NativePreviewer(getApplication(), 400, 400);

		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		params.height = getWindowManager().getDefaultDisplay().getHeight();
		params.width = (int) (params.height * 4.0 / 2.88);

		LinearLayout vidlay = new LinearLayout(getApplication());

		vidlay.setGravity(Gravity.CENTER);
		vidlay.addView(mPreview, params);
		frame.addView(vidlay);

		// make the glview overlay ontop of video preview
		mPreview.setZOrderMediaOverlay(false);

		glview = new GL2CameraViewer(getApplication(), false, 0, 0);
		glview.setZOrderMediaOverlay(true);
		glview.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		frame.addView(glview);

		setContentView(frame);
		
    }
	@Override
	protected void onPause() {
		super.onPause();

		// IMPORTANT
		// must tell the NativePreviewer of a pause
		// and the glview - so that they can release resources and start back up
		// properly
		// failing to do this will cause the application to crash with no
		// warning
		// on restart
		// clears the callback stack
		mPreview.onPause();

		glview.onPause();
 
	}
	@Override
	protected void onResume() {
		super.onResume();

		if(settingsChanged)
		detection_method =  Integer.parseInt(detectPref.getString("detectPref", ""));
		
		// resume the opengl viewer first
	glview.onResume();

		// add an initial callback stack to the preview on resume...
		// this one will just draw the frames to opengl
		LinkedList<NativeProcessor.PoolCallback> cbstack = new LinkedList<PoolCallback>();

		// SpamProcessor will be called first
	//cbstack.add(new SpamProcessor());

		// then the same idx and pool will be passed to
		// the glview callback -
		// so operate on the image at idx, and modify, and then
		// it will be drawn in the glview
		// or remove this, and call glview manually in SpamProcessor
	 cbstack.add(glview.getDrawCallback());

		mPreview.addCallbackStack(cbstack);
		mPreview.onResume();
		
	

	}
	// final processor so taht these processor callbacks can access it
	//final Processor processor = new Processor();


	class FastProcessor implements NativeProcessor.PoolCallback {

		@Override
		public void process(int idx, image_pool pool, long timestamp,
				NativeProcessor nativeProcessor) {
			processor.detectAndDrawFeatures(idx, pool);
			
		}

	}
	
	class ARProcessor implements NativeProcessor.PoolCallback {

		@Override
		public void process(int idx, image_pool pool, long timestamp,
				NativeProcessor nativeProcessor) {
			processor.processAR(idx, pool, detection_method);
			//processor.detectAndDrawFeatures(idx, pool);
			
		}

	}

}