package com.example.folio9470m.rendezvous_re.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.folio9470m.rendezvous_re.R;
import com.example.folio9470m.rendezvous_re.util.RoundBitmapWithLetter;
import com.example.folio9470m.rendezvous_re.models.UserLocation;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class CloseByFriendsAdapter extends ArrayAdapter<UserLocation> {
    private Context mContext;
    private int mResource;
    private ArrayList<UserLocation> userLocations;
    private ArrayList<Double> friendsDistances;
    private View.OnClickListener onClickListener;
    private int expandedPosition = -1;

    public CloseByFriendsAdapter(@NonNull Context context, int resource, @NonNull ArrayList<UserLocation> objects,
                                 ArrayList<Double> distances) {
        super(context, resource, objects);
        mContext= context;
        mResource = resource;
        userLocations = objects;
        friendsDistances = distances;
    }


    @Override
    public View getView(int position, View convertView,  ViewGroup parent) {
        String name = getItem(position).getName();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);
        LinearLayout llExpandArea = convertView.findViewById(R.id.llExpandArea);

        if (position == expandedPosition) {
            llExpandArea.setVisibility(View.VISIBLE);
        } else {
            llExpandArea.setVisibility(View.GONE);
        }

        TextView nameText = convertView.findViewById(R.id.membername);
        ImageView imageView = convertView.findViewById(R.id.friend_image);
        TextView distanceText = convertView.findViewById(R.id.friendStatus);
        Double d = (friendsDistances.get(position))/1000; // Convert to kilometers
        distanceText.setText(String.format("%.1f", d)+" km away");
        Button inviteButton = convertView.findViewById(R.id.inviteButton_closeby);
        inviteButton.setTag(position);
        inviteButton.setOnClickListener(this.onClickListener);
        convertView.setTag(position);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = Integer.parseInt( v.getTag().toString());
                if(expandedPosition == position){
                    expandedPosition = -1;
                    notifyDataSetChanged();
                }
                else{
                    if (expandedPosition >= 0) {
                        expandedPosition = -1;
                        notifyDataSetChanged();
                    }
                    // Set the current position to "expanded"
                    expandedPosition = position;
                    notifyDataSetChanged();
                }

            }
        });

        nameText.setText(name);
        imageView
                .setImageBitmap(RoundBitmapWithLetter
                        .generateCircleBitmap(mContext,
                                RoundBitmapWithLetter.getMaterialColor(position),
                                60f,
                                (String.valueOf(name.charAt(0))).toUpperCase()
                        ));

        return convertView;
    }
    public void setOnClickListener( View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }





}
