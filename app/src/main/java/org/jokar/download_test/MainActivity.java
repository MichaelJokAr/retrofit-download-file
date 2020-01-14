package org.jokar.download_test;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jokar.permission.PermissionUtil;

import org.jokar.download_test.bean.Download;
import org.jokar.download_test.service.DownloadService;
import org.jokar.download_test.utils.StringUtils;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class MainActivity extends AppCompatActivity {
    public static final String MESSAGE_PROGRESS = "message_progress";
    private static final String TAG = "MainActivity";
    private LocalBroadcastManager bManager;


    private AppCompatButton btn_download;
    private ProgressBar progress;
    private TextView progress_text;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(MESSAGE_PROGRESS)) {

                Download download = intent.getParcelableExtra("download");
                progress.setProgress(download.getProgress());
                if (download.getProgress() == 100) {

                    progress_text.setText("File Download Complete");

                } else {

                    progress_text.setText(
                            StringUtils.getDataSize(download.getCurrentFileSize())
                                    + "/" +
                                    StringUtils.getDataSize(download.getTotalFileSize()));

                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_download = (AppCompatButton) findViewById(R.id.btn_download);
        progress = (ProgressBar) findViewById(R.id.progress);
        progress_text = (TextView) findViewById(R.id.progress_text);

        registerReceiver();

        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PermissionUtil.Builder(MainActivity.this)
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                        .setDenied(() -> {
                            toast("denied");
                            return null;
                        })
                        .setGrant(() -> {
                            Intent intent = new Intent(MainActivity.this, DownloadService.class);
                            startService(intent);
                            return null;
                        })
                        .setNeverAskAgain(() -> {
                            toast("neverAsk");
                            return null;
                        })
                        .request();

            }
        });
    }

    private void registerReceiver() {

        bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_PROGRESS);
        bManager.registerReceiver(broadcastReceiver, intentFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解除注册时，使用注册时的manager解绑
        bManager.unregisterReceiver(broadcastReceiver);
    }

    private void toast(String value) {
        Toast.makeText(getApplicationContext(), value, Toast.LENGTH_SHORT).show();
    }
}
