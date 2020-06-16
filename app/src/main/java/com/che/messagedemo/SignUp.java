package com.che.messagedemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class SignUp extends Fragment {
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference myRef = firebaseDatabase.getReference("DemoApp");
    Button btnSubmit;
    EditText etEmail, etPassword, etUserName;
    String email, password, userName;
    AuthDataBundle dataBundle;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.sign_up_fragment, container, false);
        btnSubmit = (Button)view.findViewById(R.id.btnSignUpSubmit);
        etEmail = (EditText)view.findViewById(R.id.etEmail);
        etPassword = (EditText)view.findViewById(R.id.etPassword);
        etUserName = (EditText)view.findViewById(R.id.etUserName);

        sharedPref= getContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = etEmail.getText().toString().trim();
                password = etPassword.getText().toString().trim();
                userName = etUserName.getText().toString().trim();

                if(!email.equals("") && !password.equals("") && !userName.equals("")){
                    SignUpToFDB();
                }else{
                    Toast.makeText(getActivity(), "Input fields cannot be empty",Toast.LENGTH_SHORT).show();
                }
            }
        });


        return view;
    }

    private void SignUpToFDB(){
        try
        {
            dataBundle = new AuthDataBundle("noPic",userName,password, email);
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // This method is called once with the initial value and again
                            // whenever data at this location is updated.
                            String dataCheck = dataSnapshot.child("MessageInfo/ID/" + email.replace('.', '_') + "/email").getValue(String.class);
                            if(dataCheck != null){
                                Toast.makeText(getActivity(), "User already exist!",Toast.LENGTH_SHORT).show();
                            }else{
                                editor.putString("UserName", userName);
                                editor.putString("UserEmail", email);
                                editor.apply();
                                MainActivity.emailPath = email.replace('.', '_');
                                MainActivity.userEmail = email;
                                MainActivity.userName = userName = sharedPref.getString("UserName", "empty");
                                myRef.child("MessageInfo/ID/" + email.replace('.', '_')).setValue(dataBundle);
                                myRef.child("MessageInfo/Accounts/" + email.replace('.', '_') + "/Contacts/pairedCount").setValue(0);

                                // This method is called once with the initial value and again
                                // whenever data at this location is updated.
                                int dataCheck2 = dataSnapshot.child("Search/UserCount").getValue(Integer.class);
                                dataCheck2 = (dataCheck2 + 1);
                                myRef.child("Search/UserCount").setValue(dataCheck2);
                                myRef.child("Search/Users/" + String.valueOf(dataCheck2)).setValue(email);
                                myRef.child("MessageInfo/Accounts/" + email.replace('.', '_') + "/Contacts/pairedCount").setValue(0);
                                etEmail.setText("");
                                etPassword.setText("");
                                etUserName.setText("");

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
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                        }
                    });

        }
        catch(Exception e) {

            Toast.makeText(getActivity(), "Error has accured",Toast.LENGTH_SHORT).show();
        }
    }
}
