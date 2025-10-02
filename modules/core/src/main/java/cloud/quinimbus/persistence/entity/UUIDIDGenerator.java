package cloud.quinimbus.persistence.entity;

import cloud.quinimbus.common.annotations.Provider;
import cloud.quinimbus.persistence.api.entity.IDGenerator;
import java.util.UUID;

@Provider(id = "uuid", name = "ID generator using UUID")
public class UUIDIDGenerator implements IDGenerator<String> {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
