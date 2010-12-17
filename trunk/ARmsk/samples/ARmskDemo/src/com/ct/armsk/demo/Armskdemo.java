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
		View addMarkerButton = findViewById(R.id.add_marker_button);
		addMarkerButton.setOnClickListener(this);
		View startARButton = findViewById(R.id.start_armsk_button);
		startARButton.setOnClickListener(this);
		View aboutButton = findViewById(R.id.about_button);
		aboutButton.setOnClickListener(this);
		View exitButton = findViewById(R.id.exit_button);
		exitButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add_marker_button:
			Intent i = new Intent(this, Marker.class);
			startActivity(i);
			break;
			
		case R.id.start_armsk_button:
			Intent j = new Intent(this, AR.class);
			startActivity(j);
			break;
		// More buttons go here (if any) ...
		case R.id.about_button:
			Intent k = new Intent(this, About.class);
			startActivity(k);
			break;

		case R.id.exit_button:
			finish();
			break;
		}
	}
}