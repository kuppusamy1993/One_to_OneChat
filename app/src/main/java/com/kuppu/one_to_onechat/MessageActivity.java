package com.kuppu.one_to_onechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kuppu.one_to_onechat.Adapter.MessageAdapter;
import com.kuppu.one_to_onechat.Model.Chat;
import com.kuppu.one_to_onechat.Model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;

    ImageButton btn_send;
    EditText txt_send;

    MessageAdapter messageAdapter;
    List<Chat>mchat;

    RecyclerView recyclerView;

    FirebaseUser fuser;
    DatabaseReference reference;

    Intent intent;

    ValueEventListener seenlistener;
   String userid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(MessageActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });


recyclerView=findViewById(R.id.recycler_view);
LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
linearLayoutManager.setStackFromEnd(true);
recyclerView.setLayoutManager(linearLayoutManager);


        profile_image=findViewById(R.id.profile_image);
        username=findViewById(R.id.user_name);
        btn_send=findViewById(R.id.btn_send);
        txt_send=findViewById(R.id.text_send);

        intent=getIntent();

        userid=intent.getStringExtra("userid");
        fuser= FirebaseAuth.getInstance().getCurrentUser();
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg=txt_send.getText().toString();
                if(!msg.equals("")){
                    sendMessage(fuser.getUid(),userid,msg);
                }else {
                    Toast.makeText(MessageActivity.this, "You can't send a empty message", Toast.LENGTH_SHORT).show();
                }
                txt_send.setText("");
            }
        });


        reference= FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user=dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if(user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher_round);
                }else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }
                readMessages(fuser.getUid(),userid,user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        seenMessage(userid);
    }

    private void seenMessage(final String userid){
        reference=FirebaseDatabase.getInstance().getReference("Chats");
        seenlistener=reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                Chat chat=snapshot.getValue(Chat.class);

                if (chat.getReceiver().equals(fuser.getUid())&& chat.getSender().equals(userid)){
                    HashMap<String,Object>hashMap=new HashMap<>();
                    hashMap.put("isseen",true);
                    snapshot.getRef().updateChildren(hashMap);
                }
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void sendMessage(String sender,String receiver,String message){
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();

        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isseen",false);

        databaseReference.child("Chats").push().setValue(hashMap);

        // add user to chat fragment
        final DatabaseReference chatref=FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);
        chatref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatref.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void readMessages(final String myid, final String userid, final String imageurl){
        mchat=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Chat chat=snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                    chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mchat.add(chat);
                    }
                    messageAdapter=new MessageAdapter(getApplicationContext(),mchat,imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void status(String status){
        reference=FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("status",status);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }


    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenlistener);
        status("offline");

    }
}
