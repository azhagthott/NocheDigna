package com.zecovery.android.nochedigna.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.crash.FirebaseCrash;
import com.zecovery.android.nochedigna.R;
import com.zecovery.android.nochedigna.activity.CreateAccountActivity;
import com.zecovery.android.nochedigna.activity.MapsActivity;
import com.zecovery.android.nochedigna.activity.SettingsActivity;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = LoginActivity.class.getName();
    private static final int RC_SIGN_IN = 9001;

    private ProgressDialog mProgressDialog;
    private Button buttonCreateAccount;

    //Facebook Login
    private CallbackManager mCallbackManager;
    private Button facebookLoginButton;

    //Firebase Auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;

    //Google Login
    private GoogleApiClient mGoogleApiClient;
    private Button googleLoginButton;

    //settings
    private boolean sessionPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        sessionPreferences = preferences.getBoolean(SettingsActivity.KEY_PREF_LOGIN, true);

        // Google Login
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //Instancia GoogleApiCliente
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Init firebase auth
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();

                // Si el usuario está logueado -> pasa directo al mapa
                if (user != null) {
                    Log.d(LOG_TAG, "signed in: " + user.getUid());
                    gotoMap();
                } else {
                    Log.d(LOG_TAG, "signed out: ");
                }
            }
        };

        // CallbackManager de Facebook
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                // Si la loginResult está OK -> muestra el mapa
                Log.d(LOG_TAG, "onSuccess: loginResult OK");
                handleFacebookAccessToken(loginResult.getAccessToken());
                gotoMap();
            }

            @Override
            public void onCancel() {
                // Si el usuario cancela inicio de sesion con Facebook -> muestra mensaje
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_facebook_canceled), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                // En caso de error, envio error a Firebase
                FirebaseCrash.log("FACEBOOK ERROR LOGIN: " + error);
            }
        });

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        buttonCreateAccount = (Button) findViewById(R.id.buttonCreateAccount);

        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        googleLoginButton = (Button) findViewById(R.id.buttonGoogleLogin);
        facebookLoginButton = (Button) findViewById(R.id.buttonFacebookLogin);

        buttonCreateAccount.setOnClickListener(this);

        buttonLogin.setOnClickListener(this);
        googleLoginButton.setOnClickListener(this);
        facebookLoginButton.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (sessionPreferences) {
            Log.d(LOG_TAG, "user: " + user);
            mAuth.addAuthStateListener(mAuthListener);
            gotoMap();
        } else {
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Facebook
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                gotoMap();
            } else {
                // Google Sign In failed, update UI appropriately
                Log.d(LOG_TAG, "result: " + result);
                Toast.makeText(this, getResources().getString(R.string.error_login), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(LOG_TAG, "firebaseAuthWithGoogle:" + acct.getId());

        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d(LOG_TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            FirebaseCrash.log("ERROR GOOGLE LOGIN" + task.getException());
                            Log.w(LOG_TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.error_login), Toast.LENGTH_SHORT).show();
                        }
                        dismissProgressDialog();
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        //updateUI(null);
                    }
                });
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        //updateUI(null);
                    }
                });
    }

    private void signInEmail(String email, String password) {

        Log.d(LOG_TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOG_TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            FirebaseCrash.log("ERROR LOGIN EMAIL_PASSWORD" + task.getException());
                            Log.w(LOG_TAG, "signInWithEmail", task.getException());
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.error_login), Toast.LENGTH_SHORT).show();
                        }
                        dismissProgressDialog();
                        gotoMap();
                    }
                });
    }

    private void signOutEmail() {
        mAuth.signOut();
        //updateUI(null);
    }

    private boolean validateForm() {

        boolean valid = true;

        String email = editTextEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("El email es requerido.");
            valid = false;
        } else {
            editTextEmail.setError(null);
        }

        String password = editTextPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("La contraseña es requerida.");
            valid = false;
        } else {
            editTextPassword.setError(null);
        }
        return valid;
    }

    @Override
    public void onClick(View view) {

        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "buttonLogin - clicked!!!");
                signInEmail(editTextEmail.getText().toString(), editTextPassword.getText().toString());
            }
        });

        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "facebookLoginButton - clicked!!!");
                facebookLogin();
            }
        });

        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        FirebaseCrash.log("google login connection failed!!!" + connectionResult);
    }


    private void facebookLogin() {
        //LoginManager.getInstance().logOut();
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(LOG_TAG, "handleFacebookAccessToken:" + token);

        showProgressDialog();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOG_TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(LOG_TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        dismissProgressDialog();
                        gotoMap();
                    }
                });
    }

    private void gotoMap() {
        startActivity(new Intent(LoginActivity.this, MapsActivity.class));
        finish();
    }

    private ProgressDialog initiaizePrgressDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setMessage(getResources().getString(R.string.com_facebook_loading));
        return dialog;
    }

    private void showProgressDialog() {
        if (mProgressDialog != null && !mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}