package crypto;

import android.content.Context;
import android.content.SharedPreferences;

import il.co.ytcom.vm580d.VM580D;

public class Preferences {

    private static Preferences instance = null;
    public static Preferences getInstance() {
        if(instance == null) {
            instance = new Preferences();
        }
        return instance;
    }

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private Preferences() {
        Context c = VM580D.getInstance().getApplicationContext();
        preferences = c.getSharedPreferences("app_ref", Context.MODE_PRIVATE);
        editor = c.getSharedPreferences("app_ref", Context.MODE_PRIVATE).edit();
    }


    public void setString(String k, String v) {
        editor.putString(k,v).commit();
    }

    public String getString(String k, String d) {
        return preferences.getString(k,d);
    }
}
