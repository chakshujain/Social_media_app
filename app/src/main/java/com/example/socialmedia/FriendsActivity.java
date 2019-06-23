package com.example.socialmedia;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {
    private RecyclerView FriendsList;
    private DatabaseReference FriendsRef,UsersRef;
    private FirebaseAuth mAuth;
    private String online_user_id;

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
        setContentView(R.layout.activity_friends);
        FriendsList = (RecyclerView)findViewById(R.id.friends_list);
        FriendsList.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        FriendsList.setLayoutManager(linearLayoutManager);
        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");
        DisplayAllFriends();

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
        UsersRef.child(online_user_id).child("userState").updateChildren(currentStateMap);
    }


    private void DisplayAllFriends() {
        FirebaseRecyclerOptions<Friends> options=new FirebaseRecyclerOptions.Builder<Friends>().
                setQuery(FriendsRef, Friends.class).build(); //query build past the query to FirebaseRecyclerAdapter
        FirebaseRecyclerAdapter<Friends, FriendsActivity.FriendsViewHolder> adapter=new FirebaseRecyclerAdapter<Friends, FriendsActivity.FriendsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsActivity.FriendsViewHolder holder, final int position, @NonNull Friends model)
            {
                holder.date.setText("Friends since: " + model.getDate());


                final String userIds = getRef(position).getKey();
                UsersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            final String fullname = dataSnapshot.child("fullname").getValue().toString();
                            final String type;
                            if(dataSnapshot.hasChild("userState")){
                                type = dataSnapshot.child("userState").child("type").getValue().toString();
                                if(type.equals("online")){
                                    holder.greenDot.setVisibility(View.VISIBLE);
                                }
                                else{
                                    holder.greenDot.setVisibility(View.INVISIBLE);
                                }
                            }
                            holder.fullname.setText(fullname);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CharSequence options[] = new CharSequence[]{
                                            "View" + fullname + "'s Profile",
                                            "Send Message"
                                    };
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select option");
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which==0){
                                                Intent personprofileIntent = new Intent(FriendsActivity.this,PersonProfieActivity.class);
                                                personprofileIntent.putExtra("visitedUserId",userIds);
                                                startActivity(personprofileIntent);
                                            }
                                            if(which==1){
                                                Intent ChatIntent = new Intent(FriendsActivity.this,ChatActivity.class);
                                                ChatIntent.putExtra("visitedUserId",userIds);
                                                ChatIntent.putExtra("fullname",fullname);
                                                startActivity(ChatIntent);

                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });
                            if(dataSnapshot.hasChild("profileimage")) {
                                final String image = dataSnapshot.child("profileimage").getValue().toString();
                                Picasso.get().load(image).into(holder.profileimage);
                            }
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
            @NonNull
            @Override
            public FriendsActivity.FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_posts_display_layout,viewGroup,false);

                FriendsActivity.FriendsViewHolder viewHolder=new FriendsActivity.FriendsViewHolder(view);
                return viewHolder;
            }
        };

        FriendsList.setAdapter(adapter);
        adapter.startListening();

    }
    public class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView fullname, date;
        CircleImageView profileimage;
        ImageView greenDot;

        public FriendsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            fullname = itemView.findViewById(R.id.search_fullname);
            date = itemView.findViewById(R.id.search_status);
            profileimage = itemView.findViewById(R.id.search_image);
            greenDot = itemView.findViewById(R.id.green_dot);
        }
    }
}
