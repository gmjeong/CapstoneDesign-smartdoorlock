package com.example.cheonjunhyeon.ssgdoorlock;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ChkPasswdActivity extends Activity implements Button.OnClickListener {
    private int idx;
    private String passwd;
    private String ipt;

    private SharedPreferences pref;
    private Boolean chkHandlder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwd);

        initValue();

        setViewIptBlank();
        setEventView();
    }

    private void initValue() {
        idx = 0;
        ipt = "";
        pref = getSharedPreferences("pref", MODE_PRIVATE);
        passwd = pref.getString("passwd","123456");
        chkHandlder = false;
    }

    private void setViewIptBlank() {
        TextView tv = (TextView) findViewById(R.id.passwd_ipt_1);
        tv.setTextColor(ContextCompat.getColor(this,R.color.btn_passwd));
        tv = (TextView) findViewById(R.id.passwd_ipt_2);
        tv.setTextColor(ContextCompat.getColor(this,R.color.btn_passwd));
        tv = (TextView) findViewById(R.id.passwd_ipt_3);
        tv.setTextColor(ContextCompat.getColor(this,R.color.btn_passwd));
        tv = (TextView) findViewById(R.id.passwd_ipt_4);
        tv.setTextColor(ContextCompat.getColor(this,R.color.btn_passwd));
        tv = (TextView) findViewById(R.id.passwd_ipt_5);
        tv.setTextColor(ContextCompat.getColor(this,R.color.btn_passwd));
        tv = (TextView) findViewById(R.id.passwd_ipt_6);
        tv.setTextColor(ContextCompat.getColor(this,R.color.btn_passwd));
    }
    private void setEventView() {
        Button btn = (Button)findViewById(R.id.passwd_btn_0);
        btn.setOnClickListener(this);
        btn = (Button)findViewById(R.id.passwd_btn_1);
        btn.setOnClickListener(this);
        btn = (Button)findViewById(R.id.passwd_btn_2);
        btn.setOnClickListener(this);
        btn = (Button)findViewById(R.id.passwd_btn_3);
        btn.setOnClickListener(this);
        btn = (Button)findViewById(R.id.passwd_btn_4);
        btn.setOnClickListener(this);
        btn = (Button)findViewById(R.id.passwd_btn_5);
        btn.setOnClickListener(this);
        btn = (Button)findViewById(R.id.passwd_btn_6);
        btn.setOnClickListener(this);
        btn = (Button)findViewById(R.id.passwd_btn_7);
        btn.setOnClickListener(this);
        btn = (Button)findViewById(R.id.passwd_btn_8);
        btn.setOnClickListener(this);
        btn = (Button)findViewById(R.id.passwd_btn_9);
        btn.setOnClickListener(this);
        btn = (Button)findViewById(R.id.passwd_btn_erase);
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (chkHandlder) return;

        switch (view.getId()) {
            case R.id.passwd_btn_0:
                setIpt("0");
                break;
            case R.id.passwd_btn_1:
                setIpt("1");
                break;
            case R.id.passwd_btn_2:
                setIpt("2");
                break;
            case R.id.passwd_btn_3:
                setIpt("3");
                break;
            case R.id.passwd_btn_4:
                setIpt("4");
                break;
            case R.id.passwd_btn_5:
                setIpt("5");
                break;
            case R.id.passwd_btn_6:
                setIpt("6");
                break;
            case R.id.passwd_btn_7:
                setIpt("7");
                break;
            case R.id.passwd_btn_8:
                setIpt("8");
                break;
            case R.id.passwd_btn_9:
                setIpt("9");
                break;
            case R.id.passwd_btn_erase:
                removeIpt();
                break;
        }
    }

    private void setIpt(String val) {
        TextView tv = null;

        switch (idx) {
            case 0:
                tv = (TextView) findViewById(R.id.passwd_ipt_1);
                break;
            case 1:
                tv = (TextView) findViewById(R.id.passwd_ipt_2);
                break;
            case 2:
                tv = (TextView) findViewById(R.id.passwd_ipt_3);
                break;
            case 3:
                tv = (TextView) findViewById(R.id.passwd_ipt_4);
                break;
            case 4:
                tv = (TextView) findViewById(R.id.passwd_ipt_5);
                break;
            case 5:
                tv = (TextView) findViewById(R.id.passwd_ipt_6);
                break;
        }

        tv.setTextColor(ContextCompat.getColor(this,R.color.text));

        ipt += val;
        idx++;

        if (idx == 6 && !ipt.equals(passwd)) {
            chkHandlder = true;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    reStart();
                    chkHandlder = false;
                }
            }, 300);
        }
        else if (idx == 6 && ipt.equals(passwd)) {
            end();
        }
    }

    private void removeIpt() {
        if (idx ==  0) return;

        TextView tv = null;

        switch (idx) {
            case 1:
                tv = (TextView) findViewById(R.id.passwd_ipt_1);
                break;
            case 2:
                tv = (TextView) findViewById(R.id.passwd_ipt_2);
                break;
            case 3:
                tv = (TextView) findViewById(R.id.passwd_ipt_3);
                break;
            case 4:
                tv = (TextView) findViewById(R.id.passwd_ipt_4);
                break;
            case 5:
                tv = (TextView) findViewById(R.id.passwd_ipt_5);
                break;
            case 6:
                tv = (TextView) findViewById(R.id.passwd_ipt_6);
                break;
        }

        tv.setTextColor(ContextCompat.getColor(this,R.color.btn_passwd));

        if (idx < 6) ipt = ipt.substring(0, --idx);
    }

    private void reStart() {
        idx = 0;
        ipt = "";

        TextView tv = (TextView) findViewById(R.id.passwd_guidMsg);
        tv.setText(getText(R.string.passwd_guid_set_re));
        setViewIptBlank();
    }

    private void end() {
        Intent intent = new Intent(ChkPasswdActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
