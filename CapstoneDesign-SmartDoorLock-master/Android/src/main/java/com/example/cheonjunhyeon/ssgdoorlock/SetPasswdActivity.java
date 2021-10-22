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

public class SetPasswdActivity extends Activity implements Button.OnClickListener {
    private int idx;
    private String ipt;
    private String ipt_one_more;
    private int methods;

    private Boolean chkHandler;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwd);
        Intent intent = this.getIntent();
        methods = intent.getIntExtra("methods", 0);

        initValue();

        setViewIptBlank();
        setEventView();
    }

    private void initValue() {
        idx = 0;
        ipt = "";
        ipt_one_more = "";
        chkHandler = false;
        pref = getSharedPreferences("pref", MODE_PRIVATE);
    }

    private void setViewIptBlank() {
        TextView tv = (TextView) findViewById(R.id.passwd_ipt_1);
        tv.setText(getText(R.string.passwd_ipt_blank));
        tv.setTextColor(ContextCompat.getColor(this,R.color.btn_passwd));
        tv = (TextView) findViewById(R.id.passwd_ipt_2);
        tv.setText(getText(R.string.passwd_ipt_blank));
        tv.setTextColor(ContextCompat.getColor(this,R.color.btn_passwd));
        tv = (TextView) findViewById(R.id.passwd_ipt_3);
        tv.setText(getText(R.string.passwd_ipt_blank));
        tv.setTextColor(ContextCompat.getColor(this,R.color.btn_passwd));
        tv = (TextView) findViewById(R.id.passwd_ipt_4);
        tv.setText(getText(R.string.passwd_ipt_blank));
        tv.setTextColor(ContextCompat.getColor(this,R.color.btn_passwd));
        tv = (TextView) findViewById(R.id.passwd_ipt_5);
        tv.setText(getText(R.string.passwd_ipt_blank));
        tv.setTextColor(ContextCompat.getColor(this,R.color.btn_passwd));
        tv = (TextView) findViewById(R.id.passwd_ipt_6);
        tv.setText(getText(R.string.passwd_ipt_blank));
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
        if(chkHandler) return;

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

        switch (idx % 6) {
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

        tv.setText(val);
        tv.setTextColor(ContextCompat.getColor(this,R.color.text));

        if(idx < 6) ipt += val;
        else        ipt_one_more += val;

        idx++;

        if (idx == 6) {
            chkHandler = true;

            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    TextView tv = (TextView) findViewById(R.id.passwd_guidMsg);
                    tv.setText(getText(R.string.passwd_guid_set_more));
                    setViewIptBlank();
                    chkHandler = false;

                }
            }, 300);
        }
        else if (idx == 12 && !ipt.equals(ipt_one_more)){
            chkHandler = true;

            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    reStart();
                    chkHandler = false;
                }
            }, 300);

        }
        else if (idx == 12 && ipt.equals(ipt_one_more)) {
            end();
        }
    }

    private void removeIpt() {
        if (idx % 6 ==  0) return;

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

        tv.setText(getText(R.string.passwd_ipt_blank));

        if (idx < 6) ipt = ipt.substring(0, --idx);
        else         ipt_one_more = ipt_one_more.substring(0, --idx);
    }

    private void reStart() {
        idx = 0;
        ipt = "";
        ipt_one_more = "";

        TextView tv = (TextView) findViewById(R.id.passwd_guidMsg);
        tv.setText(getText(R.string.passwd_guid_set_re));
        setViewIptBlank();
    }

    private void end() {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isInitPasswd", true);
        editor.putString("passwd", ipt);
        editor.commit();

        if(methods == 0) {
            Intent intent = new Intent(SetPasswdActivity.this, SetLonLagActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(SetPasswdActivity.this, DoorLockService.class); //다음넘어갈 컴포넌트 정의
            intent.putExtra("methods", DoorLockService.METHODS_CHANGE_PASSWD);

            startService(intent);

            intent = new Intent(SetPasswdActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
