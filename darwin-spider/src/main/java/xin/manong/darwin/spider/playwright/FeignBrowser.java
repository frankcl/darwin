package xin.manong.darwin.spider.playwright;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.ScreenSize;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * 伪装浏览器
 *
 * @author frankcl
 * @date 2026-04-21 16:27:08
 */
public class FeignBrowser implements AutoCloseable {

    private static final String TEMP_DIRECTORY = "/tmp/playwright/";
    private static final String FINGERPRINT_SCRIPT_TEMPLATE = """
            // 1. 去除 webdriver 标记
            Object.defineProperty(navigator, 'webdriver', { get: () => undefined });

            // 2. 伪造 platform
            Object.defineProperty(navigator, 'platform', { get: () => '%s' });

            // 3. 伪造 plugins
            Object.defineProperty(navigator, 'plugins', {
                get: () => Object.create(PluginArray.prototype, {
                    length: { value: 3 },
                    0: { value: { name: 'Chrome PDF Plugin',   filename: 'internal-pdf-viewer' } },
                    1: { value: { name: 'Chrome PDF Viewer',   filename: 'mhjfbmdgcfjbbpaeojofohoefgiehjai' } },
                    2: { value: { name: 'Native Client',       filename: 'internal-nacl-plugin' } }
                })
            });

            // 4. Canvas 噪声注入
            const origToDataURL = HTMLCanvasElement.prototype.toDataURL;
            HTMLCanvasElement.prototype.toDataURL = function(type) {
                const ctx = this.getContext('2d');
                if (ctx) {
                    const d = ctx.getImageData(0, 0, this.width || 1, this.height || 1);
                    for (let i = 0; i < d.data.length; i += 200) {
                        d.data[i] ^= (Math.random() * 2) | 0;
                    }
                    ctx.putImageData(d, 0, 0);
                }
                return origToDataURL.apply(this, arguments);
            };

            // 5. WebGL 指纹伪造
            const origGetParam = WebGLRenderingContext.prototype.getParameter;
            WebGLRenderingContext.prototype.getParameter = function(p) {
                if (p === 37445) return '%s';
                if (p === 37446) return '%s';
                return origGetParam.apply(this, arguments);
            };

            // 6. 屏幕尺寸
            Object.defineProperty(screen, 'width',  { get: () => %d });
                        Object.defineProperty(screen, 'height', { get: () => %d });
            """;

    @Setter
    private String tempDirectory;
    private final FingerprintProfile profile;
    private final SessionManager sessionManager;
    private Browser browser;
    private Playwright playwright;

    public FeignBrowser(FingerprintProfile profile) {
        this(profile, null);
    }

    public FeignBrowser(FingerprintProfile profile,
                        String executablePath) {
        this.tempDirectory = TEMP_DIRECTORY;
        this.profile = profile;
        this.playwright = Playwright.create();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(List.of(
                        "--disable-blink-features=AutomationControlled",
                        "--no-sandbox",
                        "--disable-web-security",           // 禁用同源策略
                        "--disable-site-isolation-trials",  // 禁用站点隔离
                        "--disable-dev-shm-usage",
                        "--disable-gpu",
                        "--disable-setuid-sandbox"));
        if (StringUtils.isNotEmpty(executablePath)) launchOptions.setExecutablePath(Path.of(executablePath));
        this.browser = playwright.chromium().launch(launchOptions);
        this.sessionManager = new SessionManager(this, 10);
    }

    /**
     * 抓取数据
     *
     * @param request 抓取请求
     * @return 抓取响应
     */
    public FetchResponse fetch(FetchRequest request) {
        Session session = sessionManager.acquire();
        try {
            session.setTempDirectory(tempDirectory);
            return session.execute(request);
        } finally {
            sessionManager.release(session);
        }
    }

    /**
     * 抓取数据
     *
     * @param requestURL 抓取URL
     * @return 响应资源
     */
    public FetchResponse fetch(String requestURL) {
        FetchRequest request = FetchRequest.builder().requestURL(requestURL).build();
        return fetch(request);
    }

    /**
     * 关闭浏览器
     *
     * @throws Exception 异常
     */
    @Override
    public void close() throws Exception {
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }

    BrowserContext newContext() {
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent(profile.userAgent())
                .setViewportSize(profile.screenWidth(), profile.screenHeight())
                .setScreenSize(new ScreenSize(profile.screenWidth(), profile.screenHeight()))
                .setTimezoneId(profile.timezone())
                .setLocale(profile.locale())
                .setAcceptDownloads(true)
                .setExtraHTTPHeaders(Map.of(
                        "Accept-Language", profile.locale() + ",en;q=0.8",
                        "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                        "Accept-Encoding", "gzip, deflate, br",
                        "Cache-Control",   "no-cache",
                        "Pragma",          "no-cache"
                ))
        );
        context.addInitScript(buildFingerprintScript());
        return context;
    }

    /**
     * 构建指纹脚本
     *
     * @return 指纹脚本
     */
    private String buildFingerprintScript() {
        return FINGERPRINT_SCRIPT_TEMPLATE.formatted(profile.platform(), profile.webGLVendor(),
                profile.webGLRenderer(), profile.screenWidth(), profile.screenHeight());
    }
}
