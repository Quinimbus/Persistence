package cloud.quinimbus.persistence.schema.json;

import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.tools.throwing.ThrowingOptional;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class SingleJsonSchemaProvider extends AbstractJsonSchemaProvider {

    public void importSchema(InputStream is) throws IOException {
        var mapper = new ObjectMapper();
        var root = mapper.readTree(is);
        var id = root.get("id").asText();
        var version = root.get("version").asLong();
        var schema = new Schema(
                id,
                ThrowingOptional.ofNullable(root.get("entityTypes"), IOException.class)
                        .map(n -> this.importTypes(mapper, n))
                        .orElse(Map.of()),
                version);
        this.addSchema(schema);
    }
}
