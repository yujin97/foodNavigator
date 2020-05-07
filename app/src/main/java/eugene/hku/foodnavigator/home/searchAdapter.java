package eugene.hku.foodnavigator.home;

import android.content.Context;
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

public class searchAdapter extends RecyclerView.Adapter<homeHolder> {
    private Context c;
    private ArrayList<RestaurantSimple> model;
    private FirebaseFirestore db;

    public searchAdapter(Context c, ArrayList<RestaurantSimple> model) {
        this.c = c;
        this.model = model;
    }

    @NonNull
    @Override
    public homeHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.restaurant_list_item,viewGroup,false);

        return new homeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final homeHolder myHolder, final int i) {
        db = FirebaseFirestore.getInstance();
        CollectionReference restaurants = db.collection("Restaurant");
        restaurants.document(model.get(i).getPlace_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        Restaurant mRestaurant = document.toObject(Restaurant.class);
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

                    } else {
                        Log.d("get Restaurant", model.get(i).getPlace_name() + "is not recorded");
                    }

                } else {
                    Log.d("GetData", "Failed!");
                }
            }
        });


        myHolder.mName.setText(model.get(i).getPlace_name());
        myHolder.mVicinity.setText(model.get(i).getVicinity());
        myHolder.mDistance.setText("Distance: "+model.get(i).getDistance()+"m");



        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("placeId", model.get(i).getPlace_id());
                bundle.putInt("distance",model.get(i).getDistance());
                Navigation.findNavController(v).navigate(R.id.action_nav_search_to_nav_detail,bundle);
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
