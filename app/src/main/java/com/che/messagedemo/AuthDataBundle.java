package com.che.messagedemo;

public class AuthDataBundle{
    String picProfile;
    String name;
    String password;
    String email;
    public AuthDataBundle(){
        //
    }

    public AuthDataBundle(String picProfile, String name, String password, String email){

        this.picProfile = picProfile;
        this.name = name;
        this.password = password;
        this.email = email;
    }

    public String getPicProfile(){return picProfile;}
    public void setPicProfile(String picProfile) {
        this.picProfile = picProfile;
    }
    public String getPassword(){return password;}
    public void setPassword(String password) {
        this.password = password;
    }
    public String getName(){return name;}
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail(){return email;}
    public void setEmail(String email) {
        this.email = email;
    }

}
