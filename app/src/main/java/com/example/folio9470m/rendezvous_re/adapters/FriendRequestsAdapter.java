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
import androidx.recyclerview.widget.RecyclerView;

public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.RequestViewHolder>{
    private Context mCtx;
    private final List<Friend> requestsList;

    public FriendRequestsAdapter(Context mCtx, List<Friend> requestsList){
        this.mCtx = mCtx;
        this.requestsList = requestsList;

    }

    @NonNull
    @Override
    public FriendRequestsAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.friend_request_item, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestsAdapter.RequestViewHolder holder, int position) {
        Friend request = requestsList.get(position);
        String name = request.getName();
        holder.requestName.setText(name);
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
        return requestsList.size();
    }


    public class RequestViewHolder extends RecyclerView.ViewHolder{
        TextView requestName;
        ImageView imageView;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            requestName = itemView.findViewById(R.id.requestname);
            imageView = itemView.findViewById(R.id.friend_image);
        }
    }
}
