package com.example.tastifyapp;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context){
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void saveUserSession(int userId){
        editor.putInt(KEY_USER_ID, userId);
        editor.apply();
    }

    public int getUserId(){
        return pref.getInt(KEY_USER_ID, -1); // -1 indicates no user is logged in
    }

    public void clearSession(){
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn(){
        return getUserId() != -1;
    }
}
