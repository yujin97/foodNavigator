package eugene.hku.foodnavigator.home;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eugene.hku.foodnavigator.R;
import eugene.hku.foodnavigator.dataClass.RestaurantSimple;
import eugene.hku.foodnavigator.helper.DataParser;
import eugene.hku.foodnavigator.helper.DownloadUrl;
import eugene.hku.foodnavigator.helper.GetNearByPlaces;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.content.Context.MODE_PRIVATE;
import static com.android.volley.VolleyLog.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private final int REQUEST_LOCATION = 1000;
    private String url;
    private double lat,lng;
    private RequestQueue requestQueue;
    private RecyclerView myRecycleriew;
    private homeAdapter myAdapter;
    private FusedLocationProviderClient client;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private ArrayList<RestaurantSimple> models;
    private int count = 0;

    private Button loadBtn;
    private String nextToken;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        client = LocationServices.getFusedLocationProviderClient(getActivity());


        final HandlerThread handlerThread = new HandlerThread("RequestLocation");

        models = new ArrayList<>();

        loadBtn = root.findViewById(R.id.loadBtn);

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
                        url = getUrl(lat,lng);

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



        if(ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION
                    );
        } else{
            Log.d("Permission", "OK");



            handlerThread.start();
            client.requestLocationUpdates(mLocationRequest,mLocationCallback,handlerThread.getLooper());
        }

        SharedPreferences prefs = getActivity().getSharedPreferences("userInfo", MODE_PRIVATE);
        String userId = prefs.getString("userID", "noUserId");

        Log.d("UserID", "UserId is" + userId);

        return root;
    }

    private String getUrl (double latitude, double longitude){
        StringBuilder googleUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleUrl.append("location=" + latitude +"," + longitude);
        googleUrl.append("&radius=1500");
        googleUrl.append("&type=restaurant");
        googleUrl.append("&sensor=true");
        googleUrl.append("&key=" + "AIzaSyCg-arg7UnVevetAspDg8jssi9KOp1vhho");

        return googleUrl.toString();

    }


    private void loadPlaces(){
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getActivity().getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        DataParser dataParser = new DataParser();
                        List <HashMap<String,String>> nearbyPlaceList = null;
                        JSONArray jsonArray = null;
                        nextToken = null;
                        try {
                            jsonArray = response.getJSONArray("results");
                            if(!response.getString("next_page_token").isEmpty()){
                                nextToken = response.getString("next_page_token");
                                Log.d("token", nextToken);
                            }
                            nearbyPlaceList = dataParser.getAllNearbyPlaces(jsonArray);
                            for (int i=0; i<nearbyPlaceList.size(); i++){
                                HashMap<String, String> googleNearbyPlace = nearbyPlaceList.get(i);
                                String name = googleNearbyPlace.get("place_name");
                                String vicinity = googleNearbyPlace.get("vicinity");
                                double latitude = Double.parseDouble(googleNearbyPlace.get("latitude"));
                                double longitude = Double.parseDouble(googleNearbyPlace.get("longitude"));
                                String placeId = googleNearbyPlace.get("placeId");

                                RestaurantSimple restaurant = new RestaurantSimple();
                                restaurant.setLat(latitude);
                                restaurant.setLng(longitude);
                                restaurant.setPlace_id(placeId);
                                restaurant.setPlace_name(name);
                                restaurant.setVicinity(vicinity);
                                restaurant.setDistance(restaurant.calDistance(lat,lng));
                                models.add(restaurant);
                            }
                            myAdapter = new homeAdapter(getActivity(),models);
                            myRecycleriew.setAdapter(myAdapter);
                            if(nextToken != null){
                                loadBtn.setAlpha(1);
                                loadBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        fetchData(getNextUrl(nextToken));
                                    }
                                });

                            }
                        }
                        catch(JSONException e){
                            e.getStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: Error= " + error);
                Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
            }
        });

        requestQueue.add(request);

    }

    private void fetchData (String mUrl) {
        requestQueue.start();
        Log.d("nextUrl", mUrl);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,mUrl,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Second", "onResponse");
                        DataParser dataParser = new DataParser();
                        List <HashMap<String,String>> nearbyPlaceList = null;
                        JSONArray jsonArray = null;
                        Log.d("Result",response.toString());
                        try {
                            jsonArray = response.getJSONArray("results");
                            nearbyPlaceList = dataParser.getAllNearbyPlaces(jsonArray);
                            for (int i=0; i<nearbyPlaceList.size(); i++){
                                nextToken = null;
                                loadBtn.setAlpha((float)0.2);
                                loadBtn.setEnabled(false);
                                if(!response.getString("next_page_token").isEmpty()){
                                    nextToken = response.getString("next_page_token");
                                    Log.d("token", nextToken);
                                }
                                HashMap<String, String> googleNearbyPlace = nearbyPlaceList.get(i);
                                String name = googleNearbyPlace.get("place_name");
                                String vicinity = googleNearbyPlace.get("vicinity");
                                double latitude = Double.parseDouble(googleNearbyPlace.get("latitude"));
                                double longitude = Double.parseDouble(googleNearbyPlace.get("longitude"));
                                String placeId = googleNearbyPlace.get("placeId");

                                RestaurantSimple restaurant = new RestaurantSimple();
                                restaurant.setLat(latitude);
                                restaurant.setLng(longitude);
                                restaurant.setPlace_id(placeId);
                                restaurant.setPlace_name(name);
                                restaurant.setVicinity(vicinity);
                                restaurant.setDistance(restaurant.calDistance(lat,lng));
                                models.add(restaurant);
                                Log.d("fetch","Added");
                            }
                            if(nextToken != null){
                                loadBtn.setEnabled(true);
                                loadBtn.setAlpha(1);
                            }
                            myAdapter = new homeAdapter(getActivity(),models);
                            myRecycleriew.setAdapter(myAdapter);

                        }
                        catch(JSONException e){
                            e.getStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: Error= " + error);
                Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
            }
        });

        requestQueue.add(request);
    }

    private String getNextUrl(String token){
        StringBuilder googleUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleUrl.append("pagetoken=" + token);
        googleUrl.append("&key=" + "AIzaSyCg-arg7UnVevetAspDg8jssi9KOp1vhho");

        return googleUrl.toString();
    }

    private void getLocation() {
        if(ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION
            );
        } else{
            Log.d("Permission", "OK");
            client.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Log.d("SUCCESS", "it is probably null");
                    if(location != null){
                        Log.d("SUCCESS",location.toString());
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                        url = getUrl(lat,lng);

                        //save into shared preferences
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                        editor.putFloat("lat",(float)lat);
                        editor.putFloat("lng", (float)lng);
                        editor.apply();

                        loadPlaces();


                    }
                }
            });
        }

    }






}
