package com.hashbang.coffeenerd;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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
    
    private Activity lMainActivity;
    private Button emailButton;
    private Button rateButton;
    private Button donateButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {

        View rootView = inflater.inflate(R.layout.about_fragment, container,
                false);
        lMainActivity = getActivity();
        lMainActivity.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        
        PackageInfo pInfo = null;
		try
		{
			pInfo = lMainActivity.getPackageManager().getPackageInfo(lMainActivity.getPackageName(), 0);
		}
		catch (NameNotFoundException e1)
		{
			e1.printStackTrace();
		}
        final String version = pInfo.versionName;
        
        emailButton = (Button) rootView.findViewById(R.id.email_button);
        emailButton.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",lMainActivity.getResources().getString(R.string.email), null));
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "CoffeeNerd v"+version);
				emailIntent.putExtra(Intent.EXTRA_TEXT, Build.DEVICE+":"+android.os.Build.VERSION.SDK_INT);
				startActivity(Intent.createChooser(emailIntent, "Send email..."));	
			}
		});
       
        rateButton = (Button) rootView.findViewById(R.id.rate_button);
        rateButton.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				Uri uri = Uri.parse("market://details?id=" + lMainActivity.getPackageName());
			    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
			    try
			    {
			    	lMainActivity.startActivity(goToMarket);
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
        	public void onClick(View v)
            {
                final Dialog dialog = new Dialog(lMainActivity);
                dialog.setContentView(R.layout.support_popup);
                dialog.setTitle("Support");
                Button dialogButton = (Button) dialog.findViewById(R.id.paypal_button);
                dialogButton.setOnClickListener(new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=hashbangsoftware%40gmail%2ecom&lc=US&item_name=HashbangSoftware&currency_code=CAD&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted"));
                        startActivity(i);
                    }
                });
                
                Button btcButton = (Button) dialog.findViewById(R.id.btc_button);
                btcButton.setOnClickListener(new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                        ClipboardManager clipboard = (ClipboardManager) lMainActivity.getSystemService(Context.CLIPBOARD_SERVICE); 
                        ClipData clip = ClipData.newPlainText("wallet", "18DCG6e1GMQLvLdiCEZabxJhUPB9EREmsC");
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(lMainActivity, "Wallet address copied to your clipboard",
                                Toast.LENGTH_LONG).show();
                    }
                });
                dialog.show();    
            }
		});
        return rootView;
        
    }
}
