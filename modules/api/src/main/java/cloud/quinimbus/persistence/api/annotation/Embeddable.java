package cloud.quinimbus.persistence.api.annotation;

import cloud.quinimbus.persistence.api.entity.EmbeddedPropertyHandler;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Embeddable {
    Class<? extends EmbeddedPropertyHandler> handler() default EmbeddedPropertyHandler.class;
}
