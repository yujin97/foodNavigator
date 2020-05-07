package eugene.hku.foodnavigator.restaurantDetail;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import eugene.hku.foodnavigator.R;
import eugene.hku.foodnavigator.dataClass.PriceLevel;
import eugene.hku.foodnavigator.dataClass.Restaurant;
import eugene.hku.foodnavigator.dataClass.Type;
import eugene.hku.foodnavigator.dataClass.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class UpdateDetailFragment extends Fragment {
    //UI
    private TextView title;
    private EditText description;
    private CircleImageView icon;
    private TextWatcher checkPost;
    private Button postBtn;
    private TextView cancelBtn;
    private RadioGroup opGroup;
    private RadioButton op_yes;
    private RadioButton op_no;
    private Spinner type;
    private Spinner price;

    //Data
    private User poster;
    private String userId;
    private String restaurantId;
    private String restaurantName;
    private String address;
    private double lat;
    private double lng;
    private Restaurant restaurant;
    private String opText;
    private Boolean opSelected;
    private Boolean recorded;
    private User user;
    ArrayAdapter<Type> adapter;
    ArrayAdapter<PriceLevel> pAdapter;

    //Database
    private FirebaseFirestore db;

    public UpdateDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_update_detail, container, false);

        SharedPreferences prefs = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userId = prefs.getString("userID", "noUserId");

        recorded = false;

        //passed by bundle
        restaurantId = getArguments().getString("placeId");
        restaurantName = getArguments().getString("placeName");
        address =getArguments().getString("address");
        lat = getArguments().getDouble("lat");
        lng = getArguments().getDouble("lng");

        //UI
        title = (TextView) rootView.findViewById(R.id.title);
        description = (EditText)rootView.findViewById(R.id.description);
        icon = rootView.findViewById(R.id.user_image);
        opGroup = rootView.findViewById(R.id.ratingGroup);
        op_yes = rootView.findViewById(R.id.op_yes);
        op_no = rootView.findViewById(R.id.op_no);
        postBtn = rootView.findViewById(R.id.postBtn);
        cancelBtn = rootView.findViewById(R.id.cancelBtn);
        type = (Spinner) rootView.findViewById(R.id.type);
        price = (Spinner) rootView.findViewById(R.id.price);

        title.setText("DESCRIPTION OF " + restaurantName);

        ArrayList<Type> typeList = new ArrayList<>();
        typeList.add(new Type(1,"Chinese Food"));
        typeList.add(new Type(2,"Western Food"));
        typeList.add(new Type(3,"Japanese Food"));
        typeList.add(new Type(4,"Indian Food"));
        typeList.add(new Type(5,"Others"));

        adapter = new ArrayAdapter<>(getActivity(), R.layout.type_drop_down_item, typeList);
        type.setAdapter(adapter);

        ArrayList<PriceLevel> priceList = new ArrayList<>();
        priceList.add(new PriceLevel(1,"Bellow $50"));
        priceList.add(new PriceLevel(2,"$51 - $100"));
        priceList.add(new PriceLevel(3,"$101 - $200"));
        priceList.add(new PriceLevel(4,"$201 - $500"));
        priceList.add(new PriceLevel(5,"Above $500"));

        pAdapter = new ArrayAdapter<>(getActivity(), R.layout.type_drop_down_item, priceList);
        price.setAdapter(pAdapter);




        //db
        db = FirebaseFirestore.getInstance();

        //Load Restaurant from DB
        getDataFromDB();
        loadRestaurant();

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

        description.addTextChangedListener(checkPost);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("placeId", restaurantId);
                Navigation.findNavController(v).navigate(R.id.action_nav_update_detail_to_nav_detail,bundle);
            }
        });


        op_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        op_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRestaurant();
            }
        });


        return rootView;
    }

    public void loadRestaurant() {
        db.collection("Restaurant").document(restaurantId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        restaurant = document.toObject(Restaurant.class);
                        description.setText(restaurant.getDescription());
                        if(restaurant.isOperation()){
                            op_yes.setChecked(true);
                        } else {
                            op_no.setChecked(true);
                        }
                        opSelected = restaurant.isOperation();
                        recorded = true;
                        if(restaurant.getType() != 0){
                            type.setSelection(restaurant.getType()-1);
                        }
                        if(restaurant.getPrice() != 0){
                            price.setSelection(restaurant.getPrice()-1);
                        }

                        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if(checkReadiness()){
                                    postBtn.setAlpha(1);
                                }
                                else {
                                    postBtn.setAlpha((float) 0.2);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        price.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if(checkReadiness()){
                                    postBtn.setAlpha(1);
                                }
                                else {
                                    postBtn.setAlpha((float) 0.2);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    } else {
                        Log.d("LoadRestaurant", "No Restaurant info yet");
                    }
                } else {
                    Toast.makeText(getActivity(),"Failed to fetch!", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private Boolean checkReadiness() {
        if (opGroup.getCheckedRadioButtonId() != -1){
            if (!description.getText().toString().matches("")) {
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

    public void onRadioButtonClicked(View v){
        int radioId = opGroup.getCheckedRadioButtonId();

        RadioButton rating = v.findViewById(radioId);
        opText = rating.getText().toString();

        Log.d("radio", "Value is" + opText);

        if(opText.equals("YES")){
            opSelected = true;
        } else {
            opSelected = false;
        }

        Log.d("Op Value", "Value is " + opSelected);

        postBtn.setEnabled(checkReadiness());
        if(checkReadiness()){
            postBtn.setAlpha(1);
        }
        else {
            postBtn.setAlpha((float) 0.2);
        }


    }

    public void updateRestaurant() {
        postBtn.setAlpha((float)0.2);
        postBtn.setEnabled(false);
        Type mType = (Type) type.getSelectedItem();
        PriceLevel mPrice = (PriceLevel) price.getSelectedItem();
        if(!recorded){
            restaurant = new Restaurant();
            restaurant.setPlace_name(restaurantName);
            restaurant.setPlace_id(restaurantId);
            restaurant.setAddress(address);
            restaurant.setLat(lat);
            restaurant.setLng(lng);
            restaurant.setRating_good(0);
            restaurant.setRating_bad(0);
            restaurant.setRating_ok(0);
            restaurant.setOperation(opSelected);
            restaurant.setDescription(description.getText().toString());
            restaurant.setType(mType.getIndex());
            restaurant.setPrice(mPrice.getIndex());

            db.collection("Restaurant").document(restaurantId).set(restaurant).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("create restaurant", "Succeed");
                    Bundle bundle = new Bundle();
                    bundle.putString("placeId", restaurantId);
                    Navigation.findNavController(getView()).navigate(R.id.action_nav_update_detail_to_nav_detail,bundle);
                }
            });
        } else {
            db.collection("Restaurant").document(restaurantId).update("description",description.getText().toString());
            db.collection("Restaurant").document(restaurantId).update("operation",opSelected);
            db.collection("Restaurant").document(restaurantId).update("type",mType.getIndex());
            db.collection("Restaurant").document(restaurantId).update("price",mPrice.getIndex());
            Bundle bundle = new Bundle();
            bundle.putString("placeId", restaurantId);
            Navigation.findNavController(getView()).navigate(R.id.action_nav_update_detail_to_nav_detail,bundle);
        }
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
