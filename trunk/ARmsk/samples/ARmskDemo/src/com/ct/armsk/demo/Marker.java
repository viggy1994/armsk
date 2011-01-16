
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

import java.io.File;
import java.util.Date;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ct.armsk.jni.Native;
import com.opencv.camera.NativePreviewer;
import com.opencv.camera.NativeProcessor;
import com.opencv.camera.NativeProcessor.PoolCallback;
import com.opencv.jni.image_pool;
import com.opencv.opengl.GL2CameraViewer;


public class Marker extends Activity{
	
	NativePreviewer mPreview;
	private GL2CameraViewer glview;
	Native armsk = new Native();
	String markerPath = "";
	boolean markerSaved = false;
	Context test = this;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Add Marker");
		menu.add("Settings");
		menu.add("How-To & Tips");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		LinkedList<PoolCallback> defaultcallbackstack = new LinkedList<PoolCallback>();
		defaultcallbackstack.addFirst(glview.getDrawCallback());
		
		if (item.getTitle().equals("Add Marker")) {
			defaultcallbackstack.addFirst(new MarkerProcessor());
			mPreview.addCallbackStack(defaultcallbackstack);
			
			Toast.makeText(this, "Marker saved in local folder",Toast.LENGTH_SHORT).show();

		}

		if (item.getTitle().equals("How-To & Tips")) {
			
			//Intent j = new Intent(this, AddMarkerHelp.class);
			//startActivity(j);
			
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
		
		ImageButton addMarkerButton = new ImageButton(getApplicationContext());
		addMarkerButton.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_camera));
		addMarkerButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		addMarkerButton.setOnClickListener(new View.OnClickListener() {
			
			
			@Override
			public void onClick(View v) {
				
				LinkedList<PoolCallback> defaultcallbackstack = new LinkedList<PoolCallback>();
				defaultcallbackstack.addFirst(glview.getDrawCallback());
				
				defaultcallbackstack.addFirst(new MarkerProcessor());
				mPreview.addCallbackStack(defaultcallbackstack);
				
			}
		});
		
		LinearLayout buttons = new LinearLayout(getApplicationContext());
		buttons.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		
		
	
		buttons.addView(addMarkerButton);
		
		frame.addView(buttons);
		
		setContentView(frame);

	}
	@Override
	protected void onPause() {
		super.onPause();

		mPreview.onPause();
		glview.onPause();


	}
	@Override
	protected void onResume() {
		super.onResume();

		glview.onResume();

		LinkedList<NativeProcessor.PoolCallback> cbstack = new LinkedList<PoolCallback>();

		cbstack.add(glview.getDrawCallback());
		
		mPreview.addCallbackStack(cbstack);
		mPreview.onResume();

	}
	
	class MarkerProcessor implements NativeProcessor.PoolCallback {

		@Override
		public void process(int idx, image_pool pool, long timestamp,
				NativeProcessor nativeProcessor) {
			if (!markerSaved) {
				markerSaved = true;

			File armskdir = new File(Environment.getExternalStorageDirectory(), "ARmsk");
			if (!armskdir.exists())
				armskdir.mkdir();
			
			File markerdir = new File(armskdir, "Markers");
			if (!markerdir.exists())
				markerdir.mkdir();
			
			String filePath = "ImageMarker"+ new Date().getTime() + ".jpg";
			File markerFile = new File(markerdir, filePath);
			//armsk.saveMarker(idx,pool,markerFile.getAbsolutePath());
			
			File evaldir = new File(markerdir, "MarkerEvaluations");
			if (!evaldir.exists())
				evaldir.mkdir();
			
			File evalFile = new File(evaldir, filePath);
			//armsk.findFeatures(markerFile.getAbsolutePath(),evalFile.getAbsolutePath());
			
			Bundle bundle = new Bundle();
	    	bundle.putString("filename", markerFile.getName());
	    	Intent mIntent = new Intent();
	    	mIntent.putExtras(bundle);
	    	setResult(RESULT_OK, mIntent);
	    	finish();
			
			}
		}

	}
	
	

}

	

