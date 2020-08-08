package com.example.folio9470m.rendezvous_re;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.example.folio9470m.rendezvous_re.adapters.NoSwipeViewPager;
import com.example.folio9470m.rendezvous_re.adapters.SectionsPageAdapter;
import com.google.android.material.tabs.TabLayout;

public class InvitePeopleActivity extends AppCompatActivity {

    private SectionsPageAdapter mSectionsPageAdapter;
    private NoSwipeViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_people);
        setTitle("Invite People");
        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager =  (NoSwipeViewPager) findViewById(R.id.container);
        mViewPager.setSwipeable(false);
        setupViewPager(mViewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


    }
    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new InviteFragment(), "Invite");
        adapter.addFragment(new CloseByFriendsFragment(), "Closeby Friends");
        viewPager.setAdapter(adapter);
    }

}
