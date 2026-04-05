package com.electro.hycitizens.api;

import com.electro.hycitizens.HyCitizensPlugin;
import com.electro.hycitizens.managers.TemplateManager;
import com.electro.hycitizens.models.*;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import dev.hytalemodding.api.DashboardSchemaProvider;

import javax.annotation.Nonnull;
import java.util.*;

public class CitizenSchemaProvider implements DashboardSchemaProvider {

    private final HyCitizensPlugin plugin;

    public CitizenSchemaProvider(@Nonnull HyCitizensPlugin plugin) {
        this.plugin = plugin;
    }

    @Nonnull @Override public String getPluginId() { return "hycitizens"; }
    @Nonnull @Override public String getPluginName() { return "HyCitizens"; }
    @Nonnull @Override public String getVersion() { return "1.6.1"; }
    @Nonnull @Override public String getEntityType() { return "citizen"; }
    @Nonnull @Override public String getEntityLabel() { return "Citizens"; }

    // ── Schema ────────────────────────────────────────────────────

    @Nonnull
    @Override
    public Map<String, Object> buildSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("pluginId", getPluginId());
        schema.put("pluginName", getPluginName());
        schema.put("version", getVersion());
        schema.put("entityType", getEntityType());
        schema.put("entityLabel", getEntityLabel());

        List<Map<String, Object>> groups = new ArrayList<>();
        groups.add(identityGroup());
        groups.add(appearanceGroup());
        groups.add(movementGroup());
        groups.add(interactionGroup());
        groups.add(messagesGroup());
        groups.add(combatGroup());
        groups.add(detectionGroup());
        groups.add(healthGroup());
        groups.add(deathGroup());
        groups.add(scheduleGroup());
        groups.add(advancedGroup());
        schema.put("groups", groups);

        List<Map<String, Object>> actions = new ArrayList<>();
        actions.add(createAction());
        actions.add(createFromTemplateAction());
        actions.add(createTemplateAction());
        actions.add(deleteTemplateAction());
        actions.add(deleteAction());
        schema.put("actions", actions);

        // Templates list for reference
        List<Map<String, Object>> templatesList = new ArrayList<>();
        for (CitizenTemplate t : plugin.getTemplateManager().getAll()) {
            Map<String, Object> tm = new LinkedHashMap<>();
            tm.put("id", t.getId());
            tm.put("name", t.getName());
            tm.put("description", t.getDescription());
            templatesList.add(tm);
        }
        schema.put("templates", templatesList);

        return schema;
    }

    // ── Entity List ───────────────────────────────────────────────

    @Nonnull
    @Override
    public List<Map<String, Object>> listEntities() {
        List<CitizenData> citizens = plugin.getCitizensManager().getAllCitizens();
        List<Map<String, Object>> result = new ArrayList<>(citizens.size());
        for (CitizenData c : citizens) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", c.getId());
            entry.put("label", c.getName());
            entry.put("group", c.getGroup());
            Vector3d pos = c.getCurrentPosition() != null ? c.getCurrentPosition() : c.getPosition();
            entry.put("x", round2(pos.x));
            entry.put("y", round2(pos.y));
            entry.put("z", round2(pos.z));
            result.add(entry);
        }
        return result;
    }

    // ── Get Values ────────────────────────────────────────────────

    @Nonnull
    @Override
    public Map<String, Object> getEntityValues(@Nonnull String entityId) {
        CitizenData c = findCitizen(entityId);
        if (c == null) return Map.of();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("entityId", c.getId());
        result.put("entityLabel", c.getName());

        Map<String, Object> values = new LinkedHashMap<>();

        // Identity
        values.put("name", c.getName());
        values.put("modelId", c.getModelId());
        values.put("group", c.getGroup());
        values.put("attitude", c.getAttitude());
        values.put("scale", c.getScale());
        values.put("position", vec3dMap(c.getPosition()));
        values.put("rotation", vec3fMap(c.getRotation()));
        values.put("requiredPermission", c.getRequiredPermission());
        values.put("noPermissionMessage", c.getNoPermissionMessage());

        // Appearance
        values.put("isPlayerModel", c.isPlayerModel());
        values.put("skinUsername", c.getSkinUsername());
        values.put("useLiveSkin", c.isUseLiveSkin());
        values.put("hideNametag", c.isHideNametag());
        values.put("hideNpc", c.isHideNpc());
        values.put("nametagOffset", c.getNametagOffset());
        values.put("npcHelmet", orEmpty(c.getNpcHelmet()));
        values.put("npcChest", orEmpty(c.getNpcChest()));
        values.put("npcLeggings", orEmpty(c.getNpcLeggings()));
        values.put("npcGloves", orEmpty(c.getNpcGloves()));
        values.put("npcHand", orEmpty(c.getNpcHand()));
        values.put("npcOffHand", orEmpty(c.getNpcOffHand()));

        // Movement
        MovementBehavior mb = c.getMovementBehavior();
        values.put("movement.type", mb.getType());
        values.put("movement.walkSpeed", mb.getWalkSpeed());
        values.put("movement.wanderRadius", mb.getWanderRadius());
        values.put("movement.wanderWidth", mb.getWanderWidth());
        values.put("movement.wanderDepth", mb.getWanderDepth());
        values.put("rotateTowardsPlayer", c.getRotateTowardsPlayer());
        values.put("lookAtDistance", c.getLookAtDistance());
        values.put("followCitizenEnabled", c.isFollowCitizenEnabled());
        values.put("followCitizenId", c.getFollowCitizenId());
        values.put("followDistance", c.getFollowDistance());

        // Interaction
        values.put("fKeyInteractionEnabled", c.getFKeyInteractionEnabled());
        values.put("forceFKeyInteractionText", c.getForceFKeyInteractionText());
        values.put("commandSelectionMode", c.getCommandSelectionMode());
        values.put("firstInteractionEnabled", c.isFirstInteractionEnabled());
        values.put("firstInteractionCommandSelectionMode", c.getFirstInteractionCommandSelectionMode());
        values.put("postFirstInteractionBehavior", c.getPostFirstInteractionBehavior());
        values.put("runNormalOnFirstInteraction", c.isRunNormalOnFirstInteraction());

        // Messages
        MessagesConfig msg = c.getMessagesConfig();
        values.put("messages.enabled", msg.isEnabled());
        values.put("messages.selectionMode", msg.getSelectionMode());

        // Combat
        CombatConfig cc = c.getCombatConfig();
        values.put("combat.attackType", cc.getAttackType());
        values.put("combat.attackDistance", cc.getAttackDistance());
        values.put("combat.chaseSpeed", cc.getChaseSpeed());
        values.put("combat.combatBehaviorDistance", cc.getCombatBehaviorDistance());
        values.put("combat.combatStrafeWeight", cc.getCombatStrafeWeight());
        values.put("combat.combatDirectWeight", cc.getCombatDirectWeight());
        values.put("combat.backOffAfterAttack", cc.isBackOffAfterAttack());
        values.put("combat.backOffDistance", cc.getBackOffDistance());
        values.put("combat.desiredAttackDistanceMin", cc.getDesiredAttackDistanceMin());
        values.put("combat.desiredAttackDistanceMax", cc.getDesiredAttackDistanceMax());
        values.put("combat.attackPauseMin", cc.getAttackPauseMin());
        values.put("combat.attackPauseMax", cc.getAttackPauseMax());
        values.put("combat.blockAbility", cc.getBlockAbility());
        values.put("combat.blockProbability", cc.getBlockProbability());
        values.put("combat.targetRange", cc.getTargetRange());
        values.put("combat.useCombatActionEvaluator", cc.isUseCombatActionEvaluator());

        // Detection
        DetectionConfig dc = c.getDetectionConfig();
        values.put("detection.viewRange", dc.getViewRange());
        values.put("detection.viewSector", dc.getViewSector());
        values.put("detection.hearingRange", dc.getHearingRange());
        values.put("detection.absoluteDetectionRange", dc.getAbsoluteDetectionRange());
        values.put("detection.alertedRange", dc.getAlertedRange());
        values.put("detection.investigateRange", dc.getInvestigateRange());

        // Health
        values.put("takesDamage", c.isTakesDamage());
        values.put("overrideHealth", c.isOverrideHealth());
        values.put("healthAmount", c.getHealthAmount());
        values.put("maxHealth", c.getMaxHealth());
        values.put("overrideDamage", c.isOverrideDamage());
        values.put("damageAmount", c.getDamageAmount());
        values.put("healthRegenEnabled", c.isHealthRegenEnabled());
        values.put("healthRegenAmount", c.getHealthRegenAmount());
        values.put("healthRegenIntervalSeconds", c.getHealthRegenIntervalSeconds());
        values.put("healthRegenDelayAfterDamageSeconds", c.getHealthRegenDelayAfterDamageSeconds());

        // Death / Respawn
        values.put("respawnOnDeath", c.isRespawnOnDeath());
        values.put("respawnDelaySeconds", c.getRespawnDelaySeconds());
        values.put("knockbackScale", c.getKnockbackScale());

        // Schedule
        ScheduleConfig sc = c.getScheduleConfig();
        values.put("schedule.enabled", sc.isEnabled());
        values.put("schedule.fallbackMode", sc.getFallbackMode().name());

        // Advanced / Role generation
        values.put("leashDistance", c.getLeashDistance());
        values.put("defaultNpcAttitude", c.getDefaultNpcAttitude());
        values.put("applySeparation", c.isApplySeparation());
        values.put("dropList", c.getDropList());
        values.put("runThreshold", c.getRunThreshold());
        values.put("breathesInWater", c.isBreathesInWater());

        // Path config
        PathConfig pc = c.getPathConfig();
        values.put("path.followPath", pc.isFollowPath());
        values.put("path.pathName", pc.getPathName());
        values.put("path.patrol", pc.isPatrol());
        values.put("path.loopMode", pc.getLoopMode());

        result.put("values", values);
        return result;
    }

    // ── Update ────────────────────────────────────────────────────

    @Nonnull
    @Override
    public List<String> updateEntity(@Nonnull String entityId, @Nonnull Map<String, String> values) {
        CitizenData c = findCitizen(entityId);
        if (c == null) return List.of("Citizen not found: " + entityId);

        List<String> errors = new ArrayList<>();

        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            try {
                if (!applyField(c, key, val)) {
                    errors.add("Unknown field: " + key);
                }
            } catch (Exception e) {
                errors.add("Error setting " + key + ": " + e.getMessage());
            }
        }

        if (errors.isEmpty()) {
            // Persist and respawn
            plugin.getCitizensManager().updateCitizen(c, true);
        }

        return errors;
    }

    // ── Execute Action ────────────────────────────────────────────

    @Nonnull
    @Override
    public Map<String, Object> executeAction(@Nonnull String actionId,
                                              @javax.annotation.Nullable String entityId,
                                              @Nonnull Map<String, String> parameters) {
        return switch (actionId) {
            case "create" -> executeCreate(parameters);
            case "createFromTemplate" -> executeCreateFromTemplate(parameters);
            case "createTemplate" -> executeCreateTemplate(parameters);
            case "deleteTemplate" -> executeDeleteTemplate(parameters);
            case "delete" -> executeDelete(entityId);
            default -> Map.of("success", false, "errors", List.of("Unknown action: " + actionId));
        };
    }

    private Map<String, Object> executeCreate(Map<String, String> params) {
        String name = params.getOrDefault("name", "").trim();
        if (name.isEmpty()) return Map.of("success", false, "errors", List.of("Name is required"));

        String modelId = params.getOrDefault("modelId", "Template_Citizen");
        String attitude = params.getOrDefault("attitude", "PASSIVE");

        float x, y, z, scale;
        try {
            x = Float.parseFloat(params.getOrDefault("x", "0"));
            y = Float.parseFloat(params.getOrDefault("y", "64"));
            z = Float.parseFloat(params.getOrDefault("z", "0"));
            scale = Float.parseFloat(params.getOrDefault("scale", "1.0"));
        } catch (NumberFormatException e) {
            return Map.of("success", false, "errors", List.of("Invalid number: " + e.getMessage()));
        }

        // Use the default world (or first available)
        String worldName = params.getOrDefault("world", "default");
        var world = com.hypixel.hytale.server.core.universe.Universe.get().getWorld(worldName);
        if (world == null) {
            // Try first available world
            var worldsMap = com.hypixel.hytale.server.core.universe.Universe.get().getWorlds();
            if (worldsMap.isEmpty()) {
                return Map.of("success", false, "errors", List.of("No worlds available"));
            }
            world = worldsMap.values().iterator().next();
        }
        // Get world UUID by looking up an existing citizen in that world, or use the world's entity store
        UUID worldUUID = null;
        for (CitizenData existing : plugin.getCitizensManager().getAllCitizens()) {
            if (existing.getWorldUUID() != null) {
                worldUUID = existing.getWorldUUID();
                break;
            }
        }
        if (worldUUID == null) {
            // Fallback: generate a UUID for this world
            worldUUID = UUID.nameUUIDFromBytes(world.getName().getBytes());
        }

        String id = UUID.randomUUID().toString();
        CitizenData citizen = new CitizenData(
                id, name, modelId, worldUUID,
                new Vector3d(x, y, z), new Vector3f(0, 0, 0), scale,
                null, null, "", "",
                new ArrayList<>(), false, false,
                null, null, 0, false
        );
        citizen.setAttitude(attitude);

        // Apply all additional fields from the expanded create form
        Set<String> coreFields = Set.of("name", "modelId", "attitude", "x", "y", "z", "scale", "world");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (coreFields.contains(entry.getKey()) || entry.getValue().isEmpty()) continue;
            try {
                applyField(citizen, entry.getKey(), entry.getValue());
            } catch (Exception ignored) {
                // Non-critical: skip fields that fail to apply
            }
        }

        plugin.getCitizensManager().addCitizen(citizen, true);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("entityId", id);
        result.put("message", "Citizen '" + name + "' created");
        return result;
    }

    private Map<String, Object> executeDelete(String entityId) {
        if (entityId == null || entityId.isEmpty()) {
            return Map.of("success", false, "errors", List.of("Entity ID required"));
        }
        CitizenData c = findCitizen(entityId);
        if (c == null) {
            return Map.of("success", false, "errors", List.of("Citizen not found: " + entityId));
        }
        plugin.getCitizensManager().removeCitizen(entityId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "Citizen '" + c.getName() + "' deleted");
        return result;
    }

    private Map<String, Object> executeCreateTemplate(Map<String, String> params) {
        String name = params.getOrDefault("templateName", "").trim();
        if (name.isEmpty()) return Map.of("success", false, "errors", List.of("Template name is required"));

        String description = params.getOrDefault("templateDescription", "");

        // Collect all non-meta fields as template values
        Map<String, String> values = new LinkedHashMap<>(params);
        values.remove("templateName");
        values.remove("templateDescription");
        // Remove empty values
        values.entrySet().removeIf(e -> e.getValue() == null || e.getValue().isEmpty());

        String id = UUID.randomUUID().toString();
        CitizenTemplate template = new CitizenTemplate(id, name, description, values);
        plugin.getTemplateManager().save(template);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "Template '" + name + "' created");
        return result;
    }

    private Map<String, Object> executeCreateFromTemplate(Map<String, String> params) {
        String templateName = params.getOrDefault("template", "").trim();
        if (templateName.isEmpty()) return Map.of("success", false, "errors", List.of("Template is required"));

        CitizenTemplate template = plugin.getTemplateManager().findByName(templateName);
        if (template == null) return Map.of("success", false, "errors", List.of("Template not found: " + templateName));

        // Merge template values with the provided params (params override template)
        Map<String, String> merged = new LinkedHashMap<>(template.getValues());
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!entry.getKey().equals("template") && !entry.getValue().isEmpty()) {
                merged.put(entry.getKey(), entry.getValue());
            }
        }
        // Use template name as default citizen name if not provided
        if (!merged.containsKey("name") || merged.get("name").isEmpty()) {
            merged.put("name", template.getName());
        }

        return executeCreate(merged);
    }

    private Map<String, Object> executeDeleteTemplate(Map<String, String> params) {
        String templateName = params.getOrDefault("template", "").trim();
        if (templateName.isEmpty()) return Map.of("success", false, "errors", List.of("Template is required"));

        CitizenTemplate template = plugin.getTemplateManager().findByName(templateName);
        if (template == null) return Map.of("success", false, "errors", List.of("Template not found: " + templateName));

        plugin.getTemplateManager().delete(template.getId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "Template '" + templateName + "' deleted");
        return result;
    }

    // ── Action Schema Definitions ─────────────────────────────────

    private Map<String, Object> createAction() {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("id", "create");
        action.put("label", "New Citizen");
        action.put("description", "Create a new NPC in the world");
        action.put("requiresEntity", false);
        action.put("groups", List.of(
            group("identity", "Identity", 0, List.of(
                fieldRequired("name", "Name", "string"),
                fieldEnum("modelId", "Model", getModelIds()),
                field("group", "Group", "string"),
                fieldEnum("attitude", "Attitude", "PASSIVE", "NEUTRAL", "AGGRESSIVE")
            )),
            group("appearance", "Appearance", 1, List.of(
                fieldBool("isPlayerModel", "Player Model"),
                field("skinUsername", "Skin Username", "string"),
                fieldBool("hideNametag", "Hide Nametag"),
                fieldBool("hideNpc", "Hide NPC"),
                fieldFloat("nametagOffset", "Nametag Offset", -5.0f, 5.0f),
                fieldFloat("scale", "Scale", 0.1f, 10.0f)
            )),
            group("location", "Spawn Location", 2, List.of(
                fieldRequired("x", "X", "float"),
                fieldRequired("y", "Y", "float"),
                fieldRequired("z", "Z", "float")
            )),
            group("movement", "Movement", 3, List.of(
                fieldEnum("movement.type", "Movement Type", "IDLE", "WANDER", "PATROL", "FOLLOW"),
                fieldFloat("movement.walkSpeed", "Walk Speed", 0.0f, 50.0f),
                fieldFloat("movement.wanderRadius", "Wander Radius", 0.0f, 100.0f),
                fieldBool("rotateTowardsPlayer", "Rotate Towards Player"),
                fieldFloat("lookAtDistance", "Look At Distance", 0.0f, 100.0f)
            )),
            group("health", "Health & Combat", 4, List.of(
                fieldBool("takesDamage", "Takes Damage"),
                fieldBool("overrideHealth", "Override Health"),
                fieldFloat("healthAmount", "Health Amount", 0.0f, 10000.0f),
                fieldBool("respawnOnDeath", "Respawn On Death"),
                fieldFloat("respawnDelaySeconds", "Respawn Delay (s)", 0.0f, 300.0f)
            ))
        ));
        return action;
    }

    private Map<String, Object> deleteAction() {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("id", "delete");
        action.put("label", "Delete Citizen");
        action.put("description", "Remove this citizen from the world");
        action.put("requiresEntity", true);
        action.put("confirm", "Are you sure you want to delete this citizen?");
        action.put("groups", List.of());
        return action;
    }

    private Map<String, Object> createFromTemplateAction() {
        List<String> templateNames = plugin.getTemplateManager().getTemplateNames();
        String[] templateOptions = templateNames.isEmpty()
                ? new String[]{"(no templates)"}
                : templateNames.toArray(new String[0]);

        Map<String, Object> action = new LinkedHashMap<>();
        action.put("id", "createFromTemplate");
        action.put("label", "From Template");
        action.put("description", "Create a citizen pre-filled from a saved template");
        action.put("requiresEntity", false);
        action.put("groups", List.of(
            group("template", "Template", 0, List.of(
                fieldEnum("template", "Template", templateOptions),
                field("name", "Name Override", "string")
            )),
            group("location", "Spawn Location", 1, List.of(
                fieldRequired("x", "X", "float"),
                fieldRequired("y", "Y", "float"),
                fieldRequired("z", "Z", "float")
            ))
        ));
        return action;
    }

    private Map<String, Object> createTemplateAction() {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("id", "createTemplate");
        action.put("label", "Save Template");
        action.put("description", "Save current settings as a reusable citizen template");
        action.put("requiresEntity", false);
        action.put("groups", List.of(
            group("meta", "Template Info", 0, List.of(
                fieldRequired("templateName", "Template Name", "string"),
                field("templateDescription", "Description", "string")
            )),
            group("identity", "Identity", 1, List.of(
                fieldEnum("modelId", "Model", getModelIds()),
                field("group", "Group", "string"),
                fieldEnum("attitude", "Attitude", "PASSIVE", "NEUTRAL", "AGGRESSIVE")
            )),
            group("appearance", "Appearance", 2, List.of(
                fieldBool("isPlayerModel", "Player Model"),
                field("skinUsername", "Skin Username", "string"),
                fieldBool("hideNametag", "Hide Nametag"),
                fieldFloat("nametagOffset", "Nametag Offset", -5.0f, 5.0f),
                fieldFloat("scale", "Scale", 0.1f, 10.0f)
            )),
            group("movement", "Movement", 3, List.of(
                fieldEnum("movement.type", "Movement Type", "IDLE", "WANDER", "PATROL", "FOLLOW"),
                fieldFloat("movement.walkSpeed", "Walk Speed", 0.0f, 50.0f),
                fieldFloat("movement.wanderRadius", "Wander Radius", 0.0f, 100.0f),
                fieldBool("rotateTowardsPlayer", "Rotate Towards Player"),
                fieldFloat("lookAtDistance", "Look At Distance", 0.0f, 100.0f)
            )),
            group("combat", "Combat", 4, List.of(
                fieldFloat("combat.attackDistance", "Attack Distance", 0.0f, 50.0f),
                fieldFloat("combat.chaseSpeed", "Chase Speed", 0.0f, 5.0f),
                fieldBool("combat.backOffAfterAttack", "Back Off After Attack"),
                fieldFloat("combat.targetRange", "Target Range", 0.0f, 100.0f)
            )),
            group("health", "Health & Respawn", 5, List.of(
                fieldBool("takesDamage", "Takes Damage"),
                fieldBool("overrideHealth", "Override Health"),
                fieldFloat("healthAmount", "Health Amount", 0.0f, 10000.0f),
                fieldBool("respawnOnDeath", "Respawn On Death"),
                fieldFloat("respawnDelaySeconds", "Respawn Delay (s)", 0.0f, 300.0f)
            ))
        ));
        return action;
    }

    private Map<String, Object> deleteTemplateAction() {
        List<String> templateNames = plugin.getTemplateManager().getTemplateNames();
        String[] templateOptions = templateNames.isEmpty()
                ? new String[]{"(no templates)"}
                : templateNames.toArray(new String[0]);

        Map<String, Object> action = new LinkedHashMap<>();
        action.put("id", "deleteTemplate");
        action.put("label", "Delete Template");
        action.put("description", "Remove a saved citizen template");
        action.put("requiresEntity", false);
        action.put("confirm", "Are you sure you want to delete this template?");
        action.put("groups", List.of(
            group("select", "Select Template", 0, List.of(
                fieldEnum("template", "Template", templateOptions)
            ))
        ));
        return action;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean applyField(CitizenData c, String key, String val) {
        switch (key) {
            // Identity
            case "name" -> c.setName(val);
            case "modelId" -> c.setModelId(val);
            case "group" -> c.setGroup(val);
            case "attitude" -> c.setAttitude(val);
            case "scale" -> c.setScale(Float.parseFloat(val));
            case "requiredPermission" -> c.setRequiredPermission(val);
            case "noPermissionMessage" -> c.setNoPermissionMessage(val);

            // Appearance
            case "isPlayerModel" -> c.setPlayerModel(Boolean.parseBoolean(val));
            case "skinUsername" -> c.setSkinUsername(val);
            case "useLiveSkin" -> c.setUseLiveSkin(Boolean.parseBoolean(val));
            case "hideNametag" -> c.setHideNametag(Boolean.parseBoolean(val));
            case "hideNpc" -> c.setHideNpc(Boolean.parseBoolean(val));
            case "nametagOffset" -> c.setNametagOffset(Float.parseFloat(val));
            case "npcHelmet" -> c.setNpcHelmet(val.isEmpty() ? null : val);
            case "npcChest" -> c.setNpcChest(val.isEmpty() ? null : val);
            case "npcLeggings" -> c.setNpcLeggings(val.isEmpty() ? null : val);
            case "npcGloves" -> c.setNpcGloves(val.isEmpty() ? null : val);
            case "npcHand" -> c.setNpcHand(val.isEmpty() ? null : val);
            case "npcOffHand" -> c.setNpcOffHand(val.isEmpty() ? null : val);

            // Movement
            case "movement.type" -> { MovementBehavior mb = c.getMovementBehavior(); mb.setType(val); c.setMovementBehavior(mb); }
            case "movement.walkSpeed" -> { MovementBehavior mb = c.getMovementBehavior(); mb.setWalkSpeed(Float.parseFloat(val)); c.setMovementBehavior(mb); }
            case "movement.wanderRadius" -> { MovementBehavior mb = c.getMovementBehavior(); mb.setWanderRadius(Float.parseFloat(val)); c.setMovementBehavior(mb); }
            case "movement.wanderWidth" -> { MovementBehavior mb = c.getMovementBehavior(); mb.setWanderWidth(Float.parseFloat(val)); c.setMovementBehavior(mb); }
            case "movement.wanderDepth" -> { MovementBehavior mb = c.getMovementBehavior(); mb.setWanderDepth(Float.parseFloat(val)); c.setMovementBehavior(mb); }
            case "rotateTowardsPlayer" -> c.setRotateTowardsPlayer(Boolean.parseBoolean(val));
            case "lookAtDistance" -> c.setLookAtDistance(Float.parseFloat(val));
            case "followCitizenEnabled" -> c.setFollowCitizenEnabled(Boolean.parseBoolean(val));
            case "followCitizenId" -> c.setFollowCitizenId(val);
            case "followDistance" -> c.setFollowDistance(Float.parseFloat(val));

            // Interaction
            case "fKeyInteractionEnabled" -> c.setFKeyInteractionEnabled(Boolean.parseBoolean(val));
            case "forceFKeyInteractionText" -> c.setForceFKeyInteractionText(Boolean.parseBoolean(val));
            case "commandSelectionMode" -> c.setCommandSelectionMode(val);
            case "firstInteractionEnabled" -> c.setFirstInteractionEnabled(Boolean.parseBoolean(val));
            case "firstInteractionCommandSelectionMode" -> c.setFirstInteractionCommandSelectionMode(val);
            case "postFirstInteractionBehavior" -> c.setPostFirstInteractionBehavior(val);
            case "runNormalOnFirstInteraction" -> c.setRunNormalOnFirstInteraction(Boolean.parseBoolean(val));

            // Messages
            case "messages.enabled" -> { MessagesConfig m = c.getMessagesConfig(); m.setEnabled(Boolean.parseBoolean(val)); c.setMessagesConfig(m); }
            case "messages.selectionMode" -> { MessagesConfig m = c.getMessagesConfig(); m.setSelectionMode(val); c.setMessagesConfig(m); }

            // Combat
            case "combat.attackType" -> { CombatConfig cc = c.getCombatConfig(); cc.setAttackType(val); c.setCombatConfig(cc); }
            case "combat.attackDistance" -> { CombatConfig cc = c.getCombatConfig(); cc.setAttackDistance(Float.parseFloat(val)); c.setCombatConfig(cc); }
            case "combat.chaseSpeed" -> { CombatConfig cc = c.getCombatConfig(); cc.setChaseSpeed(Float.parseFloat(val)); c.setCombatConfig(cc); }
            case "combat.combatBehaviorDistance" -> { CombatConfig cc = c.getCombatConfig(); cc.setCombatBehaviorDistance(Float.parseFloat(val)); c.setCombatConfig(cc); }
            case "combat.combatStrafeWeight" -> { CombatConfig cc = c.getCombatConfig(); cc.setCombatStrafeWeight(Integer.parseInt(val)); c.setCombatConfig(cc); }
            case "combat.combatDirectWeight" -> { CombatConfig cc = c.getCombatConfig(); cc.setCombatDirectWeight(Integer.parseInt(val)); c.setCombatConfig(cc); }
            case "combat.backOffAfterAttack" -> { CombatConfig cc = c.getCombatConfig(); cc.setBackOffAfterAttack(Boolean.parseBoolean(val)); c.setCombatConfig(cc); }
            case "combat.backOffDistance" -> { CombatConfig cc = c.getCombatConfig(); cc.setBackOffDistance(Float.parseFloat(val)); c.setCombatConfig(cc); }
            case "combat.desiredAttackDistanceMin" -> { CombatConfig cc = c.getCombatConfig(); cc.setDesiredAttackDistanceMin(Float.parseFloat(val)); c.setCombatConfig(cc); }
            case "combat.desiredAttackDistanceMax" -> { CombatConfig cc = c.getCombatConfig(); cc.setDesiredAttackDistanceMax(Float.parseFloat(val)); c.setCombatConfig(cc); }
            case "combat.attackPauseMin" -> { CombatConfig cc = c.getCombatConfig(); cc.setAttackPauseMin(Float.parseFloat(val)); c.setCombatConfig(cc); }
            case "combat.attackPauseMax" -> { CombatConfig cc = c.getCombatConfig(); cc.setAttackPauseMax(Float.parseFloat(val)); c.setCombatConfig(cc); }
            case "combat.blockAbility" -> { CombatConfig cc = c.getCombatConfig(); cc.setBlockAbility(val); c.setCombatConfig(cc); }
            case "combat.blockProbability" -> { CombatConfig cc = c.getCombatConfig(); cc.setBlockProbability(Integer.parseInt(val)); c.setCombatConfig(cc); }
            case "combat.targetRange" -> { CombatConfig cc = c.getCombatConfig(); cc.setTargetRange(Float.parseFloat(val)); c.setCombatConfig(cc); }
            case "combat.useCombatActionEvaluator" -> { CombatConfig cc = c.getCombatConfig(); cc.setUseCombatActionEvaluator(Boolean.parseBoolean(val)); c.setCombatConfig(cc); }

            // Detection
            case "detection.viewRange" -> { DetectionConfig d = c.getDetectionConfig(); d.setViewRange(Float.parseFloat(val)); c.setDetectionConfig(d); }
            case "detection.viewSector" -> { DetectionConfig d = c.getDetectionConfig(); d.setViewSector(Float.parseFloat(val)); c.setDetectionConfig(d); }
            case "detection.hearingRange" -> { DetectionConfig d = c.getDetectionConfig(); d.setHearingRange(Float.parseFloat(val)); c.setDetectionConfig(d); }
            case "detection.absoluteDetectionRange" -> { DetectionConfig d = c.getDetectionConfig(); d.setAbsoluteDetectionRange(Float.parseFloat(val)); c.setDetectionConfig(d); }
            case "detection.alertedRange" -> { DetectionConfig d = c.getDetectionConfig(); d.setAlertedRange(Float.parseFloat(val)); c.setDetectionConfig(d); }
            case "detection.investigateRange" -> { DetectionConfig d = c.getDetectionConfig(); d.setInvestigateRange(Float.parseFloat(val)); c.setDetectionConfig(d); }

            // Health
            case "takesDamage" -> c.setTakesDamage(Boolean.parseBoolean(val));
            case "overrideHealth" -> c.setOverrideHealth(Boolean.parseBoolean(val));
            case "healthAmount" -> c.setHealthAmount(Float.parseFloat(val));
            case "maxHealth" -> c.setMaxHealth(Float.parseFloat(val));
            case "overrideDamage" -> c.setOverrideDamage(Boolean.parseBoolean(val));
            case "damageAmount" -> c.setDamageAmount(Float.parseFloat(val));
            case "healthRegenEnabled" -> c.setHealthRegenEnabled(Boolean.parseBoolean(val));
            case "healthRegenAmount" -> c.setHealthRegenAmount(Float.parseFloat(val));
            case "healthRegenIntervalSeconds" -> c.setHealthRegenIntervalSeconds(Float.parseFloat(val));
            case "healthRegenDelayAfterDamageSeconds" -> c.setHealthRegenDelayAfterDamageSeconds(Float.parseFloat(val));

            // Death / Respawn
            case "respawnOnDeath" -> c.setRespawnOnDeath(Boolean.parseBoolean(val));
            case "respawnDelaySeconds" -> c.setRespawnDelaySeconds(Float.parseFloat(val));
            case "knockbackScale" -> c.setKnockbackScale(Float.parseFloat(val));

            // Schedule
            case "schedule.enabled" -> { ScheduleConfig sc = c.getScheduleConfig(); sc.setEnabled(Boolean.parseBoolean(val)); c.setScheduleConfig(sc); }
            case "schedule.fallbackMode" -> { ScheduleConfig sc = c.getScheduleConfig(); sc.setFallbackMode(ScheduleFallbackMode.valueOf(val)); c.setScheduleConfig(sc); }

            // Advanced
            case "leashDistance" -> c.setLeashDistance(Float.parseFloat(val));
            case "defaultNpcAttitude" -> c.setDefaultNpcAttitude(val);
            case "applySeparation" -> c.setApplySeparation(Boolean.parseBoolean(val));
            case "dropList" -> c.setDropList(val);
            case "runThreshold" -> c.setRunThreshold(Float.parseFloat(val));
            case "breathesInWater" -> c.setBreathesInWater(Boolean.parseBoolean(val));

            // Path
            case "path.followPath" -> { PathConfig pc = c.getPathConfig(); pc.setFollowPath(Boolean.parseBoolean(val)); c.setPathConfig(pc); }
            case "path.pathName" -> { PathConfig pc = c.getPathConfig(); pc.setPathName(val); c.setPathConfig(pc); }
            case "path.patrol" -> { PathConfig pc = c.getPathConfig(); pc.setPatrol(Boolean.parseBoolean(val)); c.setPathConfig(pc); }
            case "path.loopMode" -> { PathConfig pc = c.getPathConfig(); pc.setLoopMode(val); c.setPathConfig(pc); }

            default -> { return false; }
        }
        return true;
    }

    // ── Schema Group Definitions ──────────────────────────────────

    private Map<String, Object> identityGroup() {
        return group("identity", "Identity", 0, List.of(
            field("name", "Name", "string"),
            fieldEnum("modelId", "Model", getModelIds()),
            field("group", "Group", "string"),
            fieldEnum("attitude", "Attitude", "PASSIVE", "NEUTRAL", "AGGRESSIVE"),
            fieldFloat("scale", "Scale", 0.1f, 10.0f),
            fieldReadOnly("position", "Position", "vector3d"),
            fieldReadOnly("rotation", "Rotation", "vector3f"),
            field("requiredPermission", "Required Permission", "string"),
            field("noPermissionMessage", "No Permission Message", "string")
        ));
    }

    private Map<String, Object> appearanceGroup() {
        return group("appearance", "Appearance", 1, List.of(
            fieldBool("isPlayerModel", "Player Model"),
            field("skinUsername", "Skin Username", "string"),
            fieldBool("useLiveSkin", "Live Skin"),
            fieldBool("hideNametag", "Hide Nametag"),
            fieldBool("hideNpc", "Hide NPC"),
            fieldFloat("nametagOffset", "Nametag Offset", -5.0f, 5.0f),
            field("npcHelmet", "Helmet", "string"),
            field("npcChest", "Chestplate", "string"),
            field("npcLeggings", "Leggings", "string"),
            field("npcGloves", "Gloves", "string"),
            field("npcHand", "Main Hand", "string"),
            field("npcOffHand", "Off Hand", "string")
        ));
    }

    private Map<String, Object> movementGroup() {
        return group("movement", "Movement", 2, List.of(
            fieldEnum("movement.type", "Movement Type", "IDLE", "WANDER", "PATROL", "FOLLOW"),
            fieldFloat("movement.walkSpeed", "Walk Speed", 0.0f, 50.0f),
            fieldFloat("movement.wanderRadius", "Wander Radius", 0.0f, 100.0f),
            fieldFloat("movement.wanderWidth", "Wander Width", 0.0f, 100.0f),
            fieldFloat("movement.wanderDepth", "Wander Depth", 0.0f, 100.0f),
            fieldBool("rotateTowardsPlayer", "Rotate Towards Player"),
            fieldFloat("lookAtDistance", "Look At Distance", 0.0f, 100.0f),
            fieldBool("followCitizenEnabled", "Follow Citizen"),
            field("followCitizenId", "Follow Citizen ID", "string"),
            fieldFloat("followDistance", "Follow Distance", 0.1f, 50.0f),
            fieldBool("path.followPath", "Follow Path"),
            field("path.pathName", "Path Name", "string"),
            fieldBool("path.patrol", "Patrol"),
            fieldEnum("path.loopMode", "Loop Mode", "LOOP", "PING_PONG")
        ));
    }

    private Map<String, Object> interactionGroup() {
        return group("interaction", "Interaction", 3, List.of(
            fieldBool("fKeyInteractionEnabled", "F-Key Interaction"),
            fieldBool("forceFKeyInteractionText", "Force F-Key Text"),
            fieldEnum("commandSelectionMode", "Command Selection", "ALL", "RANDOM", "SEQUENTIAL"),
            fieldBool("firstInteractionEnabled", "First Interaction Enabled"),
            fieldEnum("firstInteractionCommandSelectionMode", "First Interaction Selection", "ALL", "RANDOM", "SEQUENTIAL"),
            fieldEnum("postFirstInteractionBehavior", "Post First Interaction", "NORMAL", "DISABLE"),
            fieldBool("runNormalOnFirstInteraction", "Run Normal On First")
        ));
    }

    private Map<String, Object> messagesGroup() {
        return group("messages", "Messages", 4, List.of(
            fieldBool("messages.enabled", "Messages Enabled"),
            fieldEnum("messages.selectionMode", "Selection Mode", "ALL", "RANDOM", "SEQUENTIAL")
        ));
    }

    private Map<String, Object> combatGroup() {
        return group("combat", "Combat", 5, List.of(
            field("combat.attackType", "Attack Type", "string"),
            fieldFloat("combat.attackDistance", "Attack Distance", 0.0f, 50.0f),
            fieldFloat("combat.chaseSpeed", "Chase Speed", 0.0f, 5.0f),
            fieldFloat("combat.combatBehaviorDistance", "Combat Behavior Distance", 0.0f, 50.0f),
            fieldInt("combat.combatStrafeWeight", "Strafe Weight", 0, 100),
            fieldInt("combat.combatDirectWeight", "Direct Weight", 0, 100),
            fieldBool("combat.backOffAfterAttack", "Back Off After Attack"),
            fieldFloat("combat.backOffDistance", "Back Off Distance", 0.0f, 20.0f),
            fieldFloat("combat.desiredAttackDistanceMin", "Desired Attack Dist Min", 0.0f, 50.0f),
            fieldFloat("combat.desiredAttackDistanceMax", "Desired Attack Dist Max", 0.0f, 50.0f),
            fieldFloat("combat.attackPauseMin", "Attack Pause Min", 0.0f, 10.0f),
            fieldFloat("combat.attackPauseMax", "Attack Pause Max", 0.0f, 10.0f),
            field("combat.blockAbility", "Block Ability", "string"),
            fieldInt("combat.blockProbability", "Block Probability", 0, 100),
            fieldFloat("combat.targetRange", "Target Range", 0.0f, 100.0f),
            fieldBool("combat.useCombatActionEvaluator", "Use Combat Evaluator")
        ));
    }

    private Map<String, Object> detectionGroup() {
        return group("detection", "Detection", 6, List.of(
            fieldFloat("detection.viewRange", "View Range", 0.0f, 100.0f),
            fieldFloat("detection.viewSector", "View Sector", 0.0f, 360.0f),
            fieldFloat("detection.hearingRange", "Hearing Range", 0.0f, 100.0f),
            fieldFloat("detection.absoluteDetectionRange", "Absolute Detection Range", 0.0f, 100.0f),
            fieldFloat("detection.alertedRange", "Alerted Range", 0.0f, 100.0f),
            fieldFloat("detection.investigateRange", "Investigate Range", 0.0f, 100.0f)
        ));
    }

    private Map<String, Object> healthGroup() {
        return group("health", "Health & Damage", 7, List.of(
            fieldBool("takesDamage", "Takes Damage"),
            fieldBool("overrideHealth", "Override Health"),
            fieldFloat("healthAmount", "Health Amount", 0.0f, 10000.0f),
            fieldFloat("maxHealth", "Max Health", 0.0f, 10000.0f),
            fieldBool("overrideDamage", "Override Damage"),
            fieldFloat("damageAmount", "Damage Amount", 0.0f, 10000.0f),
            fieldBool("healthRegenEnabled", "Health Regen"),
            fieldFloat("healthRegenAmount", "Regen Amount", 0.0f, 1000.0f),
            fieldFloat("healthRegenIntervalSeconds", "Regen Interval (s)", 0.1f, 60.0f),
            fieldFloat("healthRegenDelayAfterDamageSeconds", "Regen Delay After Damage (s)", 0.0f, 60.0f)
        ));
    }

    private Map<String, Object> deathGroup() {
        return group("death", "Death & Respawn", 8, List.of(
            fieldBool("respawnOnDeath", "Respawn On Death"),
            fieldFloat("respawnDelaySeconds", "Respawn Delay (s)", 0.0f, 300.0f),
            fieldFloat("knockbackScale", "Knockback Scale", 0.0f, 5.0f)
        ));
    }

    private Map<String, Object> scheduleGroup() {
        return group("schedule", "Schedule", 9, List.of(
            fieldBool("schedule.enabled", "Schedule Enabled"),
            fieldEnum("schedule.fallbackMode", "Fallback Mode", "USE_BASE_BEHAVIOR", "DISABLE_CITIZEN")
        ));
    }

    private Map<String, Object> advancedGroup() {
        return group("advanced", "Advanced", 10, List.of(
            fieldFloat("leashDistance", "Leash Distance", 0.0f, 200.0f),
            fieldEnum("defaultNpcAttitude", "Default NPC Attitude", "Ignore", "Friendly", "Hostile"),
            fieldBool("applySeparation", "Apply Separation"),
            field("dropList", "Drop List", "string"),
            fieldFloat("runThreshold", "Run Threshold", 0.0f, 1.0f),
            fieldBool("breathesInWater", "Breathes In Water")
        ));
    }

    // ── Model IDs ─────────────────────────────────────────────────

    private String[] getModelIds() {
        try {
            Set<String> ids = ModelAsset.getAssetMap().getAssetMap().keySet();
            List<String> sorted = new ArrayList<>(ids);
            sorted.sort(String.CASE_INSENSITIVE_ORDER);
            return sorted.toArray(new String[0]);
        } catch (Exception e) {
            return new String[]{"Player", "Template_Citizen"};
        }
    }

    // ── Helpers ───────────────────────────────────────────────────

    private CitizenData findCitizen(String id) {
        for (CitizenData c : plugin.getCitizensManager().getAllCitizens()) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    private static Map<String, Object> group(String id, String label, int order, List<Map<String, Object>> fields) {
        Map<String, Object> g = new LinkedHashMap<>();
        g.put("id", id);
        g.put("label", label);
        g.put("order", order);
        g.put("fields", fields);
        return g;
    }

    private static Map<String, Object> field(String id, String label, String type) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("id", id);
        f.put("label", label);
        f.put("type", type);
        return f;
    }

    private static Map<String, Object> fieldBool(String id, String label) {
        return field(id, label, "bool");
    }

    private static Map<String, Object> fieldFloat(String id, String label, float min, float max) {
        Map<String, Object> f = field(id, label, "float");
        f.put("min", min);
        f.put("max", max);
        return f;
    }

    private static Map<String, Object> fieldInt(String id, String label, int min, int max) {
        Map<String, Object> f = field(id, label, "int");
        f.put("min", min);
        f.put("max", max);
        return f;
    }

    private static Map<String, Object> fieldEnum(String id, String label, String... values) {
        Map<String, Object> f = field(id, label, "enum");
        f.put("enumValues", List.of(values));
        return f;
    }

    private static Map<String, Object> fieldRequired(String id, String label, String type) {
        Map<String, Object> f = field(id, label, type);
        f.put("required", true);
        return f;
    }

    private static Map<String, Object> fieldReadOnly(String id, String label, String type) {
        Map<String, Object> f = field(id, label, type);
        f.put("readOnly", true);
        return f;
    }

    private static Map<String, Object> vec3dMap(Vector3d v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("x", round2(v.x));
        m.put("y", round2(v.y));
        m.put("z", round2(v.z));
        return m;
    }

    private static Map<String, Object> vec3fMap(Vector3f v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("x", round2(v.x));
        m.put("y", round2(v.y));
        m.put("z", round2(v.z));
        return m;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static double round2(float v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static String orEmpty(String s) {
        return s != null ? s : "";
    }
}
