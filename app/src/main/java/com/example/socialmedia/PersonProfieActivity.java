package com.example.socialmedia;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfieActivity extends AppCompatActivity {
    private TextView userName,userProfName,userStatus,userCountry,userGender,userRelation,userDOB;
    private CircleImageView userProfImage;
    private DatabaseReference ProfileRef,FriendRequestsRef,FriendsRef;
    private FirebaseAuth mAuth;
    String receiverUserId,senderUserId;
    String currentUserId,Current_state,saveCurrentDate;
    private Button SendFriendRequestButton,DeclineFriendRequestButton;

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
        setContentView(R.layout.activity_person_profie);
        userProfImage = (CircleImageView)findViewById(R.id.person_profile_image);
        userName = (TextView) findViewById(R.id.person_profile_username);
        userProfName = (TextView) findViewById(R.id.person_profile_name);
        userStatus = (TextView) findViewById(R.id.person_profile_status);
        userCountry = (TextView) findViewById(R.id.person_profile_country);
        userGender = (TextView) findViewById(R.id.person_profile_gender);
        userRelation = (TextView) findViewById(R.id.person_profile_relationship_status);
        userDOB = (TextView) findViewById(R.id.person_profile_dob);
        Current_state = "not_friends";
        SendFriendRequestButton = (Button)findViewById(R.id.send_request_button);
        DeclineFriendRequestButton = (Button)findViewById(R.id.decline_request_button);
        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();
        receiverUserId = getIntent().getExtras().get("visitedUserId").toString();
        ProfileRef = FirebaseDatabase.getInstance().getReference().child("users");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendRequestsRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
        DeclineFriendRequestButton.setEnabled(false);
        if(!senderUserId.equals(receiverUserId)){
            SendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   if(Current_state=="not_friends"){
                       SendFriendRequest();
                   }
                   if(Current_state=="request_sent"){
                       CancelFriendRequest();
                   }
                   if(Current_state=="request_received"){
                       AcceptFriendRequest();
                   }
                   if(Current_state=="friends"){
                       UnfriendExistingFriend();
                   }


                }
            });
        }
        else{
            SendFriendRequestButton.setVisibility(View.INVISIBLE);
            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
        }
        ProfileRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
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
                    if(dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(userProfImage);
                    }
                    MaintainenceOfButtons();

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
        ProfileRef.child(senderUserId).child("userState").updateChildren(currentStateMap);
    }


    private void UnfriendExistingFriend() {
        FriendsRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    FriendsRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            SendFriendRequestButton.setEnabled(true);
                            Current_state = "not_friends";
                            SendFriendRequestButton.setText("Send Request");
                            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                            DeclineFriendRequestButton.setEnabled(false);
                        }
                    });

                }
            }
        });

    }

    private void AcceptFriendRequest() {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        FriendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    FriendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            FriendRequestsRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        FriendRequestsRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                SendFriendRequestButton.setEnabled(true);
                                                Current_state = "friends";
                                                SendFriendRequestButton.setText("Unfriend");
                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        });

                                    }
                                }
                            });


                        }
                    });

                }
            }
        });

    }


    private void CancelFriendRequest() {
        FriendRequestsRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    FriendRequestsRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            SendFriendRequestButton.setEnabled(true);
                            Current_state = "not_friends";
                            SendFriendRequestButton.setText("Send Request");
                            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                            DeclineFriendRequestButton.setEnabled(false);
                        }
                    });

                }
            }
        });

    }

    private void SendFriendRequest() {
        FriendRequestsRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    FriendRequestsRef.child(receiverUserId).child(senderUserId).child("request_type").setValue("received");

                }
            }
        });
    }

    private void MaintainenceOfButtons() {
        FriendRequestsRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiverUserId)){
                    String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                    if(request_type.equals("sent")){
                        SendFriendRequestButton.setEnabled(true);
                        Current_state = "request_sent";
                        SendFriendRequestButton.setText("Cancel Friend Request");
                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                        DeclineFriendRequestButton.setEnabled(false);
                    }
                    else if(request_type.equals("received")){
                        Current_state = "request_received";
                        SendFriendRequestButton.setText("Accept Friend Request");
                        DeclineFriendRequestButton.setVisibility(View.VISIBLE);
                        DeclineFriendRequestButton.setEnabled(true);
                        DeclineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelFriendRequest();
                            }
                        });
                    }
                }
                else{
                    FriendsRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(receiverUserId)){
                                Current_state = "friends";
                                SendFriendRequestButton.setText("Unfriend");
                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                DeclineFriendRequestButton.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
