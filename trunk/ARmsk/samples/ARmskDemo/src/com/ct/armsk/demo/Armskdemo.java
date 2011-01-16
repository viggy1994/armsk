
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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Toast;



public class Armskdemo extends Activity implements OnClickListener {
	
	String filepath = " ";
	Context main = this;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.main); 

		// Set up click listeners for all the buttons
		View startARButton = findViewById(R.id.start_armsk_button);
		startARButton.setOnClickListener(this);
		View addMarkerButton = findViewById(R.id.choose_marker_button);
		addMarkerButton.setOnClickListener(this);
		View instructionsButton = findViewById(R.id.instructions_button);
		instructionsButton.setOnClickListener(this);
		View exitButton = findViewById(R.id.exit_button);
		exitButton.setOnClickListener(this);
	}

	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
            case DialogInterface.BUTTON_POSITIVE:
            	Intent StartAR = new Intent(main, AR.class);
				startActivity(StartAR);
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                break;
            }
        }
    };
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start_armsk_button:
			if(filepath == " "){
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
			    builder.setMessage("You havn't setup the marker, do you want to proceed with a default marker?").setPositiveButton("Yes", dialogClickListener)
			        .setNegativeButton("No", dialogClickListener).show();
				
			}else{
				
				Intent StartAR = new Intent(main, AR.class);
				StartAR.putExtra("filepath", filepath);
				startActivity(StartAR);
				
			}
			
			break;
		
		case R.id.choose_marker_button:
			Intent intent = new Intent(this, ChooseMarker.class);
    		startActivityForResult(intent, 0);
			//Intent ChooseMarker = new Intent(this, ChooseMarker.class);
			//startActivity(ChooseMarker);
			break;
			
		// More buttons go here (if any) ...
		case R.id.instructions_button:
			Intent Instructions = new Intent(this, Instructions.class);
			startActivity(Instructions);
			break;

		case R.id.exit_button:
			finish();
			break;
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
			switch (resultCode) {
				case Activity.RESULT_OK:
				
					Bundle bundle = data.getExtras();
					filepath = bundle.getString("filepath");
					
					break;
		    	
				default:
		    	case Activity.RESULT_CANCELED:
		    		Toast.makeText(this, "Something happened, please try again",Toast.LENGTH_SHORT).show();
		    		break;
			}
		
	}
	
}