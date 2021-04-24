package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.whatsapp.Modelss.Users;
import com.example.whatsapp.databinding.ActivitySignInPhoneBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class SignInPhoneActivity extends AppCompatActivity {

    EditText etPhone,etVerifyCode;
    Button btnGetVf,btnSignInPhn;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ActivitySignInPhoneBinding binding;

    String codeSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  binding=ActivitySignInPhoneBinding.inflate(getLayoutInflater());
       //setContentView(binding.getRoot());

        setContentView(R.layout.activity_sign_in_phone);

        auth=FirebaseAuth.getInstance();
      //  auth.setLanguageCode("fr");

        //actionbar hiding
        getSupportActionBar().hide();

        etPhone=findViewById(R.id.etPhone);
        etVerifyCode=findViewById(R.id.etVerifyCode);
        btnGetVf=findViewById(R.id.btnGetvf);
        btnSignInPhn=findViewById(R.id.btnSignInPhn);

        btnGetVf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode();
            }
        });

       btnSignInPhn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifySignInCode();
            }
        });

        /*binding.btnGetvf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode();
            }
        });

        binding.btnSignInPhn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifySignInCode();
            }
        });*/

    }

    private void  verifySignInCode(){

        String code=etVerifyCode.getText().toString();
        if (code.isEmpty()){
            etVerifyCode.setError("Verification Code is required");
            etVerifyCode.requestFocus();
            return;
        }
        /* if(binding.etVerifyCode.getText().toString().isEmpty()){
                binding.etVerifyCode.setError("Verification code is required");
                return;
            }*/
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
        signInWithPhoneAuthCredential(credential);

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d("TAG", "signInWithCredential:success");
                            Intent intent=new Intent(SignInPhoneActivity.this,MainActivity.class);
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(),
                                    "Login Successful", Toast.LENGTH_LONG).show();


                            FirebaseUser user = task.getResult().getUser();
                          /*  Users users=new Users();
                            users.setUserId(user.getUid());
                            users.setUserName(user.getDisplayName());
                            users.setProfilePic(user.getPhotoUrl().toString());
                            database.getReference().child("Users").child(user.getUid()).setValue(users);*/
                        }
                        else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                                Log.w("TAG", "signInWithCredential:failure", task.getException());
                                Toast.makeText(getApplicationContext(),
                                        "Incorrect Verification Code", Toast.LENGTH_LONG).show();
                        }
                        }

                    }
                });
    }


    private void sendVerificationCode(){

       String phone =etPhone.getText().toString();

        if (phone.isEmpty()){
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return;
        }
        if (phone.length()<10){
            etPhone.setError("Please enter a valid phone");
            etPhone.requestFocus();
            return;
        }
     /* if(binding.etPhone.getText().toString().isEmpty()){
                binding.etPhone.setError("Phone number is required");
                return;
           }

        if(binding.etPhone.getText().toString().length()<10){
            binding.etPhone.setError("Please enter a valid phone");
            return;
     }*/

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);


    }


    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {

        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            codeSent =s;
        }
    };
}