package com.example.folio9470m.rendezvous_re;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class LoginChooserActivity extends AppCompatActivity {
    Button emailButton;
    Button facebookButton;
    Button phoneButton;



    @Override
    public void onBackPressed() {
        // Do nothing
        Toast.makeText(this, "You must sign in to use the app.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_chooser);

        emailButton = findViewById(R.id.email_login_button);
        facebookButton = findViewById(R.id.facebook_login_button);
        phoneButton = findViewById(R.id.phone_login_button);

        emailButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginChooserActivity.this,EmailPasswordActivity.class));
            finish();
        });

        facebookButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginChooserActivity.this,FacebookLoginActivity.class));
            finish();
        });

        phoneButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginChooserActivity.this,PhoneAuthActivity.class));
            finish();
        });

    }
}
