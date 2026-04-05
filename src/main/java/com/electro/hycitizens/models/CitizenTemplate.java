package com.electro.hycitizens.models;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A reusable template for citizen creation. Stores non-instance-specific
 * field values (appearance, behavior, combat, etc.) that can pre-fill
 * the create form when making a new citizen.
 */
public class CitizenTemplate {
    private String id;
    private String name;
    private String description;
    private Map<String, String> values;

    public CitizenTemplate() {
        this.id = "";
        this.name = "";
        this.description = "";
        this.values = new LinkedHashMap<>();
    }

    public CitizenTemplate(@Nonnull String id, @Nonnull String name, @Nonnull String description,
                            @Nonnull Map<String, String> values) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.values = new LinkedHashMap<>(values);
    }

    @Nonnull public String getId() { return id; }
    public void setId(@Nonnull String id) { this.id = id; }

    @Nonnull public String getName() { return name; }
    public void setName(@Nonnull String name) { this.name = name; }

    @Nonnull public String getDescription() { return description; }
    public void setDescription(@Nonnull String description) { this.description = description; }

    @Nonnull public Map<String, String> getValues() { return values; }
    public void setValues(@Nonnull Map<String, String> values) { this.values = new LinkedHashMap<>(values); }
}
