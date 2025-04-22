package xin.manong.darwin.service.impl.ots;

import com.alicloud.openservices.tablestore.model.ColumnValue;
import com.alicloud.openservices.tablestore.model.search.query.MatchPhraseQuery;
import com.alicloud.openservices.tablestore.model.search.query.RangeQuery;
import com.alicloud.openservices.tablestore.model.search.query.TermQuery;
import com.alicloud.openservices.tablestore.model.search.query.TermsQuery;
import xin.manong.darwin.common.model.RangeValue;

import java.util.ArrayList;
import java.util.List;

/**
 * OTS搜索query构建器
 *
 * @author frankcl
 * @date 2023-03-22 14:26:38
 */
public class SearchQueryBuilder {

    /**
     * 构建term查询条件
     *
     * @param field 字段名
     * @param termValue 字段值
     * @return term查询条件
     */
    public static TermQuery buildTermQuery(String field, Object termValue) {
        TermQuery termQuery = new TermQuery();
        termQuery.setFieldName(field);
        if (termValue instanceof Long) termQuery.setTerm(ColumnValue.fromLong((Long) termValue));
        else if (termValue instanceof Integer) termQuery.setTerm(ColumnValue.fromLong((Integer) termValue));
        else if (termValue instanceof String) termQuery.setTerm(ColumnValue.fromString((String) termValue));
        else if (termValue instanceof Boolean) termQuery.setTerm(ColumnValue.fromBoolean((Boolean) termValue));
        else if (termValue instanceof Double) termQuery.setTerm(ColumnValue.fromDouble((Double) termValue));
        else if (termValue instanceof Float) termQuery.setTerm(ColumnValue.fromDouble((Float) termValue));
        else throw new RuntimeException(String.format("Unsupported term type:%s", termValue.getClass().getName()));
        return termQuery;
    }

    /**
     * 构建terms查询条件
     *
     * @param field 字段名
     * @param termsValue 字段值列表
     * @return terms查询条件
     */
    public static <T> TermsQuery buildTermsQuery(String field, List<T> termsValue) {
        TermsQuery termsQuery = new TermsQuery();
        termsQuery.setFieldName(field);
        List<ColumnValue> columnValues = new ArrayList<>();
        for (Object termValue : termsValue) {
            if (termValue instanceof Long) columnValues.add(ColumnValue.fromLong((Long) termValue));
            else if (termValue instanceof Integer) columnValues.add(ColumnValue.fromLong((Integer) termValue));
            else if (termValue instanceof String) columnValues.add(ColumnValue.fromString((String) termValue));
            else if (termValue instanceof Boolean) columnValues.add(ColumnValue.fromBoolean((Boolean) termValue));
            else if (termValue instanceof Double) columnValues.add(ColumnValue.fromDouble((Double) termValue));
            else if (termValue instanceof Float) columnValues.add(ColumnValue.fromDouble((Float) termValue));
            else throw new RuntimeException(String.format("Unsupported term type:%s", termValue.getClass().getName()));
        }
        termsQuery.setTerms(columnValues);
        return termsQuery;
    }

    /**
     * 构建matchPhrase查询条件
     *
     * @param field 字段名
     * @param text 匹配文本
     * @return matchPhrase查询条件
     */
    public static MatchPhraseQuery buildMatchPhraseQuery(String field, String text) {
        MatchPhraseQuery matchPhraseQuery = new MatchPhraseQuery();
        matchPhraseQuery.setFieldName(field);
        matchPhraseQuery.setText(text);
        return matchPhraseQuery;
    }

    /**
     * 构建范围查询
     *
     * @param field 字段名
     * @param rangeValue 范围值
     * @return 范围查询
     */
    public static RangeQuery buildRangeQuery(String field, RangeValue<?> rangeValue) {
        RangeQuery rangeQuery = new RangeQuery();
        rangeQuery.setFieldName(field);
        if (rangeValue.start != null) {
            if (rangeValue.start instanceof Long) rangeQuery.setFrom(ColumnValue.fromLong((Long) rangeValue.start));
            else if (rangeValue.start instanceof Integer) rangeQuery.setFrom(ColumnValue.fromLong((Integer) rangeValue.start));
            else if (rangeValue.start instanceof Double) rangeQuery.setFrom(ColumnValue.fromDouble((Double) rangeValue.start));
            else if (rangeValue.start instanceof Float) rangeQuery.setFrom(ColumnValue.fromDouble((Float) rangeValue.start));
            else throw new RuntimeException(String.format("Unsupported term type:%s", rangeValue.start.getClass().getName()));
            if (rangeValue.includeLower) rangeQuery.setIncludeLower(true);
        }
        if (rangeValue.end != null) {
            if (rangeValue.end instanceof Long) rangeQuery.setTo(ColumnValue.fromLong((Long) rangeValue.end));
            else if (rangeValue.end instanceof Integer) rangeQuery.setTo(ColumnValue.fromLong((Integer) rangeValue.end));
            else if (rangeValue.end instanceof Double) rangeQuery.setTo(ColumnValue.fromDouble((Double) rangeValue.end));
            else if (rangeValue.end instanceof Float) rangeQuery.setTo(ColumnValue.fromDouble((Float) rangeValue.end));
            else throw new RuntimeException(String.format("Unsupported term type:%s", rangeValue.end.getClass().getName()));
            if (rangeValue.includeUpper) rangeQuery.setIncludeUpper(true);
        }
        return rangeQuery;
    }
}
