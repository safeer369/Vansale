package com.teamayka.vansaleandmgmt.devtools;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.teamayka.vansaleandmgmt.R;

public class ExceptionActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception);

        final String log = String.valueOf(getIntent().getStringExtra("exception_log"));

        // show error in logcat
        Log.e(getString(R.string.app_name), String.valueOf(log));

        EditText tvLog = findViewById(R.id.etLog);
        tvLog.setText(log);

        findViewById(R.id.bLog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("message", log);
                manager.setPrimaryClip(clipData);
                Toast.makeText(ExceptionActivity.this, "text copied", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
