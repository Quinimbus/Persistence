package cloud.quinimbus.persistence.schema.json;

import cloud.quinimbus.common.annotations.Provider;
import cloud.quinimbus.config.api.ConfigNode;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.tools.throwing.ThrowingOptional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Provider(name = "Single JSON schema provider", alias = "singlefile", priority = 0)
public class SingleJsonSchemaProvider extends AbstractJsonSchemaProvider {

    public Schema importSchema(Reader reader) throws IOException {
        var mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        var root = mapper.readTree(reader);
        var id = root.get("id").asText();
        var version = root.get("version").asLong();
        return new Schema(
                id,
                ThrowingOptional.ofNullable(root.get("entityTypes"), IOException.class)
                        .map(n -> this.importTypes(mapper, n))
                        .orElse(Map.of()),
                version);
    }

    @Override
    public Schema loadSchema(Map<String, Object> params) throws InvalidSchemaException {
        try {
            if (params.containsKey("file")) {
                var file = params.get("file");
                if (file instanceof Path p) {
                    try (var reader = Files.newBufferedReader(p, Charset.forName("UTF-8"))) {
                        return this.importSchema(reader);
                    }
                } else if (file instanceof String s) {
                    try (var reader = Files.newBufferedReader(Path.of(s), Charset.forName("UTF-8"))) {
                        return this.importSchema(reader);
                    }
                } else {
                    throw new InvalidSchemaException("Unknown type %s in configuration for file"
                            .formatted(file.getClass().getName()));
                }
            } else if (params.containsKey("resource")) {
                var resource = params.get("resource");
                if (resource instanceof String s) {
                    try (var reader =
                            new InputStreamReader(this.getClass().getResourceAsStream(s), Charset.forName("UTF-8"))) {
                        return this.importSchema(reader);
                    }
                } else {
                    throw new InvalidSchemaException("Unknown type %s in configuration for resource"
                            .formatted(resource.getClass().getName()));
                }
            } else {
                throw new InvalidSchemaException("Cannot find either file nor resource keys in the configuration");
            }
        } catch (IOException ex) {
            throw new InvalidSchemaException("Cannot read the schema", ex);
        }
    }

    @Override
    public Schema loadSchema(ConfigNode node) throws InvalidSchemaException {
        try {
            try (var reader = ThrowingOptional.ofOptional(node.asString("file"), IOException.class)
                    .map(Path::of)
                    .map(f -> (Reader) Files.newBufferedReader(f, Charset.forName("UTF-8")))
                    .or(() -> ThrowingOptional.ofOptional(node.asString("resource"), IOException.class)
                            .map(r -> new InputStreamReader(
                                    this.getClass().getResourceAsStream(r), Charset.forName("UTF-8"))))
                    .orElseThrow(() -> new InvalidSchemaException(
                            "Cannot find either file nor resource keys in the configuration"))) {
                return this.importSchema(reader);
            }
        } catch (IOException ex) {
            throw new InvalidSchemaException("Cannot read the schema", ex);
        }
    }
}
