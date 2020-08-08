package com.example.folio9470m.rendezvous_re;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.folio9470m.rendezvous_re.models.Meetup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MeetupSettingActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MeetupSettingActivity";
    private String meetupID;
    private DatabaseReference meetupReference;
    private Meetup myMeetup;

    private Button btnDatePicker, btnTimePicker;
    private TextView txtDate, txtTime;
    private EditText nameText;
    private EditText tagsText;
    private RadioGroup radioGroup;

    private Button changeLocationButton;
    private Button deleteMeetupButton;
    private Button confirmButton;

    private int mYear, mMonth, mDay, mHour, mMinute;
    private int hour1, minute1;
    private long meetupTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetup_setting);
        setTitle("Meetup Settings");


        Intent intent = getIntent();
        meetupID = intent.getStringExtra("id");


        meetupReference = FirebaseDatabase.getInstance().getReference()
                .child("meetups").child(meetupID);
        meetupReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myMeetup = dataSnapshot.getValue(Meetup.class);
                setFields();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        String[] locationOptions = {"Manual Location", "Voting System", "Intelligent Location Decider"};

        btnDatePicker=(Button)findViewById(R.id.btn_date);
        btnTimePicker=(Button)findViewById(R.id.btn_time);
        txtDate=findViewById(R.id.in_date);
        txtTime=findViewById(R.id.in_time);
        nameText = (EditText) findViewById(R.id.nameyyy);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        tagsText = findViewById(R.id.tagstextedittext);




        changeLocationButton = findViewById(R.id.changelocationbutton);
        deleteMeetupButton = findViewById(R.id.deletemeetupbuton);
        confirmButton = findViewById(R.id.confirmchangesbutton);



        changeLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(MeetupSettingActivity.this);
                builder.setTitle("Pick a Location Method");
                builder.setItems(locationOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            meetupReference.child("locationMethod").setValue(0);
                            ((Rendezvous)getApplicationContext()).setMeetupID(meetupID);
                            Intent intent = new Intent(MeetupSettingActivity.this, PlacesActivity.class);
                            intent.putExtra("ACTIVITY_NAME_BUNDLE_ID", "MeetupSettingActivity");
                            startActivity(intent);
                            finish();
                        }
                        else if(which == 1){
                            meetupReference.child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    meetupReference.child("locationMethod").setValue(1);
                                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                                        FirebaseDatabase.getInstance().getReference().child("voting")
                                                .child(meetupID).child("peopleNotVoted")
                                                .push().setValue(ds.getKey());
                                    }
                                    Intent intent = new Intent(MeetupSettingActivity.this, MeetupOverviewNav.class);
                                    intent.putExtra("id",meetupID);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        else if(which == 2){
                            meetupReference.child("locationMethod").setValue(2);
                            Intent intent = new Intent(MeetupSettingActivity.this, MeetupOverviewNav.class);
                            intent.putExtra("id",meetupID);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
                builder.show();
            }
        });
        deleteMeetupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder deleteDialog = new AlertDialog.Builder(MeetupSettingActivity.this);
                deleteDialog.setMessage("Delete Meetup?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                meetupReference.removeValue();
                                DynamicToast.makeSuccess(MeetupSettingActivity.this,"Meetup Deleted!").show();
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(MeetupSettingActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }, 2000);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                deleteDialog.show();


            }
        });


        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Date meetupDate = new Date();
                try {
                    meetupDate = new SimpleDateFormat("dd-MM-yyyy").parse(txtDate.getText().toString());
                } catch (ParseException e) {
                    Log.e(TAG, "onCreate: ",e );
                    DynamicToast.makeError(MeetupSettingActivity.this, "Error in saving changes").show();
                    finish();
                }
                Date currentDate = new Date();
                final Calendar c = Calendar.getInstance();
                long different;
                different = meetupDate.getTime() - currentDate.getTime();
                different = different + (hour1*60*60*1000)+(minute1*60*1000);
                meetupTime = meetupDate.getTime() + (hour1*60*60*1000)+(minute1*60*1000);

                if(different< 0){
                    txtDate.setError("Invalid Time or Date");
                    txtTime.setError("Invalid Time or Date");
                }
                else{
                    if(nameText.getText() == null || nameText.getText().toString().equals("") ){
                        nameText.setError("Field must be filled");
                    }

                    else{
                        saveIntoDatabase();
                    }
                }

            }
        });
        btnDatePicker.setOnClickListener(this);
        btnTimePicker.setOnClickListener(this);

    }

    private void saveIntoDatabase() {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) findViewById(selectedId);
        boolean isPublic = false;
        if(radioButton.getText().equals("Public"))
            isPublic = true;

        if(isPublic){
            meetupReference.child("isPublic").setValue(true);
            meetupReference.child("public").setValue(true);
        }
        else{
            meetupReference.child("isPublic").setValue(false);
            meetupReference.child("public").setValue(false);
        }
        meetupReference.child("name").setValue(nameText.getText().toString());
        meetupReference.child("time").setValue(txtTime.getText().toString());
        meetupReference.child("date").setValue(txtDate.getText().toString());
        meetupReference.child("tags").setValue(tagsText.getText().toString());
        meetupReference.child("meetupDateTime").setValue(meetupTime);
        DynamicToast.makeSuccess(MeetupSettingActivity.this,"Changes Saved!").show();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(MeetupSettingActivity.this, MeetupOverviewNav.class);
                intent.putExtra("id",meetupID);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }

    private void setFields() {
        nameText.setText(myMeetup.getName());
        txtDate.setText(myMeetup.getDate());
        txtTime.setText(myMeetup.getTime());
        tagsText.setText(myMeetup.getTags());
        if(myMeetup.isPublic())
            radioGroup.check(R.id.radioButton);
        else
            radioGroup.check(R.id.radioButton2);

    }

    @Override
    public void onClick(View v) {

        if (v == btnDatePicker) {

            // Get Current Date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, monthOfYear, dayOfMonth) ->
                            txtDate.setText(String.format("%02d-%02d-%04d", dayOfMonth, (monthOfYear+1), year)), mYear, mMonth, mDay

            );
            datePickerDialog.show();
        }
        if (v == btnTimePicker) {

            // Get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) ->{
                        txtTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                        hour1 = hourOfDay;
                        minute1 = minute;
                    }

                    , mHour, mMinute, false);
            timePickerDialog.show();
        }
    }



}
