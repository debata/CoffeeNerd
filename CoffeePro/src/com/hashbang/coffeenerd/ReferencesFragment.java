package com.hashbang.coffeenerd;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hashbang.coffeepro.R;

public class ReferencesFragment extends Fragment
{
    public static ReferencesFragment newInstance()
    {
        // Supply index input as an argument.
        ReferencesFragment f = new ReferencesFragment();
        return f;
    }
    
    public ReferencesFragment(){}
    
    private Activity mainActivity;
    
    private TextView chemexLabel;
    private TextView stumpLabel;
    private TextView bodumRefLabel;
    private TextView harioLabel;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {

        View rootView = inflater.inflate(R.layout.references_fragment, container,
                false);
        mainActivity = getActivity();
        mainActivity.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        
        bodumRefLabel = (TextView) rootView.findViewById(R.id.bodum_link);
        bodumRefLabel.setMovementMethod(LinkMovementMethod.getInstance());
        chemexLabel = (TextView) rootView.findViewById(R.id.chemex_link);
        chemexLabel.setMovementMethod(LinkMovementMethod.getInstance());
        stumpLabel = (TextView) rootView.findViewById(R.id.stump_link);
        stumpLabel.setMovementMethod(LinkMovementMethod.getInstance());
        harioLabel = (TextView) rootView.findViewById(R.id.hario_link);
        harioLabel.setMovementMethod(LinkMovementMethod.getInstance());
        
        return rootView;
        
    }
}
