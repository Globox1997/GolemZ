package net.golem;

import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.AttackWithOwnerGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TrackOwnerAttackerGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraft.entity.passive.WolfEntity;

public class Goli extends TameableEntity {
    private static final TrackedData<Boolean> BEGGING;
    private static final TrackedData<Integer> COLLAR_COLOR;
    public static final Predicate<LivingEntity> FOLLOW_TAMED_PREDICATE;
    private float begAnimationProgress;
    private float lastBegAnimationProgress;
    private boolean furWet;
    private boolean canShakeWaterOff;
    private float shakeProgress;
    private float lastShakeProgress;
    private int attackTicksLeft;

    public Goli(EntityType<? extends Goli> entityType, World world) {
        super(entityType, world);
        this.setTamed(false);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new SitGoal(this));
        this.goalSelector.add(3, new PounceAtTargetGoal(this, 0.2F));
        this.goalSelector.add(4, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.add(5, new FollowOwnerGoal(this, 1.0D, 10.0F, 3.0F, true));
        this.goalSelector.add(6, new AnimalMateGoal(this, 1.0D));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
        this.targetSelector.add(3, (new RevengeGoal(this, new Class[0])).setGroupRevenge());
        this.targetSelector.add(4, new FollowTargetGoal<>(this, AbstractSkeletonEntity.class, false));
    }

    @Override
    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.26D);
        if (this.isTamed()) {
            this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(50.0D);
        } else {
            this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(50.0D);
        }

        this.getAttributes().register(EntityAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        this.getAttributeInstance(EntityAttributes.KNOCKBACK_RESISTANCE).setBaseValue(2.0D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isTamed()) {
            if (this.isInLove()) {
                return lemo.AMBIEVENT;
            } else {
                return this.random.nextInt(4) == 0 ? lemo.AMBIEVENT : lemo.AMBIEVENT;
            }
        } else {
            return lemo.AMBIEVENT;
        }
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (target == null) {
            this.setAngry(false);
        } else if (!this.isTamed()) {
            this.setAngry(true);
        }

    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(BEGGING, false);
        this.dataTracker.startTracking(COLLAR_COLOR, DyeColor.RED.getId());
    }

    @Override
    protected void playStepSound(BlockPos blockPos_1, BlockState blockState_1) {
        this.playSound(lemo.WALKEVENT, 0.15F, 1.0F);
    }

    @Override
    public void writeCustomDataToTag(CompoundTag tag) {
        super.writeCustomDataToTag(tag);
        tag.putBoolean("Angry", this.isAngry());
        tag.putByte("CollarColor", (byte) this.getCollarColor().getId());
    }

    @Override
    public void readCustomDataFromTag(CompoundTag tag) {
        super.readCustomDataFromTag(tag);
        this.setAngry(tag.getBoolean("Angry"));
        if (tag.contains("CollarColor", 99)) {
            this.setCollarColor(DyeColor.byId(tag.getInt("CollarColor")));
        }

    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource_1) {
        return lemo.HITEVENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return lemo.DEATHEVENT;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.world.isClient && this.furWet && !this.canShakeWaterOff && !this.isNavigating() && this.onGround) {
            this.canShakeWaterOff = true;
            this.shakeProgress = 0.0F;
            this.lastShakeProgress = 0.0F;
            this.world.sendEntityStatus(this, (byte) 8);
        }

        if (!this.world.isClient && this.getTarget() == null && this.isAngry()) {
            this.setAngry(false);
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (this.isAlive()) {
            this.lastBegAnimationProgress = this.begAnimationProgress;
            if (this.isBegging()) {
                this.begAnimationProgress += (1.0F - this.begAnimationProgress) * 0.4F;
            } else {
                this.begAnimationProgress += (0.0F - this.begAnimationProgress) * 0.4F;
            }

            if (this.isWet()) {
                this.furWet = true;
                this.canShakeWaterOff = false;
                this.shakeProgress = 0.0F;
                this.lastShakeProgress = 0.0F;
            } else if ((this.furWet || this.canShakeWaterOff) && this.canShakeWaterOff) {
                if (this.shakeProgress == 0.0F) {
                    this.playSound(lemo.AMBIEVENT, this.getSoundVolume(),
                            (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                }

                this.lastShakeProgress = this.shakeProgress;
                this.shakeProgress += 0.05F;
                if (this.lastShakeProgress >= 2.0F) {
                    this.furWet = false;
                    this.canShakeWaterOff = false;
                    this.lastShakeProgress = 0.0F;
                    this.shakeProgress = 0.0F;
                }

                if (this.shakeProgress > 0.4F) {
                    float f = (float) this.getY();
                    int i = (int) (MathHelper.sin((this.shakeProgress - 0.4F) * 3.1415927F) * 7.0F);
                    Vec3d vec3d = this.getVelocity();

                    for (int j = 0; j < i; ++j) {
                        float g = (this.random.nextFloat() * 2.0F - 1.0F) * this.getWidth() * 0.5F;
                        float h = (this.random.nextFloat() * 2.0F - 1.0F) * this.getWidth() * 0.5F;
                        this.world.addParticle(ParticleTypes.SPLASH, this.getX() + (double) g, (double) (f + 0.8F),
                                this.getZ() + (double) h, vec3d.x, vec3d.y, vec3d.z);
                    }
                }
            }

        }
    }

    @Override
    public void onDeath(DamageSource source) {
        this.furWet = false;
        this.canShakeWaterOff = false;
        this.lastShakeProgress = 0.0F;
        this.shakeProgress = 0.0F;
        super.onDeath(source);
    }

    @Environment(EnvType.CLIENT)
    public boolean isFurWet() {
        return false;
    }

    /**
     * Returns this wolf's brightness multiplier based on the fur wetness.
     * <p>
     * The brightness multiplier represents how much darker the wolf gets while its
     * fur is wet. The multiplier changes (from 0.75 to 1.0 incrementally) when a
     * wolf shakes.
     * 
     * @param tickDelta Progress for linearly interpolating between the previous and
     *                  current game state.
     * @return Brightness as a float value between 0.75 and 1.0.
     * @see net.minecraft.client.render.entity.model.TintableAnimalModel#setColorMultiplier(float,
     *      float, float)
     */
    @Environment(EnvType.CLIENT)
    public float getFurWetBrightnessMultiplier(float tickDelta) {
        return 0.75F + MathHelper.lerp(tickDelta, this.lastShakeProgress, this.shakeProgress) / 2.0F * 0.25F;
    }

    @Environment(EnvType.CLIENT)
    public float getShakeAnimationProgress(float tickDelta, float f) {
        float g = (MathHelper.lerp(tickDelta, this.lastShakeProgress, this.shakeProgress) + f) / 1.8F;
        if (g < 0.0F) {
            g = 0.0F;
        } else if (g > 1.0F) {
            g = 1.0F;
        }

        return MathHelper.sin(g * 3.1415927F) * MathHelper.sin(g * 3.1415927F * 11.0F) * 0.15F * 3.1415927F;
    }

    @Environment(EnvType.CLIENT)
    public float getBegAnimationProgress(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastBegAnimationProgress, this.begAnimationProgress) * 0.15F
                * 3.1415927F;
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.8F;
    }

    @Override
    public int getLookPitchSpeed() {
        return this.isSitting() ? 20 : super.getLookPitchSpeed();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            Entity entity = source.getAttacker();
            this.method_24346(false);
            if (entity != null && !(entity instanceof PlayerEntity) && !(entity instanceof ProjectileEntity)) {
                amount = (amount + 1.0F) / 2.0F;
            }

            return super.damage(source, amount);
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean bl = target.damage(DamageSource.mob(this),
                (float) ((int) this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).getValue()));
        if (bl) {
            this.dealDamage(this, target);
        }

        return bl;
    }

    @Override
    public void setTamed(boolean tamed) {
        super.setTamed(tamed);
        if (tamed) {
            this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(50.0D);
            this.setHealth(50.0F);
        } else {
            this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(50.0D);
        }

        this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
    }

    @Override
    public boolean interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();
        if (itemStack.getItem() instanceof SpawnEggItem) {
            return super.interactMob(player, hand);
        } else if (this.world.isClient) {
            return this.isOwner(player) || item == Items.IRON_INGOT && !this.isTamed() && !this.isAngry();
        } else {
            if (this.isTamed()) {
                if (item == Items.IRON_INGOT && this.getHealth() < this.getMaximumHealth()) {
                    if (!player.abilities.creativeMode) {
                        itemStack.decrement(1);
                    }
                    this.playSound(lemo.REPEVENT, 1.0F, 1.0F);
                    this.heal(10F);
                    return true;
                }

                if (!(item instanceof DyeItem)) {
                    boolean bl = super.interactMob(player, hand);
                    if ((!bl || this.isBaby()) && this.isOwner(player) && !this.isBreedingItem(itemStack)) {
                        this.method_24346(!this.method_24345());
                        this.jumping = false;
                        this.navigation.stop();
                        this.setTarget((LivingEntity) null);
                    }

                    return bl;
                }

                DyeColor dyeColor = ((DyeItem) item).getColor();
                if (dyeColor != this.getCollarColor()) {
                    this.setCollarColor(dyeColor);
                    if (!player.abilities.creativeMode) {
                        itemStack.decrement(1);
                    }

                    return true;
                }
            } else if (item == Items.IRON_INGOT && !this.isAngry()) {
                if (!player.abilities.creativeMode) {
                    itemStack.decrement(1);
                }

                if (!this.world.isClient) {
                    this.setOwner(player);
                    this.navigation.stop();
                    this.setTarget((LivingEntity) null);
                    this.method_24346(true);
                    this.world.sendEntityStatus(this, (byte) 7);
                } else {
                    this.world.sendEntityStatus(this, (byte) 6);
                }

                return true;
            }

            return super.interactMob(player, hand);
        }
    }

    @Environment(EnvType.CLIENT)
    public void handleStatus(byte status) {
        if (status == 8) {
            this.canShakeWaterOff = true;
            this.shakeProgress = 0.0F;
            this.lastShakeProgress = 0.0F;
        } else {
            super.handleStatus(status);
        }

    }

    @Environment(EnvType.CLIENT)
    public float getTailAngle() {
        if (this.isAngry()) {
            return 1.5393804F;
        } else {
            return this.isTamed() ? (0.55F - (this.getMaximumHealth() - this.getHealth()) * 0.02F) * 3.1415927F
                    : 0.62831855F;
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Override
    public int getLimitPerChunk() {
        return 1;
    }

    public boolean isAngry() {
        return ((Byte) this.dataTracker.get(TAMEABLE_FLAGS) & 2) != 0;
    }

    public void setAngry(boolean angry) {
        byte b = (Byte) this.dataTracker.get(TAMEABLE_FLAGS);
        if (angry) {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte) (b | 2));
        } else {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte) (b & -3));
        }

    }

    public DyeColor getCollarColor() {
        return DyeColor.byId((Integer) this.dataTracker.get(COLLAR_COLOR));
    }

    public void setCollarColor(DyeColor color) {
        this.dataTracker.set(COLLAR_COLOR, color.getId());
    }

    public void setBegging(boolean begging) {
        this.dataTracker.set(BEGGING, begging);
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        return false;
    }

    public boolean isBegging() {
        return (Boolean) this.dataTracker.get(BEGGING);
    }

    @Override
    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        if (!(target instanceof CreeperEntity) && !(target instanceof GhastEntity)) {
            if (target instanceof Goli) {
                Goli Goli = (Goli) target;
                return !Goli.isTamed() || Goli.getOwner() != owner;
            } else if (target instanceof PlayerEntity && owner instanceof PlayerEntity
                    && !((PlayerEntity) owner).shouldDamagePlayer((PlayerEntity) target)) {
                return false;
            } else if (target instanceof HorseBaseEntity && ((HorseBaseEntity) target).isTame()) {
                return false;
            } else {
                return !(target instanceof TameableEntity) || !((TameableEntity) target).isTamed();
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean canBeLeashedBy(PlayerEntity player) {
        return !this.isAngry() && super.canBeLeashedBy(player);
    }

    static {
        BEGGING = DataTracker.registerData(Goli.class, TrackedDataHandlerRegistry.BOOLEAN);
        COLLAR_COLOR = DataTracker.registerData(Goli.class, TrackedDataHandlerRegistry.INTEGER);
        FOLLOW_TAMED_PREDICATE = (livingEntity) -> {
            EntityType<?> entityType = livingEntity.getType();
            return entityType == EntityType.SHEEP || entityType == EntityType.RABBIT || entityType == EntityType.FOX;
        };
    }

    class AvoidLlamaGoal<T extends LivingEntity> extends FleeEntityGoal<T> {
        private final Goli wolf;

        public AvoidLlamaGoal(Goli wolf, Class<T> fleeFromType, float distance, double slowSpeed, double fastSpeed) {
            super(wolf, fleeFromType, distance, slowSpeed, fastSpeed);
            this.wolf = wolf;
        }

        public boolean canStart() {
            if (super.canStart() && this.targetEntity instanceof LlamaEntity) {
                return !this.wolf.isTamed() && this.isScaredOf((LlamaEntity) this.targetEntity);
            } else {
                return false;
            }
        }

        private boolean isScaredOf(LlamaEntity llama) {
            return llama.getStrength() >= Goli.this.random.nextInt(5);
        }

        public void start() {
            Goli.this.setTarget((LivingEntity) null);
            super.start();
        }

        public void tick() {
            Goli.this.setTarget((LivingEntity) null);
            super.tick();
        }
    }

    @Override
    public PassiveEntity createChild(PassiveEntity mate) {

        return mate;
    }

    @Environment(EnvType.CLIENT)
    public int getAttackTicksLeft() {
        return this.attackTicksLeft;
    }

}
