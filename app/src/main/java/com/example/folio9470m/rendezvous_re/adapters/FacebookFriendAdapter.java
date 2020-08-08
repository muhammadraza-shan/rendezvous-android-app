package com.example.folio9470m.rendezvous_re.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.folio9470m.rendezvous_re.R;
import com.example.folio9470m.rendezvous_re.models.FacebookFriend;
import com.example.folio9470m.rendezvous_re.models.Friend;
import com.example.folio9470m.rendezvous_re.models.User;
import com.example.folio9470m.rendezvous_re.util.RoundBitmapWithLetter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoProvider;


import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class FacebookFriendAdapter extends RecyclerView.Adapter<FacebookFriendAdapter.FacebookFriendViewHolder>{
    private Context mCtx;
    private List<FacebookFriend> friendsList;
    private ArrayList<User> users;
    private int expandedPosition = -1;

    public FacebookFriendAdapter(Context mCtx, List<FacebookFriend> friendsList, ArrayList<User> users){
        this.mCtx = mCtx;
        this.friendsList = friendsList;
        this.users = users;

    }

    @NonNull
    @Override
    public FacebookFriendAdapter.FacebookFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.facebook_friend_item, parent, false);
        return new FacebookFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FacebookFriendAdapter.FacebookFriendViewHolder holder, int position) {


        if (position == expandedPosition) {
            holder.llExpandArea.setVisibility(View.VISIBLE);
        } else {
            holder.llExpandArea.setVisibility(View.GONE);
        }

       holder.setOnClickListener(new FacebookFriendViewHolder.ClickListener() {
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
               for(User user: users){
                   if(user.getFacebookid().equals(friendsList.get(position).getId())){
                       FirebaseDatabase.getInstance().getReference().child("users").child(user.getUserID())
                               .child("requests").child(FirebaseAuth.getInstance().getUid()).setValue(true);
                       friendsList.remove(position);
                       notifyDataSetChanged();
                       Toast.makeText(mCtx, "Friend Request sent", Toast.LENGTH_SHORT).show();
                       break;
                   }
               }
           }
       });


        FacebookFriend request = friendsList.get(position);
        String name = request.getName();
        holder.requestName.setText(name);
        Picasso.get()
                .load("https://graph.facebook.com/"+request.getId()+"/picture")
                .error(R.drawable.questionmark)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }


    public static class FacebookFriendViewHolder extends RecyclerView.ViewHolder{
        TextView requestName;
        CircleImageView imageView;
        LinearLayout llExpandArea;
        Button inviteButton;


        public FacebookFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            llExpandArea = itemView.findViewById(R.id.llExpandArea);
            requestName = itemView.findViewById(R.id.requestname);
            imageView = itemView.findViewById(R.id.friend_image);
            inviteButton = itemView.findViewById(R.id.inviteButton_closeby);
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
        private FacebookFriendViewHolder.ClickListener  mClickListener;
        public interface ClickListener{
            public void onItemClick(View view, int position);
            public void onInvideClick(View view, int position);
        }
        public void setOnClickListener(FacebookFriendViewHolder.ClickListener clickListener){
            mClickListener = clickListener;
        }
    }
}
