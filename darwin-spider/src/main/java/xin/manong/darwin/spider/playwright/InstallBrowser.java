package xin.manong.darwin.spider.playwright;

import com.microsoft.playwright.CLI;

/**
 * @author frankcl
 * @date 2026-04-30 17:04:47
 */
public class InstallBrowser {

    public static void main(String[] args) throws Exception {
        CLI.main(new String[]{"install", "chromium"});
    }
}
