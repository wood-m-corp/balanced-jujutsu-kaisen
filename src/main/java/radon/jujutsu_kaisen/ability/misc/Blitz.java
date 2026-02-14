package radon.jujutsu_kaisen.ability.misc;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.client.particle.MirageParticle;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.entity.base.ISorcerer;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Blitz extends Ability {
    private static final double RANGE = 40.0D;
    public static int DURATION = 10;

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (target == null || target.isDeadOrDying()) return false;
        if (!owner.hasLineOfSight(target) || owner.distanceTo(target) > RANGE) return false;
        return HelperMethods.RANDOM.nextInt(6) == 0;
    }

    @Override
    public boolean isTechnique() {
        return false;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
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

        Vec3 look2 = RotationUtil.getTargetAdjustedLookAngle(owner).scale(5);
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        Vec3 target = getTarget(owner);
        Vec3 ogPos = owner.position();
        Vec3 oldV = owner.getDeltaMovement();
        owner.moveTo(target);
        owner.setDeltaMovement(oldV);

        if (!(owner.level() instanceof ServerLevel level)) return;
        owner.addEffect(new MobEffectInstance(JJKEffects.INVISIBILITY.get(), 5, 0, false, false, false));
        level.sendParticles(new MirageParticle.MirageParticleOptions(owner.getId()), ogPos.x, ogPos.y, ogPos.z,
                0, 0.0D, 0.0D, 0.0D, 1.0D);
        double dist = ogPos.distanceTo(target);
        owner.level().playSound(null, target.x, target.y, target.z, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.MASTER, 0.8F, 1F);
        owner.level().playSound(null, target.x, target.y, target.z, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.MASTER, 2F, 0.7F);
        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);
        level.sendParticles(ParticleTypes.FLASH, ogPos.x, ogPos.y+1, ogPos.z, 0, 0D, 0.0D, 0.0D, 1.0D);
        level.sendParticles(ParticleTypes.SONIC_BOOM, ogPos.x, ogPos.y+1, ogPos.z, 0, 0D, 0.0D, 0.0D, 1.0D);
        Vec3 pos = owner.getEyePosition().add(look);
        owner.level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_SMALL_FALL, SoundSource.MASTER, 1.0F, 1F);
        owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), JJKSounds.DASH.get(), SoundSource.MASTER, 1.0F, 2.0F);

        List<String> targets = new ArrayList<String>();
        for ( double i = 0; i < dist; i+=0.2)  {
            Vec3 cPos = ogPos.lerp(target,1/dist*i).add(0,1,0);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, cPos.x, cPos.y, cPos.z, 0, look2.x/4, look2.y/4, look2.z/4, 0D);
            for (LivingEntity entity : owner.level().getEntitiesOfClass(LivingEntity.class, AABB.ofSize(cPos, 8, 8, 8),
                    entity -> entity != owner)) {
                boolean found = false;
                for (String enemy: targets) {
                    if (Objects.equals(enemy, entity.getStringUUID())) {
                        found = true;
                    }
                }
                if (found) {
                    continue;
                }
                targets.add(entity.getStringUUID());
                Vec3 center = entity.position().add(0.0D, entity.getBbHeight() / 2.0F, 0.0D);
                level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 0, 1.0D, 0.0D, 0.0D, 1.0D);
                entity.level().playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.MASTER, 1.0F, 1.0F);

                if (owner instanceof Player player) {
                    player.attack(entity);
                } else {
                    owner.doHurtTarget(entity);
                }
                entity.invulnerableTime = 0;
                float newDMG;
                newDMG = 8;
                if (!(owner instanceof Player player)) {
                    newDMG/=1.65F;
                }
                if (entity.hurt(owner instanceof Player player ? owner.damageSources().playerAttack(player) : owner.damageSources().mobAttack(owner), (newDMG * 1.45F) * this.getPower(owner))) {
                    entity.setDeltaMovement(look.scale(1 * (1.0F + this.getPower(owner) * 0.1F) * 2.0F)
                            .multiply(1.0D, 0.25D, 1.0D));
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            cap.delayTickEvent(() -> {
                for (int a = 0; a < 5;a++) {
                    double xPos = owner.getX() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 1.25F) - owner.getLookAngle().scale(0.35D).x;
                    double yPos= owner.getY() + HelperMethods.RANDOM.nextDouble() * (owner.getBbHeight());
                    double zPos = owner.getZ() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 1.25F) - owner.getLookAngle().scale(0.35D).z;
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, xPos, yPos, zPos, 0, look2.x/4, look2.y/4, look2.z/4, 0D);
                }

            }, i);
        }

        /*Vec3 offset = owner.getEyePosition().add(look.scale(RANGE / 2));

        for (LivingEntity entity : owner.level().getEntitiesOfClass(LivingEntity.class, AABB.ofSize(offset, RANGE, RANGE, RANGE),
                entity -> entity != owner && owner.hasLineOfSight(entity))) {
            if (owner instanceof Player player) {
                player.attack(entity);
            } else {
                owner.doHurtTarget(entity);
            }
            entity.invulnerableTime = 0;
        }*/


    }

    @Override
    public boolean isValid(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return (!(owner instanceof ISorcerer sorcerer) || sorcerer.hasMeleeAttack() && sorcerer.hasArms()) && cap.hasTrait(Trait.HEAVENLY_RESTRICTION_PHYSICAL) && super.isValid(owner);
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        return owner.isUsingItem() ? Status.FAILURE : super.isTriggerable(owner);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return JJKAbilities.hasTrait(owner, Trait.HEAVENLY_RESTRICTION_PHYSICAL) ? 0.0F : 15.0F;
    }

    public int getCooldown() {
        return 28 * 20;
    }

    @Override
    public boolean isMelee() {
        return true;
    }

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }



    @Override
    public MenuType getMenuType() {
        return MenuType.J2TSU;
    }
}
