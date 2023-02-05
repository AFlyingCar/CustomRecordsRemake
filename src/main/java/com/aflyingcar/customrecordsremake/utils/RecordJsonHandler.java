package com.aflyingcar.customrecordsremake.utils;

import com.aflyingcar.customrecordsremake.CustomRecordsRemakeMod;
import com.aflyingcar.customrecordsremake.item.ItemCustomRecord;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class RecordJsonHandler {
    public static class Record {
        /**
         * The filename to use when finding the .png and .ogg for this record
         */
        public String fileName;

        /**
         * The resource location to find the components required for this record
         */
        public ResourceLocation location;

        /**
         * The human-readable name for this record
         */
        public String name;

        /**
         * The SoundEvent that this record will trigger when played
         */
        public SoundEvent sound;

        /**
         * The length of this record in seconds
         */
        public int length;

        /**
         * A pair specifying the item and metadata used in the recipe for this record. Can be null
         */
        @Nullable
        public Map.Entry<String, Integer> recipe;

        /**
         * A singleton for the Item tied to this Record
         */
        private ItemCustomRecord item = null;

        /**
         * Gets the item tied to this Record. Will construct a new ItemCustomRecord if one hasn't been made yet
         *
         * @return The ItemCustomRecord tied to this Record
         */
        @NotNull
        public ItemCustomRecord getItem() {
            if(item == null) {
                CustomRecordsRemakeMod.getLogger().debug("Building item '" + fileName + "' with soundEvent " + sound.getLocation());

                item = ItemCustomRecord.RecordItemBuilder.create(sound, length)
                        .buildItem();
            }

            return item;
        }
    }

    /**
     * A map of all records loaded.
     */
    private static final HashMap<String, Record> records = new HashMap<>();

    /**
     * Loads all record entries from 'config/records.json'
     *
     * @param file A File pointing to the records.json file
     */
    public static void handleConfig(File file) {
        try {
            /*
             * Format:
             * {
             *     "<unique_id>": {
             *         "item": "<item used in crafting recipe>",
             *         "filename": "<name of the asset files, no extension>",
             *         "name": "<human readable name of the record>",
             *         "meta": "<metadata for crafting item. If there is none, leave this as 0>",
             *         "length": "<The length of the track, in seconds>",
             *     }
             * }
             */

            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(file));
            JsonObject element = gson.fromJson(reader, JsonObject.class);

            if (element != null) {
                // Go through each entry and try to add it as a new record
                for (Map.Entry<String, JsonElement> object : element.entrySet()) {
                    var record = new Record();

                    var jsonTag = object.getKey();
                    var jsonObject = object.getValue().getAsJsonObject();

                    // Check for required fields. If those fields are missing, then warn and skip the entry
                    if(!jsonObject.has("name")) {
                        // TODO: Should we optionally crash the game here? That would require some sort of config.
                        CustomRecordsRemakeMod.getLogger().warn("Entry for record tagged '" + jsonTag + "' is missing the required 'name' field! This record entry will be skipped.");
                        continue;
                    }
                    if(!jsonObject.has("filename")) {
                        CustomRecordsRemakeMod.getLogger().warn("Entry for record tagged '" + jsonTag + "' is missing the required 'fileName' field! This record entry will be skipped.");
                        continue;
                    }
                    if(!jsonObject.has("length")) {
                        CustomRecordsRemakeMod.getLogger().warn("Entry for record tagged '" + jsonTag + "' is missing the required 'length' field! This record entry will be skipped.");
                        continue;
                    }

                    // Load the required fields into the record object
                    record.name = jsonObject.get("name").getAsString();
                    record.fileName = jsonObject.get("filename").getAsString();
                    record.length = jsonObject.get("length").getAsInt();

                    // Build the item's location and the sound event for this record
                    record.location = new ResourceLocation(CustomRecordsRemakeMod.MODID, record.fileName);
                    record.sound = new SoundEvent(record.location);

                    // Check for optional fields
                    if(jsonObject.has("item")) {
                        var itemRecipe = jsonObject.get("item").getAsString();
                        var meta = 0;

                        // Metadata is optional, if it isn't specified we can assume it's just '0'
                        if(jsonObject.has("meta")) {
                            meta = jsonObject.get("meta").getAsInt();
                        } else {
                            CustomRecordsRemakeMod.getLogger().debug("Entry for record '" + record.name + "' is missing the 'meta' field. Assuming meta=0.");
                        }

                        // Load the recipe into the record object
                        record.recipe = new AbstractMap.SimpleEntry<>(itemRecipe, meta);
                    } else {
                        CustomRecordsRemakeMod.getLogger().warn("Entry for record '" + record.name + "' is missing the 'item' field! This will not be treated as an error, but it does mean that the record will not be craftable.");
                    }

                    // Add this record last, so that if any of the above fails we don't end up with half-formed data
                    records.put(record.fileName, record);

                    CustomRecordsRemakeMod.getLogger().info("Successfully loaded record '" + record.fileName + "'");
                }

                CustomRecordsRemakeMod.getLogger().info("Successfully loaded " + records.size() + " records!");
                CustomRecordsRemakeMod.getLogger().debug("Records: [" + String.join(", ", getFileNames()) + "]");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates and returns a new sounds.json file.
     *
     * @return The contents of sounds.json
     */
    public static String setupSoundsJson() {
        CustomRecordsRemakeMod.getLogger().debug("Generating sounds JSON...");

        Gson gson = new Gson();
        JsonObject finishedFile = new JsonObject();
        for (SoundEvent sound : getSounds()) {
            JsonObject soundInfo = new JsonObject();
            soundInfo.addProperty("category", "record");
            JsonObject trackInfo = new JsonObject();

            trackInfo.addProperty("name", CustomRecordsRemakeMod.MODID + ":music/" + sound.getLocation().getPath());
            trackInfo.addProperty("stream", true);
            JsonArray array = new JsonArray();
            array.add(trackInfo);
            soundInfo.add("sounds", array);
            finishedFile.add(sound.getLocation().getPath(), soundInfo);
        }

        CustomRecordsRemakeMod.getLogger().debug("Successfully generated sounds.json: " + finishedFile);

        return gson.toJson(finishedFile);
    }

    /**
     * Generates and returns a model json file for the given record.
     *
     * @param name The name of the record to generate a model json for.
     *
     * @return The contents of sounds.json
     */
    public static String genModelJson(String name) {
        CustomRecordsRemakeMod.getLogger().debug("Generating model JSON for '" + name + "'");

        JsonObject finishedFile = new JsonObject();
        finishedFile.addProperty("forge_marker", 1);
        finishedFile.addProperty("parent", "item/generated");
        JsonObject texture = new JsonObject();
        texture.addProperty("layer0", CustomRecordsRemakeMod.MODID + ":items/" + name);
        finishedFile.add("textures", texture);
        return finishedFile.toString();
    }

    /**
     * Gets the recipe JSON for the given record, or returns an empty Optional if there is no valid recipe defined.
     *
     * @param name The name of the record to get the recipe for
     * @return The recipe json of this record if a valid recipe is registered for it, otherwise an empty Optional is returned.
     */
    public static Optional<String> genRecipeJson(String name) {
        CustomRecordsRemakeMod.getLogger().debug("Generating recipe JSON for '" + name + "'");

        var record = records.get(name);

        if(record.recipe == null) {
            return Optional.empty();
        }

        JsonObject finishedFile = new JsonObject();

        finishedFile.addProperty("type", "minecraft:crafting_shaped");

        // pattern
        JsonArray pattern = new JsonArray();
        {
            pattern.add(" B ");
            pattern.add("BIB");
            pattern.add(" B ");
        }
        finishedFile.add("pattern", pattern);

        // key
        JsonObject key = new JsonObject();
        {
            JsonObject B = new JsonObject();
            B.addProperty("item", "minecraft:black_terracotta");
            key.add("B", B);

            JsonObject I = new JsonObject();
            {
                var recipeItemPair = record.recipe;
                var recipeItem = recipeItemPair.getKey();
                var recipeItemMeta = recipeItemPair.getKey();

                I.addProperty("item", recipeItem);

                // Omit the metadata value if it is just 0
                if(recipeItemPair.getValue() != 0) {
                    I.addProperty("data", recipeItemMeta);
                }
            }
            key.add("I", I);
        }
        finishedFile.add("key", key);

        // result
        JsonObject result = new JsonObject();
        {
            result.addProperty("item", record.location.toString());
            result.addProperty("count", 1);
        }
        finishedFile.add("result", result);

        CustomRecordsRemakeMod.getLogger().debug("Generated following recipe JSON: " + finishedFile);

        return Optional.of(finishedFile.toString());
    }

    public static List<SoundEvent> getSounds() {
        return records.values().stream().map(r -> r.sound).collect(Collectors.toList());
    }

    public static List<String> getFileNames() {
        return records.values().stream().map(r -> r.fileName).collect(Collectors.toList());
    }

    public static Map<String, Record> getRecords() {
        return records;
    }

}
