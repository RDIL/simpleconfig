package rocks.rdil.simpleconfig;

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
 * The system's main class used for management.
 * Every JSON file should have an instance of this, and JSON files can
 * have unlimited options/option classes.
 * 
 * @since 2.0.0
 */
@SuppressWarnings("deprecation")
public class ConfigurationSystem {
    private JsonObject cfg;
    private final List<Object> configObjs;
    private final File file;

    /**
     * Creates a new instance of the configuration handler.
     * 
     * @param file The file that the configuration should be saved/loaded from.
     */
    public ConfigurationSystem(File file) {
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
     * Get the JSON object for this instance of the system's file.
     * 
     * @return The JsonObject instance.
     */
    public JsonObject getJsonObject() {
        return this.cfg;
    }

    /**
     * Registers a class and all the {@link rocks.rdil.simpleconfig.Option}s
     * it contains to the current JSON file.
     * 
     * @param config The class to be registered.
     */
    public void register(Object config) {
        this.configObjs.add(config);

        Field[] classFields = config.getClass().getDeclaredFields();
        Collection<Field> dest = new ArrayList<>();

        for (Field f : classFields) {
            if (f.isAnnotationPresent(Option.class)) {
                dest.add(f);
            }
        }

        for (Field it : dest) {
            if (!it.isAccessible()) {
                it.setAccessible(true);
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
     * Removes a class and any option it contains from this handler.
     * This may cause changes to be discarded!
     * 
     * @param config The class to unregister.
     */
    public void unregister(Object config) {
        this.configObjs.remove(config);
    }

    /**
     * Adds all the fields from a class to the JSON object.
     * 
     * @param config The class to add the fields from.
     */
    private void loadConfigurationToJsonFile(Object config) {
        Field[] fields = config.getClass().getDeclaredFields();
        Collection<Field> dest = new ArrayList<>();

        for (Field theField : fields) {
            if (theField.isAnnotationPresent(Option.class)) {
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
        for (Object it : this.configObjs) {
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
