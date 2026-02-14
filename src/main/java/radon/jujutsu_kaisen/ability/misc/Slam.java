package radon.jujutsu_kaisen.ability.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ExplosionHandler;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.client.ClientWrapper;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.entity.base.ISorcerer;
import radon.jujutsu_kaisen.entity.ten_shadows.RabbitEscapeEntity;
import radon.jujutsu_kaisen.item.cursed_tool.SteelGauntletItem;
import radon.jujutsu_kaisen.item.cursed_tool.SlaughterDemonItem;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.*;

public class Slam extends Ability implements Ability.ICharged {
    private static final double RANGE = 50.0D;
    private static final double LAUNCH_POWER = 2.0D;
    private static final float MAX_EXPLOSION = 5.0F;

    public static Map<UUID, Float> TARGETS = new HashMap<>();

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (target == null || target.isDeadOrDying()) return false;
        return owner.hasLineOfSight(target) && HelperMethods.RANDOM.nextInt(8) == 0;
    }

    @Override
    public boolean isTechnique() {
        return false;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.CHANNELED;
    }

    private Vec3 getTarget(LivingEntity owner) {
        Vec3 start = owner.getEyePosition();
        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);
        Vec3 end = start.add(look.scale(RANGE));
        HitResult result = RotationUtil.getHitResult(owner, start, end);
        return result.getType() == HitResult.Type.MISS ? end : result.getLocation();
    }

    @Override
    public void run(LivingEntity owner) {
        if (!(owner instanceof Player) || !owner.level().isClientSide) return;
        ClientWrapper.setOverlayMessage(Component.translatable(String.format("chat.%s.charge", JujutsuKaisen.MOD_ID),
                Math.round(((float) Math.min(20, this.getCharge(owner)) / 20) * 100)), false);
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        return (!(owner instanceof ISorcerer sorcerer) || sorcerer.hasMeleeAttack() && sorcerer.canJump()) && super.isValid(owner);
    }

    @Override
    public Status isStillUsable(LivingEntity owner) {
        if (owner.hasEffect(JJKEffects.STUN.get())) {
            return Status.FAILURE;
        }
        return super.isStillUsable(owner);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return JJKAbilities.hasTrait(owner, Trait.HEAVENLY_RESTRICTION_PHYSICAL) ? 0.0F : 30.0F;
    }

    public int getCooldown() {
        return 10 * 20;
    }

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean isMelee() {
        return true;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.MELEE;
    }

    @Override
    public boolean usesHands() {
        return false;
    }

    public static void onHitGround(LivingEntity owner, float distance) {
        slamCrater(owner,distance);
    }

    public static void slamCrater(LivingEntity owner, float distance) {
        if (owner.level().isClientSide) return;
        
        float radius = MAX_EXPLOSION;
        float dmgMult = 0.75F;
        if (JJKAbilities.hasTrait(owner, Trait.HEAVENLY_RESTRICTION_PHYSICAL)) {
            dmgMult = 0.8F;
            radius = radius*1.35f+1.5f;
        }
        if (JJKAbilities.hasTrait(owner, Trait.HEAVENLY_RESTRICTION_CE)) {
            dmgMult = 0.65F;
            radius = radius*0.65f-1.5f;
        }
        if (owner instanceof RabbitEscapeEntity) {
            radius = 1f;
            dmgMult = 0.3f;
        }
        boolean steeled = false;
        if (owner.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SteelGauntletItem) {
            steeled = true;
            radius = radius*1.2f+1.0f;

        }

   
         if (owner.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
                 ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        if (cap.hasToggled(JJKAbilities.RATIO_RULE.get())) {
            int cooldown = cap.getRemainingCooldown(JJKAbilities.RATIO_RULE.get());
            if (cooldown <= 0) {
                dmgMult = 0.7F;
                radius = radius * 1.5f;
            }
        }
        }

        owner.swing(InteractionHand.MAIN_HAND);

        if (!owner.level().isClientSide) {
            for (LivingEntity entity : owner.level().getEntitiesOfClass(LivingEntity.class, AABB.ofSize(owner.position(), radius*2, radius*2, radius*2),
                    entity -> entity != owner )) {
                int stunDuration = 40;
                int staggerDuration = 0;

                if (owner.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SteelGauntletItem) {
                    stunDuration = 50;
                }

                if (owner.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SlaughterDemonItem) {
                    staggerDuration = 20;
                }
                if (owner.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
                    ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                    if (cap.hasTrait(Trait.HEAVENLY_RESTRICTION_CE)) {
                        entity.addEffect(new MobEffectInstance(JJKEffects.STUN.get(), stunDuration, 0, false, false, false));
                        entity.addEffect(new MobEffectInstance(JJKEffects.STAGGER.get(), staggerDuration, 0, false, false, false));
                    }
                }
            }
             ExplosionHandler.spawn(owner.level().dimension(), owner.position(), radius, 5, Ability.getPower(JJKAbilities.SLAM.get(), owner) * dmgMult, owner,
                    owner instanceof Player player ? owner.damageSources().playerAttack(player) : owner.damageSources().mobAttack(owner), false, true );
        }
        if (steeled) {
            owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.MASTER, 3F, 0.8F);
        }
        owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), JJKSounds.SLAM.get(), SoundSource.MASTER, 1.0F, 1.0F);
        owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.SNIFFER_EGG_CRACK, SoundSource.MASTER, 2.0F, 1.0F);

        TARGETS.remove(owner.getUUID());
    }

    @Override
    public boolean onRelease(LivingEntity owner) {
        if (owner.hasEffect(JJKEffects.STAGGER.get())) {
            return false;
        }
       

        double launchPower = 2.0D + (2.0D * (Math.min(20, this.getCharge(owner)) / 20));
        if (owner.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            if (cap.hasTrait(Trait.HEAVENLY_RESTRICTION_CE)){
                launchPower /= 2D;
            }
        }
        float checkcharge = (float) Math.min(20, this.getCharge(owner)) / 20;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (checkcharge >= 0.65f && checkcharge <= 0.75f && cap.hasToggled(JJKAbilities.RATIO_RULE.get())) {
            int cooldown = cap.getRemainingCooldown(JJKAbilities.RATIO_RULE.get());
            if (cooldown <= 0) {
                launchPower = 16.0D;
                cap.moreBlackFlash(true);
                //System.out.println(this.getCharge(owner));

                cap.delayTickEvent(() -> {
                    cap.moreBlackFlash(false);
                }, 40);
            }
        }

        double reallaunch = launchPower;

        if (!owner.onGround()) {
            if (!owner.level().isClientSide) {
                TARGETS.put(owner.getUUID(), ((float) Math.min(20, this.getCharge(owner)) / 20));
            }

            Vec3 target = this.getTarget(owner);
            Vec3 velocity = (target.subtract(owner.position()).normalize().scale(reallaunch));
            if (velocity.y > 0) {
                velocity = velocity.multiply(1.0D, 0D, 1.0D);
            }
            else {
                velocity = velocity.multiply(1.2D, 1.7D, 1.2D);
            }
            owner.setDeltaMovement(velocity);
            owner.swing(InteractionHand.MAIN_HAND);
            cap.delayTickEvent(() -> {
                TARGETS.remove(owner.getUUID());
            }, 20*3);
        }
        else {
            if (owner.isShiftKeyDown()) {
                if (!owner.level().isClientSide) {
                    TARGETS.put(owner.getUUID(), ((float) Math.min(20, this.getCharge(owner)) / 20));
                }
                owner.swing(InteractionHand.MAIN_HAND);
                slamCrater(owner, 1);
            }
            else {
                Vec3 direction = new Vec3(0.0D, Math.min(3.0D,reallaunch*0.65D), 0.0D);
                owner.setDeltaMovement(owner.getDeltaMovement().add(direction));

                float power = ((float) Math.min(20, this.getCharge(owner)) / 20);

                cap.delayTickEvent(() -> {
                    if (!owner.level().isClientSide) {
                        TARGETS.put(owner.getUUID(), power);
                    }


                    Vec3 target = this.getTarget(owner);
                    Vec3 velocity = (target.subtract(owner.position()).normalize().scale(reallaunch));
                    if (velocity.y > 0) {
                        velocity = velocity.multiply(1.0D, 0, 1.0D);
                    }
                    else {
                        velocity = velocity.multiply(1.0D, 2D, 1.0D);
                    }
                    owner.setDeltaMovement(velocity);
                    cap.delayTickEvent(() -> {
                        TARGETS.remove(owner.getUUID());
                    }, 20*3);
                }, 15);
            }
        }
        return true;
    }
}
