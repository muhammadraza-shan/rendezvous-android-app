package com.example.folio9470m.rendezvous_re.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.recyclerview.widget.RecyclerView;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.folio9470m.rendezvous_re.MeetupOverviewNav;
import com.example.folio9470m.rendezvous_re.models.Meetup;
import com.example.folio9470m.rendezvous_re.R;
import com.example.folio9470m.rendezvous_re.util.GlideApp;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class MeetupRecyclerAdapter extends RecyclerView.Adapter<MeetupRecyclerAdapter.MeetupViewHolder>  {
    private static final String TAG = MeetupRecyclerAdapter.class.getSimpleName();

    //this context we will use to inflate the layout
    private Context mCtx;
    private PlacesClient placesClient;
    private Geocoder mGeocoder;

    //we are storing all the products in a list
    private final List<Meetup> meetupList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Meetup item);
    }


    //getting the context and product list with constructor
    public  MeetupRecyclerAdapter(Context mCtx, List<Meetup> meetupList, OnItemClickListener listener) {
        placesClient = Places.createClient(mCtx);
        mGeocoder = new Geocoder(mCtx, Locale.ENGLISH);
        this.mCtx = mCtx;
        this.meetupList = meetupList;
        this.listener = listener;

    }

    @Override
    public MeetupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflating and returning our view holder
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.layout_meetups_recycler, parent,false);
        return new MeetupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MeetupViewHolder holder, int position) {
        //getting the product of the specified position
        Meetup meetup = meetupList.get(position);



        String imagePath = meetup.getImagepath();
        if(imagePath!=null && !imagePath.equals("")){
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(imagePath);
            GlideApp.with(mCtx)
                    .load(storageReference)
                    .fitCenter()
                    .into(holder.imageView);
        }
        else{
            getImageFromPlaceApi(meetup.getPlaceID(), holder);
        }


        String address = "Address: ";
        if(meetup.getPlaceID()!=null && !meetup.getPlaceID().equals("customplace")){
            address = address+meetup.getLocation()+" , ";
        }


        if(meetup.getLocationMethod() == 0){
            holder.textViewAddress.setText(address+meetup.getAddress());
        }
        else{
            holder.textViewAddress.setText(address+"Not yet set");
        }
        //binding the data with the viewholder views
        holder.textViewTitle.setText(meetup.getName());
        holder.textViewDate.setText(meetup.getDate().toString());
        holder.bind(meetup, listener);

    }

    private void getImageFromPlaceApi(String placeId, MeetupViewHolder holder) {
        if(placeId == null || placeId.equals("customplace")){
            holder.imageView.setImageResource(R.drawable.questionmark);
        }
        else{
            List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);
            FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeId, fields).build();
            placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
                Place place = response.getPlace();
                // Get the photo metadata.
                if(place.getPhotoMetadatas() == null){
                    holder.imageView.setImageResource(R.drawable.questionmark);
                }
                else{
                    PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);
                    // Get the attribution text.
                    String attributions = photoMetadata.getAttributions();
                    // Create a FetchPhotoRequest.
                    FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                            .setMaxWidth(200) // Optional.
                            .setMaxHeight(200) // Optional.
                            .build();
                    placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                        Bitmap bitmap = fetchPhotoResponse.getBitmap();
                        holder.imageView.setImageBitmap(bitmap);
                    }).addOnFailureListener((exception) -> {
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            int statusCode = apiException.getStatusCode();
                            // Handle error with given status code.
                            Log.e(TAG, "Place not found: " + exception.getMessage());
                            holder.imageView.setImageResource(R.drawable.questionmark);
                        }
                    });
                }

            });

        }
    }


    @Override
    public int getItemCount() {
        return meetupList.size();
    }
    private String getAddressByCoordinates(double lat, double lon) throws IOException {
        Log.d(TAG, "getAddressByCoordinates: called");

        List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            return addresses.get(0).getAddressLine(0);
        }
        return null;
    }


    class MeetupViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle, textViewAddress, textViewDate;
        ImageView imageView;

        public MeetupViewHolder(View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewAddress = itemView.findViewById(R.id.Address_recycler);
            textViewDate = itemView.findViewById(R.id.date_recycler);
            imageView = itemView.findViewById(R.id.nav_user_image);
        }
        public void bind(final Meetup item, final OnItemClickListener listener) {
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }


}
