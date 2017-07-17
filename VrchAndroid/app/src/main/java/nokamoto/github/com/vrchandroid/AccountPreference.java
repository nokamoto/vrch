package nokamoto.github.com.vrchandroid;

import android.content.Context;
import android.preference.PreferenceManager;

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
}
