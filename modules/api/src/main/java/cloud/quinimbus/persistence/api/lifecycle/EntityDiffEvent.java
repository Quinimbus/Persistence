package cloud.quinimbus.persistence.api.lifecycle;

import cloud.quinimbus.persistence.api.lifecycle.diff.Diff;
import java.util.Set;
import java.util.stream.Stream;

public interface EntityDiffEvent {

    Set<Diff<Object>> diffs();

    default boolean propertyMutated(String name) {
        return this.diffs().stream().anyMatch(d -> d.name().equals(name));
    }

    default boolean onlyPropertyMutated(String name) {
        return this.diffs().stream().noneMatch(d -> !d.name().equals(name));
    }

    default <PT> Stream<Diff<PT>> streamDiffsForProperty(String name, Class<PT> type) {
        return this.diffs().stream().filter(d -> d.name().equals(name)).map(d -> (Diff<PT>) d);
    }
}
