package xin.manong.darwin.service.impl.ots;

import com.alicloud.openservices.tablestore.model.search.query.MatchPhraseQuery;
import com.alicloud.openservices.tablestore.model.search.query.RangeQuery;
import com.alicloud.openservices.tablestore.model.search.query.TermQuery;
import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.model.RangeValue;

/**
 * @author frankcl
 * @date 2023-04-04 15:27:40
 */
public class SearchQueryBuilderTest {

    @Test
    public void testBuildTermQuery() {
        {
            TermQuery termQuery = SearchQueryBuilder.buildTermQuery("key", 123L);
            Assert.assertNotNull(termQuery);
            Assert.assertEquals("key", termQuery.getFieldName());
            Assert.assertEquals(123L, termQuery.getTerm().asLong());
        }
        {
            TermQuery termQuery = SearchQueryBuilder.buildTermQuery("key", 123);
            Assert.assertNotNull(termQuery);
            Assert.assertEquals("key", termQuery.getFieldName());
            Assert.assertEquals(123L, termQuery.getTerm().asLong());
        }
        {
            TermQuery termQuery = SearchQueryBuilder.buildTermQuery("key", 1.0d);
            Assert.assertNotNull(termQuery);
            Assert.assertEquals("key", termQuery.getFieldName());
            Assert.assertEquals(1.0d, termQuery.getTerm().asDouble(), 0.1d);
        }
        {
            TermQuery termQuery = SearchQueryBuilder.buildTermQuery("key", 10f);
            Assert.assertNotNull(termQuery);
            Assert.assertEquals("key", termQuery.getFieldName());
            Assert.assertEquals(10d, termQuery.getTerm().asDouble(), 0.1d);
        }
        {
            TermQuery termQuery = SearchQueryBuilder.buildTermQuery("key", true);
            Assert.assertNotNull(termQuery);
            Assert.assertEquals("key", termQuery.getFieldName());
            Assert.assertTrue(termQuery.getTerm().asBoolean());
        }
        {
            TermQuery termQuery = SearchQueryBuilder.buildTermQuery("key", "abc");
            Assert.assertNotNull(termQuery);
            Assert.assertEquals("key", termQuery.getFieldName());
            Assert.assertEquals("abc", termQuery.getTerm().asString());
        }
    }

    @Test
    public void testBuildMatchPhraseQuery() {
        MatchPhraseQuery matchPhraseQuery = SearchQueryBuilder.buildMatchPhraseQuery("key", "abc");
        Assert.assertNotNull(matchPhraseQuery);
        Assert.assertEquals("key", matchPhraseQuery.getFieldName());
        Assert.assertEquals("abc", matchPhraseQuery.getText());
    }

    @Test
    public void testBuildRangeQuery() {
        {
            RangeValue<Double> rangeValue = new RangeValue<>();
            rangeValue.start = -1d;
            rangeValue.end = 1d;
            rangeValue.includeLower = true;
            rangeValue.includeUpper = true;
            RangeQuery rangeQuery = SearchQueryBuilder.buildRangeQuery("key", rangeValue);
            Assert.assertNotNull(rangeQuery);
            Assert.assertEquals("key", rangeQuery.getFieldName());
            Assert.assertEquals(-1d, rangeQuery.getFrom().asDouble(), 0.1d);
            Assert.assertEquals(1d, rangeQuery.getTo().asDouble(), 0.1d);
            Assert.assertTrue(rangeQuery.isIncludeLower());
            Assert.assertTrue(rangeQuery.isIncludeUpper());
        }
        {
            RangeValue<Float> rangeValue = new RangeValue<>();
            rangeValue.start = -1f;
            rangeValue.end = 1f;
            rangeValue.includeLower = true;
            rangeValue.includeUpper = true;
            RangeQuery rangeQuery = SearchQueryBuilder.buildRangeQuery("key", rangeValue);
            Assert.assertNotNull(rangeQuery);
            Assert.assertEquals("key", rangeQuery.getFieldName());
            Assert.assertEquals(-1d, rangeQuery.getFrom().asDouble(), 0.1d);
            Assert.assertEquals(1d, rangeQuery.getTo().asDouble(), 0.1d);
            Assert.assertTrue(rangeQuery.isIncludeLower());
            Assert.assertTrue(rangeQuery.isIncludeUpper());
        }
        {
            RangeValue<Long> rangeValue = new RangeValue<>();
            rangeValue.start = -10L;
            rangeValue.end = 10L;
            rangeValue.includeLower = false;
            rangeValue.includeUpper = true;
            RangeQuery rangeQuery = SearchQueryBuilder.buildRangeQuery("key", rangeValue);
            Assert.assertNotNull(rangeQuery);
            Assert.assertEquals("key", rangeQuery.getFieldName());
            Assert.assertEquals(-10L, rangeQuery.getFrom().asLong());
            Assert.assertEquals(10L, rangeQuery.getTo().asLong());
            Assert.assertFalse(rangeQuery.isIncludeLower());
            Assert.assertTrue(rangeQuery.isIncludeUpper());
        }
        {
            RangeValue<Integer> rangeValue = new RangeValue<>();
            rangeValue.start = -10;
            rangeValue.end = 10;
            rangeValue.includeLower = false;
            rangeValue.includeUpper = true;
            RangeQuery rangeQuery = SearchQueryBuilder.buildRangeQuery("key", rangeValue);
            Assert.assertNotNull(rangeQuery);
            Assert.assertEquals("key", rangeQuery.getFieldName());
            Assert.assertEquals(-10L, rangeQuery.getFrom().asLong());
            Assert.assertEquals(10L, rangeQuery.getTo().asLong());
            Assert.assertFalse(rangeQuery.isIncludeLower());
            Assert.assertTrue(rangeQuery.isIncludeUpper());
        }
    }
}
