package eugene.hku.foodnavigator.review;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import eugene.hku.foodnavigator.R;
import eugene.hku.foodnavigator.dataClass.Restaurant;
import eugene.hku.foodnavigator.dataClass.Review;
import eugene.hku.foodnavigator.dataClass.User;


public class PostReviewFragment extends Fragment {

    //UI
    private TextView title;
    private EditText content;
    private CircleImageView icon;
    private TextWatcher checkPost;
    private Button postBtn;
    private TextView cancelBtn;
    private RadioGroup ratingGroup;
    private RadioButton rating_good;
    private RadioButton rating_ok;
    private RadioButton rating_bad;

    //Data
    private User poster;
    private String userId;
    private String restaurantId;
    private String restaurantName;
    private String ratingText;
    private String address;
    private double lat;
    private double lng;
    private int rateSelected;
    private Review review;
    private User user;

    //Database
    private FirebaseFirestore db;

    public PostReviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post_review, container, false);

        SharedPreferences prefs = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userId = prefs.getString("userID", "noUserId");

        //passed by bundle
        restaurantId = getArguments().getString("placeId");
        restaurantName = getArguments().getString("placeName");
        address =getArguments().getString("address");
        lat = getArguments().getDouble("lat");
        lng = getArguments().getDouble("lng");


        //UI
        title = (TextView) rootView.findViewById(R.id.title);
        content = (EditText)rootView.findViewById(R.id.content);
        icon = rootView.findViewById(R.id.user_image);
        ratingGroup = rootView.findViewById(R.id.ratingGroup);
        rating_good = rootView.findViewById(R.id.rating_good);
        rating_ok = rootView.findViewById(R.id.rating_ok);
        rating_bad = rootView.findViewById(R.id.rating_bad);
        postBtn = rootView.findViewById(R.id.postBtn);
        cancelBtn = rootView.findViewById(R.id.cancelBtn);

        title.setText("REVIEW OF " + restaurantName);

        //db
        db = FirebaseFirestore.getInstance();


        //Validation
        postBtn.setEnabled(false);
        checkPost = new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                postBtn.setEnabled(checkReadiness());
                if(checkReadiness()){
                    postBtn.setAlpha(1);
                }
                else {
                    postBtn.setAlpha((float) 0.2);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        getDataFromDB();

        content.addTextChangedListener(checkPost);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("placeId", restaurantId);
                Navigation.findNavController(v).navigate(R.id.action_nav_post_review_to_nav_detail,bundle);
            }
        });


        rating_good.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        rating_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        rating_bad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postReview();
            }
        });

        return rootView;
    }

    public void onRadioButtonClicked(View v){
        int radioId = ratingGroup.getCheckedRadioButtonId();

        RadioButton rating = v.findViewById(radioId);
        ratingText = rating.getText().toString();

        Log.d("radio", "Value is" + ratingText);

        if(ratingText.equals("GOOD")){
            rateSelected = 2;
        } else {
            if(ratingText.equals("OK")){
                rateSelected = 1;
            } else {
                rateSelected = 0;
            }
        }

        Log.d("Rate Value", "Value is " + rateSelected);

        postBtn.setEnabled(checkReadiness());
        if(checkReadiness()){
            postBtn.setAlpha(1);
        }
        else {
            postBtn.setAlpha((float) 0.2);
        }


    }

    private Boolean checkReadiness() {
        if (ratingGroup.getCheckedRadioButtonId() != -1){
            if (!content.getText().toString().matches("")) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    private void postReview () {
        postBtn.setAlpha((float)0.2);
        postBtn.setEnabled(false);

        review = new Review();
        review.setContent(content.getText().toString().trim());
        review.setRestaurantId(restaurantId);
        review.setRating(rateSelected);
        review.setUserID(userId);

        CollectionReference restaurants = db.collection("Restaurant");
        Query restaurantQuery = restaurants.whereEqualTo("place_id",restaurantId);

        restaurantQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful()){
                    if(!task.getResult().isEmpty()){

                        for (QueryDocumentSnapshot document : task.getResult()) {

                            Restaurant mRestaurant = document.toObject(Restaurant.class);

                            String rId = document.getId();

                            String updateRate;
                            int updateRatev;

                            if(rateSelected == 0){
                                updateRate = "rating_bad";
                                updateRatev = mRestaurant.getRating_bad() + 1;
                            } else {
                                if (rateSelected == 1){
                                    updateRate = "rating_ok";
                                    updateRatev = mRestaurant.getRating_ok() + 1;
                                } else {
                                    updateRate = "rating_good";
                                    updateRatev = mRestaurant.getRating_good() + 1;
                                }
                            }

                            db.collection("Restaurant").document(rId).update(updateRate,updateRatev).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    CollectionReference reviews = db.collection("Review");
                                    review.setCreated(Calendar.getInstance().getTime());

                                    reviews.add(review).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Bundle bundle = new Bundle();
                                            bundle.putString("placeId", restaurantId);
                                            Navigation.findNavController(getView()).navigate(R.id.action_nav_post_review_to_nav_detail,bundle);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getActivity(),"Failed to add review", Toast.LENGTH_SHORT);
                                        }
                                    });

                                }
                            });


                        }


                    } else {

                        Restaurant mRestaurant = new Restaurant();
                        mRestaurant.setRating_bad(0);
                        mRestaurant.setRating_ok(0);
                        mRestaurant.setRating_good(0);
                        if(rateSelected == 0){
                            mRestaurant.setRating_bad(1);
                        } else {
                            if (rateSelected == 1){
                                mRestaurant.setRating_ok(1);
                            } else {
                                mRestaurant.setRating_good(1);
                            }
                        }
                        mRestaurant.setPlace_id(restaurantId);
                        mRestaurant.setPlace_name(restaurantName);
                        mRestaurant.setAddress(address);
                        mRestaurant.setLat(lat);
                        mRestaurant.setLng(lng);
                        mRestaurant.setOperation(true);

                        db.collection("Restaurant").document(restaurantId).set(mRestaurant).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                CollectionReference reviews = db.collection("Review");
                                review.setCreated(Calendar.getInstance().getTime());

                                reviews.add(review).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Bundle bundle = new Bundle();
                                        bundle.putString("placeId", restaurantId);
                                        Navigation.findNavController(getView()).navigate(R.id.action_nav_post_review_to_nav_detail,bundle);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity(),"Failed to add review", Toast.LENGTH_SHORT);
                                    }
                                });
                            }
                        });


                    }

                } else {
                    Toast.makeText(getActivity(),"Failed to fetch restaurant",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void getDataFromDB () {
        CollectionReference users = db.collection("User");
        users.document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){

                        user = document.toObject(User.class);


                        if(user.getIcon()!=null){
                            Picasso.with(getContext())
                                    .load(user.getIcon())
                                    .fit()
                                    .centerCrop()
                                    .into(icon);
                        }

                    }



                } else {
                    Log.d("GetData", "Failed!");
                }


            }
        });

    }

}
