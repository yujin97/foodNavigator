package eugene.hku.foodnavigator.restaurantDetail;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import eugene.hku.foodnavigator.R;
import eugene.hku.foodnavigator.dataClass.Bookmark;
import eugene.hku.foodnavigator.dataClass.Restaurant;
import eugene.hku.foodnavigator.dataClass.RestaurantSimple;
import eugene.hku.foodnavigator.dataClass.Review;
import eugene.hku.foodnavigator.helper.DataParser;
import eugene.hku.foodnavigator.home.homeAdapter;

import static com.android.volley.VolleyLog.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {

    private RequestQueue requestQueue;
    private String url;
    //from google api
    private Restaurant mRestaurant;
    //from db
    private Restaurant restaurant;
    private FirebaseFirestore db;

    //UI
    private MaterialTextView name;
    private MaterialTextView timeSlot;
    private MaterialTextView openStatus;
    private MaterialTextView address;
    private MaterialTextView description;
    private TextView rating_good_v;
    private TextView rating_ok_v;
    private TextView rating_bad_v;
    private Button reviewBtn;
    private Button updateBtn;
    private Button markBtn;
    private RecyclerView myRecycleriew;
    private reviewAdapter myAdapter;
    private AppCompatImageView icon;
    private TextView type;
    private TextView price;


    // Data
    private String placeId;
    private String userId;
    private String bookmarkId;
    private int distance;


    public DetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View root = inflater.inflate(R.layout.fragment_detail, container, false);

        //initialize fireStore
        db = FirebaseFirestore.getInstance();

        // get the place id from bundle;
        placeId = getArguments().getString("placeId");
        distance = getArguments().getInt("distance");




        Log.d("placeId", "ID is " + placeId);

        //get user id from shared preferences
        SharedPreferences prefs = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userId = prefs.getString("userID", "noUserId");


        //initialize view
        name =  (MaterialTextView) root.findViewById(R.id.name);
        description = (MaterialTextView) root.findViewById(R.id.restaurant_description);
        timeSlot = (MaterialTextView)root.findViewById(R.id.time);
        openStatus = (MaterialTextView)root.findViewById(R.id.opening);
        address = (MaterialTextView)root.findViewById(R.id.address);
        rating_good_v = (TextView) root.findViewById(R.id.rating_good_v);
        rating_ok_v = (TextView) root.findViewById(R.id.rating_ok_v);
        rating_bad_v = (TextView) root.findViewById(R.id.rating_bad_v);
        reviewBtn = (Button)root.findViewById(R.id.reviewBtn);
        updateBtn = (Button)root.findViewById(R.id.updateBtn);
        markBtn = (Button)root.findViewById(R.id.markBtn);
        type = (TextView)root.findViewById(R.id.type);
        price = (TextView)root.findViewById(R.id.price);
        markBtn.setEnabled(false);
        icon = (AppCompatImageView)root.findViewById(R.id.restaurant_image);





        url = getUrl();

        Log.d("URL","URL is " + url);

        loadPlaces();

        // draw the reviews
        myRecycleriew = (RecyclerView) root.findViewById(R.id.recyclerView);
        myRecycleriew.setLayoutManager(new LinearLayoutManager(getActivity()));
        return root;
    }

    // Create Url for the google api request
    private String getUrl (){
        StringBuilder googleUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        googleUrl.append("place_id=" + placeId);
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
                        HashMap<String,String> placeDetails = null;
                        JSONObject jsonObject = null;
                        ArrayList<Restaurant> models = new ArrayList<>();
                        try {
                            jsonObject = response.getJSONObject("result");
                            placeDetails = dataParser.getPlaceDetails(jsonObject);


                            String name = placeDetails.get("place_name");
                            String formatted_address = placeDetails.get("formatted_address");
                            String temp1 = placeDetails.get("latitude");
                            String temp2 = placeDetails.get("longitude");
                            Log.d("temp",temp1 + temp2);
                            double latitude = 0;
                            double longitude = 0;
                            if(temp1 != null && temp2 != null) {
                                latitude = Double.parseDouble(placeDetails.get("latitude"));
                                longitude = Double.parseDouble(placeDetails.get("longitude"));
                            }
                            String placeId = placeDetails.get("placeId");
                            String time_open = placeDetails.get("time_open");
                            String time_closed =placeDetails.get("time_closed");
                            String open_now_temp = placeDetails.get("open_now");

                            // create the Restaurant object

                            mRestaurant = new Restaurant();

                            Log.d("recieved open_now", "Value is " + open_now_temp);
                            Boolean open_now;

                            if(!open_now_temp.equals("")) {
                                open_now = Boolean.parseBoolean(open_now_temp);
                                mRestaurant.setOpen_now(open_now);
                                if(open_now){
                                    openStatus.setText("Open");
                                }
                                else {
                                    openStatus.setText("Closed");
                                }
                            }

                            mRestaurant.setLat(latitude);
                            mRestaurant.setLng(longitude);
                            mRestaurant.setPlace_id(placeId);
                            mRestaurant.setPlace_name(name);
                            mRestaurant.setAddress(formatted_address);
                            mRestaurant.setTime_close(time_closed);
                            mRestaurant.setTime_open(time_open);

                            draw();

                            if (distance <= 1500) {
                                reviewBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Bundle bundle = new Bundle();
                                        bundle.putString("placeId", mRestaurant.getPlace_id());
                                        bundle.putString("placeName", mRestaurant.getPlace_name());
                                        bundle.putString("address", mRestaurant.getAddress());
                                        bundle.putDouble("lat", mRestaurant.getLat());
                                        bundle.putDouble("lng", mRestaurant.getLng());


                                        Navigation.findNavController(v).navigate(R.id.action_nav_detail_to_nav_post_review, bundle);
                                    }
                                });

                                updateBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Bundle bundle = new Bundle();
                                        bundle.putString("placeId", mRestaurant.getPlace_id());
                                        bundle.putString("placeName",mRestaurant.getPlace_name());
                                        bundle.putString("address",mRestaurant.getAddress());
                                        bundle.putDouble("lat", mRestaurant.getLat());
                                        bundle.putDouble("lng",mRestaurant.getLng());


                                        Navigation.findNavController(v).navigate(R.id.action_nav_detail_to_nav_update_detail,bundle);
                                    }
                                });

                                icon.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Bundle bundle = new Bundle();
                                        bundle.putString("restaurantId", mRestaurant.getPlace_id());
                                        bundle.putString("restaurantName",mRestaurant.getPlace_name());
                                        bundle.putString("address",mRestaurant.getAddress());
                                        bundle.putDouble("lat", mRestaurant.getLat());
                                        bundle.putDouble("lng",mRestaurant.getLng());
                                        Navigation.findNavController(getView()).navigate(R.id.action_nav_detail_to_nav_rest_pic_update, bundle);
                                    }
                                });
                            } else {
                                reviewBtn.setVisibility(View.GONE);
                                updateBtn.setAlpha((float)0.2);
                            }



                            address.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("placeName",mRestaurant.getPlace_name());
                                    bundle.putDouble("lat", mRestaurant.getLat());
                                    bundle.putDouble("lng",mRestaurant.getLng());
                                    Navigation.findNavController(getView()).navigate(R.id.action_nav_detail_to_nav_detail_map, bundle);
                                }
                            });



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

    private void draw() {
        name.setText(mRestaurant.getPlace_name());
        timeSlot.setText("" + mRestaurant.getTime_open() + " - " + mRestaurant.getTime_close());
        address.setText(mRestaurant.getAddress());

        loadRestaurant();

    }

    // load reviews of the restaurant
    private void loadReviews(){
        final ArrayList<Review> models = new ArrayList<>();

        CollectionReference reviewRef = db.collection("Review");
        Query reviewQuery = reviewRef.whereEqualTo("restaurantId", mRestaurant.getPlace_id());

        reviewQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("task", "The task is successful");
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Review review = document.toObject(Review.class);
                        models.add(review);
                        Log.d("review", "The review is " + review.getContent());
                    }
                    Collections.sort(models);
                    myAdapter = new reviewAdapter(getActivity(),models);
                    myRecycleriew.setAdapter(myAdapter);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

    }

    public void loadRestaurant() {
        db.collection("Restaurant").document(placeId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        restaurant = document.toObject(Restaurant.class);
                        if(restaurant.getDescription()!= null) {
                            description.setText(restaurant.getDescription());
                            Log.d("Description", restaurant.getDescription());
                        }
                        rating_good_v.setText(""+restaurant.getRating_good());
                        rating_ok_v.setText(""+restaurant.getRating_ok());
                        rating_bad_v.setText(""+restaurant.getRating_bad());


                        if(restaurant.getIcon() != null){
                            Picasso.with(getContext())
                                    .load(restaurant.getIcon())
                                    .fit()
                                    .centerCrop()
                                    .into(icon);
                        }


                        if(restaurant.getType() != 0){
                            int mType = restaurant.getType();
                            switch(mType) {
                                case 1:
                                    type.setText("Chinese Food");
                                    break;
                                case 2:
                                    type.setText("Western Food");
                                    break;
                                case 3:
                                    type.setText("Japanese Food");
                                    break;
                                case 4:
                                    type.setText("Indian Food");
                                    break;
                                case 5:
                                    type.setText("Others");
                                    break;
                            }
                        }

                        if(restaurant.getPrice() != 0){
                            int mPrice = restaurant.getPrice();
                            switch(mPrice) {
                                case 1:
                                    price.setText("Bellow $50");
                                    break;
                                case 2:
                                    price.setText("$51 - $100");
                                    break;
                                case 3:
                                    price.setText("$101 - $200");
                                    break;
                                case 4:
                                    price.setText("$201 - $500");
                                    break;
                                case 5:
                                    price.setText("Above $500");
                                    break;
                            }
                        }
                        loadReviews();

                    } else {
                        Log.d("LoadRestaurant", "No Restaurant info yet");
                    }
                    loadBookmark();
                } else {
                    Toast.makeText(getActivity(),"Failed to fetch!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void loadBookmark() {
        Query reviewQuery = db.collection("Bookmark").whereEqualTo("restaurantId", placeId).whereEqualTo("userId",userId);
        reviewQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(! task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            bookmarkId = document.getId();
                        }
                        markBtn.setText("BOOKMARKED");
                        markBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteBookmark();
                            }
                        });
                        markBtn.setEnabled(true);
                    } else {
                        Log.d("Load bookmark", "Not bookmarked");
                        markBtn.setText("BOOKMARK");
                        markBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addBookmark();
                            }
                        });
                        markBtn.setEnabled(true);
                    }

                } else {
                    Log.d("Load bookmark", "Failed");
                }
            }
        });
    }

    public void addBookmark() {
        markBtn.setEnabled(false);
        final CollectionReference bookmarks = db.collection("Bookmark");
        final Bookmark bookmark = new Bookmark();
        bookmark.setRestaurantId(placeId);
        bookmark.setUserId(userId);
        if(restaurant == null){
            Restaurant newRestaurant = new Restaurant();
            newRestaurant.setRating_bad(0);
            newRestaurant.setRating_ok(0);
            newRestaurant.setRating_good(0);

            newRestaurant.setPlace_id(placeId);
            newRestaurant.setPlace_name(mRestaurant.getPlace_name());
            newRestaurant.setOperation(true);
            newRestaurant.setAddress(mRestaurant.getAddress());
            newRestaurant.setLat(mRestaurant.getLat());
            newRestaurant.setLng(mRestaurant.getLng());
            CollectionReference restaurants = db.collection("Restaurant");
            restaurants.document(placeId).set(newRestaurant).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    bookmarks.add(bookmark).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            bookmarkId = task.getResult().getId();
                            markBtn.setText("BOOKMARKED");
                            Toast.makeText(getActivity(),"Bookmarked restaurant", Toast.LENGTH_SHORT).show();
                            markBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteBookmark();
                                }
                            });
                            markBtn.setEnabled(true);
                        }
                    });

                }
            });
        } else {
            bookmarks.add(bookmark).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    bookmarkId = task.getResult().getId();
                    markBtn.setText("BOOKMARKED");
                    Toast.makeText(getActivity(), "Bookmarked restaurant", Toast.LENGTH_SHORT).show();
                    markBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteBookmark();
                        }
                    });
                    markBtn.setEnabled(true);
                }
            });
        }

    }

    public void deleteBookmark() {
        markBtn.setEnabled(false);
        CollectionReference bookmarks = db.collection("Bookmark");

        bookmarks.document(bookmarkId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                markBtn.setText("BOOKMARK");
                Toast.makeText(getActivity(),"Remove bookmark of restaurant", Toast.LENGTH_SHORT).show();
                markBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addBookmark();
                    }
                });
                markBtn.setEnabled(true);
            }
        });

    }



}
