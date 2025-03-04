package xin.manong.darwin.parser.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.parser.service.ScopeExtractService;
import xin.manong.darwin.parser.service.request.HTMLParseRequest;
import xin.manong.weapon.base.util.CommonUtil;
import xin.manong.weapon.base.util.DomainUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 范围抽链服务实现
 *
 * @author frankcl
 * @date 2023-11-15 14:49:36
 */
@Service
public class ScopeExtractServiceImpl implements ScopeExtractService {

    private static final Logger logger = LoggerFactory.getLogger(ScopeExtractServiceImpl.class);

    private static final String TAG_NAME_A = "a";
    private static final String ATTR_NAME_HREF = "href";
    private static final String ATTR_NAME_STYLE = "style";

    @Override
    public ParseResponse parse(HTMLParseRequest request) {
        String parentURL = StringUtils.isEmpty(request.redirectURL) ? request.url : request.redirectURL;
        Document document = Jsoup.parse(request.html, parentURL);
        Element body = document.body();
        List<URLRecord> childURLs = new ArrayList<>();
        scopeExtract(body, parentURL, request.scope, childURLs);
        return ParseResponse.buildOK(null, childURLs, null);
    }

    /**
     * 抽取链接
     *
     * @param element 元素
     * @param parentURL 父URL
     * @param scope 抽链范围
     * @param childURLs 抽链结果
     */
    private void scopeExtract(Element element, String parentURL, int scope,
                              List<URLRecord> childURLs) {
        if (!isVisible(element)) return;
        if (element.tagName().equals(TAG_NAME_A)) {
            if (!element.hasAttr(ATTR_NAME_HREF)) return;
            String childURL = element.absUrl(ATTR_NAME_HREF);
            if (!supportExtract(childURL, scope, parentURL)) return;
            try {
                new URL(childURL);
                childURLs.add(new URLRecord(childURL));
            } catch (Exception e) {
                logger.warn("invalid child URL[{}]", childURL);
            }
            return;
        }
        Elements children = element.children();
        for (Element child : children) scopeExtract(child, parentURL, scope, childURLs);
    }

    /**
     * 是否支持抽取
     *
     * @param childURL 抽取URL
     * @param scope 抽链范围
     * @param parentURL 父URL
     * @return 支持抽取返回true，否则返回false
     */
    private boolean supportExtract(String childURL, int scope, String parentURL) {
        if (StringUtils.isEmpty(childURL)) return false;
        if (scope == Constants.LINK_SCOPE_ALL) return true;
        String host = CommonUtil.getHost(childURL);
        String parentHost = CommonUtil.getHost(parentURL);
        if (StringUtils.isEmpty(host) || StringUtils.isEmpty(parentHost)) return false;
        if (scope == Constants.LINK_SCOPE_DOMAIN) {
            String domain = DomainUtil.getDomain(host);
            String parentDomain = DomainUtil.getDomain(parentHost);
            return domain.equals(parentDomain);
        } else if (scope == Constants.LINK_SCOPE_HOST) {
            return host.equals(parentHost);
        }
        return false;
    }

    /**
     * 判断元素是否可见
     *
     * @param element 元素
     * @return 可见返回true，否则返回false
     */
    private boolean isVisible(Element element) {
        if (element == null) return false;
        String style = element.attr(ATTR_NAME_STYLE);
        style = style.replaceAll("\\s", "");
        return !style.contains("display:none");
    }
}
