package rocks.rdil.simpleconfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A configuration option.
 * Any fields annotated with this should be in a class that extends {@link rocks.rdil.simpleconfig.Config}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {
    /**
     * An (optional) way to make this field use a different name in the saved JSON file.
     * Leave this empty if you want the JSON file to just use the field's name.
     * 
     * @return The name this field will use in the saved JSON file.
     */
    String alt() default "";
}
