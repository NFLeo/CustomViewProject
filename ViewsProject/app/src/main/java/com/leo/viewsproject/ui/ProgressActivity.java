package com.leo.viewsproject.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.leo.viewsproject.R;
import com.leo.viewsproject.widget.PKProgressBar;

public class ProgressActivity extends AppCompatActivity {

    private Button btnstart;
    private EditText ev_left, ev_right;
    private PKProgressBar pkProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        this.btnstart = findViewById(R.id.btn_start);
        this.ev_left = findViewById(R.id.ev_left);
        this.ev_right = findViewById(R.id.ev_right);
        pkProgressBar = findViewById(R.id.progress);


        btnstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String stringL = ev_left.getText().toString();
                String stringR = ev_right.getText().toString();
                long parseFloatl = Long.parseLong(stringL);
                long parseFloatr = Long.parseLong(stringR);
                pkProgressBar.setAnimProgress(parseFloatl, parseFloatr);
            }
        });
    }
}
