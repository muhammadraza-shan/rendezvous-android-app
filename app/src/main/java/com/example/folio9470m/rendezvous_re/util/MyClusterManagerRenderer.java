package com.example.folio9470m.rendezvous_re.util;

import android.content.Context;


import com.example.folio9470m.rendezvous_re.models.ClusterMarker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;

import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;



public class MyClusterManagerRenderer extends DefaultClusterRenderer<ClusterMarker>
{
    String currentUser;

    public MyClusterManagerRenderer(Context context, GoogleMap googleMap,
                                    ClusterManager<ClusterMarker> clusterManager, String currentUser) {
        super(context, googleMap, clusterManager);
        this.currentUser = currentUser;

    }


    @Override
    protected void onBeforeClusterItemRendered(ClusterMarker item, MarkerOptions markerOptions) {
        if(item.getUser().equals(currentUser)){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        }


    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        return false;
    }

    public void setUpdateMarker(ClusterMarker clusterMarker) {
        Marker marker = getMarker(clusterMarker);
        if (marker != null) {
            marker.setPosition(clusterMarker.getPosition());
            if(clusterMarker.getSnippet()!= null){
                marker.setSnippet(clusterMarker.getSnippet());
            }
        }
    }
}

