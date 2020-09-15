package net.golem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class lemo implements ModInitializer {

        public static final EntityType<Goli> GOLI = FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, Goli::new)
                        .trackRangeBlocks(74).dimensions(EntityDimensions.fixed(0.45F, 1.08F)).build();

        public static final Identifier AMBI = new Identifier("golem:ambi");
        public static SoundEvent AMBIEVENT = new SoundEvent(AMBI);
        public static final Identifier HIT = new Identifier("golem:hit");
        public static SoundEvent HITEVENT = new SoundEvent(HIT);
        public static final Identifier WALK = new Identifier("golem:walk");
        public static SoundEvent WALKEVENT = new SoundEvent(WALK);
        public static final Identifier DEATH = new Identifier("golem:death");
        public static SoundEvent DEATHEVENT = new SoundEvent(DEATH);
        public static final Identifier REP = new Identifier("golem:rep");
        public static SoundEvent REPEVENT = new SoundEvent(REP);

        @Override
        public void onInitialize() {
                Registry.register(Registry.ENTITY_TYPE, new Identifier("golem", "goli"), GOLI);
                Registry.register(Registry.SOUND_EVENT, lemo.AMBI, AMBIEVENT);
                Registry.register(Registry.SOUND_EVENT, lemo.HIT, HITEVENT);
                Registry.register(Registry.SOUND_EVENT, lemo.WALK, WALKEVENT);
                Registry.register(Registry.SOUND_EVENT, lemo.DEATH, DEATHEVENT);
                Registry.register(Registry.SOUND_EVENT, lemo.REP, REPEVENT);
                Registry.register(Registry.ITEM, new Identifier("golem", "spawn_goli"),
                                new SpawnEggItem(GOLI, 10198167, 6329475, new Item.Settings().group(ItemGroup.MISC)));
                FabricDefaultAttributeRegistry.register(GOLI, Goli.createGoliAttributes());

        }
}

// You are LOVED!!!
// Jesus loves you unconditional!