package com.aflyingcar.customrecordsremake;

import com.aflyingcar.customrecordsremake.packs.CustomRecordsRepositorySource;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;

import com.aflyingcar.customrecordsremake.utils.RecordJsonHandler;
import com.aflyingcar.customrecordsremake.item.ModItems;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

@Mod(CustomRecordsRemakeMod .MODID)
public class CustomRecordsRemakeMod {
    public static final String MODID = "customrecordsremake";

    private static final Logger LOGGER = LogUtils.getLogger();

    public static Logger getLogger() {
        return LOGGER;
    }

    public static void initSounds(BiConsumer<SoundEvent, ResourceLocation> r) {
        for(SoundEvent soundEvent : RecordJsonHandler.getSounds()) {
            CustomRecordsRemakeMod.getLogger().debug("Initializing sound " + soundEvent.getLocation());
            r.accept(soundEvent, soundEvent.getLocation());
        }
    }

    public CustomRecordsRemakeMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Load all records here
        loadCustomRecordsJson();

        // Register every SoundEvent
        FMLJavaModLoadingContext.get().getModEventBus().addListener((RegisterEvent event) -> {
            var soundEventRegistry = Registry.SOUND_EVENT_REGISTRY;

            if (soundEventRegistry.equals(event.getRegistryKey())) {
                CustomRecordsRemakeMod.getLogger().debug("Initializing all sounds!");
                initSounds((sound, location) -> event.register(soundEventRegistry, location, () -> sound));
            }
        });

        getLogger().info("Registering an item supplier for every record loaded from JSON.");
        RecordJsonHandler.getRecords().forEach((name, record) -> ModItems.ITEMS.register(name, record::getItem));

        // Register every music disc
        ModItems.register(modEventBus);

        // Register the event listener to make sure that we get a chance to inject the virtual resource pack containing
        //   the records + recipes
        modEventBus.addListener(this::injectPackRepository);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void injectPackRepository(AddPackFindersEvent event) {
        event.addRepositorySource(new CustomRecordsRepositorySource());
    }

    private static File recordsJson;
    private static String soundsJson;

    public static Path getConfigDir() {
        return FMLLoader.getGamePath().resolve("config");
    }

    public static Path getRecordsResourcesDir() {
        return Paths.get(getConfigDir() + File.separator + CustomRecordsRemakeMod.MODID);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected static File setupFiles() {
        if(recordsJson == null) {
            recordsJson = new File(getRecordsResourcesDir() + File.separator + "records.json");
            if(!recordsJson.exists()) {
                try {
                    recordsJson.getParentFile().mkdirs();
                    recordsJson.createNewFile();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            getLogger().warn("recordsJson is not null! setupFiles was likely called twice, this should not happen.");
        }
        return recordsJson;
    }

    private static void loadCustomRecordsJson() {
        File recordsJson = setupFiles();
        RecordJsonHandler.handleConfig(recordsJson);
        soundsJson = RecordJsonHandler.setupSoundsJson();
    }

    public static String getSoundsJson() {
        return soundsJson;
    }
}
