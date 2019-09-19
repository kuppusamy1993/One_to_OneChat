package com.kuppu.one_to_onechat.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kuppu.one_to_onechat.Adapter.UserAdapter;
import com.kuppu.one_to_onechat.Model.Chat;
import com.kuppu.one_to_onechat.Model.Chatlist;
import com.kuppu.one_to_onechat.Model.User;
import com.kuppu.one_to_onechat.R;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;


public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User>mUser;
    FirebaseUser fuser;
    DatabaseReference reference;
    private List<Chatlist>userList;

    public ChatFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_chat,container,false);

        recyclerView=view.findViewById(R.id.recycler_view_chat);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser= FirebaseAuth.getInstance().getCurrentUser();
        userList=new ArrayList<>();

        reference=FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            userList.clear();
            for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                Chatlist chatlist=snapshot.getValue(Chatlist.class);
                userList.add(chatlist);
                chatList();
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return view;
    }

    private void chatList() {
        mUser=new ArrayList<>();

        reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUser.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    User user=snapshot.getValue(User.class);
                    for (Chatlist chatlist:userList){
                        if (user.getId().equals(chatlist.getId())){
                            mUser.add(user);
                        }
                    }
                }
                userAdapter=new UserAdapter(getContext(),mUser,true);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


}
