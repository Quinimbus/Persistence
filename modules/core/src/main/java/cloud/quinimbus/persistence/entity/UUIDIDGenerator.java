package cloud.quinimbus.persistence.entity;

import cloud.quinimbus.common.annotations.Provider;
import cloud.quinimbus.persistence.api.entity.IDGenerator;
import java.util.UUID;

@Provider(name = "ID generator using UUID", alias = "uuid")
public class UUIDIDGenerator implements IDGenerator<String> {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
