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

public class SignIn extends Fragment {

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference myRef = firebaseDatabase.getReference("DemoApp");
    Button btnSubmit;
    EditText etEmail, etPassword, etUserName;
    String email, password, userName;
    DataBundle dataBundle;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.sign_in_fragment, container, false);
        btnSubmit = (Button)view.findViewById(R.id.btnSignInSubmit);
        etEmail = (EditText)view.findViewById(R.id.etEmail);
        etPassword = (EditText)view.findViewById(R.id.etPassword);

        sharedPref= getContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = etEmail.getText().toString().trim();
                password = etPassword.getText().toString().trim();

                if(!email.equals("") && !password.equals("")){
                    SignInToFDB();
                }else{
                    Toast.makeText(getActivity(), "Input fields cannot be empty",Toast.LENGTH_SHORT).show();

                }
            }
        });
        return view;
    }

    private void SignInToFDB(){
        try
        {
            myRef.child("MessageInfo/ID/" + email.replace('.', '_'))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // This method is called once with the initial value and again
                            // whenever data at this location is updated.
                            String dataCheck = dataSnapshot.child("email").getValue(String.class);
                            String dataPass = dataSnapshot.child("password").getValue(String.class);
                            String dataName = dataSnapshot.child("name").getValue(String.class);
                            if(dataCheck != null){
                                if(dataPass.equals(password)){
                                    editor.putString("UserEmail", email);
                                    editor.putString("UserName", dataSnapshot.child("name").getValue(String.class));
                                    editor.apply();
                                    MainActivity.emailPath = email.replace('.', '_');
                                    MainActivity.userEmail = email;
                                    MainActivity.userName = userName = sharedPref.getString("UserName", "empty");
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

//                                    RelativeLayout relativeLayout = getActivity().findViewById(R.id.LayoutMain);
//                                    RelativeLayout.LayoutParams mblp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
//                                    RelativeLayout.LayoutParams mblp = (RelativeLayout.LayoutParams)relativeLayout.getLayoutParams();
//                                    mblp.width= RelativeLayout.LayoutParams.MATCH_PARENT;
//                                    mblp.height = RelativeLayout.LayoutParams.MATCH_PARENT;
//                                    relativeLayout.setLayoutParams(mblp);
                                }
                            }else{
                                Toast.makeText(getActivity(), "Incorrect password",Toast.LENGTH_SHORT).show();
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

