package com.che.messagedemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsFragment extends Fragment {
    private static final String TAG = "Contacts";
    List<String> username = new ArrayList<>();// stores app users names.
    List<Integer> uId = new ArrayList<>();// stores app users account id.
    List<String> uEmail = new ArrayList<>();//stores app users email.
    List<String> uDisplayPic = new ArrayList<>();//stores app users display picture.
    ListAdapter adapter;// adapter to load the list_view.
    CircleImageView displayPic; ImageView bkStrip;// used for the list_view custom display.
    ListView listView;

    // saved data used for profile use.
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    // database reference for identifying user projects.
    DatabaseReference mDatabaseRef= FirebaseDatabase.getInstance().getReference();
    DatabaseReference mContactsRef= mDatabaseRef.child("DemoApp/MessageInfo");
    DatabaseReference  mChildListener= mDatabaseRef.child("DemoApp/MessageInfo/Accounts/"+MainActivity.emailPath+"/Contacts/pairedId");
    DatabaseReference linkRootRef;
    TextView getUserId, getUserNameDisplay, getUserEmail, changeOnSelect;// used for the list_view custom display.



    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.contact_fragment, container, false);

        // initialising
        sharedPref = getContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        editor= sharedPref.edit();
        listView = (ListView)view.findViewById(R.id.lvContacts);
        listView.setAdapter(null);


        mContactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int pairedCount;
                pairedCount = dataSnapshot.child("Accounts/"+MainActivity.emailPath+"/Contacts/pairedCount").getValue(Integer.class);

                // a loop for each child in the 'users' root which stores the username and id of each account.
                for(DataSnapshot data : dataSnapshot.child("/Accounts/"+MainActivity.emailPath+"/Contacts/pairedId").getChildren()){
                    String getUserEmail= data.getValue(String.class);
                    uEmail.add(getUserEmail);

                }

                for(int i=0; i < pairedCount; i++){
                    String getUserName = dataSnapshot.child("ID/" + uEmail.get(i).replace(".", "_")+ "/name").getValue(String.class);
                    String getProfilePic = dataSnapshot.child("ID/" + uEmail.get(i).replace(".", "_")+ "/picProfile").getValue(String.class);
                    int getPairedId= dataSnapshot.child("Accounts/"+MainActivity.emailPath+"/Contacts/pairedInfo/"+uEmail.get(i).replace(".", "_")+"/pairedId").getValue(Integer.class);
                    username.add(getUserName);
                    uDisplayPic.add(getProfilePic);
                    uId.add(getPairedId);
                }

                // a custom class that creates the custom list_view.
                class UserContacts extends ArrayAdapter<String> {

                    private UserContacts(Context context, int textViewResourceId, List<String> objects) {
                        super(context, textViewResourceId, objects);
                        // TODO Auto-generated constructor stub
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        // TODO Auto-generated method stub
                        View row = convertView;
                        if (row == null) {
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            row = inflater.inflate(R.layout.contact_display, parent, false);
                        }
                        displayPic=(CircleImageView)row.findViewById(R.id.imgProfilePic);
                        bkStrip=(ImageView)row.findViewById(R.id.bkStrip);
                        getUserId= (TextView)row.findViewById(R.id.txtPairedId);
                        getUserNameDisplay= (TextView)row.findViewById(R.id.txtUserName);
                        getUserEmail= (TextView)row.findViewById(R.id.txtUserEmail);
                        getUserId.setText(uId.get(position).toString());
                        getUserNameDisplay.setText(username.get(position));
                        getUserEmail.setText(uEmail.get(position));
                        if(!uDisplayPic.get(position).equalsIgnoreCase("noPic")){
                            displayPic.setBorderWidth(1);
                            Glide.with(displayPic.getContext()).load(uDisplayPic.get(position)).into(displayPic);
                        }else{
                            displayPic.setImageResource(R.mipmap.ic_default_pic);
                        }

                        return row;
                    }
                }

                ArrayAdapter<String> adapter= new UserContacts(getActivity(), R.layout.contact_fragment, username);
                listView.setAdapter(adapter);
//                class GetLinkAccountData extends AsyncTask<Void, Void, Void> {
//                    @Override
//                    protected void onPreExecute(){
//                        pairedCount[0] = dataSnapshot.child("Accounts/che@examp_com/Contacts/pairedCount").getValue(Integer.class);
//                    }
//
//                    @Override
//                    protected Void doInBackground(Void... params) {
//
//                        // a loop for each child in the 'users' root which stores the username and id of each account.
//                        for(DataSnapshot data : dataSnapshot.child("/Accounts/che@examp_com/Contacts/pairedId").getChildren()){
//                            String getUserEmail= data.getValue(String.class);
//                            uEmail.add(getUserEmail);
//
//                        }
//
//                        for(int i=0; i < pairedCount[0]; i++){
//                            String getUserName = dataSnapshot.child("ID/" + uEmail.get(i).replace(".", "_")+ "/name").getValue(String.class);
//                            String getProfilePic = dataSnapshot.child("ID/" + uEmail.get(i).replace(".", "_")+ "/picProfile").getValue(String.class);
//                            int getPairedId= dataSnapshot.child("Accounts/che@examp_com/Contacts/pairedInfo/"+uEmail.get(i).replace(".", "_")+"/pairedId").getValue(Integer.class);
//                            username.add(getUserName);
//                            uDisplayPic.add(getProfilePic);
//                            uId.add(getPairedId);
//                        }
//
//                        return null;
//                    }
//
//                    @Override
//                    protected void onPostExecute(Void result) {
//                        // a custom class that creates the custom list_view.
//                        class UserContacts extends ArrayAdapter<String> {
//
//                            private UserContacts(Context context, int textViewResourceId, List<String> objects) {
//                                super(context, textViewResourceId, objects);
//                                // TODO Auto-generated constructor stub
//                            }
//
//                            @Override
//                            public View getView(int position, View convertView, ViewGroup parent) {
//                                // TODO Auto-generated method stub
//                                View row = convertView;
//                                if (row == null) {
//                                    LayoutInflater inflater = getActivity().getLayoutInflater();
//                                    row = inflater.inflate(R.layout.contact_display, parent, false);
//                                }
//                                displayPic=(CircleImageView)row.findViewById(R.id.imgProfilePic);
//                                bkStrip=(ImageView)row.findViewById(R.id.bkStrip);
//                                getUserId= (TextView)row.findViewById(R.id.txtPairedId);
//                                getUserNameDisplay= (TextView)row.findViewById(R.id.txtUserName);
//                                getUserEmail= (TextView)row.findViewById(R.id.txtUserEmail);
//                                getUserId.setText(uId.get(position));
//                                getUserNameDisplay.setText(username.get(position));
//                                getUserEmail.setText(uEmail.get(position));
//                                if(!uDisplayPic.get(position).equalsIgnoreCase("noPic")){
//                                    displayPic.setBorderWidth(1);
//                                    Glide.with(displayPic.getContext()).load(uDisplayPic.get(position)).into(displayPic);
//                                }else{
//                                    displayPic.setImageResource(R.mipmap.ic_default_pic);
//                                }
//
//                                return row;
//                            }
//                        }
//
//                        ArrayAdapter<String> adapter= new UserContacts(getActivity(), R.layout.contact_fragment, username);
//                        listView.setAdapter(adapter);
//
//                    }
//                }
//                new GetLinkAccountData().execute();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

            // A on click listener for the list_view that take you into a chatRoom with your contacts.
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                    MainActivity.email = ((TextView)view.findViewById(R.id.txtUserEmail)).getText().toString();
                    MainActivity.pairedId = ((TextView)view.findViewById(R.id.txtPairedId)).getText().toString();
                    MainActivity.contactName = ((TextView)view.findViewById(R.id.txtUserName)).getText().toString();

                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();

                    Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.mainFragment); //used to take out fragment

                    if(fragment != null){
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .remove(getActivity().getSupportFragmentManager().findFragmentById(R.id.mainFragment)).commit();
                    }

                    fragmentTransaction.add(R.id.mainFragment, new MessageFragment());
                    fragmentTransaction.commit();
//                    editor.putString("GetUserID",((TextView)view.findViewById(R.id.Uid)).getText().toString());
//                    editor.apply();
//                    editor.putString("GetUserName",((TextView)view.findViewById(R.id.nameDisplayed)).getText().toString());
//                    editor.putString("GetUserDp", ((TextView)view.findViewById(R.id.displayPicText)).getText().toString());
//                    editor.apply();
                }
            });

        mChildListener.limitToLast(1).addChildEventListener(new ChildEventListener() {
            int c;
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(c > 0){
                    Toast.makeText(getActivity(), "User Added ",Toast.LENGTH_SHORT).show();
                    updateContact();
                }
               c++;

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //Toast.makeText(getActivity(), "User Added "+ s, Toast.LENGTH_SHORT).show();
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


        return view;
    }

    private void updateContact(){
        mContactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uEmail.clear();
                username.clear();
                uDisplayPic.clear();
                uId.clear();

                int pairedCount;
                pairedCount = dataSnapshot.child("Accounts/"+MainActivity.emailPath+"/Contacts/pairedCount").getValue(Integer.class);

                // a loop for each child in the 'users' root which stores the username and id of each account.
                for(DataSnapshot data : dataSnapshot.child("Accounts/"+MainActivity.emailPath+"/Contacts/pairedId").getChildren()){
                    String getUserEmail= data.getValue(String.class);
                    uEmail.add(getUserEmail);

                }

                for(int i=0; i < pairedCount; i++){
                    String getUserName = dataSnapshot.child("ID/" + uEmail.get(i).replace(".", "_")+ "/name").getValue(String.class);
                    String getProfilePic = dataSnapshot.child("ID/" + uEmail.get(i).replace(".", "_")+ "/picProfile").getValue(String.class);
                    int getPairedId= dataSnapshot.child("Accounts/"+MainActivity.emailPath+"/Contacts/pairedInfo/"+uEmail.get(i).replace(".", "_")+"/pairedId").getValue(Integer.class);
                    username.add(getUserName);
                    uDisplayPic.add(getProfilePic);
                    uId.add(getPairedId);
                }
                // a custom class that creates the custom list_view.
                class UserContacts extends ArrayAdapter<String> {

                    private UserContacts(Context context, int textViewResourceId, List<String> objects) {
                        super(context, textViewResourceId, objects);
                        // TODO Auto-generated constructor stub
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        // TODO Auto-generated method stub
                        View row = convertView;
                        if (row == null) {
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            row = inflater.inflate(R.layout.contact_display, parent, false);
                        }
                        displayPic=(CircleImageView)row.findViewById(R.id.imgProfilePic);
                        bkStrip=(ImageView)row.findViewById(R.id.bkStrip);
                        getUserId= (TextView)row.findViewById(R.id.txtPairedId);
                        getUserNameDisplay= (TextView)row.findViewById(R.id.txtUserName);
                        getUserEmail= (TextView)row.findViewById(R.id.txtUserEmail);
                        getUserId.setText(uId.get(position).toString());
                        getUserNameDisplay.setText(username.get(position));
                        getUserEmail.setText(uEmail.get(position));
                        if(!uDisplayPic.get(position).equalsIgnoreCase("noPic")){
                            displayPic.setBorderWidth(1);
                            Glide.with(displayPic.getContext()).load(uDisplayPic.get(position)).into(displayPic);
                        }else{
                            displayPic.setImageResource(R.mipmap.ic_default_pic);
                        }

                        return row;
                    }
                }
                ArrayAdapter<String> adapter= new UserContacts(getActivity(), R.layout.contact_fragment, username);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        updateContact();
    }
    private class GetLinkAccountData2 extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute(){
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }
    }
}
