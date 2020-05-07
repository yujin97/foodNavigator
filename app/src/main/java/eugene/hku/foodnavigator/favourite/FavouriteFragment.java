package eugene.hku.foodnavigator.favourite;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.HandlerThread;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.util.ArrayList;

import eugene.hku.foodnavigator.R;
import eugene.hku.foodnavigator.dataClass.Bookmark;
import eugene.hku.foodnavigator.dataClass.Restaurant;
import eugene.hku.foodnavigator.dataClass.RestaurantSimple;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavouriteFragment extends Fragment {

    private final int REQUEST_LOCATION = 1000;
    private double lat,lng;
    private RequestQueue requestQueue;
    private favouriteAdapter myAdapter;
    private RecyclerView myRecycleriew;
    private FusedLocationProviderClient client;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private FirebaseFirestore db;
    private String userID;


    public FavouriteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favourite, container, false);

        db = FirebaseFirestore.getInstance();
        client = LocationServices.getFusedLocationProviderClient(getActivity());

        SharedPreferences prefs = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userID = prefs.getString("userID", "noUserId");

        final HandlerThread handlerThread = new HandlerThread("RequestLocation");

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(2000);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d("callBack", "inside");
                if (locationResult == null) {
                    Log.d("callback", "still null");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.d("inside callback", "still null");
                    if (location != null) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();

                        SharedPreferences.Editor editor = getActivity().getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                        editor.putFloat("lat",(float)lat);
                        editor.putFloat("lng", (float)lng);
                        editor.apply();

                        loadPlaces();
                        Log.d("location:", "" + lat + lng);
                        client.removeLocationUpdates(mLocationCallback);
                        handlerThread.quit();
                    }
                }


            }
        };

        //set RecyclerView
        myRecycleriew = (RecyclerView) root.findViewById(R.id.recyclerView);

        myRecycleriew.setLayoutManager(new LinearLayoutManager(getActivity()));

        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION
            );
        } else{
            Log.d("Permission", "OK");


            handlerThread.start();
            client.requestLocationUpdates(mLocationRequest,mLocationCallback,handlerThread.getLooper());
        }


        return root;
    }

    public void loadPlaces () {
        CollectionReference bookmarks = db.collection("Bookmark");
        Query bookmarkQuery = bookmarks.whereEqualTo("userId", userID);
        final ArrayList<String> restaurantIds = new ArrayList<>();
        final ArrayList<RestaurantSimple> models = new ArrayList<>();
        bookmarkQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Bookmark review = document.toObject(Bookmark.class);
                            RestaurantSimple restaurant = new RestaurantSimple();
                            restaurant.setPlace_id(review.getRestaurantId());
                            models.add(restaurant);
                        }
                        myAdapter = new favouriteAdapter(getActivity(),models);
                        myRecycleriew.setAdapter(myAdapter);
                        CollectionReference restaurants = db.collection("Restaurant");
                        for (String restaurantId : restaurantIds){

                            restaurants.document(restaurantId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if(document.exists()) {
                                            Restaurant restaurant = document.toObject(Restaurant.class);
                                            RestaurantSimple sRestaurant = new RestaurantSimple();
                                            sRestaurant.setPlace_name(restaurant.getPlace_name());
                                            sRestaurant.setPlace_id(restaurant.getPlace_id());
                                            sRestaurant.setLat(restaurant.getLat());
                                            sRestaurant.setLng(restaurant.getLng());
                                            sRestaurant.setDistance(sRestaurant.calDistance(lat,lng));
                                            if(restaurant.getIcon() != null){
                                                sRestaurant.setIcon(restaurant.getIcon());
                                            }
                                            if(restaurant.getDescription()!= null){
                                                sRestaurant.setDescription(restaurant.getDescription());
                                            }
                                        }
                                    }
                                }
                            });
                        }

                    }

                } else {
                    Log.d("Load bookmark", "failed");
                }
            }
        });


    }

}
