package com.che.messagedemo;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class Searchable extends Activity implements SearchView.OnQueryTextListener {

    List<String> username = new ArrayList<>();// stores app users names.
    List<String> email = new ArrayList<>();// stores app users account id.
    List<String> displayUri = new ArrayList<>();
    List<String> uEmail = new ArrayList<>();
    List<String> userList = new ArrayList<>();
    ArrayAdapter<String> adapter;

    CircleImageView displayPic;
    ImageView bkStrip;// used for the list_view custom display.
    ListView listView;
    TextView getUserId, getUserNameDisplay, getUserEmail, changeOnSelect;// used for the list_view custom display.
    SearchView searchView;

    // saved data used for profile use.
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    // database reference for identifying user projects.
    DatabaseReference mDatabaseRef= FirebaseDatabase.getInstance().getReference();
    DatabaseReference mRootRef= mDatabaseRef.child("DemoApp/Search/Users");
    DatabaseReference mContactsRef = mDatabaseRef.child("Contacts");
    DatabaseReference mPairedRef = mDatabaseRef.child("PairedUsers");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        searchView = (SearchView)findViewById(R.id.svSearchContainer);
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search...");

        //Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    @Override
    public void onStart(){
        super.onStart();

    }

    public void doMySearch(final String adapter){

        // initialising
        listView= (ListView)findViewById(R.id.lvSearch);
        //changeOnSelect=(TextView)findViewById(R.id.UidDisplay);

        sharedPref= getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        //Root reference to the users of the app.
        mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String firstChar = String.valueOf(adapter.toUpperCase().charAt(0));
                //adapter.replaceFirst(String.valueOf(adapter.charAt(0)),firstChar);
                if(!uEmail.isEmpty()){
                    uEmail.clear();
                }

                // a loop for each child in the 'users' root which stores the email as id of each account.
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    String getUserEmail = data.getValue(String.class);
                    if(getUserEmail.equalsIgnoreCase(adapter)){
                        uEmail.add(0, getUserEmail);
                    }else if(getUserEmail.contains(firstChar) || getUserEmail.contains(adapter)){
                        uEmail.add(getUserEmail);
                    }
                }


                ArrayAdapter<String> ladapter = new ArrayAdapter<String>(
                        Searchable.this,
                        android.R.layout.simple_list_item_1,
                        uEmail);

                if(!ladapter.isEmpty()){
                    listView.setAdapter(ladapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // a onclick listener for the custom made  list_view.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            public void onItemClick(AdapterView<?> parent, View view, final int position, long id){

                // an alert dialog that prompt the user on whether they want to add the selected user or not.
                final int num = position;
                AlertDialog.Builder builder= new AlertDialog.Builder(Searchable.this);

                builder.setMessage("Would you like to add "+uEmail.get(position)+" to your contacts?").setTitle("Add to contacts");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        //add app user to contacts
                        if(uEmail.get(position).equals(MainActivity.userEmail)){
                            Toast.makeText(Searchable.this, "Cannot add your account", Toast.LENGTH_SHORT).show();
                        }else{
                            addToContacts(num);
                        }

                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                    }
                });
                AlertDialog dialog= builder.create();
                dialog.show();
            }
        });
    }

    public void addToContacts(final int num){
        try
        {
            String searchText = uEmail.get(num);
            MainActivity.contactName= uEmail.get(num);
            MainActivity.contactPath = searchText.replace('.', '_');
            mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!userList.isEmpty()){
                        userList.clear();
                    }

                    // a loop for each child in the 'users' root which stores the email as id of each account.
                    for(DataSnapshot data : dataSnapshot.child("DemoApp/Search/Users").getChildren()){
                        userList.add(data.getValue(String.class));
                    }

                    for (int i = 0; i < userList.size(); i++)
                    {
                        int pairedCount = dataSnapshot.child("DemoApp/MessageInfo/Accounts/" + MainActivity.contactPath + "/Contacts/pairedCount").getValue(Integer.class);
                        if (pairedCount == 0)
                        {
                            int pairedAcc = dataSnapshot.child("DemoApp/PairedAccount").getValue(Integer.class) + 1;
                            mDatabaseRef.child("DemoApp/PairedAccount").setValue(pairedAcc);

                            mDatabaseRef.child("DemoApp/MessageInfo/Accounts/" + MainActivity.contactPath + "/Contacts/pairedInfo/" + MainActivity.emailPath + "/pairedId").setValue(pairedAcc);
                            mDatabaseRef.child("DemoApp/MessageInfo/Accounts/" + MainActivity.emailPath + "/Contacts/pairedInfo/" + MainActivity.contactPath + "/pairedId").setValue(pairedAcc);
                            mDatabaseRef.child("DemoApp/Messages/" +String.valueOf(pairedAcc)+"/messages/complete/count").setValue(0);
                            mDatabaseRef.child("DemoApp/Messages/" +String.valueOf(pairedAcc)+"/msgCount").setValue(0);


                            mDatabaseRef.child("DemoApp/MessageInfo/Accounts/" + MainActivity.contactPath + "/Contacts/pairedId/" + String.valueOf((pairedCount + 1))).setValue(MainActivity.userEmail);
                            mDatabaseRef.child("DemoApp/MessageInfo/Accounts/" + MainActivity.contactPath + "/Contacts/pairedCount").setValue((pairedCount + 1));

                            pairedCount =dataSnapshot.child("DemoApp/MessageInfo/Accounts/" +MainActivity.emailPath + "/Contacts/pairedCount").getValue(Integer.class);
                            mDatabaseRef.child("DemoApp/MessageInfo/Accounts/" + MainActivity.emailPath + "/Contacts/pairedId/" + String.valueOf((pairedCount + 1))).setValue(MainActivity.contactName);
                            mDatabaseRef.child("DemoApp/MessageInfo/Accounts/" + MainActivity.emailPath + "/Contacts/pairedCount").setValue((pairedCount + 1));

                            finish();
                            break;
                        }

                        for (int ii = 0; ii < pairedCount; ii++)
                        {
                            int count = ii + 1;
                            String result = dataSnapshot.child("DemoApp/MessageInfo/Accounts/" + MainActivity.contactPath + "/Contacts/pairedId/" + String.valueOf(count)).getValue(String.class);
                            if (result.equals(MainActivity.userEmail))
                            {
                                Toast.makeText(Searchable.this, "Accounts already connected.", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            else if (count == pairedCount)
                            {
                                int pairedAcc = dataSnapshot.child("DemoApp/PairedAccount").getValue(Integer.class) + 1;
                                mDatabaseRef.child("DemoApp/PairedAccount").setValue(pairedAcc);

                                mDatabaseRef.child("DemoApp/MessageInfo/Accounts/" + MainActivity.contactPath + "/Contacts/pairedInfo/" +MainActivity.emailPath + "/pairedId").setValue(pairedAcc);
                                mDatabaseRef.child("DemoApp/MessageInfo/Accounts/" + MainActivity.emailPath + "/Contacts/pairedInfo/" + MainActivity.contactPath + "/pairedId").setValue(pairedAcc);
                                mDatabaseRef.child("DemoApp/Messages/" + String.valueOf(pairedAcc) + "/messages/complete/count").setValue(0);
                                mDatabaseRef.child("DemoApp/Messages/" + String.valueOf(pairedAcc) + "/msgCount").setValue(0);

                                mDatabaseRef.child("DemoApp/MessageInfo/Accounts/" + MainActivity.contactPath + "/Contacts/pairedId/" + String.valueOf((pairedCount + 1))).setValue(MainActivity.userEmail);
                                mDatabaseRef.child("DemoApp/MessageInfo/Accounts/" + MainActivity.contactPath + "/Contacts/pairedCount").setValue((pairedCount + 1));

                                pairedCount = dataSnapshot.child("DemoApp/MessageInfo/Accounts/" + MainActivity.emailPath + "/Contacts/pairedCount").getValue(Integer.class);
                                mDatabaseRef.child("DemoApp/MessageInfo/Accounts/" + MainActivity.emailPath + "/Contacts/pairedId/" + String.valueOf((pairedCount + 1))).setValue(MainActivity.contactName);
                                mDatabaseRef.child("DemoApp/MessageInfo/Accounts/" + MainActivity.emailPath + "/Contacts/pairedCount").setValue((pairedCount + 1));
                                finish();
                                break;
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



        } catch(Exception e)
            {
                Toast.makeText(this, "An Error has accured.",Toast.LENGTH_SHORT).show();
            }


    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(!newText.equals("")){
            doMySearch(newText);
        }else{
            listView.setAdapter(null);
        }
        return false;
    }
}