package lili.com.simpledownloader.wrapper;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by Administrator on 2017/5/8 0008.
 */

public class WrappedRequestBody extends RequestBody {
    public interface ProgressListener {
        void beforeExecute(long totalByte);

        void update(long sentBytes, boolean finished);
    }

    private final RequestBody requestBody;
    private final ProgressListener listener;
    private BufferedSink bufferedSink;

    public WrappedRequestBody(RequestBody requestBody, ProgressListener listener) {
        this.requestBody = requestBody;
        this.listener = listener;
//        if (listener != null) {
//            try {
//                listener.beforeExecute(contentLength());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (bufferedSink != null) {
            bufferedSink = Okio.buffer(sink(sink));
        }
        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    private Sink sink(BufferedSink sink) {
        return new ForwardingSink(sink) {
            long bytesWritten = 0L;
            long contentLength = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    contentLength = contentLength();
                }
                bytesWritten += byteCount != -1 ? byteCount : 0;
                if (listener != null) {
                    listener.beforeExecute(contentLength);
                    listener.update(bytesWritten, byteCount == -1);
                }

            }
        };
    }
}
