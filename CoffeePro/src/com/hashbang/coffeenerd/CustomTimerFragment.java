package com.hashbang.coffeenerd;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.hashbang.coffeepro.R;

public class CustomTimerFragment extends Fragment
{
    public static CustomTimerFragment newInstance()
    {
        CustomTimerFragment f = new CustomTimerFragment();
        return f;
    }
    
    public CustomTimerFragment(){}
    
    private Activity mainActivity;  
    private Button startButton;
    private Button resetButton;
    private SeekBar timerMinuteSeekBar;
    private SeekBar timerSecondSeekBar;
    
    private SharedPreferences sharedPref;
    static boolean isStarted = false;
    private TextView timerValue;
    private Handler customHandler = new Handler();
    private PowerManager.WakeLock wl;
    long timerStartValue = 0L;
    private long startTime = 0L;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    
    private Runnable updateTimerThread = new Runnable()
    {
        @Override
        public void run()
        {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            long countdown = timerStartValue - updatedTime;
            if(countdown <= 0)
            {
                customHandler.removeCallbacks(this);
                timerValue.setTextColor(Color.RED);
                setTimerLabel(0);
                try
                {
                    Uri notification = RingtoneManager
                            .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(mainActivity,
                            notification);
                    r.play();
                }
                catch (Exception e)
                {
                	Log.e("NotificationSound", e.toString());
                }
                wl.release();
                isStarted = false;
            }
            else
            {
                timerValue.setTextColor(Color.WHITE);
                customHandler.postDelayed(this, 0);
                setTimerLabel(countdown);
            }
        }
    };
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {

        View rootView = inflater.inflate(R.layout.timer_fragment, container,
                false);
        mainActivity = getActivity();
        mainActivity.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        
        PowerManager manager = (PowerManager) mainActivity
                .getSystemService(Context.POWER_SERVICE);
        wl = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "Partial WakeLock");
        wl.setReferenceCounted(false);
        
        sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);
        
        timerMinuteSeekBar = (SeekBar) rootView.findViewById(R.id.timerMinuteSeekBar);
        timerMinuteSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar){	
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				timerStartValue = Math.round(progress/60000)*60000;
				timerStartValue += Math.round(timerSecondSeekBar.getProgress()/1000)*1000;
				setTimerLabel(timerStartValue);
				resetTimer();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
		});
        
        timerSecondSeekBar = (SeekBar) rootView.findViewById(R.id.timerSecondSeekBar);
        timerSecondSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar){	
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				timerStartValue = Math.round(timerMinuteSeekBar.getProgress()/60000)*60000;
				timerStartValue += Math.round(progress/1000)*1000;
				setTimerLabel(timerStartValue);
				resetTimer();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
		});
        
        
        // Set timer to start value
        timerValue = (TextView) rootView.findViewById(R.id.timerValue);
        setTimerLabel(timerStartValue);
        
        startButton = (Button) rootView.findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {
                if(!isStarted)
                {
                	if ((wl != null) &&           // we have a WakeLock
                		    (wl.isHeld() == false))
                	{  // but we don't hold it 
                		wl.acquire();
                	}
                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread, 0);
                    ((Button) view).setText("Stop");
                    isStarted = true;
                    // Lock Screen in current orientation
                    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    {
                        mainActivity
                                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                    else
                    {
                        mainActivity
                                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                }
                // Stop Button Pressed
                else
                {
                    wl.release();
                    timeSwapBuff += timeInMilliseconds;
                    customHandler.removeCallbacks(updateTimerThread);
                    ((Button) view).setText("Start");
                    isStarted = false;
                    // Allow rotation when complete
                    mainActivity
                            .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }

            }
        });

        resetButton = (Button) rootView.findViewById(R.id.pauseButton);
        resetButton.setOnClickListener(new View.OnClickListener()
        {
            // Stop the timer, reset the value
            @Override
            public void onClick(View view)
            {
                resetTimer();
            }
        });

        
        return rootView;
        
    }
    
    @Override
	public void onDestroy()
	{	
		//Release the wake lock if the fragment is interrupted or closed.
		if(wl != null)
		{
			if(wl.isHeld())
			{
				try
				{
					resetTimer();
				}
				catch(Exception ex)
				{
					Log.e("CoffeeNerdException", ex.toString());
				}
			}
		}
		super.onDestroy();
	}
    

	private void resetTimer()
	{
		customHandler.removeCallbacks(updateTimerThread);
        startTime = Math.round(timerMinuteSeekBar.getProgress()/60000)*60000 + Math.round(timerSecondSeekBar.getProgress()/1000)*1000;
        timeInMilliseconds = 0L;
        timeSwapBuff = 0L;
        updatedTime = 0L;
        setTimerLabel(timerStartValue);
        isStarted = false;
        startButton.setText("Start");
        timerValue.setTextColor(Color.WHITE);
        if(isStarted)
        {
            wl.release();
        }
	}
	
    private void setTimerLabel(long countdown)
    {
        int secs = (int) (countdown / 1000);
        int mins = secs / 60;
        secs = secs % 60;
        int milliseconds = (int) (countdown % 100);
        timerValue.setText("" + mins + ":" + String.format("%02d", secs) + ":"
                + String.format("%02d", milliseconds));
    }
}
