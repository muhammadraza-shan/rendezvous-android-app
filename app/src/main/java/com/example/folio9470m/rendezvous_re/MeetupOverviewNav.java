package com.example.folio9470m.rendezvous_re;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.folio9470m.rendezvous_re.models.LatLong;
import com.example.folio9470m.rendezvous_re.models.Meetup;
import com.example.folio9470m.rendezvous_re.models.User;
import com.example.folio9470m.rendezvous_re.util.GlideApp;
import com.example.folio9470m.rendezvous_re.util.RoundBitmapWithLetter;

import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.iambedant.text.OutlineTextView;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MeetupOverviewNav extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MeetupOverviewActivity";

    private DatabaseReference myRef;
    private Geocoder mGeocoder;

    private OutlineTextView nameText;
    private TextView locationText;
    private TextView timeText;
    private TextView dateText;
    private TextView addressText;
    private TextView celsuisText;
    private TextView weatherText;
    private TextView creatorText;
    private TextView tagsText;
    private ImageView topImage;

    private Button navButton;
    private Button alertButton;
    private Button voteButton;
    private Button addMembersButton;


    private Meetup currentMeetup;
    private double longitude;
    private double latitude;
    private String argument;
    private String imagePath;

    private ProgressBar progressBar;



    private String time;
    private String date;
    private String[] dateArray;
    private String[] timeArray;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int min;
    private String mID;
    private boolean locationSet = false;
    private boolean canUseIntel = true;
    private boolean nonMembers = false;
    private JSONObject weatherData = null;
    private int hours;
    private Date meetupDate;
    private ConstraintLayout parentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetup_overview_nav);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = findViewById(R.id.progressBar2);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(Color.WHITE);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        Menu navMenu = navigationView.getMenu();
        MenuItem navSettings = navMenu.findItem(R.id.nav_meetup_settings);

        String userName = ((Rendezvous)getApplicationContext()).getUserName();
        View hView =  navigationView.getHeaderView(0);
        TextView nav_user = (TextView)hView.findViewById(R.id.nav_users_name);
        nav_user.setText(userName);
        CircleImageView nav_image = hView.findViewById(R.id.nav_user_image);
        nav_image
                .setImageBitmap(RoundBitmapWithLetter
                        .generateCircleBitmap(this,
                                RoundBitmapWithLetter.getMaterialColor(5),
                                60f,
                                (String.valueOf(userName.charAt(0))).toUpperCase()
                        ));




        navButton = findViewById(R.id.navigation_button);
        alertButton = findViewById(R.id.alert_button);
        addMembersButton = findViewById(R.id.floatingAddMembers);
        voteButton = findViewById(R.id.vote_button_overview);





        Intent intent = getIntent();
        mID = intent.getStringExtra("id");
        String senderActivityName = intent.getStringExtra("ACTIVITY_NAME_BUNDLE_ID");
        if(senderActivityName!=null && senderActivityName.equals("CurrMeetupsActivity")){
            nonMembers = true;
        }
        else{
            nonMembers = false;
        }



        myRef = FirebaseDatabase.getInstance().getReference().child("meetups").child(mID);
        nameText = findViewById(R.id.nameText);
        locationText = findViewById(R.id.locationText);
        timeText = findViewById(R.id.timeText);
        dateText = findViewById(R.id.dateText);
        addressText = findViewById(R.id.addressText);
        celsuisText = findViewById(R.id.celsuisText);
        weatherText = findViewById(R.id.weatherText);
        tagsText = findViewById(R.id.tagsText);
        creatorText = findViewById(R.id.creatorText);
        topImage = findViewById(R.id.topimageview);

        mGeocoder = new Geocoder(MeetupOverviewNav.this, Locale.ENGLISH);


        myRef.keepSynced(true);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long size = 0;
                currentMeetup = dataSnapshot.getValue(Meetup.class);
                if(currentMeetup == null){
                    DynamicToast.makeError(MeetupOverviewNav.this,"Could not find Meetup details. Might be deleted").show();
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(MeetupOverviewNav.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }, 2000);
                    finish();
                }
                ((Rendezvous)getApplicationContext()).setMeetupID(mID);
                size = dataSnapshot.child("members").getChildrenCount();
                imagePath = dataSnapshot.child("imagepath").getValue(String.class);
                if(size<2 ){
                    canUseIntel = false;
                }
                Log.d(TAG, "onDataChange: Successfully retrieved Meetup Details");
                nameText.setText(currentMeetup.getName());
                locationText.setText(currentMeetup.getLocation());
                if(imagePath!=null){
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(imagePath);
                    GlideApp.with(MeetupOverviewNav.this)
                            .load(storageReference)
                            .centerCrop()
                            .into(topImage);
                    topImage.setColorFilter(0xffb0bec5, PorterDuff.Mode.MULTIPLY );

                }

                celsuisText.setText("Forecast Not Yet Available");
                weatherText.setText("Forecast Unavailable");
                try{
                    meetupDate = new SimpleDateFormat("dd-MM-yyyy").parse(currentMeetup.getDate());
                    String dayOfWeek = new SimpleDateFormat("EEEE").format(meetupDate);;
                    String month = new SimpleDateFormat("MMMM",Locale.US).format(meetupDate);
                    String date1 = new SimpleDateFormat("dd").format(meetupDate);
                    dateText.setText(dayOfWeek+", "+month+" "+date1);
                    Date currentDate = new Date();
                    long different = (currentMeetup.getMeetupDateTime() - currentDate.getTime())/1000;
                    hours = (int) TimeUnit.SECONDS.toHours((different));
                    if(hours<0){
                        hours = 1;
                    }
                    getJSON(currentMeetup.getCoordinates(), hours);
                    String time = new SimpleDateFormat("hh:mm a")
                            .format(new SimpleDateFormat("HH:mm")
                                    .parse(currentMeetup.getTime()));
                    timeText.setText(time);


                }
                catch (Exception e){
                    Log.e(TAG, "onDataChange: ",e );
                    dateText.setText(currentMeetup.getDate());
                    timeText.setText(currentMeetup.getTime());
                }


                String id = FirebaseAuth.getInstance().getUid();

                if(currentMeetup.getLocationMethod() == 0){
                    locationSet = true;
                }
                else if(currentMeetup.getLocationMethod() == 1){
                    voteButton.setVisibility(View.VISIBLE);
                }
                else if(currentMeetup.getCreator().equals(FirebaseAuth.getInstance().getUid())){
                    voteButton.setVisibility(View.VISIBLE);
                    voteButton.setText("Intelligent Location Decider");
                }
                if(locationSet){
                    try{
                        addressText
                                .setText(getAddressByCoordinates(currentMeetup.getCoordinates().getLatitude(),
                                        currentMeetup.getCoordinates().getLongitude()));
                        addressText.setMovementMethod(new ScrollingMovementMethod());
                    }
                    catch (IOException e){
                        Log.e(TAG, "onDataChange: GeoCoder IO Exception" );
                    }
                    latitude = currentMeetup.getCoordinates().getLatitude();
                    longitude =currentMeetup.getCoordinates().getLongitude();
                    navButton.setClickable(true);
                    voteButton.setVisibility(View.GONE);
                }
                else{
                    addressText.setText("Not Yet Set");
                    navButton.setClickable(false);
                }
                time = currentMeetup.getTime();
                date = currentMeetup.getDate();
                argument = "google.navigation:q="+latitude+","+longitude;
                dateArray = date.split("-");
                timeArray = time.split(":");
                day = Integer.parseInt(dateArray[0]);
                month = Integer.parseInt(dateArray[1]);
                year = Integer.parseInt(dateArray[2]);
                hour = Integer.parseInt(timeArray[0]);
                min = Integer.parseInt(timeArray[1]);


                if(currentMeetup.getCreator().equals(FirebaseAuth.getInstance().getUid())){
                    navSettings.setVisible(true);
                }
                if(nonMembers){
                    voteButton.setVisibility(View.GONE);
                    alertButton.setVisibility(View.GONE);
                    navButton.setVisibility(View.GONE);
                    addMembersButton.setVisibility(View.GONE);
                    navigationView.setVisibility(View.GONE);
                    parentLayout = findViewById(R.id.parentlayout);
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(parentLayout);
                    constraintSet.connect(R.id.relativeLayout2,ConstraintSet.TOP,R.id.topimageview,ConstraintSet.BOTTOM,50);
                    constraintSet.applyTo(parentLayout);
                }
                if(currentMeetup.getTags()!= null && !currentMeetup.getTags().equals("")){
                    tagsText.setText(currentMeetup.getTags());
                }
                else{
                    tagsText.setText("No tags set");
                }
                FirebaseDatabase.getInstance().getReference().child("users")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds : dataSnapshot.getChildren()){
                                    if(ds.getKey().equals(currentMeetup.getCreator())){
                                        creatorText.setText("Created by "+ds.getValue(User.class).getName());
                                        break;
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        //-------------------------
        // Button On Click Listeners
        // -------------------------

        addMembersButton.setOnClickListener(v ->
                startActivity(new Intent(MeetupOverviewNav.this,InvitePeopleActivity.class)));

        String[] alertOptions = {"Calender Alert", "Alarm Alert"};
        alertButton.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(MeetupOverviewNav.this);
            builder.setTitle("Pick an Alert Method");
            builder.setItems(alertOptions, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == 0){
                        String tags;
                        if(currentMeetup.getTags()== null || currentMeetup.getTags().equals(""))
                            tags = "Hangout";
                        else
                            tags = currentMeetup.getTags();

                        Calendar beginTime = Calendar.getInstance();
                        beginTime.set(year, (month-1), day, hour, min);
                        Calendar endTime = Calendar.getInstance();
                        endTime.set(year, (month-1), day, (hour+1), min);
                        Intent intent13 = new Intent(Intent.ACTION_INSERT)
                                .setData(CalendarContract.Events.CONTENT_URI)
                                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                                .putExtra(CalendarContract.Events.TITLE, currentMeetup.getName())
                                .putExtra(CalendarContract.Events.DESCRIPTION, tags)
                                .putExtra(CalendarContract.Events.EVENT_LOCATION, currentMeetup.getLocation())
                                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
                                //.putExtra(Intent.EXTRA_EMAIL, "one@example.com,two@example.com");
                        startActivity(intent13);
                    }
                    else if(which == 1){
                        Intent intent1 = new Intent(AlarmClock.ACTION_SET_ALARM);
                        intent1.putExtra(AlarmClock.EXTRA_HOUR, hour);
                        intent1.putExtra(AlarmClock.EXTRA_MINUTES, min);
                        intent1.putExtra(AlarmClock.EXTRA_MESSAGE, currentMeetup.getName());
                        Date current = new Date();
                        if( (meetupDate.getTime() - current.getTime()) < 0){
                            intent1.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
                            startActivity(intent1);
                            Toast.makeText(MeetupOverviewNav.this, "Alarm set successfully", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(meetupDate);
                            PendingIntent pendingIntent = PendingIntent.getActivity(MeetupOverviewNav.this,
                                    0, intent1, 0);
                            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                            Toast.makeText(MeetupOverviewNav.this, "Alarm will appear in your Clock App on the Meetup Day.", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });
            builder.show();


        });

        navButton.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse(argument);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        });

        voteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentMeetup.getLocationMethod() == 1){
                    startActivity(new Intent(MeetupOverviewNav.this,VotingActivity.class));
                }
                else if(currentMeetup.getLocationMethod() == 2){if(canUseIntel){
                    startActivity(new Intent(MeetupOverviewNav.this, IntelligentLocationActivity.class));
                    finish();
                }
                else{
                    DynamicToast.makeWarning(MeetupOverviewNav.this, "Feature requires at least 2 members", Toast.LENGTH_LONG).show();
                }

                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.meetup_overview_nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_chat) {
            Intent intent12 = new Intent(MeetupOverviewNav.this, Main2Activity.class);
            intent12.putExtra("id",mID);
            startActivity(intent12);

        } else if (id == R.id.nav_location) {
            if(locationSet){
                Intent intent1 = new Intent(MeetupOverviewNav.this, LiveLocation.class);
                intent1.putExtra("id",mID);
                startActivity(intent1);
            }
            else{
                DynamicToast.makeWarning(MeetupOverviewNav.this,"Unavailable until Location is Selected", Toast.LENGTH_SHORT).show();
            }


        } else if (id == R.id.nav_members) {
            startActivity(new Intent(MeetupOverviewNav.this,MembersListActivity.class));
        } else if (id == R.id.nav_meetup_settings) {
            Intent intent1 = new Intent(MeetupOverviewNav.this, MeetupSettingActivity.class);
            intent1.putExtra("id",mID);
            startActivity(intent1);
            finish();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private String getAddressByCoordinates(double lat, double lon) throws IOException {

        List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            return addresses.get(0).getAddressLine(0);
        }
        return null;
    }

    @SuppressLint("StaticFieldLeak")
    public void getJSON(final LatLong coord, final int hours) {

        new AsyncTask<Void, Void, Void>() {


            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    double lat =  coord.getLatitude();
                    double lon =  coord.getLongitude();
                    URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?lat="+lat+"&lon="+lon+"&units=metric&appid=ba755199a7b3ddfd24dce4da4c659454 ");

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    StringBuffer json = new StringBuffer(1024);
                    String tmp = "";

                    while((tmp = reader.readLine()) != null)
                        json.append(tmp).append("\n");
                    reader.close();

                    weatherData = new JSONObject(json.toString());

                    if(weatherData.getInt("cod") != 200) {
                        System.out.println("Cancelled");
                        return null;
                    }

                } catch (Exception e) {

                    Log.e(TAG, "doInBackground: ",e );
                    return null;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void Void) {
                progressBar.setVisibility(View.GONE);
                if(weatherData!=null){
                    Log.d("my weather received",weatherData.toString());
                    if((hours/3)<40){
                        try{
                            JSONArray list = weatherData.getJSONArray("list");
                            JSONObject item = list.getJSONObject((hours/3));
                            String weather = item.getJSONArray("weather").getJSONObject(0).get("main").toString();
                            JSONObject main = item.getJSONObject("main");
                            String temp = main.get("temp").toString();
                            celsuisText.setText(temp+" Celsius");
                            weatherText.setText("Condition: "+weather);
                        }
                        catch (Exception o){
                            Log.e(TAG, "onPostExecute: ",o );
                        }
                    }
                }
            }
        }.execute();
    }

}
