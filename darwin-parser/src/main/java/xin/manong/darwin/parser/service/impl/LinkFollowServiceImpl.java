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
import xin.manong.darwin.parser.sdk.ParseRequest;
import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.parser.service.LinkFollowService;
import xin.manong.weapon.base.util.CommonUtil;
import xin.manong.weapon.base.util.DomainUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 全局抽链服务实现
 *
 * @author frankcl
 * @date 2023-11-15 14:49:36
 */
@Service
public class LinkFollowServiceImpl implements LinkFollowService {

    private static final Logger logger = LoggerFactory.getLogger(LinkFollowServiceImpl.class);

    private static final String TAG_NAME_A = "a";
    private static final String ATTR_NAME_HREF = "href";
    private static final String ATTR_NAME_STYLE = "style";

    @Override
    public ParseResponse parse(ParseRequest request) {
        if (request == null || !request.check()) {
            logger.error("parse request is invalid");
            return ParseResponse.buildError("解析请求非法");
        }
        if (!Constants.SUPPORT_LINK_SCOPES.containsKey(request.scope)) {
            logger.error("link scope[{}] is not supported", request.scope);
            return ParseResponse.buildError(String.format("不支持全局抽链范围[%d]", request.scope));
        }
        String parentURL = StringUtils.isEmpty(request.redirectURL) ? request.url : request.redirectURL;
        Document document = Jsoup.parse(request.html, parentURL);
        Element body = document.body();
        if (body == null) {
            logger.error("body is not found");
            return ParseResponse.buildError("HTML body不存在");
        }
        List<URLRecord> followURLs = new ArrayList<>();
        extractLinks(body, parentURL, request.scope, followURLs);
        return ParseResponse.buildOK(null, followURLs, null);
    }

    /**
     * 抽取元素链接
     *
     * @param element 元素
     * @param parentURL 父URL
     * @param scope 抽链范围
     * @param followURLs 抽链结果
     */
    private void extractLinks(Element element, String parentURL, int scope,
                              List<URLRecord> followURLs) {
        if (!isVisible(element)) return;
        if (element.tagName().equals(TAG_NAME_A)) {
            if (!element.hasAttr(ATTR_NAME_HREF)) return;
            String href = element.absUrl(ATTR_NAME_HREF);
            if (StringUtils.isEmpty(href) || !isExtract(href, scope, parentURL)) return;
            try {
                new URL(href);
                followURLs.add(new URLRecord(href));
            } catch (Exception e) {
            }
            return;
        }
        Elements children = element.children();
        for (Element child : children) extractLinks(child, parentURL, scope, followURLs);
    }

    /**
     * 是否抽取链接
     *
     * @param url 抽取URL
     * @param scope 抽链范围
     * @param parentURL 父URL
     * @return 需要抽取返回true，否则返回false
     */
    private boolean isExtract(String url, int scope, String parentURL) {
        if (scope == Constants.LINK_SCOPE_ALL) return true;
        String host = CommonUtil.getHost(url);
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
        style = style == null ? "" : style.replaceAll("\\s", "");
        return style.indexOf("display:none") == -1;
    }
}
