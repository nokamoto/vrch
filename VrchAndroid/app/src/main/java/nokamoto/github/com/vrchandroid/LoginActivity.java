package nokamoto.github.com.vrchandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity {
    private final static String TAG = LoginActivity.class.getSimpleName();

    // UI references.
    private TextView displayNameView;

    private FirebaseAuth auth;

    private ProgressDialog progressDialog;

    private AccountPreference account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        account = new AccountPreference(getBaseContext());

        displayNameView = (TextView) findViewById(R.id.display_name);
        displayNameView.setText(account.displayName());

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Loading...");
        }

        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        showProgressDialog();

        // Reset errors.
        displayNameView.setError(null);

        // Store values at the time of the login attempt.
        final String displayName = displayNameView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(displayName)) {
            displayNameView.setError(getString(R.string.error_field_required));
            focusView = displayNameView;
            cancel = true;
        } else if (!isDisplayNameValid(displayName)) {
            displayNameView.setError(getString(R.string.error_invalid_display_name));
            focusView = displayNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            hideProgressDialog();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            auth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "signInAnonymously:success");
                        account.setDisplayName(displayName);
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }

                    hideProgressDialog();
                }
            });
        }
    }

    private boolean isDisplayNameValid(String displayName) {
        return displayName.matches("[a-zA-Z+\\-_.0-9]{1,128}");
    }
}

