package com.hashbang.coffeenerd;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
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
    private Button donateButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {

        View rootView = inflater.inflate(R.layout.about_fragment, container,
                false);
        mainActivity = getActivity();
        mainActivity.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        
        final Resources res = mainActivity.getResources();
        
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
        
        donateButton = (Button) rootView.findViewById(R.id.donate_button);
        donateButton.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=hashbangsoftware%40gmail%2ecom&lc=US&item_name=HashbangSoftware&currency_code=CAD&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted"));
				startActivity(i);
			}
		});
        return rootView;
        
    }
}
