package com.bits_perform.vertretungsplan;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.HashMap;
import java.util.Map;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    public static final String TABLE = "com.bits-perform.vertretungsplan.CODE";
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private CheckBox mSaveData;
    private File userdataFile;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.usernameTextView);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mSaveData = (CheckBox) findViewById(R.id.saveUserdata);

        Button mSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        userdataFile = new File(getFilesDir(), "userdata.xxx");

        if (isDataSaved()) {
            String[] userdata = getUserData();
            mUsernameView.setText(userdata[0]);
            mPasswordView.setText(userdata[1]);
            mSaveData.setChecked(true);
            attemptLogin();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String username = mUsernameView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;
        if (!isPasswordValid(password)) {
            focusView = mPasswordView;
            mPasswordView.setError("Falsches Passwort");
            cancel = true;
        }
        if (!isUsernameValid(username)) {
            focusView = mUsernameView;
            mUsernameView.setError("Ungültiger Benutzername");
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            showProgress(true);
            if (mSaveData.isChecked()) {
                saveUserdata(username, password);
            } else if (isDataSaved()) {
                userdataFile.delete();
            }
            CookieManager manager = new CookieManager();
            CookieHandler.setDefault(manager);
            final RequestQueue q = Volley.newRequestQueue(this);

            //login
            StringRequest loginRequest = new StringRequest(Request.Method.POST, "https://bid.lspb.de/signin/", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response.contains("Ungültiger Benutzername oder falsches Passwort. Bitte überprüfen Sie Ihre Zugangsdaten und versuchen Sie es erneut.")) {
                        showProgress(false);
                        mUsernameView.setError("Ungültiger Benutzername oder falsches Passwort.");
                        return;
                    }
                    q.add(downloadVertretungsplan(q));
                }

            }, EmptyErrorListener) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("values[login]", username);
                    params.put("values[password]", password);
                    params.put("values[req]", "/");
                    return params;
                }
            };
            q.add(loginRequest);
        }
    }

    private StringRequest downloadVertretungsplan(final RequestQueue q) {
        return new StringRequest
                (Request.Method.GET, "https://bid.lspb.de/explorer/download/1221635/", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        q.add(signout());
                        Intent intent = new Intent(LoginActivity.this, VertretungsplanActivity.class);

                        intent.putExtra(TABLE, ParsePlan.parsePlan(response));
                        startActivity(intent);
                    }

                }, EmptyErrorListener);
    }

    private StringRequest signout() {
        return new StringRequest
                ("https://bid.lspb.de/signout", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, EmptyErrorListener);
    }

    private static final Response.ErrorListener EmptyErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            error.printStackTrace();
        }
    };

    private void saveUserdata(String username, String password) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(userdataFile));
            bw.write(username);
            bw.newLine();
            bw.write(password);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean isUsernameValid(String username) {
        //TODO: Replace this with your own logic
        return username.length() < 9 && String.valueOf(username.charAt(username.length() - 1)).matches("\\d");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Login Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    public boolean isDataSaved() {
        return getUserData() != null;
    }

    public String[] getUserData() {
        String[] ret = new String[2];
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(userdataFile));
            ret[0] = r.readLine();
            ret[1] = r.readLine();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                r.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
}