package com.example.socialmedia;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>{
    private List<Messages> userMessagesList;

    private FirebaseAuth mAuth;
    private DatabaseReference usersDatabaseRef;

    public MessagesAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView SenderMessageText,ReceiverMessageTxt;
        public CircleImageView receiverProfileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            SenderMessageText = (TextView)itemView.findViewById(R.id.sender_message_text);
            ReceiverMessageTxt = (TextView)itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView)itemView.findViewById(R.id.message_profile_image);
        }
    }

    @NonNull
    @Override
    public MessagesAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_layout_of_users,viewGroup,false);
        mAuth = FirebaseAuth.getInstance();
        MessageViewHolder viewHolder=new MessageViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessagesAdapter.MessageViewHolder messageViewHolder, int i) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);
        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();
        usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(fromUserId);
        usersDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    if(dataSnapshot.hasChild("profileimage")){
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(messageViewHolder.receiverProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(fromMessageType.equals("text")){
            messageViewHolder.ReceiverMessageTxt.setVisibility(View.INVISIBLE);
            messageViewHolder.receiverProfileImage.setVisibility(View.INVISIBLE);
            if(fromUserId.equals(messageSenderId)){
                messageViewHolder.SenderMessageText.setBackgroundResource(R.drawable.sender_message_text_background);
                messageViewHolder.SenderMessageText.setTextColor(Color.WHITE);
                messageViewHolder.SenderMessageText.setGravity(Gravity.LEFT);
                messageViewHolder.SenderMessageText.setText(messages.getMessage());

            }
            else{
                messageViewHolder.SenderMessageText.setVisibility(View.INVISIBLE);
                messageViewHolder.ReceiverMessageTxt.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.ReceiverMessageTxt.setBackgroundResource(R.drawable.receiver_message_text_background);
                messageViewHolder.ReceiverMessageTxt.setTextColor(Color.WHITE);
                messageViewHolder.ReceiverMessageTxt.setGravity(Gravity.LEFT);
                messageViewHolder.ReceiverMessageTxt.setText(messages.getMessage());
            }
        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }


}