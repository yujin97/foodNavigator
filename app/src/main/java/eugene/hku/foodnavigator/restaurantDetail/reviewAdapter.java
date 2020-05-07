package eugene.hku.foodnavigator.restaurantDetail;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import eugene.hku.foodnavigator.R;
import eugene.hku.foodnavigator.dataClass.Review;
import eugene.hku.foodnavigator.dataClass.User;
import eugene.hku.foodnavigator.home.homeHolder;

public class reviewAdapter extends RecyclerView.Adapter<reviewHolder> {
    private Context c;
    private ArrayList<Review> model;
    private FirebaseFirestore db;
    private String userID;

    public reviewAdapter(Context c, ArrayList<Review> model) {
        this.c = c;
        this.model = model;
    }

    @NonNull
    @Override
    public reviewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.review_item,viewGroup,false);

        return new reviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final reviewHolder myHolder, final int i) {
        userID = model.get(i).getUserID();
        db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("User");
        users.document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        User user = document.toObject(User.class);
                        if(user.getIcon() != null) {
                            Picasso.with(c)
                                    .load(user.getIcon())
                                    .fit()
                                    .centerCrop()
                                    .into(myHolder.profile);
                        }
                        myHolder.username.setText(user.getUserName());
                        }
                    else {
                        Log.d("User", "No such user");
                    }
                } else {
                    Log.d("Load User", "Failed");
                }
            }
        });


        myHolder.content.setText(model.get(i).getContent());
        myHolder.mDate.setText(DateFormat.getDateTimeInstance().format(model.get(i).getCreated()));
        int rating = model.get(i).getRating();
        if(rating == 0) {
            int id = c.getResources().getIdentifier("rating_bad","drawable","eugene.hku.foodnavigator");
            myHolder.rating.setImageResource(id);
        } else {
            if(rating == 1){
                int id = c.getResources().getIdentifier("rating_ok","drawable","eugene.hku.foodnavigator");
                myHolder.rating.setImageResource(id);
            } else {
                int id = c.getResources().getIdentifier("rating_good","drawable","eugene.hku.foodnavigator");
                myHolder.rating.setImageResource(id);
            }
        }
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
