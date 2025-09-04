package cloud.quinimbus.persistence.storage.mongo;

import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.conversions.Bson;

public class Documents {

    public static record DocumentEntry(String key, Object value) {}

    public static class CondBuilder {
        private final Object cond;

        public CondBuilder(String cond) {
            this.cond = cond;
        }

        public CondBuilder(Bson cond) {
            this.cond = cond;
        }

        public CondAndThenBuilder then(String then) {
            return new CondAndThenBuilder(cond, then);
        }

        public CondAndThenBuilder then(Bson then) {
            return new CondAndThenBuilder(cond, then);
        }
    }

    public static class CondAndThenBuilder {
        private final Object cond;
        private final Object thenCase;

        public CondAndThenBuilder(Object cond, Object thenCase) {
            this.cond = cond;
            this.thenCase = thenCase;
        }

        public Bson orElse(String elseCase) {
            return new Document("$cond", List.of(cond, thenCase, elseCase));
        }

        public Bson orElse(Bson elseCase) {
            return new Document("$cond", List.of(cond, thenCase, elseCase));
        }
    }

    public static Bson map(String input, String as, Bson in) {
        return new Document("$map", new Document(Map.of("input", input, "as", as, "in", in)));
    }

    public static Bson regexMatch(String inputExpr, String regex, String options) {
        return new Document(
                "$regexMatch",
                new Document("input", inputExpr).append("regex", regex).append("options", options));
    }

    public static Bson regexMatch(Bson inputExpr, String regex, String options) {
        return new Document(
                "$regexMatch",
                new Document("input", inputExpr).append("regex", regex).append("options", options));
    }

    public static CondBuilder cond(String cond) {
        return new CondBuilder(cond);
    }

    public static CondBuilder cond(Bson cond) {
        return new CondBuilder(cond);
    }

    public static Bson reduce(List<Bson> input, Bson initialValue, Bson in) {
        return new Document(
                "$reduce",
                new Document("input", input)
                        .append("initialValue", initialValue)
                        .append("in", in));
    }

    public static Bson let(Bson vars, String in) {
        return new Document("$let", new Document("vars", vars).append("in", in));
    }

    public static Bson toStr(String expr) {
        return new Document("$toString", expr);
    }

    public static Bson toStr(Bson expr) {
        return new Document("$toString", expr);
    }

    public static Bson isNumber(String expr) {
        return new Document("$isNumber", expr);
    }

    public static Bson isNumber(Bson expr) {
        return new Document("$isNumber", expr);
    }

    public static Document doc(String key, Object value) {
        return new Document(key, value);
    }

    public static Document doc(DocumentEntry... entries) {
        var doc = new Document();
        for (var entry : entries) {
            doc.append(entry.key(), entry.value());
        }
        return doc;
    }

    public static DocumentEntry entry(String key, Object value) {
        return new DocumentEntry(key, value);
    }
}
