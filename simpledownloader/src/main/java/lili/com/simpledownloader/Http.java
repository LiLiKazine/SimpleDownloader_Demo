package lili.com.simpledownloader;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import lili.com.simpledownloader.wrapper.WrappedRequestBody;
import lili.com.simpledownloader.wrapper.WrappedResponseBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by LiLi on 2017/3/4.
 * lilikazine@gmail.com
 */

public class Http implements WrappedResponseBody.ProgressListener, WrappedRequestBody.ProgressListener {

    private final String TAG = getClass().getName();

    private WrappedResponseBody.ProgressListener downloadListener;
    private WrappedRequestBody.ProgressListener uploadListener;

    private Call call;
    private String url;
    private File dest;
    private File path;
    private OkHttpClient client;

    private ProgressBar progressBar;

    private long totalLength = 0L;
    private long receivedBytes;
    private long breakPoints;

    private Boolean paused = false;

    private Context context;

    enum METHOD{
        GET,HEAD,POST,PUT,DELETE,CONNECT,OPTIONS,TRACE,DOWNLOAD,UPLOAD
    }


    public Http(Context context) {
        downloadListener = this;
        uploadListener = this;
//        client = getDownloadClient();
        this.context = context;
    }

    public OkHttpClient getDownloadClient() {
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response original = chain.proceed(chain.request());
                return original.newBuilder().body(new WrappedResponseBody(original.body(), downloadListener)).build();
            }
        };
        return new OkHttpClient.Builder().addNetworkInterceptor(interceptor).build();
    }

    public OkHttpClient getUploadClient() {
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                Request request = originalRequest.newBuilder()
                        .method(originalRequest.method(), new WrappedRequestBody(originalRequest.body(), uploadListener))
                        .build();
                return chain.proceed(request);
            }
        };
        return new OkHttpClient.Builder().addNetworkInterceptor(interceptor).build();
    }

//    public Http proceed(METHOD method) {
//        switch (method) {
//            case DOWNLOAD:
//        }
//        return this;
//    }

    public Http url(@NonNull String url) {
        this.url = url;
        return this;
    }

    public Http setUploadFilePath(File path) {
        this.path = path;
        return this;
    }

    public Http setSavePath(@NonNull File dest) {
        this.dest = dest;
        return this;
    }

    public Http setProgress(ProgressBar progressBar) {
        this.progressBar = progressBar;
        return this;
    }

    public void proceedDownload() {
        if (url == null) {
            Log.e(TAG, "下载失败，url为空.");
            return;
        }
        if (dest == null) {
            Log.e(TAG, "下载失败，dest为空.");
            return;
        }
        paused = false;
        Toast.makeText(context, "started", Toast.LENGTH_LONG).show();

        Log.d("call!=null:", url + dest);

        client = getDownloadClient();
        call = newDownloadCall(0L);
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

    public void proceedUpload() {
        if (url == null) {
            Log.e(TAG, "上传失败，url为空.");
            return;
        }
        if (path == null) {
            Log.e(TAG, "下载失败，path为空.");
            return;
        }
        Toast.makeText(context, "started", Toast.LENGTH_LONG).show();
        Log.d("call!=null:", url + path);

        client = getUploadClient();
        call = newUploadCall();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void pause() {
        if (call != null) {
            call.cancel();
        }
        breakPoints = receivedBytes;
        paused = true;
        Toast.makeText(context, "paused", Toast.LENGTH_LONG).show();
    }

    public void goOn() {
        if (!paused) {
            return;
        }
        paused = false;
        Toast.makeText(context, "continue", Toast.LENGTH_LONG).show();

        if (url == null) {
            return;
        }
        call = newDownloadCall(breakPoints);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                save(response, breakPoints);
            }
        });
    }

    private Call newDownloadCall(long startPoint) {
        if (url == null) {
            return null;
        }
        Request request = new Request.Builder()
                .url(url)
                .header("RANGE", "bytes=" + startPoint + "-")
                .build();
        return client.newCall(request);
    }

    private Call newUploadCall() {
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), path);
        String fileName = "test_file";
        String boundary = "xx--------------------------------------------------------------xx";

        MultipartBody multipartBody = new MultipartBody.Builder(boundary)
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_files", "files")
                .addFormDataPart("file", fileName, fileBody)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(multipartBody)
                .build();
        return client.newCall(request);

//        client = new OkHttpClient.Builder()
//                .addInterceptor(new Interceptor() {
//                    @Override
//                    public Response intercept(Chain chain) throws IOException {
//                        Request originalRequest = chain.request();
//                        Request request = originalRequest.newBuilder()
//                                .method(originalRequest.method(), new WrappedRequestBody(originalRequest.body(), uploadListener))
//                                .build();
//                        return chain.proceed(request);
//                    }
//                })
//                .build();
    }

    public void save(Response response, long startPoint) {
        if (dest == null) {
            dest = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "sample.apk");
        }
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
        if (progressBar != null) {
            progressBar.setProgress((int) (receivedBytes + breakPoints) / 1024);
        }
        if (finished) {
            Log.d("qaq", "finished");

            Flowable.just("finished")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String str) throws Exception {
                            Toast.makeText(context, str, Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}
