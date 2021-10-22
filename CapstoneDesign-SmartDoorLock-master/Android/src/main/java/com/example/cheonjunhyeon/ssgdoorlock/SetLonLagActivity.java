package com.example.cheonjunhyeon.ssgdoorlock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class SetLonLagActivity extends AppCompatActivity {
    String TAG = ".SetLonLagActivity";

    private SharedPreferences pref;
    private Location homeAddr;

    TextView tv;
    EditText addrInput;
    ImageButton btnSetAddr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addr);

        final Geocoder geocoder = new Geocoder(this);
        homeAddr = new Location("HOME");
        pref = getSharedPreferences("pref", MODE_PRIVATE);

        tv = (TextView) findViewById(R.id.addr_exp);
        addrInput = (EditText) findViewById(R.id.addr_input);

        btnSetAddr = (ImageButton) findViewById(R.id.btn_addr);
        btnSetAddr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(addrInput.getText().equals("")) {
                    Toast.makeText(SetLonLagActivity.this, "주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                else {
                    List<Address> list = null;

                    String addrStr = addrInput.getText().toString();
                    try {
                        list = geocoder.getFromLocationName(
                                addrStr,
                                10);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "입출력 오류 - 서버에서 주소변환시 ERROR 발생");
                    }

                    if (list != null) {
                        if (list.size() == 0) {
                            Toast.makeText(SetLonLagActivity.this, "해당되는 주소 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else {
                            homeAddr.setLatitude(list.get(0).getLatitude());
                            homeAddr.setLongitude(list.get(0).getLongitude());

                            Log.d(TAG, String.valueOf(homeAddr.getLatitude()) + "  " + String.valueOf(homeAddr.getLongitude()));

                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("homeLatitude", String.valueOf(homeAddr.getLatitude()));
                            editor.putString("homeLongitude", String.valueOf(homeAddr.getLongitude()));
                            editor.commit();

                            Intent intent = new Intent(SetLonLagActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }

            }
        });

    }
}
