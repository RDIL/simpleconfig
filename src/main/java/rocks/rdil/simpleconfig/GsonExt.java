package rocks.rdil.simpleconfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A container for our custom-configured GSON instance.
 * 
 * @since 1.0.0
 */
public final class GsonExt {
    /**
     * The custom-configured GSON instance.
     */
    public static final Gson gson = new GsonBuilder()
        .serializeNulls()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();
}
