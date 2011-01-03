package com.ct.armsk.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;

public class Armskdemo extends Activity implements OnClickListener {
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start_armsk_button:
			Intent StartAR = new Intent(this, AR.class);
			startActivity(StartAR);
			break;
		
		case R.id.choose_marker_button:
			Intent ChooseMarker = new Intent(this, ChooseMarker.class);
			startActivity(ChooseMarker);
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
}