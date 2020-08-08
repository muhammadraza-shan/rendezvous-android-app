package com.example.folio9470m.rendezvous_re.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.folio9470m.rendezvous_re.R;
import com.example.folio9470m.rendezvous_re.util.RoundBitmapWithLetter;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class MembersListAdapter extends ArrayAdapter<String> {
    Context mContext;
    private int mResource;

    public MembersListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
        super(context, resource, objects);
        mContext= context;
        mResource = resource;
    }


    @Override
    public View getView(int position, View convertView,  ViewGroup parent) {
        String name = getItem(position);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView nameText = convertView.findViewById(R.id.membername);
        ImageView imageView = convertView.findViewById(R.id.friend_image);

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
}
