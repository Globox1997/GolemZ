package net.golem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.entity.EntityType;

public class lemoinit implements ClientModInitializer {

        public static EntityType<?> entityType;

        @Override
        public void onInitializeClient() {

                EntityRendererRegistry.INSTANCE.register(lemo.GOLI,
                                (dispatcher, context) -> new GoliRenderer(dispatcher));

        }

}