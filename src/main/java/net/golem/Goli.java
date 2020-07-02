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
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//import net.minecraft.entity.passive.WolfEntity;

public class Goli extends TameableEntity {
    public static final Predicate<LivingEntity> FOLLOW_TAMED_PREDICATE;
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
        this.goalSelector.add(5, new FollowOwnerGoal(this, 1.0D, 11.0F, 3.0F, true));
        this.goalSelector.add(6, new AnimalMateGoal(this, 1.0D));
        this.goalSelector.add(9, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(10, new LookAroundGoal(this));
        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
        this.targetSelector.add(3, (new RevengeGoal(this, new Class[0])).setGroupRevenge());
        this.targetSelector.add(4, new FollowTargetGoal<>(this, AbstractSkeletonEntity.class, false));
    }

    public static DefaultAttributeContainer.Builder createGoliAttributes() {
        return AnimalEntity.createLivingAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.26D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 2D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0D).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 38.0D);
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
    }

    @Override
    protected void playStepSound(BlockPos blockPos_1, BlockState blockState_1) {
        this.playSound(lemo.WALKEVENT, 0.15F, 1.0F);
    }

    @Override
    public void writeCustomDataToTag(CompoundTag tag) {
        super.writeCustomDataToTag(tag);
        tag.putBoolean("Angry", this.isAngry());
    }

    @Override
    public void readCustomDataFromTag(CompoundTag tag) {
        super.readCustomDataFromTag(tag);
        this.setAngry(tag.getBoolean("Angry"));

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
        if (!this.world.isClient && this.getTarget() == null && this.isAngry()) {
            this.setAngry(false);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isAlive()) {
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
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
            this.setSitting(false);
            if (entity != null && !(entity instanceof PlayerEntity)
                    && !(entity instanceof PersistentProjectileEntity)) {
                amount = (amount + 1.0F) / 2.0F;
            }

            return super.damage(source, amount);
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean bl = target.damage(DamageSource.mob(this),
                (float) ((int) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)));
        if (bl) {
            this.dealDamage(this, target);
        }
        return bl;
    }

    @Override
    public void setTamed(boolean tamed) {
        super.setTamed(tamed);
        if (tamed) {
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(50.0D);
            this.setHealth(20.0F);
        } else {
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(50.0D);
        }
        this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(6.0D);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();
        if (this.world.isClient) {
            boolean bl = this.isOwner(player) || this.isTamed() || item == Items.IRON_INGOT && !this.isTamed();
            return bl ? ActionResult.CONSUME : ActionResult.PASS;
        } else {
            if (this.isTamed()) {
                if (this.isBreedingItem(itemStack) && this.getHealth() < this.getMaxHealth()) {
                    if (!player.abilities.creativeMode) {
                        itemStack.decrement(1);
                    }
                    this.playSound(lemo.REPEVENT, 1.0F, 1.0F);
                    this.heal(10F);
                    return ActionResult.SUCCESS;
                }

                if (!(item instanceof DyeItem)) {
                    ActionResult actionResult = super.interactMob(player, hand);
                    if ((!actionResult.isAccepted() || this.isBaby()) && this.isOwner(player)) {
                        this.setSitting(!this.isSitting());
                        this.jumping = false;
                        this.navigation.stop();
                        this.setTarget((LivingEntity) null);
                        return ActionResult.SUCCESS;
                    }

                    return actionResult;
                }

            } else if (item == Items.IRON_INGOT) {
                if (!player.abilities.creativeMode) {
                    itemStack.decrement(1);
                }
                this.setOwner(player);
                this.navigation.stop();
                this.setTarget((LivingEntity) null);
                this.setSitting(true);
                this.world.sendEntityStatus(this, (byte) 7);
                return ActionResult.SUCCESS;
            }

            return super.interactMob(player, hand);
        }
    }

    @Environment(EnvType.CLIENT)
    public void handleStatus(byte status) {
        super.handleStatus(status);

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

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        return false;
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
        FOLLOW_TAMED_PREDICATE = (livingEntity) -> {
            EntityType<?> entityType = livingEntity.getType();
            return entityType == EntityType.SHEEP || entityType == EntityType.RABBIT || entityType == EntityType.FOX;
        };
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
