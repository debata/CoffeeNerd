package com.hashbang.coffeenerd;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.hashbang.coffeepro.R;

public class PreferencesFragment extends Fragment
{
    public static PreferencesFragment newInstance()
    {
        PreferencesFragment f = new PreferencesFragment();
        return f;
    }
    
    public PreferencesFragment(){}
    
    private Activity mainActivity;
    private RadioGroup volumeGroup;
    private RadioGroup massGroup;
    
    private SharedPreferences sharedPref;
    
    public final String VOLUME_TAG = "VolumeUnit";
    public final String MASS_TAG = "MassUnit";
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {

        View rootView = inflater.inflate(R.layout.preferences_fragment, container,
                false);
        mainActivity = getActivity();
        mainActivity.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        
        sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);
        
        volumeGroup = (RadioGroup) rootView.findViewById(R.id.volume_group);
        volumeGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				SharedPreferences.Editor editor = sharedPref.edit();
				if(checkedId == R.id.cups)
				{
					editor.putString(VOLUME_TAG, "cup");
					editor.commit();
				}
				else
				{
					editor.putString(VOLUME_TAG, "ml");
					editor.commit();
				}
				
			}
		});
        
        massGroup = (RadioGroup) rootView.findViewById(R.id.mass_group);
        massGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				SharedPreferences.Editor editor = sharedPref.edit();
				if(checkedId == R.id.ounces)
				{
					editor.putString(VOLUME_TAG, "oz");
					editor.commit();
				}
				else
				{
					editor.putString(VOLUME_TAG, "g");
					editor.commit();
				}
				
			}
		});
        
        return rootView;
        
    }
}