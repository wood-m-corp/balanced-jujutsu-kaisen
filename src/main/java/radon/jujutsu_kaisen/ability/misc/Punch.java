package radon.jujutsu_kaisen.ability.misc;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.client.ClientWrapper;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.entity.base.ISorcerer;
import radon.jujutsu_kaisen.item.cursed_tool.PolearmStaffItem;
import radon.jujutsu_kaisen.item.cursed_tool.SteelGauntletItem;
import radon.jujutsu_kaisen.item.cursed_tool.SlaughterDemonItem;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.ArrayList;
import java.util.List;

public class Punch extends Ability implements Ability.ICharged{
    private static final float DAMAGE = 7.5F;
    private static final double RANGE = 7.5D;
    private static final double LAUNCH_POWER = 3.0D;
    private static final int STAGGER = 0;

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }


    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.CHANNELED;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (target == null || target.isDeadOrDying()) return false;
        if (owner.hasEffect(JJKEffects.STAGGER.get())) {
            return false;
        }
        if (owner.distanceTo(target) > RANGE) return false;
        return HelperMethods.RANDOM.nextInt(1) == 0;
    }

    @Override
    public boolean isTechnique() {
        return false;
    }

    @Override
    public boolean isMelee() {
        return true;
    }



    @Override
    public void run(LivingEntity owner) {
        if (!(owner instanceof Player) || !owner.level().isClientSide) return;
        ClientWrapper.setOverlayMessage(Component.translatable(String.format("chat.%s.charge", JujutsuKaisen.MOD_ID),
                Math.round(((float) Math.min(20, this.getCharge(owner)) / 20) * 100)), false);
    }

    @Override
    public Status isStillUsable(LivingEntity owner) {
        return super.isStillUsable(owner);
    }

    @Override
    public boolean onRelease(LivingEntity owner) {
        if (owner.hasEffect(JJKEffects.STAGGER.get())) {
            return false;
        }

        owner.swing(InteractionHand.MAIN_HAND);
        if (!(owner.level() instanceof ServerLevel level)) return false;
        float power = (float) Math.min(20, this.getCharge(owner)) / 20;
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);
        float num = 3;
        if (power >= 0.25) {
            num = 4;
        }

        double newRange = RANGE;
        int newStagger = STAGGER;

        int dash = cap.getDash();
        if (dash > 0) {
            num+=2;
            newRange+=1.5;
            Vec3 pos = owner.getEyePosition().add(look);
            owner.level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.MASTER, 1.0F, 1.5F);

        }

        if (owner.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof PolearmStaffItem) {
            newRange+=1.2;
        }

        if (owner.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SlaughterDemonItem) {
            newStagger += 10.0;
        }


        if (cap.getSpeedStacks() > 0) {
            newRange = RANGE + ((double) cap.getSpeedStacks() / 2); //someone tell brosif to use tabs
        }

        List<String> targets = new ArrayList<String>();
        Level level1 = owner.level();
        for (int i = 0; i < num; i++) {
            double finalNewRange = newRange;
            int finalStagger = newStagger;
            cap.delayTickEvent(() -> {

                Vec3 offset = owner.getEyePosition().add(look.scale(finalNewRange / 2-2));

                for (LivingEntity entity : owner.level().getEntitiesOfClass(LivingEntity.class, AABB.ofSize(offset, finalNewRange, finalNewRange+2, finalNewRange),
                        entity -> entity != owner )) {
                    //&& owner.hasLineOfSight(entity)
                    boolean found = false;
                    for (String target: targets) {
                        if (target == entity.getStringUUID()) {
                            found = true;
                        }
                    }
                    if (found) {
                        continue;
                    }
                    targets.add(entity.getStringUUID());
                    if (HelperMethods.friendsCheck(owner, entity)) continue;
                        
                    if (level1 instanceof ServerLevel) {
                        Vec3 center = entity.position().add(0.0D, entity.getBbHeight() / 2.0F, 0.0D);
                        entity.level().playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.MASTER, 0.9F, 1.2F);

                        entity.level().playSound(null, center.x, center.y, center.z, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.MASTER, 1.5F, 1.3F);
                        if (owner.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SteelGauntletItem) {
                            entity.level().playSound(null, center.x, center.y, center.z, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.MASTER, 1.5F, 0.8F);
                        }

                        ((ServerLevel) level1).sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 0, 1.0D, 0.0D, 0.0D, 1.0D);
                    }

                        int tim = 8;

                        if (power == 1) {
                            if (!cap.hasToggled(JJKAbilities.RATIO_RULE.get()) && (!JJKAbilities.hasTrait(owner, Trait.HEAVENLY_RESTRICTION_PHYSICAL))) {
                                cap.moreBlackFlash(true);
                            }

                            if (owner.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SteelGauntletItem) {
                                tim = 10;
                            }

                            cap.delayTickEvent(() -> {
                                cap.moreBlackFlash(false);
                            }, 3);
                        }

                        if (power >= 0.65 && power <= 0.75 && cap.hasToggled(JJKAbilities.RATIO_RULE.get())) {
                            int cooldown = cap.getRemainingCooldown(JJKAbilities.RATIO_RULE.get());
                            if (cooldown <= 0) {
                                cap.moreBlackFlash(true);
                                tim = 14;
                                if (owner.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SteelGauntletItem) {
                                    tim = 16;
                                }

                                cap.delayTickEvent(() -> {
                                    cap.moreBlackFlash(false);
                                }, 3);
                            }
                        }

                    float newDMG;
                    newDMG = DAMAGE;

                    if (!(owner instanceof Player player)) {
                        newDMG/=1.65F;
                    }
                    
                    if (owner instanceof Player player) {
                        player.attack(entity);
                    } else {
                        owner.doHurtTarget(entity);
                    }

                    entity.invulnerableTime = 0;

                    float newPower = (float) (LAUNCH_POWER*(0.8+0.4*power));
                    newDMG *= (float) (1+0.75 *power);
                    
                    if (JJKAbilities.hasTrait(owner, Trait.HEAVENLY_RESTRICTION_PHYSICAL)) {
                        if (entity.hurt(owner instanceof Player player ? owner.damageSources().playerAttack(player) : owner.damageSources().mobAttack(owner), (newDMG * 1.25F) * this.getPower(owner))) {
                            entity.setDeltaMovement(look.scale(newPower * (1.0F + this.getPower(owner) * 0.1F) * 1.5F)
                                    .multiply(1.0D, 0.25D, 1.0D));
                            entity.addEffect(new MobEffectInstance(JJKEffects.STUN.get(), tim, 0, false, false, false));
                            entity.addEffect(new MobEffectInstance(JJKEffects.STAGGER.get(), finalStagger, 0, false, false, false));
                        }
                    } else if (JJKAbilities.hasTrait(owner, Trait.HEAVENLY_RESTRICTION_CE))
                        {
                        if (entity.hurt(owner instanceof Player player ? owner.damageSources().playerAttack(player) : owner.damageSources().mobAttack(owner), (newDMG * 0.5F) * this.getPower(owner))) {
                            entity.setDeltaMovement(look.scale(newPower * (1.0F + this.getPower(owner) * 0.1F) * 0.5F)
                                    .multiply(0.8D, 0.25D, 0.8D));
                            //entity.addEffect(new MobEffectInstance(JJKEffects.STUN.get(), tim, 0, false, false, false));
                            //entity.addEffect(new MobEffectInstance(JJKEffects.STAGGER.get(), finalStagger, 0, false, false, false));
                        }
                        } else {
                        if (entity.hurt(JJKDamageSources.jujutsuAttack(owner, this), (newDMG) * this.getPower(owner))) {
                            if (owner instanceof Player player) {
                                entity.setDeltaMovement(look.scale(newPower * (1.0F + this.getPower(owner) * 0.1F))
                                        .multiply(1.0D, 0.25D, 1.0D));
                            }
                            else {
                                entity.setDeltaMovement(look.scale(newPower * (1.0F + this.getPower(owner) * 0.1F))
                                        .multiply(2.0D, 0.5D, 2.0D));
                            }
                            entity.addEffect(new MobEffectInstance(JJKEffects.STUN.get(), tim, 0, false, false, false));
                            entity.addEffect(new MobEffectInstance(JJKEffects.STAGGER.get(), finalStagger, 0, false, false, false));
                        }
                    }

                    }
                }, i*1);
            }


        //if ((owner.level() instanceof ServerLevel level)) {
            if (dash > 0) {
                Vec3 pos = owner.getEyePosition().add(look.scale(2.5D));
                level.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y+1, pos.z, 0, 0D, 0.0D, 0.0D, 1.0D);
            }
            for (int i = 0; i < 4; i++) {
                Vec3 pos = owner.getEyePosition().add(look.scale(2.5D));
                Item item = owner.getItemInHand(InteractionHand.MAIN_HAND).getItem();
                level.sendParticles(JJKAbilities.hasToggled(owner, JJKAbilities.INSTANT_SPIRIT_BODY_OF_DISTORTED_KILLING.get()) || item instanceof SwordItem && !(item instanceof SteelGauntletItem) ? ParticleTypes.SWEEP_ATTACK : ParticleTypes.CLOUD,
                        pos.x + (HelperMethods.RANDOM.nextDouble() - 0.5D) * 2.5D,
                        pos.y + (HelperMethods.RANDOM.nextDouble() - 0.5D) * 2.5D,
                        pos.z + (HelperMethods.RANDOM.nextDouble() - 0.5D) * 2.5D,
                        0, 0.0D, 0.0D, 0.0D, 1.0D);
            }
            for (int i = 0; i < 4; i++) {
                Vec3 pos = owner.getEyePosition().add(look.scale(2.5D));
                level.sendParticles(ParticleTypes.CRIT,
                        pos.x + (HelperMethods.RANDOM.nextDouble() - 0.5D) * 2.5D,
                        pos.y + (HelperMethods.RANDOM.nextDouble() - 0.5D) * 2.5D,
                        pos.z + (HelperMethods.RANDOM.nextDouble() - 0.5D) * 2.5D,
                        0, 0.0D, 0.0D, 0.0D, 1.0D);
            }

            Vec3 pos = owner.getEyePosition().add(look);
            owner.level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.MASTER, 1.0F, 1F+(HelperMethods.RANDOM.nextFloat() - 0.5f) * .2f);
            owner.level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.MASTER, 1.0F, 1.6F+(HelperMethods.RANDOM.nextFloat() - 0.5f) * .4f);

       // }



        return true;
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        return owner.isUsingItem() ? Status.FAILURE : super.isTriggerable(owner);
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        return (!(owner instanceof ISorcerer sorcerer) || sorcerer.hasMeleeAttack() && sorcerer.hasArms()) && super.isValid(owner);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return JJKAbilities.hasTrait(owner, Trait.HEAVENLY_RESTRICTION_PHYSICAL) ? 0.0F : 20.0F;
    }

    @Override
    public int getCooldown() {
        return 10;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.MELEE;
    }
}
