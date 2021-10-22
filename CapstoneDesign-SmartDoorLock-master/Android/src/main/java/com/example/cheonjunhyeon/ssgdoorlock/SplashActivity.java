package com.example.cheonjunhyeon.ssgdoorlock;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class SplashActivity extends Activity {
    private final String TAG = "SplashActivity";
    private final String[] permissions = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    SharedPreferences pref;
    Boolean isInitPasswd;
    Boolean isInitKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        initValue();

//        if (!chkPermissions()) {
//            ActivityCompat.requestPermissions(SplashActivity.this, permissions, 100);
//        }

        if (isInitPasswd)
            goChkPasswd();
        else {
            if (!chkPermissions()) {
                ActivityCompat.requestPermissions(SplashActivity.this, permissions, 100);
            }
        }

        if (!isInitKey) initKey();




    }

    private void initValue() {
        pref = getSharedPreferences("pref", MODE_PRIVATE);
        isInitPasswd = pref.getBoolean("isInitPasswd",FALSE);
        isInitKey       = pref.getBoolean("isInitKey", FALSE);
    }

    private Boolean chkPermissions() {
        Boolean rtn = true;

        for (String perm : permissions) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(SplashActivity.this, perm)
                    != PackageManager.PERMISSION_GRANTED) {

                rtn = false;
            }
        }

        return rtn;
    }

    private void initKey() {
        Log.d(TAG, "initKey()");
        PublicKey pubKey = null;
        PrivateKey priKey = null;
        SharedPreferences.Editor editor = pref.edit();

        SecureRandom secureRandom = new SecureRandom();
        KeyPairGenerator keyPairGenerator;

        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(20, secureRandom);

            KeyPair keyPair = keyPairGenerator.genKeyPair();
            pubKey = keyPair.getPublic();
            priKey = keyPair.getPrivate();

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec rsaPublicKeySpec = keyFactory.getKeySpec(pubKey, RSAPublicKeySpec.class);
            RSAPrivateKeySpec rsaPrivateKeySpec = keyFactory.getKeySpec(priKey, RSAPrivateKeySpec.class);

            editor.putInt("pubM", rsaPublicKeySpec.getModulus().intValue());
            editor.putInt("pubE", rsaPublicKeySpec.getPublicExponent().intValue());
            editor.putInt("priM", rsaPrivateKeySpec.getModulus().intValue());
            editor.putInt("priE", rsaPrivateKeySpec.getPrivateExponent().intValue());
            editor.putBoolean("isInitKey", TRUE);
            editor.commit();

            Log.d(TAG,"Public  key modulus : " + rsaPublicKeySpec.getModulus());
            Log.d(TAG,"Public  key exponent: " + rsaPublicKeySpec.getPublicExponent());
            Log.d(TAG,"Private key modulus : " + rsaPrivateKeySpec.getModulus());
            Log.d(TAG,"Private key exponent: " + rsaPrivateKeySpec.getPrivateExponent());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    private void goChkPasswd() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            Intent intent = new Intent(SplashActivity.this, ChkPasswdActivity.class);
            startActivity(intent);
            finish();
            }
        }, 1000);
    }

    private void goSetPasswd() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            Intent intent = new Intent(SplashActivity.this, SetPasswdActivity.class);
            startActivity(intent);
            finish();
            }
        }, 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults.length > 0) {
                goSetPasswd();
            }
        }
    }
}

