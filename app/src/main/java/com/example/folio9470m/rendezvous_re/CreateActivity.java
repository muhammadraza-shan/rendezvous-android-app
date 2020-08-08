package com.example.folio9470m.rendezvous_re;

import android.content.Intent;
import androidx.annotation.NonNull;

import com.example.folio9470m.rendezvous_re.models.Meetup;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class CreateActivity extends AppCompatActivity implements
        View.OnClickListener, OnItemSelectedListener  {
    private static final String TAG = CreateActivity.class.getSimpleName();
    private static final int OPEN_DOCUMENT_CODE = 2;

    private Button btnDatePicker, btnTimePicker;
    private TextView txtDate, txtTime;
    private EditText nameText;
    private EditText tagsText;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private String mID;
    private Spinner spinner;
    private int hour1, minute1;

    private FirebaseDatabase database;
    private DatabaseReference meetupDatabase;
    private DatabaseReference totalDatabase;
    private DatabaseReference userDatabase;
    private Rendezvous mApp;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String idid;
    private String imagePath;
    private Uri imageUri;
    private boolean imageSelected = false;
    private boolean imageSaved = false;
    private long meetupTime;

    private int locationMethod;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        setTitle("Create a Meetup");

        mApp =((Rendezvous)getApplicationContext());

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        locationMethod = 0;

        btnDatePicker=(Button)findViewById(R.id.btn_date);
        btnTimePicker=(Button)findViewById(R.id.btn_time);
        txtDate=findViewById(R.id.in_date);
        txtTime=findViewById(R.id.in_time);
        nameText = (EditText) findViewById(R.id.nameyyy);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        spinner = (Spinner) findViewById(R.id.spinner);
        tagsText = findViewById(R.id.tagsedittext);
        ((Button)findViewById(R.id.uploadimagebutton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, OPEN_DOCUMENT_CODE);

            }
        });




        database = FirebaseDatabase.getInstance();
        meetupDatabase = database.getReference().child("meetups");
        totalDatabase = database.getReference().child("totalmeetups");
        userDatabase = database.getReference().child("users");
        totalDatabase.keepSynced(true);





        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Manual Location Selection");
        categories.add("Voting System");
        categories.add("Intelligent Location Decider");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);




        btnDatePicker.setOnClickListener(this);
        btnTimePicker.setOnClickListener(this);

        FloatingActionButton nextButton = findViewById(R.id.create_next_fab);
        nextButton.setOnClickListener(view -> {
            Date meetupDate = new Date();
            try {
                meetupDate = new SimpleDateFormat("dd-MM-yyyy").parse(txtDate.getText().toString());
            } catch (ParseException e) {
                Log.e(TAG, "onCreate: ",e );
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
                if(nameText.getText() == null || nameText.getText().toString().equals("") )
                    nameText.setError("Field must be filled");
                else saveDataIntoDatabase();
            }

        });
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

    public void saveDataIntoDatabase(){
        int selectedId = radioGroup.getCheckedRadioButtonId();
        radioButton = (RadioButton) findViewById(selectedId);
        boolean isPublic = false;
        if(radioButton.getText().equals("Public"))
            isPublic = true;




        idid = mAuth.getUid();
        final Meetup newMeetup = new Meetup(nameText.getText().toString(),
                isPublic, txtDate.getText().toString(),
                txtTime.getText().toString(), idid);
        newMeetup.setLocation("Not Yet Selected");
        newMeetup.setLocationMethod(locationMethod);
        if(tagsText.getText()!=null)
            newMeetup.setTags(tagsText.getText().toString());


        totalDatabase.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Long meetupCount = mutableData.getValue(Long.class);
                if (meetupCount == null) {
                    return Transaction.success(mutableData);
                }
                mutableData.setValue(meetupCount+1);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                if(b){
                    int number = (int)dataSnapshot.getValue(Integer.class);
                    mID = "m"+number;
                    mApp.setMeetupID(mID);
                    newMeetup.setMeetupID(mID);
                    newMeetup.setMeetupDateTime(meetupTime);
                    meetupDatabase.child(mID).setValue(newMeetup);
                    meetupDatabase.child(mID).child("members").child(idid).setValue(true);

                    if(imageSelected){
                        StorageReference riversRef = FirebaseStorage.getInstance().getReference().child("images/"+mID+"/"+imageUri.getLastPathSegment());
                        UploadTask uploadTask = riversRef.putFile(imageUri);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.e(TAG, "onFailure: ",exception );
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.d(TAG, "onSuccess: "+taskSnapshot.getMetadata().toString());
                                imagePath = "images/"+mID+"/"+imageUri.getLastPathSegment();
                                meetupDatabase.child(mID).child("imagepath").setValue(imagePath);
                            }
                        });
                    }
                    saveMeetupIntoUserList();
                }
                else{
                    DynamicToast.makeError(CreateActivity.this,"Meetup Creation Unsuccessful. Try again!", Toast.LENGTH_LONG).show();
                }

            }
        });
        
        //Accessing Database



    }

    public void saveMeetupIntoUserList(){

        userDatabase.child(mApp.getUserID())
                .child("meetupArrayList").child(mID).setValue(true);
        if(locationMethod==0){
            Intent intent = new Intent(CreateActivity.this, PlacesActivity.class);
            if(imageSelected){
                intent.putExtra("image","yes");
            }
            else{
                intent.putExtra("image","no");
            }
            startActivity(intent);
            finish();
        }
        else if(locationMethod==1){
            database.getReference().child("voting")
                    .child(mID).child("peopleNotVoted")
                    .child(mApp.getUserID()).setValue(true);
            Intent intent = new Intent(CreateActivity.this, InvitePeopleActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            Intent intent = new Intent(CreateActivity.this, InvitePeopleActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        locationMethod = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == OPEN_DOCUMENT_CODE && resultCode == RESULT_OK) {
            if (resultData != null) {
                // this is the image selected by the user
                imageUri = resultData.getData();
                imageSelected = true;
                DynamicToast.makeSuccess(this,"Image Successfully Set", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
