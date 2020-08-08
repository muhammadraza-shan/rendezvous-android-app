package com.example.folio9470m.rendezvous_re.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.folio9470m.rendezvous_re.R;
import com.example.folio9470m.rendezvous_re.models.User;
import com.example.folio9470m.rendezvous_re.util.RoundBitmapWithLetter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class PhoneContactAdapter extends RecyclerView.Adapter<PhoneContactAdapter.PhoneContactViewHolder>{
    private Context mCtx;
    private ArrayList<User> users;
    private int expandedPosition = -1;

    public PhoneContactAdapter(Context mCtx, ArrayList<User> users){
        this.mCtx = mCtx;
        this.users = users;

    }

    @NonNull
    @Override
    public PhoneContactAdapter.PhoneContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.phone_contact_item, parent, false);
        return new PhoneContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhoneContactAdapter.PhoneContactViewHolder holder, int position) {


        if (position == expandedPosition) {
            holder.llExpandArea.setVisibility(View.VISIBLE);
        } else {
            holder.llExpandArea.setVisibility(View.GONE);
        }

        holder.setOnClickListener(new PhoneContactViewHolder.ClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(expandedPosition == position){
                    int prev = expandedPosition;
                    expandedPosition = -1;
                    notifyItemChanged(prev);
                }
                else{
                    if (expandedPosition >= 0) {
                        int prev = expandedPosition;
                        notifyItemChanged(prev);
                    }
                    // Set the current position to "expanded"
                    expandedPosition = position;
                    notifyItemChanged(expandedPosition);
                }
            }

            @Override
            public void onInvideClick(View view, int position) {
                FirebaseDatabase.getInstance().getReference().child("users").child(users.get(position).getUserID())
                        .child("requests").child(FirebaseAuth.getInstance().getUid()).setValue(true);
                users.remove(position);
                notifyDataSetChanged();
                Toast.makeText(mCtx, "Friend Request sent", Toast.LENGTH_SHORT).show();            }
        });


        User request = users.get(position);
        String name = request.getName();
        holder.requestName.setText(name);
        holder.phoneNumberText.setText(request.getPhone());
        holder.imageView
                .setImageBitmap(RoundBitmapWithLetter
                        .generateCircleBitmap(mCtx,
                                RoundBitmapWithLetter.getMaterialColor(position),
                                60f,
                                (String.valueOf(name.charAt(0))).toUpperCase()
                        ));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    public static class PhoneContactViewHolder extends RecyclerView.ViewHolder{
        TextView requestName;
        CircleImageView imageView;
        LinearLayout llExpandArea;
        Button inviteButton;
        TextView phoneNumberText;


        public PhoneContactViewHolder(@NonNull View itemView) {
            super(itemView);
            llExpandArea = itemView.findViewById(R.id.llExpandArea);
            requestName = itemView.findViewById(R.id.requestname);
            imageView = itemView.findViewById(R.id.friend_image);
            inviteButton = itemView.findViewById(R.id.inviteButton_closeby);
            phoneNumberText = itemView.findViewById(R.id.phonenumbertextview);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onItemClick(v, getAdapterPosition());
                }
            });
            inviteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onInvideClick(v,getAdapterPosition());
                }
            });
        }
        private PhoneContactViewHolder.ClickListener  mClickListener;
        public interface ClickListener{
            public void onItemClick(View view, int position);
            public void onInvideClick(View view, int position);
        }
        public void setOnClickListener(PhoneContactViewHolder.ClickListener clickListener){
            mClickListener = clickListener;
        }
    }
}