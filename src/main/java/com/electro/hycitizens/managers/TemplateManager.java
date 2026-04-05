package com.electro.hycitizens.managers;

import com.electro.hycitizens.models.CitizenTemplate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;

/**
 * Manages citizen templates — reusable presets for citizen creation.
 * Templates are persisted to mods/HyCitizensData/templates.json.
 */
public class TemplateManager {

    private final Path templatesFile;
    private final Gson gson;
    private final Map<String, CitizenTemplate> templates = new ConcurrentHashMap<>();

    public TemplateManager(@Nonnull Path dataFolder) {
        this.templatesFile = dataFolder.resolve("templates.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        load();
    }

    @Nonnull
    public Collection<CitizenTemplate> getAll() {
        return Collections.unmodifiableCollection(templates.values());
    }

    @Nullable
    public CitizenTemplate get(@Nonnull String id) {
        return templates.get(id);
    }

    public void save(@Nonnull CitizenTemplate template) {
        templates.put(template.getId(), template);
        persist();
    }

    public boolean delete(@Nonnull String id) {
        boolean removed = templates.remove(id) != null;
        if (removed) persist();
        return removed;
    }

    @Nonnull
    public List<String> getTemplateNames() {
        List<String> names = new ArrayList<>();
        for (CitizenTemplate t : templates.values()) {
            names.add(t.getName());
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    @Nullable
    public CitizenTemplate findByName(@Nonnull String name) {
        for (CitizenTemplate t : templates.values()) {
            if (t.getName().equalsIgnoreCase(name)) return t;
        }
        return null;
    }

    private void load() {
        if (!Files.exists(templatesFile)) return;

        try (Reader reader = new FileReader(templatesFile.toFile())) {
            Type type = new TypeToken<Map<String, CitizenTemplate>>() {}.getType();
            Map<String, CitizenTemplate> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                templates.putAll(loaded);
            }
            getLogger().atInfo().log("[HyCitizens] Loaded %d citizen templates", templates.size());
        } catch (Exception e) {
            getLogger().atWarning().log("[HyCitizens] Failed to load templates: " + e.getMessage());
        }
    }

    private void persist() {
        try {
            Files.createDirectories(templatesFile.getParent());
            Path temp = templatesFile.getParent().resolve("templates.json.tmp");
            try (Writer writer = Files.newBufferedWriter(temp)) {
                gson.toJson(templates, writer);
            }
            Files.move(temp, templatesFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            getLogger().atWarning().log("[HyCitizens] Failed to save templates: " + e.getMessage());
        }
    }
}
