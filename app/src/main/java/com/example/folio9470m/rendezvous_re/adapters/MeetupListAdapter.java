package com.example.folio9470m.rendezvous_re.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.folio9470m.rendezvous_re.models.InvitedMeetup;
import com.example.folio9470m.rendezvous_re.models.Meetup;
import com.example.folio9470m.rendezvous_re.R;

import java.util.ArrayList;

public class MeetupListAdapter extends ArrayAdapter<InvitedMeetup> {
    private Context mContext;
    private int mResource;
    private View.OnClickListener onYesClickListener;
    private View.OnClickListener onNoClickListener;
    private View.OnClickListener onInfoClickListener;
    private int expandedPosition = -1;
    public MeetupListAdapter(Context context, int resource, ArrayList<InvitedMeetup> objects) {
        super(context, resource, objects);
        mContext= context;
        mResource = resource;
    }


    @Override
    public View getView(int position,  View convertView, ViewGroup parent) {
        String name = getItem(position).getName();
        String location = getItem(position).getLocation();
        String invitedBy = getItem(position).getInvitedBy();

        Meetup meetup = new Meetup(name, location, false);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        LinearLayout llExpandArea = convertView.findViewById(R.id.llExpandArea);
        if (position == expandedPosition) {
            llExpandArea.setVisibility(View.VISIBLE);
        } else {
            llExpandArea.setVisibility(View.GONE);
        }

        TextView nameText = convertView.findViewById(R.id.meetupName);
        TextView invitedText = convertView.findViewById(R.id.invitedText);
        Button yesButton = convertView.findViewById(R.id.yesButton);
        Button noButton = convertView.findViewById(R.id.noButton);
        Button infoButton = convertView.findViewById(R.id.infoButton);

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

        infoButton.setTag(position);
        yesButton.setTag(position);
        noButton.setTag(position);
        yesButton.setOnClickListener(this.onYesClickListener);
        noButton.setOnClickListener(this.onNoClickListener);
        infoButton.setOnClickListener(this.onInfoClickListener);

        invitedText.setText(invitedBy);
        nameText.setText(name);
        return convertView;
    }

    public void setOnYesClickListener( View.OnClickListener onClickListener) {
        onYesClickListener = onClickListener;
    }
    public void setOnNoClickListener( View.OnClickListener onClickListener) {
        onNoClickListener = onClickListener;
    }
    public void setOnInfoClickListener( View.OnClickListener onClickListener) {
        onInfoClickListener = onClickListener;
    }
}
