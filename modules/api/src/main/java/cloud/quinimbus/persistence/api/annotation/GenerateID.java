package cloud.quinimbus.persistence.api.annotation;

public @interface GenerateID {
    boolean generate() default false;

    String generator() default "";
}
