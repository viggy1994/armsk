
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

import com.ct.armsk.jni.Native;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;


public class ChooseMarker extends TabActivity implements OnClickListener{

	 Native armsk = new Native();
	 Context context = this;
	 TabHost mTabHost;
	 
	 TextView infoFilename;
	 TextView infoNumberOfFeatures;
	 
	 ImageView imageDisplay;
	 ImageView featuresDisplay;
	 String markerName = " ";
	 
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choosemarker);

        mTabHost = getTabHost();
        
        mTabHost.addTab(mTabHost.newTabSpec("load").setIndicator("Load Marker").setContent(R.id.LoadMarkerTab));
        mTabHost.addTab(mTabHost.newTabSpec("view").setIndicator("View Marker").setContent(R.id.ViewMarkerTab));
        mTabHost.addTab(mTabHost.newTabSpec("info").setIndicator("Marker Info").setContent(R.id.InfoMarkerTab));
        
        mTabHost.setCurrentTab(0);
        
        //TAB1
        View fromFileButton = findViewById(R.id.fromFileButton);
    	fromFileButton.setOnClickListener(this);
    	View newMarkerButton = findViewById(R.id.newMarkerButton);
    	newMarkerButton.setOnClickListener(this);
        
    	//TAB2
    	imageDisplay = (ImageView) findViewById(R.id.imageDisplay);
    	imageDisplay.setImageResource(R.drawable.dummymarker);
    	View useMarkerButton = findViewById(R.id.useMarkerButton);
    	useMarkerButton.setOnClickListener(this);
        
    	
    	//TAB3
    	featuresDisplay = (ImageView) findViewById(R.id.featuresDisplay);
    	View deleteMarkerButton = findViewById(R.id.deleteMarkerButton);
    	deleteMarkerButton.setOnClickListener(this);
    	
    	infoFilename = (TextView) findViewById(R.id.infoFilename);
    	infoNumberOfFeatures = (TextView) findViewById(R.id.infoNumberOfFeatures);
    	     
    	
    }
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
            case DialogInterface.BUTTON_POSITIVE:
            	File markerFile = new File("sdcard/ARmsk/Markers/", markerName);
				markerFile.delete();
				File evalFile = new File("sdcard/ARmsk/Markers/MarkerEvaluations/", markerName);
				evalFile.delete();
				
				imageDisplay.setImageResource(R.drawable.dummymarker);
				featuresDisplay.setImageResource(0);
				
				mTabHost.setCurrentTab(0);
				
				Toast.makeText(context, "Marker removed",Toast.LENGTH_SHORT).show();
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                break;
            }
        }
    };

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.fromFileButton:
			Intent intent = new Intent(this.getApplicationContext(), FileChooser.class);
    		startActivityForResult(intent, 0);
			
    		break;
		case R.id.newMarkerButton:
			Intent intent2 = new Intent(this.getApplicationContext(), Marker.class);
    		startActivityForResult(intent2, 0);
    		
			break;
		
		case R.id.useMarkerButton:
			Bundle bundle = new Bundle();
	    	bundle.putString("filepath", "sdcard/ARmsk/Markers/" + markerName);
	    	Intent mIntent = new Intent();
	    	mIntent.putExtras(bundle);
	    	setResult(RESULT_OK, mIntent);
	    	finish();
    		
			break;
				
		case R.id.deleteMarkerButton:
			if(markerName != " "){
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
			    builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
			        .setNegativeButton("No", dialogClickListener).show();
			}else
				Toast.makeText(this, "No marker loaded",Toast.LENGTH_SHORT).show();
				
			break;
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
			switch (resultCode) {
				case Activity.RESULT_OK:
				
					Bundle bundle = data.getExtras();					
					markerName = bundle.getString("filename");
					
		            
					//Changes for TAB 1
					
					//BitmapFactory.Options imageOptions = new BitmapFactory.Options();
		            Bitmap bm = BitmapFactory.decodeFile("sdcard/ARmsk/Markers/" + markerName);
		            
		        	Matrix matrix = new Matrix();
		        	matrix.postRotate(90); // Rotate 90 deg
		        	matrix.postScale(1.2f, 1.2f); // Scale by a factor of 1.2
		        	
		        	
		        	Bitmap rotadedbm = Bitmap.createBitmap(bm, 0, 0,
		        			bm.getWidth(), bm.getHeight(), matrix, true);
		        	
		            imageDisplay.setImageBitmap(rotadedbm);
		            
		            
		            // Changes for TAB 2
		            Bitmap bm2 = BitmapFactory.decodeFile("sdcard/ARmsk/Markers/MarkerEvaluations/" + markerName);
		            featuresDisplay.setImageBitmap(bm2);
		            
		            infoFilename.setText("Filename: " + markerName);
		           // infoNumberOfFeatures.setText("Number of features: " + armsk.getNumberFeatures());
		            
		            
		            // Change current tab
					mTabHost.setCurrentTab(1);
			
					break;
		    	
				default:
		    	case Activity.RESULT_CANCELED:
		    		Toast.makeText(this, "Something happened, please try again",Toast.LENGTH_SHORT).show();
		    		break;
			}
		
	}
}
