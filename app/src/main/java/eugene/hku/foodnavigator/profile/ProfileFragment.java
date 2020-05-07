package eugene.hku.foodnavigator.profile;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import eugene.hku.foodnavigator.Login.LoginActivity;
import eugene.hku.foodnavigator.R;
import eugene.hku.foodnavigator.dataClass.Restaurant;
import eugene.hku.foodnavigator.dataClass.User;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private TextView username;
    private Button editBtn;
    private Button confirmBtn;
    private Button logoutBtn;
    private CircleImageView icon;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private User user;
    private String userID;

    private Uri imageUri;

    private Intent loginIntent;

    private static final int PICK_IMAGE_REQUEST = 1;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        loginIntent = new Intent(getContext(), LoginActivity.class);
        SharedPreferences prefs = getActivity().getSharedPreferences("userInfo", MODE_PRIVATE);
        userID = prefs.getString("userID", "noUserId");

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("UserPic");
        username = (TextView) rootView.findViewById(R.id.username);
        editBtn = (Button) rootView.findViewById(R.id.editBtn);
        confirmBtn = (Button) rootView.findViewById(R.id.confirmBtn);
        confirmBtn.setEnabled(false);
        logoutBtn = (Button) rootView.findViewById(R.id.logoutBtn);

        icon = (CircleImageView) rootView.findViewById(R.id.user_image);

        getDataFromDB();

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                openFileChooser();
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getActivity().getSharedPreferences("userInfo", MODE_PRIVATE);
                pref.edit().clear().commit();
                startActivity(loginIntent);
                getActivity().finish();

            }
        });


        return rootView;
    }

    public void getDataFromDB () {
        CollectionReference users = db.collection("User");
        users.document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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

                    username.setText(user.getUserName());


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
                                        db.collection("User").document(userID).update("icon",imageUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("UserUpdate", "Works as charm!");

                                            }
                                        });



                                        Navigation.findNavController(getView()).navigate(R.id.nav_profile);
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




}
