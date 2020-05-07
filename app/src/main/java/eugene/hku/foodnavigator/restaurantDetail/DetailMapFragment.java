package eugene.hku.foodnavigator.restaurantDetail;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import eugene.hku.foodnavigator.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailMapFragment extends Fragment implements OnMapReadyCallback {

    private double lat;
    private double lng;
    private String placeName;


    public DetailMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail_map, container, false);

        //get lat and lng
        lat = getArguments().getDouble("lat");
        lng = getArguments().getDouble("lng");
        placeName = getArguments().getString("placeName");

        // Inflate the layout for this fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        LatLng place = new LatLng(lat, lng);
        googleMap.addMarker(new MarkerOptions().position(place)
                .title(placeName));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(place));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place,15));
        // Zoom in, animating the camera.
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

    }


}
