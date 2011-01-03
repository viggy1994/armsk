package com.ct.armsk.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AR extends Activity implements OnClickListener{
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.ar);
		Button button = (Button) findViewById(R.id.BackAbout);
		button.setOnClickListener(this);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Choose Marker");
		menu.add("Start AR");
		menu.add("How-To & Tips");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getTitle().equals("Choose Marker")) {
			Intent intent = new Intent(this, CheckFileManagerActivity.class);
            startActivity(intent);

		}
		
		if (item.getTitle().equals("How-To & Tips")) {
			Intent j = new Intent(this, AddMarkerHelp.class);
			startActivity(j);
			//Toast.makeText(this, "Initiate Augmented Reality", Toast.LENGTH_LONG).show();

		}

		if (item.getTitle().equals("How-To & Tips")) {
			Intent j = new Intent(this, AddMarkerHelp.class);
			startActivity(j);
			//Toast.makeText(this, "Initiate Augmented Reality", Toast.LENGTH_LONG).show();

		}

		return true;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.BackAbout:
			finish();
			break;
		}
	}
}