package com.example.socialmedia;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class CommentsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef,CommentRef;

    private ImageButton PostCommentButton;
    private EditText CommentInputText;
    private RecyclerView CommentList;
    private String PostKey;
    private String CurrentUserId,saveCurrentDate,saveCurrentTime,postRandomName;

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
        setContentView(R.layout.activity_comments);
        UserRef = FirebaseDatabase.getInstance().getReference().child("users");

        CurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CommentList = (RecyclerView)findViewById(R.id.comment_list);
        CommentList.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentList.setLayoutManager(linearLayoutManager);
        CommentInputText = (EditText) findViewById(R.id.comment_input_text);
        PostCommentButton = (ImageButton)findViewById(R.id.post_comment_button);
        PostKey = getIntent().getExtras().get("PostKey").toString();
        CommentRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey).child("comments");
        DisplayAllComments();
        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UserRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String username = dataSnapshot.child(CurrentUserId).child("username").getValue().toString();
                        ValidateComment(username);
                        CommentInputText.setText("");
                    }



                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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
        UserRef.child(CurrentUserId).child("userState").updateChildren(currentStateMap);
    }

    private void DisplayAllComments(){
        FirebaseRecyclerOptions<Comments> options=new FirebaseRecyclerOptions.Builder<Comments>().setQuery(CommentRef,Comments.class).build();
        FirebaseRecyclerAdapter<Comments,CommentsViewholder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Comments,CommentsViewholder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull CommentsActivity.CommentsViewholder holder, int position, @NonNull Comments model) {
                try {
                    holder.username.setText(model.getUsername());
                    holder.time.setText(" " + model.getTime());
                    holder.date.setText(" " + model.getDate());
                    holder.comment_text.setText(model.getComment());
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }

            @NonNull
            @Override
            public CommentsViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout,parent,false);
                CommentsViewholder viewHolder=new CommentsViewholder(view);
                return viewHolder;
            }
        };
        CommentList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class CommentsViewholder extends RecyclerView.ViewHolder{
        TextView username,date,time,comment_text;
        public CommentsViewholder(View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.comment_username);
            date=itemView.findViewById(R.id.comment_date);
            time=itemView.findViewById(R.id.comment_time);
            comment_text=itemView.findViewById(R.id.comment_text);
        }
    }
    private void ValidateComment(String username){
        String commentText = CommentInputText.getText().toString();
        if(TextUtils.isEmpty(commentText)){
            Toast.makeText(this, "Please write text to comment", Toast.LENGTH_SHORT).show();
        }
        else{
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            saveCurrentTime = currentTime.format(calFordTime.getTime());

            postRandomName = CurrentUserId + saveCurrentDate + saveCurrentTime;

            HashMap commentMap = new HashMap();
            commentMap.put("uid",CurrentUserId);
            commentMap.put("date",saveCurrentDate);
            commentMap.put("time",saveCurrentTime);
            commentMap.put("comment",commentText);
            commentMap.put("username",username);
           CommentRef.child(postRandomName).updateChildren(commentMap).addOnCompleteListener(new OnCompleteListener() {
               @Override
               public void onComplete(@NonNull Task task) {
                   if(task.isSuccessful()){
                       Toast.makeText(CommentsActivity.this, "Commented", Toast.LENGTH_SHORT).show();
                   }
                   else{
                       Toast.makeText(CommentsActivity.this, "Error occured: Try again...", Toast.LENGTH_SHORT).show();
                   }
               }
           });

        }
    }
}
