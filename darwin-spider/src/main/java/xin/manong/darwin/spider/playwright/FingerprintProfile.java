package xin.manong.darwin.spider.playwright;

/**
 * 指纹信息
 *
 * @author frankcl
 * @date 2026-04-21 16:20:49
 */
public record FingerprintProfile(
        String userAgent,
        String platform,
        int screenWidth,
        int screenHeight,
        String timezone,
        String locale,
        String webGLVendor,
        String webGLRenderer
) {
    public static final FingerprintProfile WINDOWS = new FingerprintProfile(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "Win32", 1920, 1080, "Asia/Shanghai", "zh-CN",
            "Google Inc. (NVIDIA)", "ANGLE (NVIDIA GeForce RTX 3060 Direct3D11)");

    public static final FingerprintProfile MAC = new FingerprintProfile(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36",
            "MacIntel", 2560, 1440, "Asia/Shanghai", "zh-CN",
            "Apple", "Apple M2");
}
