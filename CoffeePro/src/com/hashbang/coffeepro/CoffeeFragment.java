package com.hashbang.coffeepro;

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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

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
    private NumberPicker cupPicker;
    private Spinner unitSpinner;
    private AccordionView accordionView;
    private RatingBar ratingBar;

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
    final private static float STANDARD_WEIGHT_G = 7f;

    final private static float STANDARD_WEIGHT_OZ = 0.25f;
    final private static long DARK_TIMER = 360000L;
    final private static float DARK_WEIGHT_G = 10.5f;

    final private static float DARK_WEIGHT_OZ = 0.375f;
    final private static long LIGHT_TIMER = 120000L;
    final private static float LIGHT_WEIGHT_G = 7f;
    final private static float LIGHT_WEIGHT_OZ = 0.25f;
    protected static final String COMMENT_TAG = "Comment";

    protected static final String RATE_TAG = "Rate";
    private float weightGrams;

    private float weightOunces;
    static boolean isStarted = false;

    int type = 0;

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

        unitSpinner = (Spinner) rootView.findViewById(R.id.unitSpinner);
        unitSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int position, long id)
            {
                setWeightValues();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                // TODO Auto-generated method stub
            }

        });

        // Set up the picker for number of cups (Cannot be done in XML)
        cupPicker = (NumberPicker) rootView.findViewById(R.id.cupsPicker);
        cupPicker.setMinValue(1);
        cupPicker.setMaxValue(99);
        cupPicker
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
                else
                // Stop Button Pressed
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
        });

        ratingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);
        ratingBar.setRating(sharedPref.getFloat(RATE_TAG + type, 0f));
        ratingBar.setOnTouchListener(new OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                SharedPreferences.Editor editor = sharedPref.edit();
                float rating = ratingBar.getRating();
                editor.putFloat(RATE_TAG + type, rating);
                editor.commit();
                return false;
            }
        });

        commentView = (EditText) rootView.findViewById(R.id.comments);
        commentView.setText(sharedPref.getString(COMMENT_TAG + type, ""));
        commentView.setOnFocusChangeListener(new OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if(hasFocus)
                {
                } // Do Nothing
                else
                {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    String comment = commentView.getText().toString();
                    editor.putString(COMMENT_TAG + type, comment);
                    editor.commit();
                }
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
                setChemexValues(0, 8.4f, 0.3f);
                break;
            case 2:
                mainActivity.setTitle("Siphon");
                mActionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD); // Hide
                                                                             // Dropdown
                accordionView.getSectionByChildId(R.id.timerLayout)
                        .setVisibility(View.GONE);
                setSiphonValues(0, 8.4f, 0.3f);
                break;
            case 3:
                mainActivity.setTitle("AeroPress");
                mActionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD); // Hide
                                                                             // Dropdown
                accordionView.getSectionByChildId(R.id.cupLayout)
                        .setVisibility(View.GONE);
                setAeroPressValues(30000L, 17f, 0.6f);
            default:
                break;
        }

        return rootView;
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId)
    {
        switch (itemPosition)
        {
            case 0:
                setPressValues(STANDARD_TIMER, STANDARD_WEIGHT_G,
                        STANDARD_WEIGHT_OZ);
                break;
            case 1:
                setPressValues(DARK_TIMER, DARK_WEIGHT_G, DARK_WEIGHT_OZ);
                break;
            case 2:
                setPressValues(LIGHT_TIMER, LIGHT_WEIGHT_G, LIGHT_WEIGHT_OZ);
                break;
            default:
                // Do nothing
                break;
        }
        return false;
    }

    private void setAeroPressValues(long aSteepTime, float aWeightGrams,
            float aWeightOunces)
    {
        this.weightGrams = aWeightGrams;
        this.weightOunces = aWeightOunces;
        setWeightValues();
        timerStartValue = aSteepTime;
        setTimerLabel(timerStartValue);
        groundsView.setText("Fine Grind - " + this.weightGrams + "g/"
                + this.weightOunces + "oz per 4oz Cup");
        instructionView
                .setText("\n1.Remove the plunger and the cap from the chamber."
                        + "\n\n2. Put a filter in the cap and twist it onto the chamber."
                        + "\n\n3. Stand the chamber on a sturdy mug."
                        + "\n\n4. Put two AeroPress scoops of fine-drip grind coffee into the chamber."
                        + "\n\n5. Pour hot water slowly into the chamber up to the number 2. Use 175F (80C) water for the very best taste."
                        + "\n\n6. Mix the water and coffee with the stirrer for about 10 seconds."
                        + "\n\n7. Wet the rubber seal and insert the plunger into the chamber. Gently press down about a quarter of an inch and "
                        + "maintain that pressure for about 20 to 30 seconds until the plunger bottoms on the coffee. Gentle pressure is the key to "
                        + "easy AeroPressing.");
        // Official AeroPress Instructions

    }

    private void setChemexValues(long aSteepTime, float aWeightGrams,
            float aWeightOunces)
    {
        this.weightGrams = aWeightGrams;
        this.weightOunces = aWeightOunces;
        setWeightValues();
        timerStartValue = aSteepTime;
        setTimerLabel(timerStartValue);
        groundsView.setText("Medium Grind - " + this.weightGrams + "g/"
                + this.weightOunces + "oz per 5oz Cup");
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

    private void setPressValues(long aSteepTime, float aWeightGrams,
            float aWeightOunces)
    {
        this.weightGrams = aWeightGrams;
        this.weightOunces = aWeightOunces;
        setWeightValues();
        timerStartValue = aSteepTime;
        setTimerLabel(timerStartValue);
        groundsView.setText("Course Grind - " + this.weightGrams + "g/"
                + this.weightOunces + "oz per 4oz Cup");
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

    private void setSiphonValues(long aSteepTime, float aWeightGrams,
            float aWeightOunces)
    {
        this.weightGrams = aWeightGrams;
        this.weightOunces = aWeightOunces;
        setWeightValues();
        timerStartValue = aSteepTime;
        setTimerLabel(timerStartValue);
        groundsView.setText("Course Grind - " + this.weightGrams + "g/"
                + this.weightOunces + "oz per 4oz Cup");
        instructionView
                .setText("\n1. Measure the amount of water you are going to brew and place it in the lower chamber of the vacuum pot. Begin heating "
                        + "the water until it is almost boiling, or 190 to 195 degrees."
                        + "\n\n2.  While the water heats, measure and grind your coffee. Start with a drip course grind, measuring "
                        + this.weightGrams
                        + " grams, or "
                        + this.weightOunces
                        + " ounces, of coffee per 4 ounces of water."
                        + "\n\n3.Once the water is near boiling, place the top half of the vacuum pot back onto the lower section, being careful to allow for a tight seal"
                        + " between the upper and lower chambers. When you do this, you will create a positive pressure in the lower chamber that will force most of the water into the top bowl. "
                        + "When three-fourths of the water is in the top chamber, lower the temperature on your burner to medium-low or low. "
                        + "\n\n4. Once nearly all the water is in the upper chamber, add the coffee to the water and stir it well, wetting all the grounds to ensure a uniform extraction. "
                        + "Let the coffee brew for two minutes and then remove it from the heat source entirely. Ideally it will take 20 to 45 seconds for"
                        + " the water to drop back to the lower chamber. If it takes more than 60 seconds to drop, adjust to a coarser grind. "
                        + "If it comes down too quickly, make the grind finer."
                        + "\n\n 5. Taste the coffee and note how you would adjust the grind, brew time, and temperature for future pots.");
        // http://www.kickapoocoffee.com/Kickapoo-Coffee-Vacuum-Pot-Coffee-Brewing-Instructions-a/144.htm

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
        int cups = cupPicker.getValue();
        setWeightValues(cups);
    }

    private void setWeightValues(int cups)
    {
        int unitPosition = unitSpinner.getSelectedItemPosition();
        switch (unitPosition)
        {
            case 0:
                groundWeightView.setText(Float.toString(weightGrams));
                float totalGrams = weightGrams * cups;
                totalGroundsView.setText(Float.toString(totalGrams));
                totalGroundsUnitsView.setText("g");
                break;
            case 1:
                groundWeightView.setText(Float.toString(weightOunces));
                float totalOunces = weightOunces * cups;
                totalGroundsView.setText(Float.toString(totalOunces));
                totalGroundsUnitsView.setText("oz");
                break;
            default:
                break;
        }
    }
}