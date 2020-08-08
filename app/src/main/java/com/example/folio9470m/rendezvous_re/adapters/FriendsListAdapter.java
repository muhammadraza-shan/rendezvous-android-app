package com.example.folio9470m.rendezvous_re.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.folio9470m.rendezvous_re.R;
import com.example.folio9470m.rendezvous_re.models.Friend;
import com.example.folio9470m.rendezvous_re.util.RoundBitmapWithLetter;


import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.friendViewHolder>{
    private Context mCtx;
    private final List<Friend> friendsList;

    public FriendsListAdapter(Context mCtx, List<Friend> friendsList){
        this.mCtx = mCtx;
        this.friendsList = friendsList;

    }

    @NonNull
    @Override
    public FriendsListAdapter.friendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.friends_list_item, parent, false);
        return new friendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsListAdapter.friendViewHolder holder, int position) {
        Friend friend = friendsList.get(position);
        String name = friend.getName();
        holder.friendName.setText(name);
        holder.imageView
                .setImageBitmap(RoundBitmapWithLetter
                        .generateCircleBitmap(mCtx,
                                RoundBitmapWithLetter.getMaterialColor(position),
                                60f,
                                (String.valueOf(name.charAt(0))).toUpperCase()
                        ));
        if(friend.getOnline()){
            holder.friendStatus.setColorFilter(ContextCompat.getColor(mCtx, R.color.quantum_googgreen500));
        }
        else{
            holder.friendStatus.setColorFilter(ContextCompat.getColor(mCtx, R.color.quantum_googred500));
        }
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }


    public class friendViewHolder extends RecyclerView.ViewHolder{
        TextView friendName;
        ImageView imageView;
        CircleImageView friendStatus;

        public friendViewHolder(@NonNull View itemView) {
            super(itemView);
            friendName = itemView.findViewById(R.id.friendname);
            imageView = itemView.findViewById(R.id.friend_image);
            friendStatus = itemView.findViewById(R.id.friendStatus);
        }
    }
}
