package com.example.catfeederapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicInteger;

public class AddSchedule extends AppCompatActivity {

    LinearLayout repeat_btn, once_btn, daily_btn, weekdays_btn;
    NumberPicker hourPicker, minutePicker, amPmPicker;

    ImageView cancel_btn, save_btn;

    TextView schedDays, grams;

    EditText bodyWeight;
    BottomSheetDialog repeatItemDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);
        
        // Repeat Button
        repeat_btn = findViewById(R.id.repeat_btn);
        cancel_btn = findViewById(R.id.cancel_btn);
        save_btn = findViewById(R.id.save_btn);

        // Body Weight
        bodyWeight = findViewById(R.id.body_weight);

        //grams
        grams = findViewById(R.id.totalGrams);

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

        AtomicInteger amPmValue = new AtomicInteger();
        amPmPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            amPmValue.set(newVal);
        });

        // store the value of hour
        AtomicInteger hourValue = new AtomicInteger();
        hourPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            hourValue.set(newVal);
        });

        // store the value of minute
        AtomicInteger minuteValue = new AtomicInteger();
        minutePicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            minuteValue.set(newVal);
        });

        // format 2 digit numbers the number picker
        hourPicker.setFormatter(i -> String.format("%02d", i));
        minutePicker.setFormatter(i -> String.format("%02d", i));

        // Repeat Item Dialog

        repeatItemDialog = new BottomSheetDialog(this);
        repeatItemDialog.setContentView(R.layout.repeat_items_component);

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

            saveSchedule(time, repeatDays, bodyWeight.getText().toString(), grams.getText().toString());



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

    public void saveSchedule(String schedTime, String sched_repeat, String body_weight, String total_grams){

        // Timestamp
        long timestamp = System.currentTimeMillis();

        // format timestamp to date and time
        String date = new SimpleDateFormat("dd/MM/yyyy").format(timestamp);

        // create a token for schedule id using unicode
        String token = "SCH" + timestamp;

        boolean isEnabled = true;

        Schedule schedule = new Schedule(
                token,
                schedTime,
                sched_repeat,
                body_weight,
                total_grams,
                date,
                isEnabled);

        FirebaseDatabase.getInstance().getReference("Schedules")
                .child(token)
                .setValue(schedule)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(this, "Schedule Added", Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        Toast.makeText(this, "Error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}