package com.example.catfeederapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private Context context;
    private List<Schedule> scheduleList;

    public ScheduleAdapter(Context context, List<Schedule> scheduleList) {
        this.context = context;
        this.scheduleList = scheduleList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.schedule_list_items, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // check if the database is empty
        if (scheduleList.isEmpty()) {
            holder.schedCard.setVisibility(View.GONE);
        } else {
            holder.schedCard.setVisibility(View.VISIBLE);
        }

        Schedule schedule = scheduleList.get(position);
        String time = schedule.getSched_time();
        // split the time string into two parts
        String[] parts = time.split(" ");

        // set the time and am/pm
        holder.time.setText(parts[0]);
        holder.amPm.setText(parts[1]);

        // set the repeat days
        holder.repeat.setText(schedule.getSched_repeat());
        holder.grams.setText(schedule.getTotal_grams()+" g");
        holder.toggleButton.setChecked(schedule.isEnabled());


        holder.schedCard.setOnClickListener(v -> {
            // call the AddSchedule activity
            Intent intent = new Intent(context, editSchedule.class);
            intent.putExtra("sched_token", scheduleList.get(position).getSched_token());
            intent.putExtra("sched_time", scheduleList.get(position).getSched_time());
            intent.putExtra("sched_repeat", scheduleList.get(position).getSched_repeat());
            intent.putExtra("sched_grams", scheduleList.get(position).getTotal_grams());
            intent.putExtra("body_weight", scheduleList.get(position).getBody_weight());
            intent.putExtra("enabled", scheduleList.get(position).isEnabled());
            context.startActivity(intent);
        });


        // hold the card to delete the schedule
        holder.schedCard.setOnLongClickListener(v -> {
            // call the delete_bottom_sheet
            holder.delete_bottom_sheet = new BottomSheetDialog(context);

            holder.delete_bottom_sheet.setContentView(R.layout.delete_bottom_sheet);

            holder.delete_btn = holder.delete_bottom_sheet.findViewById(R.id.deleteBtn);

            holder.delete_btn.setOnClickListener(v1 -> {
                // delete the schedule

                String temp_token = scheduleList.get(position).getSched_token();
                FirebaseDatabase.getInstance().getReference("Schedules").child(scheduleList.get(position).getSched_token()).removeValue();

                // print log position
                Log.d("position", String.valueOf(position));

                String referencePath = "Schedule_Tokens"; // Replace with the actual path to the "sched_token" node

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(referencePath);

                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String sched_token = snapshot.getValue().toString();

                            String[] tokens = sched_token.split(",");

                            for (int i = 0; i < tokens.length; i++) {
                                if (tokens[i].equals(temp_token)) {
                                    tokens[i] = "";
                                }
                            }

                            String new_sched_token = "";

                            for (int i = 0; i < tokens.length; i++) {
                                if (!tokens[i].equals("")) {
                                    new_sched_token += tokens[i] + ",";
                                }
                            }

                            FirebaseDatabase.getInstance().getReference("Schedule_Tokens").setValue(new_sched_token);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                holder.delete_bottom_sheet.dismiss();
            });

            holder.delete_bottom_sheet.show();
            return false;
        });

        // toggle the schedule
        holder.toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // enable the schedule
                FirebaseDatabase.getInstance().getReference("Schedules").child(scheduleList.get(position).getSched_token()).child("enabled").setValue(true);
            } else {
                // disable the schedule
                FirebaseDatabase.getInstance().getReference("Schedules").child(scheduleList.get(position).getSched_token()).child("enabled").setValue(false);
            }
        });

    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }
}

class MyViewHolder extends RecyclerView.ViewHolder {

    CardView schedCard;
    TextView time, amPm, repeat, grams;
    Switch toggleButton;

    BottomSheetDialog delete_bottom_sheet;

    LinearLayout delete_btn;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        schedCard = itemView.findViewById(R.id.schedCard);
        time = itemView.findViewById(R.id.time);
        amPm = itemView.findViewById(R.id.amPm);
        repeat = itemView.findViewById(R.id.repeat);
        grams = itemView.findViewById(R.id.grams);
        toggleButton = itemView.findViewById(R.id.toggleBtn);
    }
}