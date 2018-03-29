package com.shtainyky.customview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.shtainyky.customview.views.CustomProgressView;

/**
 * Created by Irina Shtain on 01.04.2017.
 */
public class ActivityProgress extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        final CustomProgressView progressView = (CustomProgressView) findViewById(R.id.my_progress);
        Button bt_start = (Button) findViewById(R.id.bt_start);
        bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressView.startAnimation();
            }
        });

        Button bt_stop = (Button) findViewById(R.id.bt_stop);
        bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressView.cancelAnimationAndHide();
            }
        });

    }
}
