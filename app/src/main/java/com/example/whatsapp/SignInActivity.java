package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;


import com.example.whatsapp.Modelss.Users;
import com.example.whatsapp.databinding.ActivitySignInBinding;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;

public class SignInActivity extends AppCompatActivity {

    ActivitySignInBinding binding;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseDatabase database;




@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //hide actionbar
        getSupportActionBar().hide();

        //google facebook authentication initialization
        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();




        //progress Dialog making
    progressDialog=new ProgressDialog(SignInActivity.this);
    progressDialog.setTitle("Login");
    progressDialog.setMessage("Login to your account");

    //Configure Google Sign In
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
    mGoogleSignInClient=GoogleSignIn.getClient(this,gso);




// ...
    binding.tvSignUpPhn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent=new Intent(SignInActivity.this, SignInPhoneActivity.class);
            startActivity(intent);
        }
    });


    //commands given on button sign-in
    binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            //command for sign-in button which is logging without clicking on it
            if(binding.etEmail.getText().toString().isEmpty()){
                binding.etEmail.setError("Enter your email");
                return;
            }

            if(binding.etPassword.getText().toString().isEmpty()){
                binding.etPassword.setError("Enter your password");
                return;
            }

            //progress dialog show command
            progressDialog.show();

            //adding intent on sign in with email and password
            auth.signInWithEmailAndPassword(binding.etEmail.getText().toString(),binding.etPassword.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressDialog.dismiss();
                    if(task.isSuccessful())
                    {
                        Intent intent=new Intent(SignInActivity.this,MainActivity.class);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    });


    //textView sign Up command given
    binding.tvSignUp.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent=new Intent(SignInActivity.this,SignUpActivity.class);
            startActivity(intent);
        }
    });

     //Google sign in into account
    binding.btnGoogle.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
          signIn();
        }
    });


    if (auth.getCurrentUser()!=null){
        Intent intent=new Intent(SignInActivity.this,MainActivity.class);
        startActivity(intent);
    }
    }


    int RC_SIGN_IN=65;
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //on activity commands are given
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }
    //google sign in with firebase auth
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            Users users=new Users();
                            users.setUserId(user.getUid());
                            users.setUserName(user.getDisplayName());
                            users.setProfilePic(user.getPhotoUrl().toString());
                            database.getReference().child("Users").child(user.getUid()).setValue(users);

                            Intent intent=new Intent(SignInActivity.this,MainActivity.class);
                            startActivity(intent);
                            Toast.makeText(SignInActivity.this, "Sign in with Google", Toast.LENGTH_SHORT).show();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }

}