package com.hashbang.coffeenerd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.TextView;

import com.hashbang.coffeepro.R;
import com.sentaca.android.accordion.widget.AccordionView;

public class CoffeeFragment extends Fragment implements
        ActionBar.OnNavigationListener
{

	public static CoffeeFragment newInstance(int fragmentType)
    {
        // Supply index input as an argument.
        CoffeeFragment f = new CoffeeFragment();
        Bundle args = new Bundle();
        args.putInt("type", fragmentType);
        f.setArguments(args);
        return f;
    }

    Activity mainActivity;
    private Button startButton;
    private Button resetButton;
    private TextView instructionView;
    private TextView groundsView;
    private EditText groundWeightView;
    private TextView totalGroundsView;
    private TextView totalGroundsUnitsView;
    private TextView ratioLabel;
    private TextView volumeLabel;
    private NumberPicker waterVolumePicker;
    private AccordionView accordionView;
    private RatingBar ratingBar;
    
    private int mYear;
    private int mMonth;
    private int mDay;
    static final int DATE_DIALOG_ID = 0;

    private TextView mDateDisplay;
    private ImageButton mPickDate;
   
    private EditText commentView;
    private TextView timerValue;
    private Handler customHandler = new Handler();
    private PowerManager.WakeLock wl;

    private SharedPreferences sharedPref;
    private long startTime = 0L;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    long timerStartValue = 5000L;
    final private static long STANDARD_TIMER = 240000L;
    final private static long DARK_TIMER = 360000L;
    final private static long LIGHT_TIMER = 120000L;
    private static int themeColour = Color.WHITE;

    static boolean isStarted = false;

    int type = 0;
    int subType = 0;
    
    private float ratio;
    String volumeUnit;
    String massUnit;

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
                timerValue.setTextColor(themeColour);
                customHandler.postDelayed(this, 0);
                setTimerLabel(countdown);
            }
        }
    };

    public CoffeeFragment()
    {
    }
    
    @Override
    public void onResume()
    {		
    	super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Bundle savedBundle = this.getArguments();
        if(savedBundle != null)
        {
            type = savedBundle.getInt("type");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {

        View rootView = inflater.inflate(R.layout.coffee_fragment, container,
                false);
        mainActivity = getActivity();
        


        PowerManager manager = (PowerManager) mainActivity
                .getSystemService(Context.POWER_SERVICE);
        wl = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "Partial WakeLock");
        wl.setReferenceCounted(false);

        Resources res = mainActivity.getResources();
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        
        volumeUnit = sharedPref.getString(PreferencesFragment.VOLUME_TAG, "ml");
        massUnit = sharedPref.getString(PreferencesFragment.MASS_TAG, "g");
        
        volumeLabel = (TextView) rootView.findViewById(R.id.volumeLabel);
        volumeLabel.setText(res.getString(R.string.volume_label)+" ("+volumeUnit+")");
        ratioLabel = (TextView) rootView.findViewById(R.id.ratioLabel);
        ratioLabel.setText(res.getString(R.string.ratio_label)+" ("+massUnit+"/"+volumeUnit+")");
        
        // Set up the dropdown menu in the action bar
        ActionBar mActionbar = mainActivity.getActionBar();
        mActionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        // Populate Drop down
        
	    ArrayAdapter<String> lTypeAdapter = null;
	    
	    
	    if(type == 3)
	    {
	    	lTypeAdapter = new ArrayAdapter<String>(
	                mainActivity, android.R.layout.simple_dropdown_item_1line,
	                res.getStringArray(R.array.aero_types));
	    }
	    else //Default to French Press
	    {
		    lTypeAdapter = new ArrayAdapter<String>(
		                mainActivity, android.R.layout.simple_dropdown_item_1line,
		                res.getStringArray(R.array.fp_types));
	    }
        mActionbar.setListNavigationCallbacks(lTypeAdapter, this);

        instructionView = (TextView) rootView
                .findViewById(R.id.instructionsTextView);
        groundsView = (TextView) rootView.findViewById(R.id.groundsTextView);
        groundWeightView = (EditText) rootView.findViewById(R.id.groundsWeight);
        groundWeightView.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				setWeightValues(waterVolumePicker.getValue());
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});

        totalGroundsView = (TextView) rootView.findViewById(R.id.totalGrounds);
        totalGroundsUnitsView = (TextView) rootView
                .findViewById(R.id.totalGroundsUnits);
        accordionView = (AccordionView) rootView
                .findViewById(R.id.accordion_view);

        // Set up the picker for number of cups (Cannot be done in XML)
        waterVolumePicker = (NumberPicker) rootView.findViewById(R.id.cupsPicker);
        waterVolumePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        if("cup".equalsIgnoreCase(volumeUnit))
        {
	        waterVolumePicker.setMinValue(1);
	        waterVolumePicker.setMaxValue(99);
        }
        else
        {
        	int maxValue = 2005;
        	int step = 5;

        	String[] valueSet = new String[maxValue/step];
        	
        	int value = 0;
        	for (int i = 0; i <= 400; i++)
        	{
        	    valueSet[i] = String.valueOf(value);
        	    value += step;
        	}
        	waterVolumePicker.setMinValue(0);
        	waterVolumePicker.setMaxValue(400);
        	waterVolumePicker.setDisplayedValues(valueSet);
        	waterVolumePicker.setValue(50);
	    }
        waterVolumePicker
                .setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
                {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal,
                            int newVal)
                    {
                        setWeightValues(newVal);
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
       
        mDateDisplay = (TextView) rootView.findViewById(R.id.date_text);
        
        final DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener()
        {

		    public void onDateSet(DatePicker view, int aYear,
		        int aMonth, int aDay)
		    {
	            mYear = aYear;
	            mMonth = aMonth;
	            mDay = aDay;
	            updateDisplay();
	            loadLog();
		    }
		};
		
        // get the current date
        final Calendar c = Calendar.getInstance();

        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        // display the current date
        updateDisplay();
		
        mPickDate = (ImageButton) rootView.findViewById(R.id.date_button);
        mPickDate.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
            	DatePickerDialog dialog = new DatePickerDialog(mainActivity, datePickerListener, mYear, mMonth, mDay);
            	dialog.show();
            }
        });
        
        ratingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);

        commentView = (EditText) rootView.findViewById(R.id.comments);
        
        switch (type)
        {
            case 0:
                mainActivity.setTitle("French Press");
                break;
            case 1:
                mainActivity.setTitle("Chemex");
                mActionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD); // Hide
                                                                             // Dropdown
                accordionView.getSectionByChildId(R.id.timerLayout)
                        .setVisibility(View.GONE);
                if("oz".equalsIgnoreCase(massUnit))
                {
                	if("cup".equalsIgnoreCase(volumeUnit))
                	{
                		setChemexValues(0, 0.24f);
                	}
                	else //oz per ml
                	{
                		setChemexValues(0, 0.002f);
                	}
                }
                else //g
                {
                	if("cup".equalsIgnoreCase(volumeUnit))
                	{
                		setChemexValues(0, 6.72f); //g per cup
                	}
                	else
                	{
                		setChemexValues(0, 0.057f); //g per ml
                	}
                }
                break;
            case 2:
                mainActivity.setTitle("Siphon");
                mActionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD); // Hide
                                                                             // Dropdown
                accordionView.getSectionByChildId(R.id.timerLayout)
                        .setVisibility(View.GONE);
                if("oz".equalsIgnoreCase(massUnit))
                {
                	if("cup".equalsIgnoreCase(volumeUnit))
                	{
                		setSiphonValues(0, 0.3f);
                	}
                	else //oz per ml
                	{
                		setSiphonValues(0, 0.0025f);
                	}
                }
                else //g
                {
                	if("cup".equalsIgnoreCase(volumeUnit))
                	{
                		setSiphonValues(0, 8.4f); //g per cup
                	}
                	else
                	{
                		setSiphonValues(0, 0.071f); //g per ml
                	}
                }
                break;
            case 3:
                mainActivity.setTitle("AeroPress");
                break;
            case 4:
                mainActivity.setTitle("Hario V60");
                mActionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD); // Hide
                                                                             // Dropdown
                accordionView.getSectionByChildId(R.id.timerLayout)
                        .setVisibility(View.GONE);
                if("oz".equalsIgnoreCase(massUnit))
                {
                	if("cup".equalsIgnoreCase(volumeUnit))
                	{
                		setHarioValues(0, 0.35f);
                	}
                	else //oz per ml
                	{
                		setHarioValues(0, 0.003f);
                	}
                }
                else //g
                {
                	if("cup".equalsIgnoreCase(volumeUnit))
                	{
                		setHarioValues(0, 10f); //g per cup
                	}
                	else
                	{
                		setHarioValues(0, 0.085f); //g per ml
                	}
                }
                break;
            case 5:
                mainActivity.setTitle("Automatic Drip");
                mActionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD); // Hide
                                                                             // Dropdown
                accordionView.getSectionByChildId(R.id.timerLayout)
                        .setVisibility(View.GONE);
                if("oz".equalsIgnoreCase(massUnit))
                {
                	if("cup".equalsIgnoreCase(volumeUnit))
                	{
                		setDripValues(0, 0.212f);
                	}
                	else //oz per ml
                	{
                		setDripValues(0, 0.0018f);
                	}
                }
                else //g
                {
                	if("cup".equalsIgnoreCase(volumeUnit))
                	{
                		setDripValues(0, 6f); //g per cup
                	}
                	else
                	{
                		setDripValues(0, 0.051f); //g per ml
                	}
                }
                break;
            default:
                break;
        }
        
        loadLog();

        return rootView;
    }

	private void loadLog()
	{
		String lLogFileName = buildLogFileName();
    	
    	File sdcard = mainActivity.getExternalFilesDir(null);

    	//Get the text file
    	File file = new File(sdcard,lLogFileName);

    	//Read text from file
    	StringBuilder commentText = new StringBuilder();
    	String ratingAsText = "0";
    	try
    	{
    	    BufferedReader br = new BufferedReader(new FileReader(file));
    	    String line;
    	    
    	    int lineCount = 0;
    	    
    	    while ((line = br.readLine()) != null)
    	    {
    	    	if(lineCount == 0) //Rating
    	    	{
    	    		ratingAsText = line;
    	    	}
    	    	else
    	    	{
	    	        commentText.append(line);
	    	        commentText.append('\n');
    	    	}
    	    	lineCount++;
    	    }
    	    
    	    br.close();
    	}
    	catch (Exception e)
    	{
    	    Log.d("FileNotFound", e.toString());
    	}
    	
    	ratingBar.setRating(Float.parseFloat(ratingAsText));
    	commentView.setText(commentText);
	}

	private String buildLogFileName() {
		String lLogFileName = ""+ mMonth+"-"+mDay+"-"+mYear+"_"+type;
    	
    	if(type == 0 || type == 3) //French Press or Aeropress
    	{
    		lLogFileName += "_"+subType+".txt";
    	}
    	else
    	{
    		lLogFileName +=".txt";
    	}
		return lLogFileName;
	}
    


	@Override
	public void onDestroy()
	{
		Log.d("CoffeeFragment", "OnDestroy");
		
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
        startTime = 0L;
        timeInMilliseconds = 0L;
        timeSwapBuff = 0L;
        updatedTime = 0L;
        setTimerLabel(timerStartValue);
        isStarted = false;
        startButton.setText("Start");
        timerValue.setTextColor(themeColour);
        if(isStarted)
        {
            wl.release();
        }
	}
	
	@Override
	public void onPause()
	{
		outputLogFile();
		super.onPause();
	}
	
    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId)
    {
    	outputLogFile();
    	subType = itemPosition;
    	
    	if(type == 0)
    	{
	        switch (itemPosition)
	        {
	            case 0:
	                if("oz".equalsIgnoreCase(massUnit))
	                {
	                	if("cup".equalsIgnoreCase(volumeUnit))
	                	{
	                		setPressValues(STANDARD_TIMER, 0.025f);
	                	}
	                	else //oz per ml
	                	{
	                		setPressValues(STANDARD_TIMER, 0.002f);
	                	}
	                }
	                else //g
	                {
	                	if("cup".equalsIgnoreCase(volumeUnit))
	                	{
	                		setPressValues(STANDARD_TIMER, 7); //g per cup
	                	}
	                	else
	                	{
	                		setPressValues(STANDARD_TIMER, 0.059f); //g per ml
	                	}
	                }
	                
	                break;
	            case 1:
	            	if("oz".equalsIgnoreCase(massUnit))
	                {
	                	if("cup".equalsIgnoreCase(volumeUnit))
	                	{
	                		setPressValues(DARK_TIMER, 0.375f);
	                	}
	                	else //oz per ml
	                	{
	                		setPressValues(DARK_TIMER, 0.003f);
	                	}
	                }
	                else //g
	                {
	                	if("cup".equalsIgnoreCase(volumeUnit))
	                	{
	                		setPressValues(DARK_TIMER, 10.5f); //g per cup
	                	}
	                	else
	                	{
	                		setPressValues(DARK_TIMER, 0.089f); //g per ml
	                	}
	                }
	                break;
	            case 2:
	            	if("oz".equalsIgnoreCase(massUnit))
	                {
	                	if("cup".equalsIgnoreCase(volumeUnit))
	                	{
	                		setPressValues(LIGHT_TIMER, 0.025f);
	                	}
	                	else //oz per ml
	                	{
	                		setPressValues(LIGHT_TIMER, 0.002f);
	                	}
	                }
	                else //g
	                {
	                	if("cup".equalsIgnoreCase(volumeUnit))
	                	{
	                		setPressValues(LIGHT_TIMER, 7); //g per cup
	                	}
	                	else
	                	{
	                		setPressValues(LIGHT_TIMER, 0.059f); //g per ml
	                	}
	                }
	                break;
	            default:
	                // Do nothing
	                break;
	        }
    	}
    	else if(type == 3)//Aeropress
    	{
    		boolean lIsInverted = false;
    		if(itemPosition == 0)
    		{
    			lIsInverted = false;
    		}
    		else
    		{
    			lIsInverted = true;
    		}
    		accordionView.getSectionByChildId(R.id.cupLayout).setVisibility(View.GONE);
    		
			if("oz".equalsIgnoreCase(massUnit))
			{
				if("cup".equalsIgnoreCase(volumeUnit))
				{
					setAeroPressValues(30000, 0.6f, lIsInverted);//oz per cup
				}
				else //oz per ml
			 	{
			 		setAeroPressValues(30000, 0.005f,lIsInverted);
			 	}
			 }
			else //g
			{
				if("cup".equalsIgnoreCase(volumeUnit))
				{
					setAeroPressValues(30000, 17,lIsInverted); //g per cup
				}
				else
				{
					setAeroPressValues(30000, 0.144f,lIsInverted); //g per ml
			 	}
			}
    	}
    	loadLog();
        resetTimer();
        return false;
    }

    private void setAeroPressValues(long aSteepTime, float aRatio, boolean lIsInverted)
    {
    	this.ratio = aRatio;
        setWeightValues(aRatio);
        timerStartValue = aSteepTime;
        setTimerLabel(timerStartValue);
        
        if(lIsInverted)
        {
        	groundsView.setText("Recommended: Fine Grind - 1 Aeropress Scoops (~17g)");
        	instructionView
            .setText("\n1. Removed cap from the chamber."
                    + "\n\n2. Insert the plunger such that it is touching the bottom of the number 4."
                    + "\n\n3. Stand the aeropress upside down on the plunger with the cap opening is facing upward. "
                    + "\n\n4. Put one AeroPress scoop of fine-drip grind coffee into the chamber. It should reach near the number 3."
                    + "\n\n5. Pour hot water slowly into the chamber up to the top of the number 2. Use water around 175F (80C)."
                    + "\n\n6. Mix the water and coffee with the stirrer for about 10 seconds."
                    + "\n\n7. Allow the coffee to steep for an additional 30 seconds. "
                    + "\n\n8. Replace the filter paper and screw on the cap tightly."
                    + "\n\n9. Carefully flip the aeropress on to the mug. Slowly press the plunger for about to 20-30s. "
                    + "\n\n10. Top off with with hot water as necessary.");
					// Inverted AeroPress Instructions
        }
        else
        {
        	groundsView.setText("Recommended: Fine Grind - 2 Aeropress Scoops (~34g)");
	        instructionView
	                .setText("\n1. Remove the plunger and the cap from the chamber."
	                        + "\n\n2. Put a filter in the cap and twist it onto the chamber."
	                        + "\n\n3. Stand the chamber on a sturdy mug."
	                        + "\n\n4. Put two AeroPress scoops of fine-drip grind coffee into the chamber."
	                        + "\n\n5. Pour hot water slowly into the chamber up to the number 2. Use 175F (80C) water for the very best taste."
	                        + "\n\n6. Mix the water and coffee with the stirrer for about 10 seconds."
	                        + "\n\n7. Wet the rubber seal and insert the plunger into the chamber. Gently press down about a quarter of an inch and "
	                        + "maintain that pressure for about 20 to 30 seconds until the plunger bottoms on the coffee. Gentle pressure is the key to "
	                        + "easy AeroPressing."
	                        +"\n\n8. You've just made a double espresso. For American coffee,  top-off the mug with hot water. For a latte, top-off "
	                        +"the mug with hot milk.");
        					// Official AeroPress Instructions
        }

    }

    private void setChemexValues(long aSteepTime, float aRatio)
    {
        this.ratio = aRatio;
        setWeightValues(aRatio);
        timerStartValue = aSteepTime;
        setTimerLabel(timerStartValue);
        groundsView.setText("Recommended: Medium Grind - "+aRatio+massUnit+" per "+volumeUnit);
        instructionView
                .setText("\n1. Open the Chemex-Bonded® Coffee Filter into a cone. One side should have three layers. Place the cone in the top of "
                        + "your coffeemaker with the thick portion toward the pouring spout"
                        + "\n\n2. Using Regular or Automatic Grind coffee only, put one rounded tablespoon of coffee per 5 oz. cup into the filter "
                        + "cone. If you prefer stronger coffee, use more; there is never any bitterness in coffee brewed using the Chemex® method."
                        + "\n\n3. When the water is boiling, remove it from the heat until it stops boiling vigorously. It should now be at about 200°F,"
                        + " a perfect brewing temperature. Pour a small amount of water over the coffee grounds, just enough to wet them without "
                        + "floating. This is important because it allows the grounds to \"bloom\" so the desirable coffee elements can be released"
                        + "\n\n4.  After this first wetting simply pour more water, soaking the grounds each time, but keeping the water level well"
                        + " below the top of the coffeemaker. Once the desired amount of coffee is brewed, dispose of the spent grounds by lifting the "
                        + "filter out of the coffeemaker. And that's it! You are now ready to enjoy a perfect cup of coffee!");
        // Official Chemex
    }

    private void setPressValues(long aSteepTime, float aRatio)
    {
        this.ratio = aRatio;
        setWeightValues(aRatio);
        timerStartValue = aSteepTime;
        setTimerLabel(timerStartValue);
        groundsView.setText("Recommended: Course Grind - "+aRatio+massUnit+" per "+volumeUnit);
        instructionView
                .setText("\n1. Pour hot (not boiling) water into the pot. Leave a minimum of 2.5 cm/1 inch of space at"
                        + " the top. \n\n2. Stir the brew with a plastic spoon. \n\n3.Place the plunger unit on top of the pot. "
                        + "Turn lid to close off the pour spout opening. Let the coffee brew for at least "
                        + (aSteepTime / 60000)
                        + " minutes. \n\n4. Hold"
                        + " the pot handle firmly with the spout turned away from you, then using just the weight of your "
                        + "hand, apply slight pressure on top of the knob to lower the plunger straight down into the pot");
        // Bodum Instruction
    }

    private void setSiphonValues(long aSteepTime, float aRatio)
    {
    	ratio = aRatio;
        setWeightValues(aRatio);
        timerStartValue = aSteepTime;
        setTimerLabel(timerStartValue);
        groundsView.setText("Recommended: Course Grind - "+aRatio+massUnit+" per "+volumeUnit);
        instructionView
                .setText("\n1. Measure the amount of water you are going to brew and place it in the lower chamber of the vacuum pot. Begin heating "
                        + "the water until it is almost boiling, or 190 to 195 degrees."
                        + "\n\n2.  While the water heats, measure and grind your coffee. Start with a drip course grind, measuring "
                        + this.ratio+massUnit+" per "+volumeUnit+" of water."
                        + "\n\n3. Once the water is near boiling, place the top half of the vacuum pot back onto the lower section, being careful to allow for a tight seal"
                        + " between the upper and lower chambers. When you do this, you will create a positive pressure in the lower chamber that will force most of the water into the top bowl. "
                        + "When three-fourths of the water is in the top chamber, lower the temperature on your burner to medium-low or low. "
                        + "\n\n4. Once nearly all the water is in the upper chamber, add the coffee to the water and stir it well, wetting all the grounds to ensure a uniform extraction. "
                        + "Let the coffee brew for two minutes and then remove it from the heat source entirely. Ideally it will take 20 to 45 seconds for"
                        + " the water to drop back to the lower chamber. If it takes more than 60 seconds to drop, adjust to a coarser grind. "
                        + "If it comes down too quickly, make the grind finer."
                        + "\n\n 5. Taste the coffee and note how you would adjust the grind, brew time, and temperature for future pots.");
        // http://www.kickapoocoffee.com/Kickapoo-Coffee-Vacuum-Pot-Coffee-Brewing-Instructions-a/144.htm
    }
    
	private void setHarioValues(int aSteepTime, float aRatio)
	{
		 this.ratio = aRatio;
	        setWeightValues(aRatio);
	        timerStartValue = aSteepTime;
	        setTimerLabel(timerStartValue);
	        groundsView.setText("Recommended: Medium-Fine Grind - "+aRatio+massUnit+" per "+volumeUnit);
	        instructionView
	                .setText("\n1. Fold the paper ﬁlter along the seams and place inside the cone. Add coffee grounds (medium-ﬁne grind) for your " +
	                		"required servings and shake it lightly to level. 10-12g is normally good for one serving (120ml). The attached measuring spoon = 12g / 1 spoon. " +
	                		"Using freshly ground coffee is recommended. (Adjust proportions for a stronger or weaker brew)\n" +
	                		"\n2. Take the boiling water off the ﬂame. Wait for the boiling water to settle. Pour hot water slowly to moisten the grounds from the center" +
	                		" to the outward with moving circular pattern. Wait for about 30 seconds until next pouring.\n" +
	                		"\n3. Slowly start adding more water using the same speed, swirling motion as before. Make sure the water does not come in direct contact with " +
	                		"the paper ﬁlter. Brewing should take 3 minutes");
	        // Official Hario http://www.hario.jp/pdf/VDG-03B_Instruction_Manual1106.pdf
		
	}
	
	private void setDripValues(int aSteepTime, float aRatio)
	{
		this.ratio = aRatio;
        setWeightValues(aRatio);
        timerStartValue = aSteepTime;
        setTimerLabel(timerStartValue);
        groundsView.setText("Recommended: Medium Grind - "+aRatio+massUnit+" per "+volumeUnit);
        instructionView
                .setText("\n1. Place the paper filter into the filter basket. Optional: Preheat the decanter with hot water.\n" +
                		"\n2. Grind the beans to a medium grind. Use approximately "+aRatio+massUnit+" per "+volumeUnit+".\n"+
                		"\n3. Add the required amount of water to the coffee machine.\n" +
                		"\n4. Turn on the machine and wait a few minutes. Turn off the machine if necessary.");		
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

    private void setWeightValues(float aRatio)
    {
        int volumeValue = waterVolumePicker.getValue();
        groundWeightView.setText(""+aRatio);
        setWeightValues(volumeValue);
    }

    private void setWeightValues(int volume)
    {
	    float setRatio = Float.parseFloat(groundWeightView.getText().toString());
	    
	    float totalMass = setRatio * (float)volume;
	    if("ml".equalsIgnoreCase(volumeUnit))
	    {
	    	totalMass *= 5f;
	    }
	   
	    totalMass = Math.round(totalMass * 100f)/100f; //Round to 2 decimal places
	    totalGroundsView.setText(Float.toString(totalMass));
	    totalGroundsUnitsView.setText(sharedPref.getString(PreferencesFragment.MASS_TAG, ""));
    }
    
    public void setThemeColour(int aColour)
    {
    	themeColour = aColour;
    }
    
    private void updateDisplay() {
        this.mDateDisplay.setText(
            new StringBuilder()
                    // Month is 0 based so add 1
                    .append(getMonthForInt(mMonth)).append(" ")
                    .append(mDay).append(", ")
                    .append(mYear).append(" "));
    }
    
    String getMonthForInt(int num)
    {
        String month = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (num >= 0 && num <= 11 ) {
            month = months[num];
        }
        return month;
    }

	private void outputLogFile()
	{
		String data = ""+ratingBar.getRating()+"\n"+commentView.getText().toString();
		
		try
		{
			File sdcard = mainActivity.getExternalFilesDir(null);
			String lLogFileName = buildLogFileName();
		    //Get the text file
			File root =  new File(sdcard, "");
			if (!root.exists())
			{
			    root.mkdirs();
			}
			File gpxfile = new File(root, lLogFileName);
			FileWriter writer = new FileWriter(gpxfile);
			writer.append(data);
			writer.flush();
			writer.close();
		}
		catch(IOException e)
	    {
	         e.printStackTrace();
	    }
	}


}