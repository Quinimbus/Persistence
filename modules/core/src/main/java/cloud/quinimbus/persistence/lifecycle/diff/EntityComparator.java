package cloud.quinimbus.persistence.lifecycle.diff;

import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.lifecycle.diff.CompletePropertyDiff;
import cloud.quinimbus.persistence.api.lifecycle.diff.Diff;
import cloud.quinimbus.persistence.api.lifecycle.diff.ListPropertyEntryAddedDiff;
import cloud.quinimbus.persistence.api.lifecycle.diff.ListPropertyEntryRemovedDiff;
import cloud.quinimbus.persistence.api.lifecycle.diff.MapPropertyEntryAddedDiff;
import cloud.quinimbus.persistence.api.lifecycle.diff.MapPropertyEntryRemovedDiff;
import cloud.quinimbus.persistence.api.lifecycle.diff.MapPropertyEntryReplacedDiff;
import cloud.quinimbus.persistence.api.lifecycle.diff.SetPropertyEntryAddedDiff;
import cloud.quinimbus.persistence.api.lifecycle.diff.SetPropertyEntryRemovedDiff;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityComparator {

    public static <K> Set<Diff<Object>> compareEntities(Entity<K> newEntity, Entity<K> oldEntity) {
        var allPropertyKeys = new HashSet<String>();
        allPropertyKeys.addAll(newEntity.getProperties().keySet());
        allPropertyKeys.addAll(oldEntity.getProperties().keySet());
        return allPropertyKeys.stream()
                .flatMap(key -> compareProperty(newEntity, oldEntity, key))
                .collect(Collectors.toSet());
    }

    private static <K> Stream<Diff<Object>> compareProperty(Entity<K> newEntity, Entity<K> oldEntity, String key) {
        var diffs = new ArrayList<Diff<Object>>();
        var oldValue = oldEntity.getProperty(key);
        var newValue = newEntity.getProperty(key);
        if (!Objects.equals(oldValue, newValue)) {
            diffs.add(new CompletePropertyDiff<>(key, oldValue, newValue));
            var propertyType = newEntity.getType().property(key).orElseThrow();
            switch (propertyType.structure()) {
                case LIST -> compareList((List) newValue, (List) oldValue, key, diffs::add);
                case SET -> compareSet((Set) newValue, (Set) oldValue, key, diffs::add);
                case MAP -> compareMap((Map) newValue, (Map) oldValue, key, diffs::add);
            }
        }
        return diffs.stream();
    }

    private static void compareList(List newList, List oldList, String key, Consumer<Diff<Object>> diffs) {
        if (oldList == null) {
            oldList = List.of();
        }
        if (newList == null) {
            newList = List.of();
        }
        for (int i = 0; i < newList.size(); i++) {
            var v = newList.get(i);
            if (v != null && !oldList.contains(v)) {
                diffs.accept(new ListPropertyEntryAddedDiff<>(key, i, v));
            }
        }
        for (int i = 0; i < oldList.size(); i++) {
            var v = oldList.get(i);
            if (v != null && !newList.contains(v)) {
                diffs.accept(new ListPropertyEntryRemovedDiff<>(key, v));
            }
        }
    }

    private static void compareSet(Set newSet, Set oldSet, String key, Consumer<Diff<Object>> diffs) {
        if (oldSet == null) {
            oldSet = Set.of();
        }
        if (newSet == null) {
            newSet = Set.of();
        }
        for (var e : newSet) {
            if (!oldSet.contains(e)) {
                diffs.accept(new SetPropertyEntryAddedDiff<>(key, e));
            }
        }
        for (var e : oldSet) {
            if (!newSet.contains(e)) {
                diffs.accept(new SetPropertyEntryRemovedDiff<>(key, e));
            }
        }
    }

    private static void compareMap(
            Map<String, Object> newMap, Map<String, Object> oldMap, String key, Consumer<Diff<Object>> diffs) {
        if (oldMap == null) {
            oldMap = Map.of();
        }
        if (newMap == null) {
            newMap = Map.of();
        }
        for (var e : newMap.keySet()) {
            if (!oldMap.containsKey(e)) {
                diffs.accept(new MapPropertyEntryAddedDiff<>(key, e, newMap.get(e)));
            }
            if (oldMap.containsKey(e)) {
                if (!Objects.equals(newMap.get(e), oldMap.get(e))) {
                    diffs.accept(new MapPropertyEntryReplacedDiff<>(key, e, oldMap.get(e), newMap.get(e)));
                }
            }
        }
        for (var e : oldMap.keySet()) {
            if (!newMap.containsKey(e)) {
                diffs.accept(new MapPropertyEntryRemovedDiff<>(key, e, oldMap.get(e)));
            }
        }
    }
}
