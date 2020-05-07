package eugene.hku.foodnavigator.restaurantDetail;


import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import eugene.hku.foodnavigator.R;
import eugene.hku.foodnavigator.dataClass.Restaurant;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantPicUpdateFragment extends Fragment {

    private TextView restaurantName;
    private Button editBtn;
    private Button confirmBtn;
    private CircleImageView icon;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private Restaurant restaurant;
    private String projectKey;
    private String userID;
    private String restaurantID;
    private String name;
    private String address;
    private double lat;
    private double lng;

    private Uri imageUri;

    private static final int PICK_IMAGE_REQUEST = 1;


    public RestaurantPicUpdateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_restaurant_pic_update, container, false);

        storageRef = FirebaseStorage.getInstance().getReference("restaurantPic");

        Bundle bundle = getArguments();
        restaurantID = bundle.getString("restaurantId");
        name = bundle.getString("restaurantName");
        address =getArguments().getString("address");
        lat = getArguments().getDouble("lat");
        lng = getArguments().getDouble("lng");
        db = FirebaseFirestore.getInstance();

        restaurantName = (TextView) rootView.findViewById(R.id.restaurantName);
        editBtn = (Button) rootView.findViewById(R.id.editBtn);
        confirmBtn = (Button) rootView.findViewById(R.id.confirmBtn);
        confirmBtn.setEnabled(false);
        icon = (CircleImageView) rootView.findViewById(R.id.user_image);

        getDataFromDB();

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                openFileChooser();
            }
        });



        return rootView;
    }

    public void getDataFromDB () {
        CollectionReference restaurants = db.collection("Restaurant");
        Query restaurantQuery = restaurants.whereEqualTo("place_id",restaurantID);
        restaurantQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(!task.getResult().isEmpty()){

                        Restaurant mRestaurant = new Restaurant();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            mRestaurant = document.toObject(Restaurant.class);
                        }

                        if(mRestaurant.getIcon()!=null){
                            Picasso.with(getContext())
                                    .load(mRestaurant.getIcon())
                                    .fit()
                                    .centerCrop()
                                    .into(icon);
                        }

                    }

                    restaurantName.setText(name);


                } else {
                    Log.d("GetData", "Failed!");
                }


            }
        });

    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        editBtn.setEnabled(false);
        editBtn.setAlpha((float)0.2);
        confirmBtn.setEnabled(false);
        confirmBtn.setAlpha((float)0.2);
        confirmBtn.setText("LOADING");
        StorageReference fileReference = storageRef.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));
        fileReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if (taskSnapshot.getMetadata() != null) {
                            if (taskSnapshot.getMetadata().getReference() != null) {
                                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String imageUrl = uri.toString();
                                        CollectionReference restaurants = db.collection("Restaurant");
                                        Query restaurantQuery = restaurants.whereEqualTo("place_id",restaurantID);
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



                                                            db.collection("Restaurant").document(rId).update("icon",imageUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d("RestaurantUpdate", "Works as charm!");
                                                                    Bundle bundle = new Bundle();
                                                                    bundle.putString("placeId", restaurantID);
                                                                    Navigation.findNavController(getView()).navigate(R.id.action_nav_rest_pic_update_to_nav_detail, bundle);

                                                                }
                                                            });


                                                        }


                                                    } else {

                                                        Restaurant mRestaurant = new Restaurant();
                                                        mRestaurant.setRating_bad(0);
                                                        mRestaurant.setRating_ok(0);
                                                        mRestaurant.setRating_good(0);

                                                        mRestaurant.setPlace_id(restaurantID);
                                                        mRestaurant.setPlace_name(name);
                                                        mRestaurant.setOperation(true);
                                                        mRestaurant.setAddress(address);
                                                        mRestaurant.setLat(lat);
                                                        mRestaurant.setLng(lng);
                                                        mRestaurant.setIcon(imageUrl);

                                                        db.collection("Restaurant").document(restaurantID).set(mRestaurant).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d("New Restaurant", "Works as charm!");
                                                                Bundle bundle = new Bundle();
                                                                bundle.putString("placeId", restaurantID);
                                                                Navigation.findNavController(getView()).navigate(R.id.action_nav_rest_pic_update_to_nav_detail, bundle);
                                                            }
                                                        });


                                                    }

                                                } else {
                                                    Toast.makeText(getActivity(),"Failed to fetch restaurant",Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });




                                    }
                                });
                            }
                        }


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT);
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getActivity(),"Uploading",Toast.LENGTH_SHORT);
                    }
                });
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();

            Picasso.with(getActivity()).load(imageUri).into(icon);
            confirmBtn.setAlpha(1);
            confirmBtn.setEnabled(true);
            confirmBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadFile();
                }
            });
        }
    }

}
