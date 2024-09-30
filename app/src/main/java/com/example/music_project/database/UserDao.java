package com.example.music_project.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.music_project.models.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM Users")
    List<User> getAllUsers();

    @Query("SELECT * FROM Users WHERE UserID = :userId")
    User getUserById(long userId);

    @Query("SELECT * FROM Users WHERE Username = :username")
    User getUserByUsername(String username);

    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);
}