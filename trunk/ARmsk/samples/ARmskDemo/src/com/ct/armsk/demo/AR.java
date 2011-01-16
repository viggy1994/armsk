
/*
 * Copyright 2010, 2011 Project ARmsk
 * 
 * This file is part of ARmsk.
 * ARmsk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * ARmsk is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with ARmsk.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ct.armsk.demo;

import java.util.LinkedList;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
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
	CubeRenderer arRenderer;
	GLSurfaceView renderview;

	final int AR_MODE = 2;
	final int ADD_MARKER_MODE = 1;
	final int CAMERA_MODE = 0;
	int mode = CAMERA_MODE;
	boolean markerAdded = false; 

	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Add Marker");
		menu.add("Start AR");
		menu.add("Instructions");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		LinkedList<PoolCallback> defaultcallbackstack = new LinkedList<PoolCallback>();
		defaultcallbackstack.addFirst(glview.getDrawCallback());

		if (item.getTitle().equals("Start AR")) {
			if(markerAdded){
				Toast.makeText(this, "Initiate Augmented Reality",Toast.LENGTH_SHORT).show();
				mode = AR_MODE;
			}			
			else
				Toast.makeText(this, "Add Marker First!",Toast.LENGTH_SHORT).show();			
		}

		if (item.getTitle().equals("Instructions")) {
			AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
			dlgAlert.setMessage(R.string.add_marker_help_text);
			dlgAlert.setTitle("Instructions");
			dlgAlert.setPositiveButton("Back", null);
			dlgAlert.setCancelable(true);
			dlgAlert.create().show();
		}

		if (item.getTitle().equals("Add Marker")) {
			Toast.makeText(this, "Marker being added..",Toast.LENGTH_SHORT).show();
			defaultcallbackstack.addFirst(new AugmentedRealityProcessor());
			mPreview.addCallbackStack(defaultcallbackstack);
			mode = ADD_MARKER_MODE;
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

		arRenderer = new CubeRenderer(true);
		renderview = new GLSurfaceView(this);
		
		renderview.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        renderview.setRenderer(arRenderer);

        renderview.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        renderview.setZOrderMediaOverlay(true);

        frame.addView(renderview, 0);
        
		setContentView(frame);
		arRenderer.drawCube = true;
		
		/*
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null){
		String path = bundle.getString("filepath");
		armsk.setMarker(path);
		}else
			armsk.setMarker("/sdcard/ARmsk/Markers/DefaultMarker.jpg");*/
	}

	@Override
	protected void onPause() {
		super.onPause();

		// IMPORTANT
		// must tell the NativePreviewer of a pause
		// and the glview - so that they can release resources and start back up properly
		// failing to do this will cause the application to crash with no warning
		// on restart clears the callback stack
		mPreview.onPause();
		glview.onPause();
		renderview.onPause();

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// resume the opengl viewer first
		glview.onResume();
		renderview.onResume();
		// add an initial callback stack to the preview on resume...
		// this one will just draw the frames to opengl
		LinkedList<NativeProcessor.PoolCallback> cbstack = new LinkedList<PoolCallback>();

		// then the same idx and pool will be passed to
		// the glview callback -
		// so operate on the image at idx, and modify, and then
		// it will be drawn in the glview
		cbstack.add(glview.getDrawCallback());
		mPreview.addCallbackStack(cbstack);
		mPreview.onResume();
	}

	
	class AugmentedRealityProcessor implements NativeProcessor.PoolCallback {

		@Override
		public void process(int idx, image_pool pool, long timestamp,
				NativeProcessor nativeProcessor) {

			if (mode == ADD_MARKER_MODE) {
				armsk.setMarker(idx, pool);
				mode = CAMERA_MODE;
				markerAdded = true; 
			} else if (mode == AR_MODE) {
				armsk.processAR(idx, pool);
				
				float transform[] = new float[] { armsk.getMatrix(0), 
						armsk.getMatrix(1), armsk.getMatrix(2),
						armsk.getMatrix(3), armsk.getMatrix(4),
						armsk.getMatrix(5), armsk.getMatrix(6),
						armsk.getMatrix(7), armsk.getMatrix(8),
						armsk.getMatrix(9), armsk.getMatrix(10),
						armsk.getMatrix(11), armsk.getMatrix(12), 
						armsk.getMatrix(13), armsk.getMatrix(14),
						armsk.getMatrix(15) };
				
				arRenderer.updateHomography(transform, armsk.getScale(), armsk.getMatchFound());

			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}

	

