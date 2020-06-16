package com.che.messagedemo;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

public class MainActivity extends AppCompatActivity {

    private LinearLayoutManager mLinearLayoutManager;
    public static String userEmail;
    public static String emailPath;
    public static String userName;
    public static String email;
    public static  String pairedId;
    public static  String contactName;
    public static  String contactPath;
    public static  int msgCount;

    // saved data used for profile use.
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email = "empty";
        pairedId = "";
        contactName ="empty";
        contactPath = "";
        msgCount = 0;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPref = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        editor= sharedPref.edit();
        userEmail = sharedPref.getString("UserEmail", "empty");
        emailPath = sharedPref.getString("UserEmail", "empty").replace('.', '_');
        userName = sharedPref.getString("UserName", "empty");
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();


        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.mainFragment); //used to take out fragment

        if(sharedPref.getString("UserEmail", "empty").equals("empty")){

        if(fragment != null){
            getSupportFragmentManager().popBackStackImmediate();
            getSupportFragmentManager().beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentById(R.id.mainFragment)).commit();
        }

            fragmentTransaction.add(R.id.mainFragment, new AuthFragment());
            fragmentTransaction.commit();
        }else{
            if(fragment != null){
                getSupportFragmentManager().popBackStackImmediate();
                getSupportFragmentManager().beginTransaction()
                        .remove(getSupportFragmentManager().findFragmentById(R.id.mainFragment)).commit();
            }

            fragmentTransaction.add(R.id.mainFragment, new ContactsFragment());
            fragmentTransaction.commit();
        }





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){
            case R.id.action_search:
                if(!MainActivity.userEmail.equals("empty")) {
                    onSearchRequested();
                }
                return  true;
            case R.id.action_sign_out:
                editor.putString("UserEmail", "empty");
                editor.putString("UserName", "empty");
                editor.apply();
                finish();
                return true;
            default:

                return super.onOptionsItemSelected(item);
        }
    }


}
