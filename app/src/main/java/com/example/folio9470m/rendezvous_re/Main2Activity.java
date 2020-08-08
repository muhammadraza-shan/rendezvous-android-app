package com.example.folio9470m.rendezvous_re;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.folio9470m.rendezvous_re.models.ChatMessage;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class Main2Activity extends AppCompatActivity {

    private FirebaseListAdapter<ChatMessage> adapter;
    private String mID;
    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        setTitle("Chatroom");

        Intent intent = getIntent();
        mID = intent.getStringExtra("id");

            Toast.makeText(this,
                    "Welcome " + FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getEmail(),
                    Toast.LENGTH_LONG)
                    .show();

            // Load chat room contents
            displayChatMessages();
        ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("chat")
                .child(mID);

        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .setLayout(R.layout.message)
                .build();

        adapter = new FirebaseListAdapter<ChatMessage>(options) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(adapter);

        FloatingActionButton fab =
                (FloatingActionButton)findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            EditText input = (EditText)findViewById(R.id.input);

            // Read the input field and push a new instance
            // of ChatMessage to the Firebase database
            FirebaseDatabase.getInstance()
                    .getReference()
                    .child("chat")
                    .child(mID)
                    .push()
                    .setValue(new ChatMessage(input.getText().toString(),
                            ((Rendezvous)getApplicationContext()).getUserName())
                    );

            // Clear the input
            input.setText("");
        });
    }

    public void displayChatMessages(){

    }


}
