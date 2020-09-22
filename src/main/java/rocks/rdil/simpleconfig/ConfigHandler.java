package rocks.rdil.simpleconfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * The handler for JSON files - manages {@link rocks.rdil.simpleconfig.Config}
 * classes.
 */
@SuppressWarnings("deprecation")
public final class ConfigHandler {
    private JsonObject cfg;
    private final List<Config> configObjs;
    private final File file;

    /**
     * Creates a new instance of the configuration handler.
     * 
     * @param file The file that the configuration should be saved/loaded from.
     */
    public ConfigHandler(File file) {
        this.file = file;
        this.cfg = new JsonObject();
        this.configObjs = new ArrayList<>();
        try {
            if (this.file.exists()) {
                StringBuilder builder = new StringBuilder();
                FileReader fr = new FileReader(this.file);
                BufferedReader br = new BufferedReader(fr);
                String line;
                while ((line = br.readLine()) != null) {
                    builder.append(line);
                }

                this.cfg = new JsonParser().parse(builder.toString()).getAsJsonObject();
                br.close();
                fr.close();
            } else {
                this.save();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the JSON object for all the registered
     * {@link rocks.rdil.simpleconfig.Config}s.
     * 
     * @return The JsonObject instance.
     */
    public JsonObject getJsonObject() {
        return this.cfg;
    }

    /**
     * Registers a {@link rocks.rdil.simpleconfig.Config} class to the current JSON
     * file.
     * 
     * @param config The config class.
     */
    public void register(Config config) {
        this.configObjs.add(config);

        boolean isAllFields = config.getClass().isAnnotationPresent(AllFieldsAreConfigurations.class);
        Field[] classFields = config.getClass().getDeclaredFields();
        Collection<Field> dest = new ArrayList<>();

        for (Field f : classFields) {
            if (f.isAnnotationPresent(Configuration.class) || isAllFields) {
                dest.add(f);
            }
        }

        for (Field it : dest) {
            if (!it.isAccessible()) {
                it.setAccessible(true);
            }

            Configuration conf = it.getAnnotation(Configuration.class);

            if (conf != null) {
                if (
                    this.cfg.has(conf.alt())
                    && conf.alt().length() > 0
                    && !this.cfg.has(it.getName())
                ) {
                    JsonElement altResolved = this.cfg.get(conf.alt());
                    if (altResolved.getAsJsonObject().has(conf.alt())) {
                        this.cfg.add(it.getName(), this.cfg.get(conf.alt()).getAsJsonObject().get(conf.alt()));
                    }
                }
            }

            if (this.cfg.has(it.getName())) {
                try {
                    it.set(config, GsonExt.gson.fromJson(this.cfg.get(it.getName()), it.getType()));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new IllegalStateException("Config options cannot be final!");
                }
            }
        }
    }

    /**
     * Removes a {@link rocks.rdil.simpleconfig.Config} class from this handler.
     * This may cause changes to be discarded!
     * 
     * @param config The config class.
     */
    public void unregister(Config config) {
        this.configObjs.remove(config);
    }

    private void loadConfigurationToJsonFile(Config config) {
        Field[] fields = config.getClass().getDeclaredFields();
        Collection<Field> dest = new ArrayList<>();

        for (Field theField : fields) {
            if (theField.isAnnotationPresent(Configuration.class)) {
                dest.add(theField);
            }
        }

        Field it;
        try {
            for (
                Iterator<Field> annotatedFieldIter = dest.iterator();
                annotatedFieldIter.hasNext();
                this.cfg.add(it.getName(), GsonExt.gson.toJsonTree(it.get(config), it.getType()))
            ) {
                it = annotatedFieldIter.next();
                if (!it.isAccessible()) {
                    it.setAccessible(true);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the configuration to the file assigned to this handler.
     */
    public void save() {
        for (Config it : this.configObjs) {
            this.loadConfigurationToJsonFile(it);
        }

        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(this.file);
            String text = GsonExt.gson.toJson(this.cfg);
            fos.write(text.getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
