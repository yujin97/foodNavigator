package eugene.hku.foodnavigator.restaurantDetail;

import android.media.Image;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import eugene.hku.foodnavigator.R;

public class reviewHolder extends RecyclerView.ViewHolder {

    TextView username, content, mDate;
    de.hdodenhof.circleimageview.CircleImageView profile;
    ImageView rating;

    public reviewHolder(@NonNull View itemView) {
        super(itemView);
        this.username = itemView.findViewById(R.id.user);
        this.content = itemView.findViewById(R.id.description);
        this.mDate = itemView.findViewById(R.id.date);
        this.profile = itemView.findViewById(R.id.user_image);
        this.rating = itemView.findViewById(R.id.rating);
    }
}
