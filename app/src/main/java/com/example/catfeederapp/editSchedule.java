package com.example.catfeederapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class editSchedule extends AppCompatActivity {
    LinearLayout repeat_btn, once_btn, daily_btn, weekdays_btn;
    NumberPicker hourPicker, minutePicker, amPmPicker;

    ImageView cancel_btn, save_btn;

    TextView schedDays, grams;

    BottomSheetDialog repeatItemDialog;
    AtomicInteger hourValue = new AtomicInteger();
    AtomicInteger amPmValue = new AtomicInteger();
    AtomicInteger minuteValue = new AtomicInteger();
    DatabaseReference databaseReference;

    TextView weightBased, customGrams;
    EditText bodyWeight, customGramsInput;

    LinearLayout cat_body_weight, food_total_grams;

    Boolean _weightBased = true;
    Boolean _customGrams = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_schedule);

        repeat_btn = findViewById(R.id.repeat_btn);
        cancel_btn = findViewById(R.id.cancel_btn);
        save_btn = findViewById(R.id.save_btn);

        // Body Weight
        bodyWeight = findViewById(R.id.body_weight);

        //grams
        grams = findViewById(R.id.totalGrams);

        // Modes
        weightBased = findViewById(R.id.Weight_Based_btn);
        customGrams = findViewById(R.id.Custom_Grams_btn);

        // Mode Layouts
        cat_body_weight = findViewById(R.id.cat_body_weight);
        food_total_grams = findViewById(R.id.food_total_grams);

        // Custom Grams Input
        customGramsInput = findViewById(R.id.custom_totalGrams);

        //Picker Section
        hourPicker = findViewById(R.id.hourPicker);
        minutePicker = findViewById(R.id.minutePicker);
        amPmPicker = findViewById(R.id.amPmPicker);
        schedDays = findViewById(R.id.sched_days_label);

        hourPicker.setMinValue(1);
        hourPicker.setMaxValue(12);

        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);

        // for AM/PM picker
        String[] amPmValues = {"AM", "PM"};
        amPmPicker.setMinValue(0);
        amPmPicker.setMaxValue(amPmValues.length - 1);
        amPmPicker.setDisplayedValues(amPmValues);

        // store the value of AM/PM
        amPmPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            amPmValue.set(newVal);
        });

        // store the value of hour
        hourPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            hourValue.set(newVal);
        });

        // store the value of minute

        minutePicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            minuteValue.set(newVal);
        });

        // format 2 digit numbers the number picker
        hourPicker.setFormatter(i -> String.format("%02d", i));
        minutePicker.setFormatter(i -> String.format("%02d", i));

        // Repeat Item Dialog
        repeatItemDialog = new BottomSheetDialog(this);
        repeatItemDialog.setContentView(R.layout.repeat_items_component);

//        // load data from main activity
        Bundle bundle = getIntent().getExtras();
        String schedId = bundle.getString("sched_token");
        String schedTime = bundle.getString("sched_time");
        String schedRepeat = bundle.getString("sched_repeat");
        String bodyWeightValue = bundle.getString("body_weight");
        String totalGrams = bundle.getString("sched_grams");

        Log.d("sched_token", String.valueOf(schedId));
        Log.d("sched_time", String.valueOf(schedTime));
        Log.d("sched_repeat", String.valueOf(schedRepeat));
        Log.d("body_weight", String.valueOf(bodyWeightValue));
        Log.d("total_grams", String.valueOf(totalGrams));

        loadSchedData(schedId, schedTime, schedRepeat, bodyWeightValue, totalGrams);

        customGramsInput.setVisibility(EditText.GONE);

        weightBased.setOnClickListener(v -> {
            _weightBased = true;
            _customGrams = false;

            cat_body_weight.setVisibility(LinearLayout.VISIBLE);
            food_total_grams.setVisibility(LinearLayout.VISIBLE);

            grams.setVisibility(TextView.VISIBLE);
            customGramsInput.setVisibility(EditText.GONE);

            // change the color of the text
            weightBased.setTextColor(getResources().getColor(R.color.purple_200));
            customGrams.setTextColor(getResources().getColor(R.color.gray_200));

        });

        customGrams.setOnClickListener(v -> {
            _weightBased = false;
            _customGrams = true;

            cat_body_weight.setVisibility(LinearLayout.GONE);
            food_total_grams.setVisibility(LinearLayout.VISIBLE);

            customGramsInput.setVisibility(LinearLayout.VISIBLE);
            grams.setVisibility(TextView.GONE);

            // change the color of the text
            weightBased.setTextColor(getResources().getColor(R.color.gray_200));
            customGrams.setTextColor(getResources().getColor(R.color.purple_200));

        });

        cancel_btn.setOnClickListener(v -> {
            finish();
        });

        bodyWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (bodyWeight.getText().toString().equals("")){
                    grams.setText("0");
                }else{
                    grams.setText(String.valueOf(calculateGrams(Integer.parseInt(bodyWeight.getText().toString()))));
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        save_btn.setOnClickListener(v -> {

            // get the value of AM/PM, hour and minute

            String amPm = amPmValues[amPmValue.get()];

            // format 2 digit numbers of hours and minutes
            String hour = String.format("%02d", hourValue.get());
            String minute = String.format("%02d", minuteValue.get());

            // format the time to 12 hour format
            String time = hour + ":" + minute + " " + amPm;

            // get the value of repeat days
            String repeatDays = schedDays.getText().toString();

            updateSchedule(schedId, time, repeatDays, bodyWeight.getText().toString(), grams.getText().toString());

            if(_weightBased){
                updateSchedule(schedId, time, repeatDays, bodyWeight.getText().toString(), grams.getText().toString());
            }else if(_customGrams){
                updateSchedule(schedId, time, repeatDays, "", customGramsInput.getText().toString());
            }
        });

        repeat_btn.setOnClickListener(v -> {

            once_btn = repeatItemDialog.findViewById(R.id.once_btn);
            daily_btn = repeatItemDialog.findViewById(R.id.daily_btn);
            weekdays_btn = repeatItemDialog.findViewById(R.id.weekdays_btn);


            once_btn.setOnClickListener(v1 -> {

                schedDays.setText("Once");
                repeatItemDialog.dismiss();

            });

            daily_btn.setOnClickListener(v1 -> {

                schedDays.setText("Daily");
                repeatItemDialog.dismiss();

            });

            weekdays_btn.setOnClickListener(v1 -> {

                schedDays.setText("Mon to Fri");
                repeatItemDialog.dismiss();

            });

            repeatItemDialog.show();
        });
    }

    public int calculateGrams(int bodyWeight){
        int grams = ( bodyWeight * 40 ) / 3;
        return grams;
    }

    public void loadSchedData( String schedId, String schedTime, String schedRepeat, String bodyWeightValue, String totalGrams){
        // set data to the edit text
        bodyWeight.setText(bodyWeightValue);

        // set data to the text view
        grams.setText(totalGrams);

        // get the value of AM/PM, hour and minute
        String[] amPm = schedTime.split(" ");
        String[] time = amPm[0].split(":");
        Log.d("time", String.valueOf(time[0]));
        Log.d("time", String.valueOf(time[1]));
        Log.d("time", String.valueOf(amPm[1]));

        // set data to the number picker
        hourPicker.setValue(Integer.parseInt(time[0]));
        minutePicker.setValue(Integer.parseInt(time[1]));
        amPmPicker.setValue(amPm[1].equals("AM") ? 0 : 1);

        hourValue.set(Integer.parseInt(time[0]));
        minuteValue.set(Integer.parseInt(time[1]));
        amPmValue.set(amPm[1].equals("AM") ? 0 : 1);

        // set data to the repeat days
        schedDays.setText(schedRepeat);
    }

    public void updateSchedule(String schedId, String schedTime, String schedRepeat, String bodyWeightValue, String totalGrams) {

        HashMap Schedule = new HashMap();

        Schedule.put("sched_token", schedId);
        Schedule.put("sched_time", schedTime);
        Schedule.put("sched_repeat", schedRepeat);
        Schedule.put("body_weight", bodyWeightValue);
        Schedule.put("total_grams", totalGrams);

        databaseReference = FirebaseDatabase.getInstance().getReference("Schedules");

        databaseReference.child(schedId).updateChildren(Schedule).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Schedule Updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }




}