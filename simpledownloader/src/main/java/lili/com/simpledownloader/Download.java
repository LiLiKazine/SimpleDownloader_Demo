package lili.com.simpledownloader;

import android.os.RecoverySystem;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by LiLi on 2017/3/4.
 * lilikazine@gmail.com
 */

public class Download implements WrappedResponseBody.ProgressListener {

    private WrappedResponseBody.ProgressListener listener;

    private Call call;
    private String url;
    private File dest;
    private OkHttpClient client;

    private ProgressBar progressBar;

    private long totalLength = 0L;
    private long receivedBytes;
    private long breakPoints;

    private Boolean paused = false;



    public Download() {
        listener = this;
        client = getClient();
    }

    public OkHttpClient getClient() {
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response original = chain.proceed(chain.request());
                return original.newBuilder().body(new WrappedResponseBody(original.body(), listener)).build();
            }
        };
        return new OkHttpClient.Builder().addNetworkInterceptor(interceptor).build();
    }

    public Download url(@NonNull String url) {
        this.url = url;
        return this;
    }

    public Download setSavedPath(@NonNull File dest) {
        this.dest = dest;
        return this;
    }

    public Download setProgress(ProgressBar progressBar) {
        this.progressBar = progressBar;
        return this;
    }

    public void proceed() {
        if (url == null||dest==null) {
            return;
        }
        paused = false;
        Log.d("call!=null:", url + dest);
        call = newCall(0L);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                save(response, 0L);
            }
        });
    }

    public void pause() {
        if (call != null) {
            call.cancel();
        }
        breakPoints = receivedBytes;
        paused = true;
    }

    public void goOn() {
        if (!paused) {
            return;
        }
        paused = false;
        if (url == null || dest == null) {
            return;
        }
        call = newCall(breakPoints);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                save(response, breakPoints);
            }
        });
    }

    private Call newCall(long startPoint) {
        if (url == null) {
            return null;
        }
        Request request = new Request.Builder()
                .url(url)
                .header("RANGE", "bytes=" + startPoint + "-")
                .build();
        return client.newCall(request);
    }

    public void save(Response response, long startPoint) {
        ResponseBody body = response.body();
        InputStream in = body.byteStream();
        FileChannel channelOut = null;
        //appoint start position when continue a paused task
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(dest, "rwd");
            //Channel NIO，RandomAccessFile has no cache strategy，use directly is rather slow
            channelOut = randomAccessFile.getChannel();
            //Memery mapping，use RandomAccessFile directly，use seek function appoint where the download start ，use cache
            MappedByteBuffer mappedByteBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE, startPoint, body.contentLength());
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                mappedByteBuffer.put(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                if (channelOut != null) {
                    channelOut.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void beforeExecute(long totalLength) {
        if (progressBar != null) {
            if (this.totalLength == 0L) {
                this.totalLength = totalLength;
                progressBar.setMax((int) (totalLength / 1024));
            }
        }

    }

    @Override
    public void update(long receivedBytes, boolean finished) {
        this.receivedBytes = receivedBytes + breakPoints;
        progressBar.setProgress((int) (receivedBytes + breakPoints) / 1024);
        if (finished) {
            Log.d("qaq", "finished");
        }
    }
}
