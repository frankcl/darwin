package xin.manong.darwin.spider.playwright;

/**
 * 指纹信息
 *
 * @author frankcl
 * @date 2026-04-21 16:20:49
 */
public record FingerprintProfile(
        String  userAgent,
        String  platform,
        int     screenWidth,
        int     screenHeight,
        int     availHeight,        // 屏幕可用高度（减去任务栏）
        String  timezone,
        String  locale,
        String  language,
        int     hardwareConcurrency,
        int     deviceMemory,
        String  webGLVendor,
        String  webGLRenderer,
        int     colorDepth,
        String  chromeVersion
) {
    public static final FingerprintProfile WIN10 = new FingerprintProfile(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "Win32",
            1920, 1080, 1040,
            "Asia/Shanghai", "zh-CN", "zh-CN,zh;q=0.9,en;q=0.8",
            8, 8,
            "Google Inc. (NVIDIA)",
            "ANGLE (NVIDIA GeForce RTX 3060 Direct3D11 vs_5_0 ps_5_0)",
            24, "124");

    public static final FingerprintProfile WIN11 = new FingerprintProfile(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "Win32",
            2560, 1440, 1400,
            "Asia/Shanghai", "zh-CN", "zh-CN,zh;q=0.9,en;q=0.8",
            16, 16,
            "Google Inc. (NVIDIA)",
            "ANGLE (NVIDIA GeForce RTX 4080 Direct3D11 vs_5_0 ps_5_0)",
            24, "124"
    );

    public static final FingerprintProfile MAC = new FingerprintProfile(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "MacIntel",
            2560, 1440, 1395,
            "Asia/Shanghai", "zh-CN", "zh-CN,zh;q=0.9,en;q=0.8",
            8, 16,
            "Apple",
            "Apple M2",
            30, "124");

    /**
     * 构建语言字符串表示
     *
     * @return 语言字符串表示
     */
    public String buildLanguages() {
        String[] langs = language().split(",");
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < langs.length; i++) {
            String lang = langs[i].split(";")[0].trim();
            sb.append("'").append(lang).append("'");
            if (i < langs.length - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
