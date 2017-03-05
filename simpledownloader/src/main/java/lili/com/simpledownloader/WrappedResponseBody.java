package lili.com.simpledownloader;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by LiLi on 2017/3/4.
 * lilikazine@gmail.com
 */

public class WrappedResponseBody extends ResponseBody {

    public interface ProgressListener {
        void beforeExecute(long totalLength);

        void update(long receivedBytes, boolean finished);
    }

    private final ResponseBody responseBody;
    private final ProgressListener listener;
    private BufferedSource bufferedSource;

    public WrappedResponseBody(ResponseBody responseBody, ProgressListener listener) {
        this.responseBody = responseBody;
        this.listener = listener;
        if (listener != null) {
            listener.beforeExecute(contentLength());
        }
    }

    private Source Source(Source source) {
        return new ForwardingSource(source) {
            long receivedBytes = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                receivedBytes += bytesRead != -1 ? bytesRead : 0;
                if (listener != null) {
                    listener.update(receivedBytes, bytesRead == -1);
                }
                return bytesRead;
            }
        };
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(Source(responseBody.source()));
        }
        return bufferedSource;
    }
}
