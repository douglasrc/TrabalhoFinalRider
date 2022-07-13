package com.example.rider.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfigFirebase {

    private static DatabaseReference database;
    private static FirebaseAuth mAuth;

    public static DatabaseReference getFirebaseDatabase(){
        if (database == null){
            database = FirebaseDatabase.getInstance().getReference();
        }
        return database;
    }

    public static FirebaseAuth getFirebaseAutentica(){

        if (mAuth == null){
            mAuth = FirebaseAuth.getInstance();
        }
        return mAuth;
    }
}
