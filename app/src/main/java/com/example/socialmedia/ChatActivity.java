package com.example.socialmedia;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private Toolbar chatToolbar;
    private RecyclerView UserMessagesList;
    private List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private EditText InputMessage;
    private ImageButton SendImageFileButton,SendMessageButton;
    private String messageReceiverId,messageReceiverFullName;
    private TextView receiverName,receiverLastSeen;
    private CircleImageView receiverProfileImage;
    private DatabaseReference RootRef,UsersRef;
    private FirebaseAuth mAuth;
    String messageSenderId,saveCurrentDate,saveCurrentTime;

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
        setContentView(R.layout.activity_chat);
        chatToolbar = (Toolbar)findViewById(R.id.chat_bar_layout);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);
        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");
        messageSenderId = mAuth.getCurrentUser().getUid();
        UserMessagesList = (RecyclerView)findViewById(R.id.messages_list);
        InputMessage = (EditText)findViewById(R.id.input_message);
        SendImageFileButton = (ImageButton)findViewById(R.id.send_image_file_button);
        SendMessageButton = (ImageButton)findViewById(R.id.send_message_button);
        messagesAdapter = new MessagesAdapter(messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        UserMessagesList.hasFixedSize();
        UserMessagesList.setLayoutManager(linearLayoutManager);
        UserMessagesList.setAdapter(messagesAdapter);
        receiverName = (TextView)findViewById(R.id.custom_profile_name);
        receiverLastSeen = (TextView)findViewById(R.id.custom_user_last_seen);
        receiverProfileImage = (CircleImageView) findViewById(R.id.custom_profile_image);
        messageReceiverFullName = getIntent().getExtras().get("fullname").toString();
        messageReceiverId = getIntent().getExtras().get("visitedUserId").toString();
        receiverName.setText(messageReceiverFullName);
        RootRef.child("users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileimage")){
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        final String type = dataSnapshot.child("userState").child("type").getValue().toString();
                        final String lastDate = dataSnapshot.child("userState").child("date").getValue().toString();
                        final String lastTime = dataSnapshot.child("userState").child("time").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(receiverProfileImage);
                        if(type.equals("online")){
                            receiverLastSeen.setText("Onine");
                        }
                        else{
                            receiverLastSeen.setText("Last seen: "+ lastTime + " " + lastDate);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
        FetchMessages();

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
        UsersRef.child(messageSenderId).child("userState").updateChildren(currentStateMap);
    }

    private void FetchMessages() {
        RootRef.child("Messages").child(messageSenderId).child(messageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()) {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    messagesList.add(messages);

                    messagesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendMessage() {
        String messageText = InputMessage.getText().toString();
        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "Please write a message first...", Toast.LENGTH_SHORT).show();
        }
        else{
            String message_sender_ref = "Messages/" + messageSenderId + "/" + messageReceiverId;
            String message_receiver_ref = "Messages/" + messageReceiverId + "/" + messageSenderId;
            DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderId).child(messageReceiverId).push();
            String message_push_id = user_message_key.getKey();
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:aa");
            saveCurrentTime = currentTime.format(calFordTime.getTime());
            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref + "/" + message_push_id,messageTextBody);
            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id,messageTextBody);
            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        InputMessage.setText("");
                    }
                    else{
                        Toast.makeText(ChatActivity.this,task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        InputMessage.setText("");
                    }
                }
            });

        }
    }
}
