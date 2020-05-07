package eugene.hku.foodnavigator.favourite;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import eugene.hku.foodnavigator.R;
import eugene.hku.foodnavigator.dataClass.Restaurant;
import eugene.hku.foodnavigator.dataClass.RestaurantSimple;
import eugene.hku.foodnavigator.home.homeHolder;

public class favouriteAdapter extends RecyclerView.Adapter<favouriteHolder> {
    private Context c;
    private ArrayList<RestaurantSimple> model;
    private FirebaseFirestore db;

    public favouriteAdapter(Context c, ArrayList<RestaurantSimple> model) {
        this.c = c;
        this.model = model;
    }

    @NonNull
    @Override
    public favouriteHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.restaurant_list_item,viewGroup,false);

        return new favouriteHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final favouriteHolder myHolder, final int i) {
        SharedPreferences prefs = c.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final double lat = (double)prefs.getFloat("lat", 0);
        final double lng = (double)prefs.getFloat("lng", 0);
        db = FirebaseFirestore.getInstance();
        CollectionReference restaurants = db.collection("Restaurant");
        restaurants.document(model.get(i).getPlace_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        final Restaurant mRestaurant = document.toObject(Restaurant.class);
                        if(mRestaurant.getIcon()!=null){
                            Picasso.with(c)
                                    .load(mRestaurant.getIcon())
                                    .fit()
                                    .centerCrop()
                                    .into(myHolder.mImageView);
                        }

                        if(mRestaurant.getDescription() != null) {
                            myHolder.mDescription.setText(mRestaurant.getDescription());
                        }

                        myHolder.mName.setText(mRestaurant.getPlace_name());
                        myHolder.mVicinity.setText(mRestaurant.getAddress());
                        myHolder.mDistance.setText("Distance: "+mRestaurant.calDistance(lat,lng)+"m");

                        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bundle bundle = new Bundle();
                                bundle.putString("placeId", model.get(i).getPlace_id());
                                bundle.putInt("distance", mRestaurant.calDistance(lat,lng));
                                Navigation.findNavController(v).navigate(R.id.action_nav_favourite_to_nav_detail,bundle);
                            }
                        });

                    } else {
                        Log.d("get Restaurant", model.get(i).getPlace_name() + "is not recorded");
                    }

                } else {
                    Log.d("GetData", "Failed!");
                }
            }
        });





    }


    @Override
    public int getItemCount() {
        return model.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}
