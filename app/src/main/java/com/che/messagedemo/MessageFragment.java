package com.che.messagedemo;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

public class MessageFragment extends Fragment {
    public static class MessageViewHolder extends RecyclerView.ViewHolder{
        RelativeLayout rlMsgDisCont;
        TextView textEmail, textMsg;
        ImageView imageMsg;

        public MessageViewHolder(View itemView){
            super(itemView);
            rlMsgDisCont= (RelativeLayout)itemView.findViewById(R.id.rlMsgDisplayContainer);
            textEmail= (TextView)itemView.findViewById(R.id.txtUserEmail);
            textMsg= (TextView)itemView.findViewById(R.id.txtMsg);
            imageMsg = (ImageView)itemView.findViewById(R.id.imgMsg);
        }
    }
    String pairedId, contactName, email;
    ImageView btnImg;
    AutoCompleteTextView etMsg;
    InitializationVariable initVariable = new InitializationVariable();
    // saved data used for profile use.
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    DatabaseReference mDatabaseRef= FirebaseDatabase.getInstance().getReference();
    DatabaseReference mMessageRef;
    private LinearLayoutManager mLinearLayoutManager;
    FirebaseRecyclerAdapter<DataBundle, MessageViewHolder> mFirebaseAdapter;


    public static int REQUEST_IMAGE_SUB = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.message_fragment, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rvMsg);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        //mLinearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        btnImg = (ImageView)view.findViewById(R.id.btnImage);
        etMsg = (AutoCompleteTextView) view.findViewById(R.id.etMsg);
        pairedId = MainActivity.pairedId;
        contactName = MainActivity.contactName;
        email = MainActivity.email;

        // initialising
        sharedPref = getContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        editor= sharedPref.edit();

        SnapshotParser<DataBundle> parser = new SnapshotParser<DataBundle>() {
            @Override
            public DataBundle parseSnapshot( DataSnapshot dataSnapshot) {


                return dataSnapshot.getValue(DataBundle.class);
            }
        };

        Query query =mDatabaseRef.child("DemoApp/Messages/"+pairedId+"/messages");

        FirebaseRecyclerOptions<DataBundle> options =
                new FirebaseRecyclerOptions.Builder<DataBundle>()
                        .setQuery(query, DataBundle.class)
                        .build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<DataBundle, MessageViewHolder>(options) {
            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new MessageViewHolder(inflater.inflate(R.layout.message_display, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(MessageViewHolder viewHolder,
                                            int position, DataBundle model) {
                viewHolder.textEmail.setVisibility(View.GONE);
                viewHolder.textMsg.setVisibility(View.GONE);
                viewHolder.imageMsg.setVisibility(View.GONE);
                if (model.getId()!= null) {
                    viewHolder.textEmail.setVisibility(View.VISIBLE);
                    String rewrite;
                    rewrite = model.getId();
                    boolean at = false;
                    for(int i=0; i <= model.getId().length(); i++){
                        if(!at){
                            if(rewrite.endsWith("@")){
                                rewrite= rewrite.substring(0, rewrite.length()-1);
                                at = true;
                            }else{
                                rewrite= rewrite.substring(0,rewrite.length()-1);
                            }
                        }

                    }
                    viewHolder.textEmail.setText("@"+rewrite);
                    if(model.getId().equals(MainActivity.userEmail)){
                        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                                viewHolder.rlMsgDisCont.getLayoutParams();
                        mlp.setMarginStart(150);
                        mlp.setMarginEnd(10);
                        viewHolder.rlMsgDisCont.setGravity(Gravity.END);
                    }else{
                        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                                viewHolder.rlMsgDisCont.getLayoutParams();
                        mlp.setMarginStart(10);
                        mlp.setMarginEnd(150);
                        viewHolder.rlMsgDisCont.setGravity(Gravity.START);
                    }
                    if(!model.getMsg().equals("")){
                        viewHolder.imageMsg.setVisibility(View.GONE);
                        viewHolder.textMsg.setVisibility(View.VISIBLE);
                        viewHolder.textMsg.setText(model.getMsg());
                    }
                    if(!model.getImg().equals("none")){
                        viewHolder.imageMsg.setVisibility(View.VISIBLE);
                        viewHolder.textMsg.setVisibility(View.GONE);
                        viewHolder.imageMsg.setImageBitmap(null);
                        Glide.with(viewHolder.imageMsg.getContext())
                                .load(model.getImg())
                                .into(viewHolder.imageMsg);

                    }
                }



            }
        };

        recyclerView.setAdapter(mFirebaseAdapter);

        btnImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseRef.child("DemoApp/Messages/" + pairedId + "/msgCount").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        int msgCount = dataSnapshot.getValue(Integer.class);
                        MainActivity.msgCount = dataSnapshot.getValue(Integer.class);
                        initVariable.setVariable(MainActivity.msgCount);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        etMsg.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(etMsg.getText().toString().trim().equals(""))
                {
                    Toast.makeText(getActivity(),"Input field is empty", Toast.LENGTH_SHORT).show();
                }
                else
                {

                    mDatabaseRef.child("DemoApp/Messages/" + pairedId + "/msgCount").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            int msgCount = dataSnapshot.getValue(Integer.class);
                            mDatabaseRef.child("DemoApp/Messages/" + pairedId + "/msgCount").setValue((msgCount + 1));
                            String msg = etMsg.getText().toString();
                            DataBundle dataBundle = new DataBundle(msg,"none",MainActivity.userEmail,null,null,null);
                            mDatabaseRef.child("DemoApp/Messages/" + pairedId + "/messages/" + String.valueOf(msgCount)).setValue(dataBundle);
                            mDatabaseRef.child("DemoApp/Messages/" + pairedId + "/messages/complete/count").setValue((msgCount + 1));
                            etMsg.setText("");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                return false;
            }
        });
        initVariable.setValueChangeListener(new InitializationVariable.onValueChangeListener() {
            @Override
            public void onChange(int value) {
                addImageMessage();
            }
        });
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK ){
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();

                    Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.mainFragment); //used to take out fragment

                    if(fragment != null){
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .remove(getActivity().getSupportFragmentManager().findFragmentById(R.id.mainFragment)).commit();
                    }

                    fragmentTransaction.add(R.id.mainFragment, new ContactsFragment());
                    fragmentTransaction.commit();
                    return true;
                }


                return false;
            }
        });
        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mFirebaseAdapter.stopListening();
    }

    public void addImageMessage(){
        // Select image for image message on click.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        this.startActivityForResult(intent, REQUEST_IMAGE_SUB);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        final String path= "DemoApp/ImageMessage/"+MainActivity.emailPath+"/"+ String.valueOf(MainActivity.msgCount);

        if (requestCode == REQUEST_IMAGE_SUB && resultCode == RESULT_OK) {
            if (data != null) {
                final Uri uri = data.getData();
                Log.d(TAG, "Uri: " + uri.toString());
                mDatabaseRef.child("DemoApp/Messages/" + pairedId + "/msgCount").setValue((MainActivity.msgCount + 1));

                DataBundle tempMessage = new DataBundle("", "nullImage", MainActivity.userEmail,
                        null, null, null);
                mDatabaseRef.child("DemoApp/Messages/"+pairedId+"/messages/"+ String.valueOf(MainActivity.msgCount))
                        .setValue(tempMessage, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError,
                                                   DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    String key = databaseReference.getKey();
                                    StorageReference storageReference =
                                            FirebaseStorage.getInstance()
                                                    .getReference(path);

                                    putImageInStorage(storageReference, uri, key, path);
                                } else {
                                    Log.w(TAG, "Unable to write message to database.",
                                            databaseError.toException());
                                    Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }else if(resultCode == RESULT_CANCELED){
            //canceled
        }
    }

    private void putImageInStorage(final StorageReference storageReference, Uri uri, final String key, String path) {

        storageReference.putFile(uri).addOnCompleteListener(getActivity(),
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadUri) {
                                    DataBundle imageMessageModel =
                                            new DataBundle("", downloadUri.toString(),
                                                    MainActivity.userEmail, null, null, null);


                                    mDatabaseRef.child("DemoApp/Messages/"+pairedId+"/messages/"+ String.valueOf(MainActivity.msgCount))
                                            .setValue(imageMessageModel);

                                    mDatabaseRef.child("DemoApp/Messages/" + pairedId + "/messages/complete/count").setValue((MainActivity.msgCount + 1));

                                }
                            });


                        } else {
                            Log.w(TAG, "Image upload task was not successful.",
                                    task.getException());

                            Toast.makeText(getActivity(), "Image upload task was not successful.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK ){
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();

            Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.mainFragment); //used to take out fragment

            if(fragment != null){
                getActivity().getSupportFragmentManager().popBackStackImmediate();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .remove(getActivity().getSupportFragmentManager().findFragmentById(R.id.mainFragment)).commit();
            }

            fragmentTransaction.add(R.id.mainFragment, new ContactsFragment());
            fragmentTransaction.commit();
        }


        return true;
    }


}
