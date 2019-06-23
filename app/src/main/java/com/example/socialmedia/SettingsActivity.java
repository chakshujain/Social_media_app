package com.example.socialmedia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private CircleImageView userProfImage;
    private EditText userName,userProfName,userStatus,userCountry,userGender,userRelation,userDOB;
    private Button UpdateAccountSettingsButton;
    private FirebaseAuth mAuth;
    private DatabaseReference SettingsRef,PostsUpdateRef,UsersRef;
    String currentUserId,downloadUrl;
    private static final int gallery_pick = 1;
    private Uri imageUri;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;
    Query postsupdate;

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateUserStatus("online");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus("online");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mtoolbar = (Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        SettingsRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");
//        PostsUpdateRef = FirebaseDatabase.getInstance().getReference().child("Posts");
//        postsupdate = PostsUpdateRef.orderByChild("uid").equalTo(currentUserId);
        userProfImage = (CircleImageView)findViewById(R.id.settings_profile_image);
        userName = (EditText)findViewById(R.id.settings_username);
        userProfName = (EditText)findViewById(R.id.settings_profile_name);
        userStatus = (EditText)findViewById(R.id.settings_status);
        userCountry = (EditText)findViewById(R.id.settings_country);
        userGender = (EditText)findViewById(R.id.settings_gender);
        userRelation = (EditText)findViewById(R.id.settings_relationship_status);
        userDOB = (EditText)findViewById(R.id.settings_dob);
        UpdateAccountSettingsButton = (Button)findViewById(R.id.update_account_settings_button);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profileImages");
        loadingBar = new ProgressDialog(this);


        SettingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    String myUsername = dataSnapshot.child("username").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String mydob = dataSnapshot.child("dob").getValue().toString();
                    String mygender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationshipStatus = dataSnapshot.child("relationship_status").getValue().toString();
                    String myStatus = dataSnapshot.child("status").getValue().toString();
                    String myFullname = dataSnapshot.child("fullname").getValue().toString();
                    if(dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(userProfImage);
                    }
                    userName.setText(myUsername);
                    userCountry.setText(myCountry);
                    userDOB.setText(mydob);
                    userGender.setText(mygender);
                    userRelation.setText(myRelationshipStatus);
                    userStatus.setText(myStatus);
                    userProfName.setText(myFullname);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        UpdateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingBar.setTitle("Account Information");
                loadingBar.setMessage("Please wait, while we are updating your account information...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);
                String username = userName.getText().toString();
                String profilename = userProfName.getText().toString();
                String gender = userGender.getText().toString();
                String relationship = userRelation.getText().toString();
                String dob = userDOB.getText().toString();
                String status = userStatus.getText().toString();
                String country = userCountry.getText().toString();
                ValidateUserInfo(username, profilename, gender, relationship, dob, status, country);

            }
        });
        userProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,gallery_pick);

            }
        });

    }
    public void updateUserStatus(String state){
        String saveCurrentDate,saveCurrentTime;
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:aa");
        saveCurrentTime = currentTime.format(calFordTime.getTime());
        Map currentStateMap = new HashMap();
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);
        UsersRef.child(currentUserId).child("userState").updateChildren(currentStateMap);
    }

//    StoringImageToFirebaseStorage();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==gallery_pick && resultCode==RESULT_OK && data!=null) {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
            if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
            {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                imageUri = result.getUri();
                if (resultCode == RESULT_OK) {
                    userProfImage.setImageURI(imageUri);

                }
        }
    }

    private void ValidateUserInfo(String username, final String profilename, String gender, String relationship, String dob, String status, String country) {
        final HashMap usermap = new HashMap();
        usermap.put("username",username);
        usermap.put("country",country);
        usermap.put("dob",dob);
        usermap.put("status",status);
        usermap.put("relationship_status",relationship);
        usermap.put("gender",gender);
        usermap.put("fullname",profilename);


        final StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");
        if(imageUri!=null) {
            filePath.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downUri = task.getResult();
                        downloadUrl = downUri.toString();
                        Toast.makeText(SettingsActivity.this, "Image stored in database successfully", Toast.LENGTH_SHORT).show();
                        if(downloadUrl!=null) {
                            usermap.put("profileimage", downloadUrl);
                        }
//                        postsupdate.addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                if(dataSnapshot.exists()){
//                                    for(DataSnapshot child: dataSnapshot.getChildren()){
//                                        String postkey = child.getRef().getKey();
//                                        PostsUpdateRef.child(postkey).child("fullname").setValue(profilename);
//                                        PostsUpdateRef.child(postkey).child("profileimage").setValue(downloadUrl);
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                            }
//                        });
                        SettingsRef.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if(task.isSuccessful()){
                                    SendUserToMainActivity();
                                    Toast.makeText(SettingsActivity.this, "Successfully updated Account Information", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                                else{
                                    Toast.makeText(SettingsActivity.this, "Failed to update your Account Information", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });

                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SettingsActivity.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
        else{
//            postsupdate.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    if(dataSnapshot.exists()){
//                        for(DataSnapshot child: dataSnapshot.getChildren()){
//                            String postkey = child.getRef().getKey();
//                            PostsUpdateRef.child(postkey).child("fullname").setValue(profilename);
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
                SettingsRef.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Successfully updated Account Information", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else{
                        Toast.makeText(SettingsActivity.this, "Failed to update your Account Information", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });

        }


    }
   private void StoringImageToFirebaseStorage() {

       }



    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
