package com.example.folio9470m.rendezvous_re.adapters;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.folio9470m.rendezvous_re.R;
import com.example.folio9470m.rendezvous_re.models.LatLong;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

    public RelativeLayout root;
    public TextView placeName;
    public TextView voteCount;
    public CircleImageView placeImage;
    public LinearLayout llExpandArea;
    public Button voteButton;
    public MapView mapView;
    public GoogleMap map;
    public Button mapButton;
    public LatLong coordinates;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getApplicationContext());
        map = googleMap;
        setMapLocation();
    }

    public void setMapLocation() {
        if (map == null) return;

        LatLng position = new LatLng(coordinates.getLatitude(),coordinates.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 13f));
        map.addMarker(new MarkerOptions().position(position));
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

    }

    public ViewHolder(View itemView) {
        super(itemView);
        root = itemView.findViewById(R.id.parent_layout);
        placeName = itemView.findViewById(R.id.voting_place_name);
        voteCount = itemView.findViewById(R.id.friendStatus);
        placeImage = itemView.findViewById(R.id.location_image);
        llExpandArea = (LinearLayout) itemView.findViewById(R.id.llExpandArea);
        voteButton = itemView.findViewById(R.id.voteButton_voting);
        mapButton = itemView.findViewById(R.id.mapButton_voting);
        mapView = itemView.findViewById(R.id.mapView_voting);

        if (mapView != null) {
            // Initialise the MapView
            mapView.onCreate(null);
            mapView.onResume();
            // Set the map ready callback to receive the GoogleMap object
            mapView.getMapAsync(this);
        }


        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onItemClick(v, getAdapterPosition());
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mClickListener.onItemLongClick(v, getAdapterPosition());
                return true;
            }
        });
        voteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onVoteClick(v, getAdapterPosition());
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mClickListener.onMapClick(v, getAdapterPosition());
            }
        });



    }

    public void setPlaceName(String string) {
        placeName.setText(string);
    }


    public void setVoteCount(String string) {
        voteCount.setText(string);
    }

    public void setCoordinates(LatLong latLong){
        coordinates = latLong;
    }

    public void setImageView(String placeId, PlacesClient placesClient){

        if (placeId.equals("customplace")) {
            placeImage.setImageResource(R.drawable.questionmark);
        } else {
            List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);
            FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeId, fields).build();
            placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
                Place place = response.getPlace();
                // Get the photo metadata.
                if(place.getPhotoMetadatas()!=null){
                    PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);

                    // Get the attribution text.
                    String attributions = photoMetadata.getAttributions();
                    // Create a FetchPhotoRequest.
                    FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                            .setMaxWidth(80) // Optional.
                            .setMaxHeight(80) // Optional.
                            .build();
                    placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                        Bitmap bitmap = fetchPhotoResponse.getBitmap();
                        placeImage.setImageBitmap(bitmap);
                    }).addOnFailureListener((exception) -> {
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            int statusCode = apiException.getStatusCode();
                            // Handle error with given status code.
                            // Log.e(TAG, "populateView: PlacesApiException. Setting picture to questionmark");
                            placeImage.setImageResource(R.drawable.questionmark);
                        }
                    });
                }
                else{
                    placeImage.setImageResource(R.drawable.questionmark);
                }

            });
        }

    }

    private ViewHolder.ClickListener  mClickListener;
    public interface ClickListener{
        public void onItemClick(View view, int position);
        public void onItemLongClick(View view, int position);
        public void onVoteClick(View view, int position);
        public void onMapClick(View view, int position);
    }
    public void setOnClickListener(ViewHolder.ClickListener clickListener){
        mClickListener = clickListener;
    }


}