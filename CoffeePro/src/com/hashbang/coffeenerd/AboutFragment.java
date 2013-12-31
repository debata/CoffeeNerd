package com.hashbang.coffeenerd;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.hashbang.coffeepro.R;

public class AboutFragment extends Fragment
{
    public static AboutFragment newInstance()
    {
        // Supply index input as an argument.
        AboutFragment f = new AboutFragment();
        return f;
    }
    
    public AboutFragment(){}
    
    private Activity mainActivity;
    private Button emailButton;
    private Button rateButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {

        View rootView = inflater.inflate(R.layout.about_fragment, container,
                false);
        mainActivity = getActivity();
        mainActivity.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        
        emailButton = (Button) rootView.findViewById(R.id.email_button);
        emailButton.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",mainActivity.getResources().getString(R.string.email), null));
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "CoffeeNerd");
				startActivity(Intent.createChooser(emailIntent, "Send email..."));	
			}
		});
       
        rateButton = (Button) rootView.findViewById(R.id.rate_button);
        rateButton.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				Uri uri = Uri.parse("market://details?id=" + mainActivity.getPackageName());
			    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
			    try
			    {
			    	mainActivity.startActivity(goToMarket);
			    }
			    catch (ActivityNotFoundException e)
			    {
			        Log.e("MarketError", e.toString());
			    }
			}
		});
        return rootView;
        
    }
}
