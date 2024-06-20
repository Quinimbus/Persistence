package cloud.quinimbus.persistence.common.filter;

import static org.junit.jupiter.api.Assertions.*;

import cloud.quinimbus.persistence.api.filter.PropertyFilter;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class FilterFactoryTest {

    @Test
    public void testFromMap() {
        var filters = FilterFactory.fromMap(Map.of("a", "text", "b", 13));
        assertEquals(2, filters.size());
        assertTrue(filters.stream()
                .anyMatch(pf -> pf.equals(
                        new FilterFactory.DefaultPropertyFilter("a", PropertyFilter.Operator.EQUALS, "text"))));
        assertTrue(filters.stream()
                .anyMatch(pf ->
                        pf.equals(new FilterFactory.DefaultPropertyFilter("b", PropertyFilter.Operator.EQUALS, 13))));
    }

    private static record TestFilter(String a, Integer b) {}

    @Test
    public void testFromRecord() throws Exception {
        var filters = FilterFactory.fromRecord(new TestFilter("text", 13));
        assertEquals(2, filters.size());
        assertTrue(filters.stream()
                .anyMatch(pf -> pf.equals(
                        new FilterFactory.DefaultPropertyFilter("a", PropertyFilter.Operator.EQUALS, "text"))));
        assertTrue(filters.stream()
                .anyMatch(pf ->
                        pf.equals(new FilterFactory.DefaultPropertyFilter("b", PropertyFilter.Operator.EQUALS, 13))));
    }
}
