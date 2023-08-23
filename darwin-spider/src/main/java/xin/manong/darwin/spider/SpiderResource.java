package xin.manong.darwin.spider;

import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * 抓取内容资源
 *
 * @author frankcl
 * @date 2023-08-22 15:24:58
 */
public class SpiderResource implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(SpiderResource.class);

    /**
     * 是否需要猜测编码
     */
    public Boolean guessCharset;
    /**
     * 资源MIME类型
     */
    public String mimeType;
    /**
     * 资源MIME子类型
     */
    public String subMimeType;
    /**
     * 资源编码
     */
    public Charset charset;
    /**
     * 资源输入流
     */
    public InputStream inputStream;
    /**
     * HTTP响应对象
     */
    private Response httpResponse;

    public SpiderResource() {
        this.guessCharset = false;
    }

    public SpiderResource(Response httpResponse) {
        this.httpResponse = httpResponse;
        this.inputStream = httpResponse.body().byteStream();
        this.guessCharset = true;
    }

    /**
     * 销毁抓取资源
     */
    @Override
    public void close() throws IOException {
        try {
            if (inputStream != null) inputStream.close();
            if (httpResponse != null) httpResponse.close();
        } catch (Exception e) {
            logger.error("close spider resource failed");
            logger.error(e.getMessage(), e);
        }
    }
}
