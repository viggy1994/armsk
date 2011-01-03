package com.ct.armsk.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ChooseMarker extends Activity implements OnClickListener{
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
		menu.add("Choose marker from file");
		menu.add("Add new marker");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getTitle().equals("Choose marker from file")) {
			Intent intent = new Intent(this, CheckFileManagerActivity.class);
            startActivity(intent);

		}
		
		if (item.getTitle().equals("Add new marker")) {
			Intent j = new Intent(this, Marker.class);
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
	/*
	private class MarkerLayout extends RelativeLayout {
		
		private ImageView featuredMarker; 

		public MarkerLayout(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
	}*/
}


