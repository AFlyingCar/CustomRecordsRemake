package com.aflyingcar.customrecordsremake.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import org.jetbrains.annotations.NotNull;

public class ItemCustomRecord extends RecordItem {
    /**
     * Factory class for creating a new ItemCustomRecord
     */
    public static class RecordItemBuilder {
        private final int comparatorValue;
        private final SoundEvent song;
        private final int length;

        private Item.Properties properties;

        RecordItemBuilder(@NotNull SoundEvent song, int songLength) {
            this.comparatorValue = 1; // TODO: Do we want to allow this value to be modifiable from records.json?
            this.song = song;
            this.length = songLength;
        }

        @NotNull
        public static RecordItemBuilder create(@NotNull SoundEvent song, int songLength) {
            return new RecordItemBuilder(song, songLength);
        }

        public ItemCustomRecord buildItem() {
            return new ItemCustomRecord(song, length, comparatorValue, createProperties());
        }

        @NotNull
        protected Item.Properties createProperties() {
            if(properties == null) {
                properties = new Item.Properties();

                properties.tab(CreativeModeTab.TAB_MISC);
                properties.stacksTo(1);
            }

            return properties;
        }
    }

    protected ItemCustomRecord(SoundEvent sound, int length, int comparatorValue, Item.Properties properties) {
        // TODO: Do we need to multiply 'length' by 20?
        super(comparatorValue, () -> sound, properties, length);
    }
}
