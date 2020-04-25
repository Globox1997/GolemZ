package net.golem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;

import net.minecraft.client.render.entity.IronGolemEntityRenderer;
import net.minecraft.client.render.entity.model.IronGolemEntityModel;
import net.minecraft.entity.passive.IronGolemEntity;

@Environment(EnvType.CLIENT)
public class GoliRenderer extends MobEntityRenderer<Goli, GoliModel<Goli>> {
    private static final Identifier SKIN = new Identifier("golem:textures/entity/goli.png");

    public GoliRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new GoliModel<>(), 0.4F);

    }

    @Override
    protected void scale(Goli goli, MatrixStack matrixStack, float f) {
        matrixStack.scale(0.4F, 0.4F, 0.4F);
    }

    @Override
    public Identifier getTexture(Goli ice) {
        return SKIN;
    }

    @Override
    protected void setupTransforms(Goli ironGolemEntity, MatrixStack matrixStack, float f, float g, float h) {
        super.setupTransforms(ironGolemEntity, matrixStack, f, g, h);
        if ((double) ironGolemEntity.limbDistance >= 0.01D) {
            f = 13.0F;
            g = ironGolemEntity.limbAngle - ironGolemEntity.limbDistance * (1.0F - h) + 6.0F;
            h = (Math.abs(g % 13.0F - 6.5F) - 3.25F) / 3.25F;
            matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(6.5F * h));
        }
    }
}
