package com.example.socialmedia;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private TextView userName,userProfName,userStatus,userCountry,userGender,userRelation,userDOB;
    private CircleImageView userProfImage;
    private DatabaseReference ProfileRef,FriendsRef,PostsRef,UsersRef;
    private FirebaseAuth mAuth;
    String currentUserId;
    private Button myPostsButton,myFriendsButton;
    private int friendsCount=0,postsCount=0;

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
        setContentView(R.layout.activity_profile);
        userProfImage = (CircleImageView)findViewById(R.id.profile_image);
        userName = (TextView) findViewById(R.id.profile_username);
        userProfName = (TextView) findViewById(R.id.profile_name);
        userStatus = (TextView) findViewById(R.id.profile_status);
        userCountry = (TextView) findViewById(R.id.profile_country);
        userGender = (TextView) findViewById(R.id.profile_gender);
        userRelation = (TextView) findViewById(R.id.profile_relationship_status);
        userDOB = (TextView) findViewById(R.id.profile_dob);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        myFriendsButton = (Button)findViewById(R.id.my_friends_button);
        myPostsButton = (Button)findViewById(R.id.my_posts_button);
        ProfileRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");



        ProfileRef.addValueEventListener(new ValueEventListener() {
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
                    userCountry.setText("Country: " + myCountry);
                    userDOB.setText("DOB: "+mydob);
                    userGender.setText("Gender: "+mygender);
                    userRelation.setText("Relationship Status: "+myRelationshipStatus);
                    userStatus.setText(myStatus);
                    userProfName.setText(myFullname);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        myFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToFriendsActivity();
            }
        });

        myPostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToMyPostsActivity();
            }
        });
        PostsRef.orderByChild("uid").startAt(currentUserId).endAt(currentUserId + "\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    postsCount = (int)dataSnapshot.getChildrenCount();
                    myPostsButton.setText(" My Posts" + "(" + postsCount + ") ");
                }
                else{
                    myPostsButton.setText(" My Posts(0)");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        FriendsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    friendsCount = (int)dataSnapshot.getChildrenCount();
                    myFriendsButton.setText( " My Friends"+ "(" + friendsCount + ") " );
                }
                else{
                    myFriendsButton.setText(" My Friends(0) " );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

    private void SendUserToMyPostsActivity(){
        Intent mypostsIntent = new Intent(ProfileActivity.this,MyPostsActivity.class);
        startActivity(mypostsIntent);
    }
    private void SendUserToFriendsActivity() {
        Intent friendsIntent = new Intent(ProfileActivity.this,FriendsActivity.class);
        startActivity(friendsIntent);
    }
}
