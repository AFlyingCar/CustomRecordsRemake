package com.aflyingcar.customrecordsremake.packs;

import com.aflyingcar.customrecordsremake.CustomRecordsRemakeMod;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CustomRecordsRepositorySource implements RepositorySource {
    public CustomRecordsRepositorySource() { }
    @Override
    public void loadPacks(@NotNull Consumer<Pack> consumer, Pack.@NotNull PackConstructor packConstructor) {
        Pack pack = Pack.create(
                CustomRecordsRemakeMod.MODID /* packName */,
                true /* ??? */,
                createPackSupplier() /* packSupplier */,
                packConstructor,
                Pack.Position.TOP /* packPosition */,
                PackSource.BUILT_IN /* packSource */);

        consumer.accept(pack);
    }

    private Supplier<PackResources> createPackSupplier() {
        return CustomRecordsPackResources::new;
    }
}
