package nokamoto.github.com.vrchandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseUser;

public class AccountPreference {
    private Context context;

    AccountPreference(Context context) {
        this.context = context;
    }

    public String displayName() {
        String key = context.getResources().getString(R.string.pref_account_key_display_name);
        String defaultValue = context.getResources().getString(R.string.pref_account_default_display_name);
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defaultValue);
    }

    void setDisplayName(String value) {
        String key = context.getResources().getString(R.string.pref_account_key_display_name);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    String uid(FirebaseUser user) {
        try {
            return user.getUid();
        } catch (NullPointerException e) {
            return context.getResources().getString(R.string.pref_account_default_uid);
        }
    }
}
