package rocks.rdil.simpleconfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for classes that indicates all fields in the class
 * should be treated as configuration options. This removes the need
 * for every field to be annotated with
 * {@link rocks.rdil.simpleconfig.Configuration}.
 * 
 * @since 1.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AllFieldsAreConfigurations {
}
