package com.hashbang.coffeenerd;

import android.app.ActionBar;
import android.app.Activity;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView groundWeightView;
    private TextView totalGroundsView;
    private TextView totalGroundsUnitsView;
    private TextView massLabel;
    private TextView volumeLabel;
    private NumberPicker waterVolumePicker;
    private AccordionView accordionView;
    private RatingBar ratingBar;
    private Button saveButton;
   
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

    protected static final String COMMENT_TAG = "Comment";
    protected static final String RATE_TAG = "Rate";
    static boolean isStarted = false;

    int type = 0;
    
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

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        
        volumeUnit = sharedPref.getString(PreferencesFragment.VOLUME_TAG, "ml");
        massUnit = sharedPref.getString(PreferencesFragment.MASS_TAG, "g");
        
        volumeLabel = (TextView) rootView.findViewById(R.id.volumeLabel);
        volumeLabel.setText(volumeUnit);
        massLabel = (TextView) rootView.findViewById(R.id.massLabel);
        massLabel.setText(massUnit);
        
        // Set up the dropdown menu in the action bar
        ActionBar mActionbar = mainActivity.getActionBar();
        mActionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        // Populate Drop down
        Resources res = mainActivity.getResources();
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(
                mainActivity, android.R.layout.simple_dropdown_item_1line,
                res.getStringArray(R.array.fp_types));
        mActionbar.setListNavigationCallbacks(typeAdapter, this);

        instructionView = (TextView) rootView
                .findViewById(R.id.instructionsTextView);
        groundsView = (TextView) rootView.findViewById(R.id.groundsTextView);
        groundWeightView = (TextView) rootView.findViewById(R.id.groundsWeight);
        totalGroundsView = (TextView) rootView.findViewById(R.id.totalGrounds);
        totalGroundsUnitsView = (TextView) rootView
                .findViewById(R.id.totalGroundsUnits);
        accordionView = (AccordionView) rootView
                .findViewById(R.id.accordion_view);

        // Set up the picker for number of cups (Cannot be done in XML)
        waterVolumePicker = (NumberPicker) rootView.findViewById(R.id.cupsPicker);
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
                    wl.acquire();
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
       
        ratingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);
        ratingBar.setRating(sharedPref.getFloat(RATE_TAG + type, 0f));

        commentView = (EditText) rootView.findViewById(R.id.comments);
        commentView.setText(sharedPref.getString(COMMENT_TAG + type, ""));

        saveButton = (Button) rootView.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new OnClickListener()
        {
        	
        	@Override
        	public void onClick(View v) 
        	{
        		SharedPreferences.Editor editor = sharedPref.edit();
        		float rating = ratingBar.getRating();
        		editor.putFloat(RATE_TAG + type, rating);
        		
        		String comment = commentView.getText().toString();
        		editor.putString(COMMENT_TAG + type, comment);
        		editor.commit();
        		
        		Toast.makeText(mainActivity, mainActivity.getResources().getText(R.string.save_msg), Toast.LENGTH_SHORT).show();
        	}
        });
        
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
                mActionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD); // Hide
                                                                             // Dropdown
                accordionView.getSectionByChildId(R.id.cupLayout)
                        .setVisibility(View.GONE);
                if("oz".equalsIgnoreCase(massUnit))
                {
                	if("cup".equalsIgnoreCase(volumeUnit))
                	{
                		setAeroPressValues(30000, 0.6f);//oz per cup
                	}
                	else //oz per ml
                	{
                		setAeroPressValues(30000, 0.005f);
                	}
                }
                else //g
                {
                	if("cup".equalsIgnoreCase(volumeUnit))
                	{
                		setAeroPressValues(30000, 17); //g per cup
                	}
                	else
                	{
                		setAeroPressValues(30000, 0.144f); //g per ml
                	}
                }
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

        return rootView;
    }
    


	@Override
	public void onPause()
	{
		Log.d("CoffeeFragment", "onPause");
		
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
		super.onPause();
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
        timerValue.setTextColor(Color.WHITE);
        if(isStarted)
        {
            wl.release();
        }
        // Release rotation if necessary
        mainActivity
                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}
	
    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId)
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
        return false;
    }

    private void setAeroPressValues(long aSteepTime, float aRatio)
    {
    	this.ratio = aRatio;
        setWeightValues();
        timerStartValue = aSteepTime;
        setTimerLabel(timerStartValue);
        groundsView.setText("Fine Grind - 2 Aeropress Scoops (~34g)");
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

    private void setChemexValues(long aSteepTime, float aRatio)
    {
        this.ratio = aRatio;
        setWeightValues();
        timerStartValue = aSteepTime;
        setTimerLabel(timerStartValue);
        groundsView.setText("Medium Grind - "+aRatio+massUnit+" per "+volumeUnit);
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
        setWeightValues();
        timerStartValue = aSteepTime;
        setTimerLabel(timerStartValue);
        groundsView.setText("Course Grind - "+aRatio+massUnit+" per "+volumeUnit);
        instructionView
                .setText("\n1. Pour hot (not boiling) water into the pot. Leave a minimum of 2,5 cm/1 inch of space at"
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
        setWeightValues();
        timerStartValue = aSteepTime;
        setTimerLabel(timerStartValue);
        groundsView.setText("Course Grind - "+aRatio+massUnit+" per "+volumeUnit);
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
	        setWeightValues();
	        timerStartValue = aSteepTime;
	        setTimerLabel(timerStartValue);
	        groundsView.setText("Medium-Fine Grind - "+aRatio+massUnit+" per "+volumeUnit);
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
        setWeightValues();
        timerStartValue = aSteepTime;
        setTimerLabel(timerStartValue);
        groundsView.setText("Medium Grind - "+aRatio+massUnit+" per "+volumeUnit);
        instructionView
                .setText("\n1. Place the paper filter into the filter basket. Optional: Preheat the decanter with hot water.\n" +
                		"\n2. Grind the beans to a medium grind. Use approxately "+aRatio+massUnit+" per "+volumeUnit+".\n"+
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

    private void setWeightValues()
    {
        int volumeValue = waterVolumePicker.getValue();
        setWeightValues(volumeValue);
    }

    private void setWeightValues(int volume)
    {
	    groundWeightView.setText(Float.toString(ratio));
	    float totalMass = ratio * (float)volume;
	    if("ml".equalsIgnoreCase(volumeUnit))
	    {
	    	totalMass *= 5f;
	    }
	   
	    totalMass = Math.round(totalMass * 100f)/100f; //Round to 2 decimal places
	    totalGroundsView.setText(Float.toString(totalMass));
	    totalGroundsUnitsView.setText(sharedPref.getString(PreferencesFragment.MASS_TAG, ""));
    }
}