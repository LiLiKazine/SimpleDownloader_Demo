package lili.com.simpledownloader_demo;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;

import lili.com.simpledownloader.Download;
import lili.com.simpledownloader.WrappedResponseBody;

public class MainActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private long breakPoints;
    public static final String PACKAGE_URL = "http://gdown.baidu.com/data/wisegame/df65a597122796a4/weixin_821.apk";
    private Download download;
    private File file;
    private long receivedBytes;
    private long totalLength = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.download:

                breakPoints = 0L;
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "sample.apk");
                download = new Download();
                download.url(PACKAGE_URL).setSavedPath(file).setProgress(progressBar).proceed();
                Toast.makeText(this, "start", Toast.LENGTH_LONG).show();

                break;
            case R.id.pause:
                download.pause();
                Toast.makeText(this, "paused", Toast.LENGTH_LONG).show();
                //存储此时的totalBytes，即断点位置
//                breakPoints = receivedBytes;
                break;
            case R.id.go_on:
                download.goOn();
//                Toast.makeText(this, "continue", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
