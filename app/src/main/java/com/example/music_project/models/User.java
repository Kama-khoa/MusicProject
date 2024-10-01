package com.example.music_project.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity
public class User {

    @PrimaryKey(autoGenerate = true)
    public int user_id;

    public String username;
    public String email;
    public String password;
    public String role;
    public Date date_joined;
    public boolean canUploadContent;
    public String profileImagePath;
    public User(String username,String email,String password){
        this.username=username;
        this.email=email;
        this.password=password;
        this.role = "USER";
        this.date_joined = new Date();
        this.canUploadContent = true;
        this.profileImagePath=null;
    }
    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Date getDate_joined() {
        return date_joined;
    }

    public void setDate_joined(Date date_joined) {
        this.date_joined = date_joined;
    }
    public boolean isAdmin() {
        return "ADMIN".equals(this.role);
    }

    public boolean canUploadContent() {
        return this.canUploadContent;
    }

    public void setCanUploadContent(boolean canUploadContent) {
        this.canUploadContent = canUploadContent;
    }
    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }
}