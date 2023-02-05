package com.aflyingcar.customrecordsremake.packs;

import com.aflyingcar.customrecordsremake.CustomRecordsRemakeMod;
import com.aflyingcar.customrecordsremake.utils.RecordJsonHandler;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

public class CustomRecordsPackResources extends FolderPackResources {
    private static final ArrayList<String> modelFiles = new ArrayList<>();
    private static final ArrayList<String> oggFiles = new ArrayList<>();
    private static final ArrayList<String> textureFiles = new ArrayList<>();
    private static final ArrayList<String> recipeFiles = new ArrayList<>();

    public CustomRecordsPackResources() {
        super(new File("sounds.json"));

        CustomRecordsRemakeMod.getLogger().debug("Loading CustomRecordsPackResources");
        for(var record : RecordJsonHandler.getRecords().values()) {
            CustomRecordsRemakeMod.getLogger().debug("Loading resources for record '" + record.name + "'");

            modelFiles.add(getAssetPathFor("/models/item/" + record.fileName + ".json"));
            oggFiles.add(getAssetPathFor("/sounds/music/" + record.fileName + ".ogg"));
            textureFiles.add(getAssetPathFor("/textures/items/" + record.fileName + ".png"));

            // Do not add a recipe if the record doesn't have one
            if(record.recipe != null) {
                recipeFiles.add(getDataPathFor("/recipes/" + record.fileName + ".json"));
            }
        }
    }

    /**
     * Gets the resource for the given name.
     *
     * @param name The resource to get
     *
     * @return An InputStream containing the resource
     * @throws IOException
     */
    @NotNull
    @Override
    protected InputStream getResource(String name) throws IOException {
        if(name.equals(getAssetPathFor("/sounds.json"))) {
            return new ByteArrayInputStream(CustomRecordsRemakeMod.getSoundsJson().getBytes());
        } else if(name.equals("pack.mcmeta")) {
            return new ByteArrayInputStream(("{\n \"pack\": {\n   \"description\": \"Custom Records Remake's internal pack\",\n   \"pack_format\": 9\n}\n}").getBytes(StandardCharsets.UTF_8));
        } else if(name.equals(getAssetPathFor("/lang/en_us.json"))) {
            JsonObject root = new JsonObject();

            var records = RecordJsonHandler.getRecords().values();
            for (var record : records) {
                var nameTranslationKey = "item." + CustomRecordsRemakeMod.MODID + "." + record.fileName;
                var descTranslationKey = "item." + CustomRecordsRemakeMod.MODID + "." + record.fileName + ".desc";

                root.addProperty(nameTranslationKey, "Music Disc");
                root.addProperty(descTranslationKey, record.name);
            }

            return new ByteArrayInputStream(root.toString().getBytes());
        } else if(modelFiles.contains(name)) {
            // Convert the path (should be 'assets/{MODID}/models/item/{NAME}.json') to just the name so we can look it up
            var modelName = name.replace(getAssetPathFor("/models/item/"), "").replace(".json", "");
            return new ByteArrayInputStream(RecordJsonHandler.genModelJson(modelName).getBytes());
        } else if(oggFiles.contains(name)) {
            // Convert the given relative path (should be 'assets/{MODID}/sounds/music/{NAME}.ogg') to the correct path pointing at the texture in the config directory
            var oggPath = CustomRecordsRemakeMod.getRecordsResourcesDir() + File.separator + name.replace(getAssetPathFor("/sounds/music/"), "");
            return new FileInputStream(oggPath);
        } else if(textureFiles.contains(name)) {
            // Convert the given relative path (should be 'assets/{MODID}/textures/items/{NAME}.png') to the correct path pointing at the texture in the config directory
            var texturePath = CustomRecordsRemakeMod.getRecordsResourcesDir() + File.separator + name.replace(getAssetPathFor("/textures/items/"), "");
            return new FileInputStream(texturePath);
        } else if(recipeFiles.contains(name)) {
            // Convert the path (should be 'data/{MODID}/recipes/{NAME}.json') to just the name so we can look it up
            var recipeName = name.replace(getDataPathFor("/recipes/"), "").replace(".json", "");

            // Recipes can be empty, if so, then we just will return an "empty" json object
            return new ByteArrayInputStream(RecordJsonHandler.genRecipeJson(recipeName).orElse("{}").getBytes());
        }

        CustomRecordsRemakeMod.getLogger().warn("Asked for resource '" + name + "' that we do not have a handler for! Deferring to FolderPackResources::getResource()");
        return super.getResource(name);
    }

    /**
     * Checks if this resource pack contains the given name.
     * This resource pack contains the following:
     *     assets/{MODID}/sounds.json
     *     assets/{MODID}/lang/en_us.json
     *     assets/{MODID}/models/item/*.json
     *     assets/{MODID}/sounds/music/*.ogg
     *     assets/{MODID}/textures/items/*.png
     *     data/{MODID}/recipes/*.json
     *
     * @param name The name to check
     * @return True if 'name' is referring to the sounds.json, lang/en_us.json, or any model, ogg, texture, or recipe
     *         file used for a record.
     */
    @Override
    protected boolean hasResource(String name) {
        return name.equals(getAssetPathFor("/sounds.json")) ||
               name.equals(getAssetPathFor("/lang/en_us.json")) ||
               modelFiles.contains(name) ||
               oggFiles.contains(name) ||
               textureFiles.contains(name) ||
               recipeFiles.contains(name);
    }

    public String getAssetPathFor(String relativePath) {
        return "assets/" + CustomRecordsRemakeMod.MODID + relativePath;
    }

    public String getDataPathFor(String relativePath) {
        return "data/" + CustomRecordsRemakeMod.MODID + relativePath;
    }

    /**
     * Gets the relative path for a given full path, cutting out the 'assets/{MODID}/' or 'data/{MODID}/' components
     *
     * @param fullPath The full path
     * @return The relative path
     */
    public String getRelativePathForAssetOrData(String fullPath) {
        return fullPath.replace("assets/" + CustomRecordsRemakeMod.MODID + "/", "")
                .replace("data/" + CustomRecordsRemakeMod.MODID + "/", "");
    }

    @NotNull
    @Override
    public String getName() {
        return "CustomRecordsRemakeInternalResourcePack";
    }

    @NotNull
    @Override
    public Set<String> getNamespaces(@NotNull PackType p_10259_) {
        Set<String> set = Sets.newHashSet();
        set.add(CustomRecordsRemakeMod.MODID);
        return set;
    }

    /**
     * Gets a list of resources in this resource pack.
     * This pack is used for both the client and server, and is also a virtual data pack (that is: not referring to
     * files on the disk). As such: we must override this method and make sure to return both the client-side and
     * server-side resources.
     *
     * @param packType The type of pack this is
     * @param namespace The namespace to check for
     * @param path
     * @param filter A filter to apply to the list of resources.
     *
     * @return A collection of all resources in this pack.
     */
    @NotNull
    @Override
    public Collection<ResourceLocation> getResources(@NotNull PackType packType, @NotNull String namespace, @NotNull String path, @NotNull Predicate<ResourceLocation> filter) {
        if(Objects.requireNonNull(packType) == PackType.CLIENT_RESOURCES) {
            return super.getResources(packType, namespace, path, filter);
        } else {
            return recipeFiles.stream()
                    .map(p -> new ResourceLocation(CustomRecordsRemakeMod.MODID, getRelativePathForAssetOrData(p)))
                    // .peek(resourceLocation -> CustomRecordsRemakeMod.getLogger().debug(resourceLocation.toString()))
                    .filter(resourceLocation -> resourceLocation.getNamespace().equals(namespace))
                    .filter(resourceLocation -> resourceLocation.getPath().startsWith(path))
                    .filter(filter).toList();
        }
    }
}
