package com.example.folio9470m.rendezvous_re;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.folio9470m.rendezvous_re.adapters.NoSwipeViewPager;
import com.example.folio9470m.rendezvous_re.adapters.SectionsPageAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

public class FriendsActivity extends AppCompatActivity {

    private SectionsPageAdapter mSectionsPageAdapter;
    private NoSwipeViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        setTitle("Friends");
        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager =  (NoSwipeViewPager) findViewById(R.id.container);
        mViewPager.setSwipeable(false);
        setupViewPager(mViewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.nav_home:
                        startActivity(new Intent(FriendsActivity.this, MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                        break;
                    case R.id.nav_friends:

                        break;
                    case R.id.nav_meetups:
                        startActivity(new Intent(FriendsActivity.this, CurrentMeetupsRecycler.class)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                        break;
                    case R.id.nav_search:
                        startActivity(new Intent(FriendsActivity.this, SearchActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                        break;
                }
                return false;
            }
        });

    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new FriendsFragment(), "Friends");
        adapter.addFragment(new FriendRequestsFragment(), "Requests");
        adapter.addFragment(new FacebookFriendsFragment(), "Facebook");
        adapter.addFragment(new PhoneContactsFragment(), "Phone");
        viewPager.setAdapter(adapter);
    }

}
