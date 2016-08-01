package com.zecovery.android.nochedigna.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.zecovery.android.nochedigna.R;
import com.zecovery.android.nochedigna.base.BaseActivity;
import com.zecovery.android.nochedigna.login.LoginActivity;

public class CreateAccountActivity extends BaseActivity implements View.OnClickListener {

    private static final String LOG_TAG = LoginActivity.class.getName();

    private ProgressDialog mProgressDialog;

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonCreateAccount;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonCreateAccount = (Button) findViewById(R.id.buttonCreateAccount);

        buttonCreateAccount.setOnClickListener(this);

        // Init firebase auth
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(LOG_TAG, "signed in: " + user.getUid());
                } else {
                    Log.d(LOG_TAG, "signed out: ");
                }
            }
        };
    }

    public void createAccount(String email, String password) {
        Log.d(LOG_TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressDialog();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOG_TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            FirebaseCrash.log("ERROR CREATING ACCOUNT: " + task.getException());
                            Toast.makeText(CreateAccountActivity.this, getResources().getString(R.string.error_creating_new_account), Toast.LENGTH_SHORT).show();
                        }
                        dismissProgressDialog();
                        gotoMap();
                        finish();
                    }
                });
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

    private void gotoMap() {
        startActivity(new Intent(CreateAccountActivity.this, MapsActivity.class));
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

    @Override
    public void onClick(View view) {
        Log.d(LOG_TAG, "textViewCreateAccount - clicked!!!");
        FirebaseCrash.log("account create!");
        createAccount(editTextEmail.getText().toString(), editTextPassword.getText().toString());
    }
}
