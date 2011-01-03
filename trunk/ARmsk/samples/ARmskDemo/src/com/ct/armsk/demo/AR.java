package com.ct.armsk.demo;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ct.armsk.jni.Native;
import com.opencv.camera.NativePreviewer;
import com.opencv.camera.NativeProcessor;
import com.opencv.camera.NativeProcessor.PoolCallback;
import com.opencv.jni.image_pool;
import com.opencv.opengl.GL2CameraViewer;

public class AR extends Activity implements OnClickListener{
	
	NativePreviewer mPreview;
	private GL2CameraViewer glview;
	Native armsk = new Native();
	String markerPath = "";
	boolean markerSaved = false;

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Start AR");
		//menu.add("How-To & Tips");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		LinkedList<PoolCallback> defaultcallbackstack = new LinkedList<PoolCallback>();
		defaultcallbackstack.addFirst(glview.getDrawCallback());
		
		if (item.getTitle().equals("Start AR")) {
			defaultcallbackstack.addFirst(new AugmentedRealityProcessor());
			mPreview.addCallbackStack(defaultcallbackstack);
			
			Toast.makeText(this, "Initiate Augmented Reality",Toast.LENGTH_SHORT).show();

		}

		if (item.getTitle().equals("How-To & Tips")) {
			
			Intent j = new Intent(this, AddMarkerHelp.class);
			startActivity(j);
			
			//Toast.makeText(this, "Initiate Augmented Reality", Toast.LENGTH_LONG).show();
//			mPreview.addCallbackStack(defaultcallbackstack);
		}

		return true;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		FrameLayout frame = new FrameLayout(this);

		// Create our Preview view and set it as the content of our activity.
		mPreview = new NativePreviewer(getApplication(), 400, 400);

		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
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
		armsk.setMarker("/sdcard/ARmsk/Markers/ImageMarker1293952913569.jpg");

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
		// resume the opengl viewer first
		glview.onResume();
		// add an initial callback stack to the preview on resume...
		// this one will just draw the frames to opengl
		LinkedList<NativeProcessor.PoolCallback> cbstack = new LinkedList<PoolCallback>();

		// SpamProcessor will be called first
		// cbstack.add(new SpamProcessor());

		// then the same idx and pool will be passed to
		// the glview callback -
		// so operate on the image at idx, and modify, and then
		// it will be drawn in the glview
		// or remove this, and call glview manually in SpamProcessor
		cbstack.add(glview.getDrawCallback());
		
		mPreview.addCallbackStack(cbstack);
		mPreview.onResume();

	}
	
	class AugmentedRealityProcessor implements NativeProcessor.PoolCallback {

		@Override
		public void process(int idx, image_pool pool, long timestamp,
				NativeProcessor nativeProcessor) {
			/*if (!markerSaved) {
				markerSaved = true;

			File armskdir = new File(Environment.getExternalStorageDirectory(), "ARmsk");
			if (!armskdir.exists())
				armskdir.mkdir();
			
			File markerdir = new File(armskdir, "Markers");
			if (!markerdir.exists())
				markerdir.mkdir();
			
			File markerFile = new File(markerdir, "ImageMarker"
					+ new Date().getTime() + ".jpg");
			armsk.saveMarker(idx,pool,markerFile.getAbsolutePath());
			
			markerPath = markerFile.getAbsolutePath();
			finish();

			}*/
			armsk.processAR(idx, pool);
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}

	

