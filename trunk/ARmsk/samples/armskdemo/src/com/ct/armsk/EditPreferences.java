package com.ct.armsk;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class EditPreferences extends PreferenceActivity {
	
	private ListPreference detectlist,extractlist;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		setLists();
	}
	
	private void setLists() {

		String[] nameDirDet = {"FAST","GoodFeaturesToTrack","Star", "SURF"};
		String[] indxDirDet = {"1","2","3","4"};

		detectlist = (ListPreference) findPreference("detectPref");
		detectlist.setEntries(nameDirDet);
		detectlist.setEntryValues(indxDirDet);
		
		String[] nameDirExt = {"SURF","SIFT"};
		String[] indxDirExt = {"1","2"};
		
		extractlist = (ListPreference) findPreference("extractPref");
		extractlist.setEntries(nameDirExt);
		extractlist.setEntryValues(indxDirExt);
	}
	
	public void onResume(Bundle savedInstanceState) {
		setLists();
	}
	
	
}
