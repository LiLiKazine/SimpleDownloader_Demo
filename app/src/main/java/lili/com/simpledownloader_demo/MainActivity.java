package lili.com.simpledownloader_demo;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import java.io.File;

import lili.com.simpledownloader.Http;

public class MainActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    public static final String PACKAGE_URL = "http://gdown.baidu.com/data/wisegame/df65a597122796a4/weixin_821.apk";
    private Http http;
    private File file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.download:

                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "sample.apk");
                http = new Http(this);
                http.url(PACKAGE_URL).setSavePath(file).setProgress(progressBar).proceedDownload();
//                Toast.makeText(this, "start", Toast.LENGTH_LONG).show();

                break;
            case R.id.pause:
                http.pause();
//                Toast.makeText(this, "paused", Toast.LENGTH_LONG).show();
                //存储此时的totalBytes，即断点位置
//                breakPoints = receivedBytes;
                break;
            case R.id.go_on:
                http.goOn();
//                Toast.makeText(this, "continue", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
