package com.example.catfeederapp;

import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AddSchedule extends AppCompatActivity {

    LinearLayout repeat_btn, once_btn, daily_btn, weekdays_btn, repeat_days_btn;
    NumberPicker hourPicker, minutePicker, amPmPicker;

    ImageView cancel_btn, save_btn;

    TextView schedDays, grams;

    BottomSheetDialog repeatItemDialog;

    TextView weightBased, customGrams;
    EditText bodyWeight, customGramsInput;

    LinearLayout cat_body_weight, food_total_grams;

    Boolean _weightBased = true;
    Boolean _customGrams = false;
    ArrayList<String> selectedDays = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);
        
        // Repeat Button
        repeat_btn = findViewById(R.id.repeat_btn);
        cancel_btn = findViewById(R.id.cancel_btn);
        save_btn = findViewById(R.id.save_btn);

        // Modes
        weightBased = findViewById(R.id.Weight_Based_btn);
        customGrams = findViewById(R.id.Custom_Grams_btn);

        // Mode Layouts
        cat_body_weight = findViewById(R.id.cat_body_weight);
        food_total_grams = findViewById(R.id.food_total_grams);

        // Custom Grams Input
        customGramsInput = findViewById(R.id.custom_totalGrams);

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

        customGramsInput.setVisibility(EditText.GONE);
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

            if(repeatDays.equals("Repeat")){
                // loop through the selected days
                repeatDays = "";
                for (String day : selectedDays) {
                    repeatDays += day + " ";
                }
            }

            Toast.makeText(this, time + " " + repeatDays, Toast.LENGTH_SHORT).show();

            if(_weightBased){
                saveSchedule(time, repeatDays, bodyWeight.getText().toString(), grams.getText().toString(), false);
            }else if(_customGrams){
                saveSchedule(time, repeatDays, "", customGramsInput.getText().toString(), false);
            }


        });

        repeat_btn.setOnClickListener(v -> {

            once_btn = repeatItemDialog.findViewById(R.id.once_btn);
            daily_btn = repeatItemDialog.findViewById(R.id.daily_btn);
            weekdays_btn = repeatItemDialog.findViewById(R.id.weekdays_btn);
            repeat_days_btn = repeatItemDialog.findViewById(R.id.repeat_days_btn);



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

            repeat_days_btn.setOnClickListener(v1 -> {
                // show dialog box for repeat days selection
                repeatItemDialog.dismiss();

                showDialogForRepeatDays();
            });

            repeatItemDialog.show();
        });
    }
    private void showDialogForRepeatDays() {
        
        // Define the repeat days array
        String[] repeatDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        // Create a boolean array to track the selected days
        boolean[] checkedDays = new boolean[repeatDays.length];

        // Set the checked state for previously selected days
        for (int i = 0; i < repeatDays.length; i++) {
            if (selectedDays.contains(repeatDays[i])) {
                checkedDays[i] = true;
            }
        }

        // Implement your code to show the dialog box for repeat days selection
        // Here's a basic example using AlertDialog with multi-choice items:
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Repeat Days")
                .setMultiChoiceItems(repeatDays, checkedDays, (dialog, which, isChecked) -> {
                    // Update the checked state of the selected day
                    checkedDays[which] = isChecked;
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    // Process the selected repeat days
                    selectedDays.clear(); // Clear the previously selected days

                    for (int i = 0; i < repeatDays.length; i++) {
                        if (checkedDays[i]) {
                            selectedDays.add(repeatDays[i]);
                        }
                    }

                    if (selectedDays.isEmpty()) {
                        // Show an alert indicating that at least one repeat day must be selected
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                        alertBuilder.setTitle("Error")
                                .setMessage("Please select at least one repeat day.")
                                .setPositiveButton("OK", null)
                                .show();
                    } else {
                        schedDays.setText("Repeat");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public int calculateGrams(int bodyWeight){
        int grams = ( bodyWeight * 40 ) / 3;
        return grams;
    }

    public void saveSchedule(String schedTime, String sched_repeat, String body_weight, String total_grams, boolean isDone){

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
                isEnabled,
                isDone);

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

        String referencePath = "Schedule_Tokens"; // Replace with the actual path to the "sched_token" node

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(referencePath);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // "sched_token" node exists
                    // Perform your logic here

                    // get the value of sched_token
                    String sched_token = dataSnapshot.getValue(String.class);

                    // append the token to the sched_token
                    String new_sched_token = sched_token + "," + token;

                    // update the sched_token

                    FirebaseDatabase.getInstance().getReference(referencePath)
                            .setValue(new_sched_token)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    Log.d("SCHED_TOKEN", "Sched Token Updated");
                                    finish();
                                }else{
                                    Log.d("SCHED_TOKEN", "Error: "+task.getException().getMessage());
                                }
                            }).addOnFailureListener(e -> {
                                Log.d("SCHED_TOKEN", "Error: "+e.getMessage());
                    });

                } else {

                    // create a new sched_token
                    String new_sched_token = token;

                    FirebaseDatabase.getInstance().getReference(referencePath)
                            .setValue(new_sched_token)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    Log.d("SCHED_TOKEN", "Sched Token Updated");
                                    finish();
                                }else{
                                    Log.d("SCHED_TOKEN", "Error: "+task.getException().getMessage());
                                }
                            }).addOnFailureListener(e -> {
                                Log.d("SCHED_TOKEN", "Error: "+e.getMessage());
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Error occurred while accessing the database
            }
        });

    }


}