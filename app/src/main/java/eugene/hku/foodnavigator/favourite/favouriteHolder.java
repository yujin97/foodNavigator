package eugene.hku.foodnavigator.favourite;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import eugene.hku.foodnavigator.R;

public class favouriteHolder extends RecyclerView.ViewHolder{

    ImageView mImageView;
    TextView mName, mDescription, mVicinity, mDistance;

    public favouriteHolder(@NonNull View itemView) {
        super(itemView);
        this.mImageView = itemView.findViewById(R.id.imageIv);
        this.mName = itemView.findViewById(R.id.name);
        this.mDescription= itemView.findViewById(R.id.description);
        this.mVicinity = itemView.findViewById(R.id.vicinity);
        this.mDistance = itemView.findViewById(R.id.distance);
    }
}
