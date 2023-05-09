package com.example.catfeederapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton fab;
    RecyclerView schedRecyclerView;

    List<Schedule> scheduleList;

    DatabaseReference databaseReference, dbDispenseRef;

    ScheduleAdapter adapter;


    Handler handler = new Handler();
    Runnable runnable;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = findViewById(R.id.fab);
        schedRecyclerView = findViewById(R.id.schedList);

        // fetch the schedules from the database
        databaseReference = FirebaseDatabase.getInstance().getReference("Schedules");
        dbDispenseRef = FirebaseDatabase.getInstance().getReference("Dispense");

        scheduleList = new ArrayList<>();
        schedRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ScheduleAdapter(this, scheduleList);
        schedRecyclerView.setAdapter(adapter);



        // check if the sched_time is due for feeding every second
        runnable = new Runnable() {
            @Override
            public void run() {

                // loop through the scheduleList and print the sched_time
                for (int i = 0; i < scheduleList.size(); i++) {
                    String schedTime = scheduleList.get(i).getSched_time();
                    String schedToken = scheduleList.get(i).getSched_token();
                    String totalGrams = scheduleList.get(i).getTotal_grams();
                    boolean isDone = scheduleList.get(i).isDone();
                    boolean isEnabled = scheduleList.get(i).isEnabled();

                    // getReference from /dispense from the database and print the value of dispense

                    dbDispenseRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String dispense = snapshot.child("dispense").getValue().toString();


//                            if(isDue(schedTime) && dispense.equals("false") && !isDone){
//                                // if the sched_time is due for feeding, check if the schedule is enabled
//                                if(isEnabled){
//                                    // if the schedule is enabled, dispense the food
//                                    Log.d("Dispense", "Dispensing food");
//
//                                    // push dispense: "true" to the database to trigger the Arduino to dispense food
//                                    dbDispenseRef.child("dispense").setValue("true");
//                                    dbDispenseRef.child("sched_token").setValue(schedToken);
//                                    dbDispenseRef.child("total_grams").setValue(totalGrams);
//
//                                }
//                            }
                            if(isDue(schedTime)){
                                if(dispense.equals("false")){
                                    Log.d("isDone", String.valueOf(isDone));
                                    if(!isDone){
                                        if(isEnabled){
                                            Log.d("Dispense", "Dispensing food");

                                            // push dispense: "true" to the database to trigger the Arduino to dispense food
                                            dbDispenseRef.child("dispense").setValue("true");
                                            dbDispenseRef.child("sched_token").setValue(schedToken);
                                            dbDispenseRef.child("total_grams").setValue(totalGrams);
                                        }
                                    }
                                }else if(dispense.equals("true") && isDone){
                                    Log.d("stop", "=============");

                                    // push dispense: "true" to the database to trigger the Arduino to dispense food
                                    dbDispenseRef.child("dispense").setValue("false");
                                    dbDispenseRef.child("sched_token").setValue("");
                                    dbDispenseRef.child("total_grams").setValue("");

                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });




                    // isDue is true if the sched_time is due for feeding

                    handler.postDelayed(this, 1000);
                }
            }
        };

        handler.postDelayed(runnable, 5000);

        // listen for changes in the database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                scheduleList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // print the data
                    Log.d("Data", dataSnapshot.getValue().toString());

                    String schedId = dataSnapshot.child("sched_token").getValue().toString();
                    String schedTime = dataSnapshot.child("sched_time").getValue().toString();
                    String schedRepeat = dataSnapshot.child("sched_repeat").getValue().toString();
                    String bodyWeight = dataSnapshot.child("body_weight").getValue().toString();
                    String totalGrams = dataSnapshot.child("total_grams").getValue().toString();
                    String dateCreated = dataSnapshot.child("date_created").getValue().toString();
                    boolean isEnabled = Boolean.parseBoolean(dataSnapshot.child("enabled").getValue().toString());
                    boolean isDone = Boolean.parseBoolean(dataSnapshot.child("done").getValue().toString());

                    // create a new schedule object
                    Schedule schedule = new Schedule(schedId, schedTime, schedRepeat, bodyWeight, totalGrams, dateCreated, isEnabled, isDone);
                    scheduleList.add(schedule);

                    // update the adapter
                    adapter.notifyDataSetChanged();

                }

                // print the size of the list
                Log.d("Size", String.valueOf(scheduleList.size()));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        fab.setOnClickListener(v -> {
            // call the AddSchedule activity
            Intent intent = new Intent(this, AddSchedule.class);
            startActivity(intent);
        });
    }

    // check if the sched_time is due for feeding
    public boolean isDue(String sched_time) {
        // get the current time
        String currentTime = new SimpleDateFormat("hh:mm a").format(System.currentTimeMillis());

        // print the current time
        Log.d("Current Time", currentTime);

        // compare the current time and the sched_time
        if (currentTime.equals(sched_time)) {
            return true;
        } else {
            return false;
        }
    }
}