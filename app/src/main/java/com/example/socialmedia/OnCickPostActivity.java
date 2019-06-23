package com.example.socialmedia;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class OnCickPostActivity extends AppCompatActivity {
    private ImageView ClickPostImage;
    private TextView ClickPostDescription;
    private Button EditPostButton;
    private Button DeletePostButton;
    private String PostKey,description,image,currentUserId,database_user_id;
    private DatabaseReference ClickPostRef,UsersRef,deletedPostsCount;
    private FirebaseAuth mAuth;
    private long deleted_posts_count;

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
        setContentView(R.layout.activity_on_cick_post);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        ClickPostImage = (ImageView)findViewById(R.id.click_post_image);
        ClickPostDescription = (TextView)findViewById(R.id.click_post_description);
        EditPostButton = (Button)findViewById(R.id.edit_post_button);
        DeletePostButton = (Button)findViewById(R.id.delete_post_button);
        EditPostButton.setVisibility(View.INVISIBLE);
        DeletePostButton.setVisibility(View.INVISIBLE);
        PostKey = getIntent().getExtras().get("PostKey").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        deletedPostsCount = FirebaseDatabase.getInstance().getReference().child("Deleted Posts Count").child("value");
        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    description = dataSnapshot.child("description").getValue().toString();
                    image = dataSnapshot.child("postimage").getValue().toString();
                    database_user_id = dataSnapshot.child("uid").getValue().toString();
                    ClickPostDescription.setText(description);
                    Picasso.get().load(image).into(ClickPostImage);
                    if(currentUserId.equals(database_user_id)){
                        EditPostButton.setVisibility(View.VISIBLE);
                        DeletePostButton.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        DeletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeletePostFunction();
            }
        });
        EditPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditPostFunction(description);
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

    private void EditPostFunction(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(OnCickPostActivity.this);
        builder.setTitle("Edit Post");
        final EditText inputField = new EditText(OnCickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(OnCickPostActivity.this, "Post Updated successfully", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);

    }

    private void DeletePostFunction() {
        ClickPostRef.removeValue();
        deletedPostsCount.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    deleted_posts_count = Long.parseLong(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        deleted_posts_count = deleted_posts_count + 1;
        deletedPostsCount.setValue(deleted_posts_count);

        SendUserToMainActivity();
        Toast.makeText(this, "Post deleted successfully", Toast.LENGTH_SHORT).show();
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(OnCickPostActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
    }
}
