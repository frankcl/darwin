package xin.manong.darwin.parser.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.darwin.common.util.URLNormalizer;
import xin.manong.darwin.parser.sdk.ParseRequest;
import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.parser.service.LinkExtractService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 抽链服务实现
 *
 * @author frankcl
 * @date 2023-11-15 14:49:36
 */
@Service
public class LinkExtractServiceImpl implements LinkExtractService {

    private static final Logger logger = LoggerFactory.getLogger(LinkExtractServiceImpl.class);

    private static final String TAG_NAME_A = "a";
    private static final String ATTR_NAME_HREF = "href";
    private static final String ATTR_NAME_STYLE = "style";

    @Override
    public ParseResponse extract(@NonNull ParseRequest request) {
        String parentURL = StringUtils.isEmpty(request.redirectURL) ? request.url : request.redirectURL;
        Document document = Jsoup.parse(request.text, parentURL);
        Element body = document.body();
        Set<String> hashes = new HashSet<>();
        List<URLRecord> children = new ArrayList<>();
        scopeExtract(body, parentURL, request.linkScope, children, hashes);
        return ParseResponse.buildOK(null, children, null);
    }

    /**
     * 抽取链接
     *
     * @param element 元素
     * @param parentURL 父URL
     * @param scope 抽链范围
     * @param children 抽链结果
     * @param hashes 子链接hash集合，用于去重
     */
    private void scopeExtract(Element element, String parentURL, int scope,
                              List<URLRecord> children, Set<String> hashes) {
        if (!isVisible(element)) return;
        if (element.tagName().equals(TAG_NAME_A)) {
            if (!element.hasAttr(ATTR_NAME_HREF)) return;
            String child = element.absUrl(ATTR_NAME_HREF);
            try {
                child = child.trim();
                child = URLNormalizer.normalize(child);
            } catch (Exception e) {
                logger.warn("Invalid child url:{}", child);
                return;
            }
            if (!supportExtract(child, scope, parentURL)) return;
            URLRecord record = URLRecord.scopeLink(child, scope);
            if (hashes.contains(record.hash)) return;
            children.add(record);
            hashes.add(record.hash);
            return;
        }
        Elements elements = element.children();
        for (Element child : elements) scopeExtract(child, parentURL, scope, children, hashes);
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
        if (scope == Constants.LINK_SCOPE_DOMAIN) {
            return DarwinUtil.isSameDomain(childURL, parentURL);
        } else if (scope == Constants.LINK_SCOPE_HOST) {
            return DarwinUtil.isSameHost(childURL, parentURL);
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
