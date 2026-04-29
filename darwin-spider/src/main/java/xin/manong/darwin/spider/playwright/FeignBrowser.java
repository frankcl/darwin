package xin.manong.darwin.spider.playwright;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Proxy;
import com.microsoft.playwright.options.ScreenSize;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.service.iface.ProxyService;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 伪装浏览器
 *
 * @author frankcl
 * @date 2026-04-21 16:27:08
 */
public class FeignBrowser implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(FeignBrowser.class);

    private static final String TEMP_DIRECTORY = "/tmp/playwright/";
    private static final String FINGERPRINT_SCRIPT_TEMPLATE = """
            (() => {
                // 1. 去除 webdriver 标记
                Object.defineProperty(navigator, 'webdriver', { get: () => undefined });
            
                // 2. platform
                Object.defineProperty(navigator, 'platform', { get: () => '%s' });
            
                // 3. languages
                Object.defineProperty(navigator, 'languages', { get: () => %s });
            
                // 4. hardwareConcurrency
                Object.defineProperty(navigator, 'hardwareConcurrency', { get: () => %d });
            
                // 5. deviceMemory
                Object.defineProperty(navigator, 'deviceMemory', { get: () => %d });
            
                // 6. plugins（headless 默认为空）
                const pluginData = [
                    { name: 'Chrome PDF Plugin', filename: 'internal-pdf-viewer', description: 'Portable Document Format' },
                    { name: 'Chrome PDF Viewer', filename: 'mhjfbmdgcfjbbpaeojofohoefgiehjai', description: '' },
                    { name: 'Native Client', filename: 'internal-nacl-plugin', description: '' }
                ];
                Object.defineProperty(navigator, 'plugins', {
                    get: () => {
                        const arr = pluginData.map(p => {
                            const plugin = Object.create(Plugin.prototype);
                            Object.defineProperty(plugin, 'name', { value: p.name });
                            Object.defineProperty(plugin, 'filename', { value: p.filename });
                            Object.defineProperty(plugin, 'description', { value: p.description });
                            return plugin;
                        });
                        arr.__proto__ = PluginArray.prototype;
                        return arr;
                    }
                });
            
                // 7. Canvas 噪声
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
            
                // 8. WebGL 指纹
                const patchWebGL = (gl) => {
                    if (!gl) return;
                    const origGetParam = gl.getParameter.bind(gl);
                    gl.getParameter = function(param) {
                        if (param === 37445) return '%s';
                        if (param === 37446) return '%s';
                        return origGetParam(param);
                    };
                    const origGetExt = gl.getExtension.bind(gl);
                    gl.getExtension = function(name) {
                        if (name === 'WEBGL_debug_renderer_info') {
                            return { UNMASKED_VENDOR_WEBGL:   37445, UNMASKED_RENDERER_WEBGL: 37446 };
                        }
                        return origGetExt(name);
                    };
                };
                const origGetCtx = HTMLCanvasElement.prototype.getContext;
                HTMLCanvasElement.prototype.getContext = function(type, attrs) {
                    const ctx = origGetCtx.call(this, type, attrs);
                    if (type === 'webgl' || type === 'webgl2') patchWebGL(ctx);
                    return ctx;
                };
            
                // 9. 屏幕尺寸（Linux headless 默认 0x0）
                Object.defineProperty(screen, 'width', { get: () => %d });
                Object.defineProperty(screen, 'height', { get: () => %d });
                Object.defineProperty(screen, 'availWidth', { get: () => %d });
                Object.defineProperty(screen, 'availHeight', { get: () => %d });
                Object.defineProperty(screen, 'colorDepth', { get: () => %d });
                Object.defineProperty(screen, 'pixelDepth', { get: () => %d });
            
                // 10. window 尺寸
                Object.defineProperty(window, 'outerWidth', { get: () => %d });
                Object.defineProperty(window, 'outerHeight', { get: () => %d });
            
                // 11. chrome 对象（headless 可能缺失）
                if (!window.chrome) {
                    window.chrome = {
                        runtime: {
                            connect: () => {},
                            sendMessage: () => {},
                            onMessage:   { addListener: () => {} }
                        },
                        loadTimes: () => ({
                            requestTime: Date.now() / 1000,
                            startLoadTime: Date.now() / 1000,
                            commitLoadTime: Date.now() / 1000,
                            finishDocumentLoadTime: Date.now() / 1000,
                            finishLoadTime: Date.now() / 1000,
                            firstPaintTime: Date.now() / 1000,
                            firstPaintAfterLoadTime: 0,
                            navigationType: 'Other',
                            wasFetchedViaSpdy: false,
                            wasNpnNegotiated: false,
                            npnNegotiatedProtocol: 'unknown',
                            wasAlternateProtocolAvailable: false,
                            connectionInfo: 'unknown'
                        }),
                        csi: () => ({
                            startE:  Date.now(),
                            onloadT: Date.now(),
                            pageT:   Date.now(),
                            tran:    15
                        })
                    };
                }
            
                // 12. permissions
                const origQuery = navigator.permissions.query.bind(navigator.permissions);
                navigator.permissions.query = (params) => {
                    if (params.name === 'notifications') {
                        return Promise.resolve({ state: 'denied', onchange: null });
                    }
                    return origQuery(params);
                };
            
            })();
            """;
    @Setter
    private String tempDirectory;
    private final FingerprintProfile profile;
    private final SessionManager sessionManager;
    @Setter
    private ProxyService proxyService;
    private Browser browser;
    private Playwright playwright;

    public FeignBrowser(FingerprintProfile profile) {
        this(profile, null, 10);
    }

    public FeignBrowser(FingerprintProfile profile,
                        String executablePath,
                        Integer maxSessions) {
        if (maxSessions == null || maxSessions <= 0) maxSessions = 10;
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
                        "--disable-features=IsolateOrigins",
                        "--disable-dev-shm-usage",
                        "--disable-gpu",
                        "--disable-setuid-sandbox",
                        "--window-size=" + profile.screenWidth() + "," + profile.screenHeight()));
        if (StringUtils.isNotEmpty(executablePath)) launchOptions.setExecutablePath(Path.of(executablePath));
        this.browser = playwright.chromium().launch(launchOptions);
        this.sessionManager = new SessionManager(this, maxSessions);
    }

    /**
     * 抓取数据
     *
     * @param request 抓取请求
     * @return 抓取响应
     */
    public FetchResponse fetch(FetchRequest request) {
        Session session = sessionManager.acquire(request);
        try {
            session.setTempDirectory(tempDirectory);
            return session.fetch(request);
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
     * @throws IOException 异常
     */
    @Override
    public void close() throws IOException {
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }

    /**
     * 新建上下文
     *
     * @param request 抓取请求
     * @return 上下文
     */
    BrowserContext newContext(FetchRequest request) {
        Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setUserAgent(profile.userAgent())
                .setViewportSize(profile.screenWidth(), profile.screenHeight())
                .setScreenSize(new ScreenSize(profile.screenWidth(), profile.screenHeight()))
                .setTimezoneId(profile.timezone())
                .setLocale(profile.locale())
                .setAcceptDownloads(true)
                .setExtraHTTPHeaders(Map.of(
                        "Accept-Language", profile.language(),
                        "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                        "Accept-Encoding", "gzip, deflate, br",
                        "Cache-Control",   "no-cache",
                        "Pragma",          "no-cache"
                ));
        Integer fetchMethod = request.getFetchMethod();
        if (Objects.equals(fetchMethod, Constants.FETCH_METHOD_LONG_PROXY) ||
                Objects.equals(fetchMethod, Constants.FETCH_METHOD_SHORT_PROXY)) {
            xin.manong.darwin.common.model.Proxy newProxy = proxyService.randomGet(fetchMethod);
            if (newProxy != null) {
                Proxy proxy = new Proxy(String.format("http://%s:%d", newProxy.address, newProxy.port));
                if (StringUtils.isNotEmpty(newProxy.username) && StringUtils.isNotEmpty(newProxy.password)) {
                    proxy.setUsername(newProxy.username).setPassword(newProxy.password);
                }
                options.setProxy(proxy);
                logger.info("Fetch URL:{} with proxy:{}:{}",
                        request.getRequestURL(), newProxy.address, newProxy.port);
            }
        }
        BrowserContext context = browser.newContext(options);
        context.addInitScript(buildFingerprintScript());
        return context;
    }

    /**
     * 构建指纹脚本
     *
     * @return 指纹脚本
     */
    private String buildFingerprintScript() {
        return FINGERPRINT_SCRIPT_TEMPLATE.formatted(
                profile.platform(),
                profile.buildLanguages(),
                profile.hardwareConcurrency(),
                profile.deviceMemory(),
                profile.webGLVendor(),
                profile.webGLRenderer(),
                profile.screenWidth(), profile.screenHeight(),
                profile.screenWidth(), profile.availHeight(),
                profile.colorDepth(), profile.colorDepth(),
                profile.screenWidth(), profile.screenHeight());
    }
}
