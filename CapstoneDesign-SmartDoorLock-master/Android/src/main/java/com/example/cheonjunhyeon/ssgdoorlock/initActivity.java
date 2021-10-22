package com.example.cheonjunhyeon.ssgdoorlock;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class initActivity extends Activity {
    private final String TAG = "initActivity";
    SharedPreferences pref;
    Boolean isInitAES;

    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        tv = (TextView) findViewById(R.id.loading_title);
        tv.setText(R.string.msg_loding);

        initValue();
        chkAES();
    }

    private void chkAES() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isInitAES){
                    Intent itt = new Intent(initActivity.this, DoorLockService.class);
                    itt.putExtra("methods", 4);
                    startService(itt);

                    Intent intent = new Intent(initActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    initValue();
                    chkAES();
                }


            }
        }, 1000);
    }

    private void initValue() {
        pref = getSharedPreferences("pref", MODE_PRIVATE);
        if(pref.getString("AES", null) == null)
            this.isInitAES = false;
        else
            this.isInitAES = true;
    }
}

