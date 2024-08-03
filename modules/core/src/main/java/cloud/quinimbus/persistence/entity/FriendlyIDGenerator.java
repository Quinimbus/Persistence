package cloud.quinimbus.persistence.entity;

import cloud.quinimbus.common.annotations.Provider;
import cloud.quinimbus.persistence.api.entity.IDGenerator;
import com.devskiller.friendly_id.FriendlyId;

@Provider(name = "ID generator using friendly IDs", alias = "friendly")
public class FriendlyIDGenerator implements IDGenerator<String> {

    @Override
    public String generate() {
        return FriendlyId.createFriendlyId();
    }
}
