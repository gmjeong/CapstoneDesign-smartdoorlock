package com.example.cheonjunhyeon.ssgdoorlock;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import static java.lang.Boolean.FALSE;

public class MainActivity extends AppCompatActivity implements Button.OnClickListener{
    private final String TAG = "main";

    private Boolean isInit;
    private SharedPreferences pref;

    BluetoothDevice bdevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initValue();
        setEventView();

        if (isInit){
            Intent intent = new Intent(MainActivity.this, DoorLockService.class);
            intent.putExtra("methods", DoorLockService.METHODS_RELOAD);

            startService(intent);
        } else {
            Intent intent = new Intent(this, ConnectionActivity.class);
            intent.putExtra("from", "activity");
            startActivityForResult(intent, 101);
        }
    }

    private void initValue() {
        pref = getSharedPreferences("pref", MODE_PRIVATE);
        isInit = pref.getBoolean("isInit",FALSE);
    }

    private void setEventView() {
        ImageButton btn = (ImageButton)findViewById(R.id.btn_open_main);
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_open_main:
                dlServiceOpen();
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_passwd:
                Intent intent = new Intent(MainActivity.this, SetPasswdActivity.class);
                intent.putExtra("methods", DoorLockService.METHODS_CHANGE_PASSWD);
                startActivity(intent);
                return true;
            default:
                return true;
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent intent = null;
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED)
            finish();

        if (resultCode != RESULT_OK)
            return;

        if (requestCode == 101) {
            bdevice = data.getParcelableExtra("bdevice");
            Log.d("TEST", bdevice.getName());

            intent = new Intent(MainActivity.this, DoorLockService.class); //다음넘어갈 컴포넌트 정의
            intent.putExtra("methods", DoorLockService.METHODS_INIT);
            intent.putExtra("bdevice", bdevice);

            startService(intent);

            Intent int1 = new Intent(MainActivity.this, initActivity.class);
            startActivity(int1);
        }
    }


    private void reset() {
//        SharedPreferences.Editor editor = pref.edit();
//        editor.remove("isInit");
//        editor.remove("isInitPasswd");
//        editor.remove("Address");
//        editor.commit();
        Intent intent = new Intent(MainActivity.this, DoorLockService.class); //다음넘어갈 컴포넌트 정의
        intent.putExtra("methods", 100);

        startService(intent);
    }

    private void dlServiceClose() {
//        Intent intent = new Intent(MainActivity.this, DoorLockService.class); //다음넘어갈 컴포넌트 정의
//        intent.putExtra("methods", DoorLockService.METHODS_CLOSE);
//
//        startService(intent);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("isInit");
        editor.remove("isInitPasswd");
        editor.remove("Address");
        editor.commit();
    }

    private void dlServiceOpen() {
        Intent intent = new Intent(MainActivity.this, DoorLockService.class); //다음넘어갈 컴포넌트 정의
        intent.putExtra("methods", DoorLockService.METHODS_OPEN);

        startService(intent);
    }
}

