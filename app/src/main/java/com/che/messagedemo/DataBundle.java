package com.che.messagedemo;

public class DataBundle {
    String msg;
    String id;
    String img;
    String name;
    String password;
    String email;
    public DataBundle(){
        //
    }

    public DataBundle(String msg, String img, String id, String name, String password, String email){
        this.msg= msg;
        this.img = img;
        this.id = id;
        this.name = name;
        this.password = password;
        this.email = email;
    }

    public String getMsg(){return msg;}
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public String getId(){return id;}
    public void setId(String id) {
        this.id = id;
    }
    public String getImg(){return img;}
    public void setImg(String img) {
        this.img = img;
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
