package com.example.folio9470m.rendezvous_re.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.Resource;
import com.example.folio9470m.rendezvous_re.R;
import com.example.folio9470m.rendezvous_re.models.Meetup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PublicMeetupsAdapter extends ArrayAdapter<Meetup> implements Filterable {
    private ArrayList<Meetup> meetupsList;
    private ArrayList<Meetup> filteredMeetupsList;
    private Context mContext;
    private int mResource;
    private int expandedPosition = -1;
    private final PublicMeetupsAdapter.OnItemClickListener listenerInfo;
    private final PublicMeetupsAdapter.OnItemClickListener listenerYes;

    public interface OnItemClickListener {
        void onItemClick(Meetup item);
    }


    public PublicMeetupsAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Meetup> objects,
                                PublicMeetupsAdapter.OnItemClickListener listenerInfo,
                                PublicMeetupsAdapter.OnItemClickListener listenerYes) {
        super(context, resource, objects);
        this.meetupsList = objects;
        this.filteredMeetupsList = objects;
        mContext = context;
        mResource = resource;
        this.listenerInfo = listenerInfo;
        this.listenerYes = listenerYes;
    }

    @Override
    public int getCount() {
        return filteredMeetupsList.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        convertView = inflater.inflate(mResource, parent, false);

        LinearLayout llExpandArea = convertView.findViewById(R.id.llExpandArea);
        if (position == expandedPosition) {
            llExpandArea.setVisibility(View.VISIBLE);
        } else {
            llExpandArea.setVisibility(View.GONE);
        }

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


        Button yesButton = convertView.findViewById(R.id.yesButton);
        Button infoButton = convertView.findViewById(R.id.infoButton);
        infoButton.setTag(position);
        yesButton.setTag(position);

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenerInfo.onItemClick(filteredMeetupsList.get(position));
            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenerYes.onItemClick(filteredMeetupsList.get(position));

            }
        });


        TextView nameText = convertView.findViewById(R.id.meetupnametextview);
        TextView tagsText = convertView.findViewById(R.id.meetuptagstextview);
        nameText.setText(filteredMeetupsList.get(position).getName());
        if(filteredMeetupsList.get(position).getTags()!=null && !filteredMeetupsList.get(position).getTags().equals("") )
            tagsText.setText(filteredMeetupsList.get(position).getTags());

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if(charString.isEmpty()){
                    filteredMeetupsList = meetupsList;
                } else {
                    ArrayList<Meetup> filteredList = new ArrayList<>();
                    for(Meetup row: meetupsList){
                        if(row.getTags()!=null){
                            if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getTags().toLowerCase().contains(charString.toLowerCase())) {
                                filteredList.add(row);
                            }
                        }
                        else{
                            if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                                filteredList.add(row);
                            }
                        }


                    }
                    filteredMeetupsList = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredMeetupsList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredMeetupsList = (ArrayList<Meetup>) results.values;
                notifyDataSetChanged();

            }
        };
    }


}
