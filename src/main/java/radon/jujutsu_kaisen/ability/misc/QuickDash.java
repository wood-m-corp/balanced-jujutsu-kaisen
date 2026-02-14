 package radon.jujutsu_kaisen.ability.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.client.particle.MirageParticle;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

public class QuickDash extends Dash {
    public static final double RANGE = 80.0D;
    private static final float DASH = 2.0F;
    private static final float MAX_DASH = 3.0F;

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (target == null) return false;
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (cap.hasTrait(Trait.HEAVENLY_RESTRICTION_PHYSICAL)) {
            return HelperMethods.RANDOM.nextInt(1) == 0;
        }

        return HelperMethods.RANDOM.nextInt(10) == 0;
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return cap.isCooldownDone(JJKAbilities.DASH.get()) ? super.isTriggerable(owner) : Status.FAILURE;
    }


    private static double getDistanceGround(LivingEntity entity) {
        Vec3 pos = entity.position();
        Vec3 down = pos.add(0.0, -256.0, 0.0);

        var result = entity.level().clip(new net.minecraft.world.level.ClipContext(
                pos, down,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.ANY,
                entity
        ));
        if (result.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            return pos.y - result.getLocation().y;
        }
        return Double.MAX_VALUE;
    }
        

    private static float getRange(LivingEntity owner) {
        float modifier = 1.0F;
        if (JJKAbilities.hasTrait(owner, Trait.HEAVENLY_RESTRICTION_PHYSICAL)) modifier = 1.5F;
        if (JJKAbilities.hasTrait(owner, Trait.HEAVENLY_RESTRICTION_CE)) modifier = 0.5F;
        return (float) (RANGE * modifier);
    }

    private static boolean canDash(LivingEntity owner) {
           ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        if (owner.hasEffect(JJKEffects.DOMAINSTUN.get() ) || owner.hasEffect(JJKEffects.STUN.get()) || (cap.hasToggled(JJKAbilities.ANGEL_WINGS.get()) && getDistanceGround(owner) > 4.0D)) return false;

        boolean collision = false;

        AABB bounds = owner.getBoundingBox();
        int bound = 4;
        Cursor3D cursor = new Cursor3D(Mth.floor(bounds.minX - 1.0E-7D) - bound,
                Mth.floor(bounds.minY - 1.0E-7D) - bound,
                Mth.floor(bounds.minZ - 1.0E-7D) - bound,
                Mth.floor(bounds.maxX + 1.0E-7D) + bound,
                Mth.floor(bounds.maxY + 1.0E-7D) + bound,
                Mth.floor(bounds.maxZ + 1.0E-7D) + bound);

        while (cursor.advance()) {
            int i = cursor.nextX();
            int j = cursor.nextY();
            int k = cursor.nextZ();
            int l = cursor.getNextType();

            if (l == 3) continue;

            BlockState state = owner.level().getBlockState(new BlockPos(i, j, k));

            if (!state.isAir()) {
                collision = true;
                break;
            }
        }
        return collision || owner.getXRot() >= 1.0F;
    }


    @Override
    public void run(LivingEntity owner) {
        if (!(owner.level() instanceof ServerLevel level)) return;

        if (!canDash(owner)) return;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        cap.addDash();
        cap.delayTickEvent(cap::subDash
        ,10);

        owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BUNDLE_INSERT, SoundSource.MASTER, 2F, 1.25F);

        if (cap.getSpeedStacks() > 0 || cap.hasTrait(Trait.HEAVENLY_RESTRICTION_PHYSICAL)) {
            owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), JJKSounds.DASH.get(), SoundSource.MASTER, 0.2F, 1.5F);
            owner.addEffect(new MobEffectInstance(JJKEffects.INVISIBILITY.get(), 4, 0, false, false, false));
            level.sendParticles(new MirageParticle.MirageParticleOptions(owner.getId()), owner.getX(), owner.getY(), owner.getZ(),
                    0, 0.0D, 0.0D, 0.0D, 1.0D);
        }

        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);

        HitResult hit = RotationUtil.getLookAtHit(owner, getRange(owner));

        float power = Math.min(MAX_DASH,
                DASH * (1.0F + this.getPower(owner) * 0.1F));
        if (!owner.isShiftKeyDown()) {
            power*=0.55f;
        }

   
     
        //Vec3 target = this.getTarget(owner);
        if (owner.onGround() && look.y < 0) {
            look = owner.getLookAngle().multiply(1,0,1);
        }
        Vec3 velocity = look.normalize().scale(power);
        velocity = velocity.multiply(new Vec3(1.5D, 1.0D, 1.5D));
        if (velocity.y > 0) {
           velocity = velocity.multiply(new Vec3(1.2D, 0.6D, 1.2D));
        } else {
            velocity = velocity.multiply(new Vec3(1.2D,1.3,1.2D));
        }
        if (cap.hasTrait(Trait.HEAVENLY_RESTRICTION_PHYSICAL)) {
            velocity = velocity.multiply(new Vec3(1.2D, 1.0D, 1.2D));
            if (owner.isShiftKeyDown()) {
                owner.addEffect(new MobEffectInstance(JJKEffects.INVISIBILITY.get(), 8, 0, false, false, false));

            } else {
                owner.addEffect(new MobEffectInstance(JJKEffects.INVISIBILITY.get(), 4, 0, false, false, false));
                velocity = velocity.multiply(new Vec3(1.1D, 1, 1.1D));
            }
           
        }
        if (cap.hasTrait(Trait.HEAVENLY_RESTRICTION_CE)) {
            velocity = velocity.multiply(new Vec3(0.8D, 1.0D, 0.8D));
        }
        if (owner.onGround() && velocity.y < 0) {
            velocity.multiply(1,0,1);
        }
        velocity = velocity.add(new Vec3(0.0D,0.6D,0.0D));
        if (!owner.onGround() && owner.level().getBlockState(owner.blockPosition()).getFluidState().isEmpty()) {
           velocity = velocity.add(new Vec3(0.0D,-0.75D,0.0D));
        }
        owner.setDeltaMovement(velocity);
        /*if (hit.getType() == HitResult.Type.MISS) {
            float f = owner.getYRot();
            float f1 = owner.getXRot();
            float f2 = -Mth.sin(f * ((float) Math.PI / 180.0F)) * Mth.cos(f1 * ((float) Math.PI / 180.0F));
            float f3 = -Mth.sin(f1 * ((float) Math.PI / 180.0F));
            float f4 = Mth.cos(f * ((float) Math.PI / 180.0F)) * Mth.cos(f1 * ((float) Math.PI / 180.0F));
            float f5 = Mth.sqrt(f2 * f2 + f3 * f3 + f4 * f4);
            f2 *= power / f5;
            f3 *= power / f5;
            f4 *= power / f5;
            owner.push(f2, f3, f4);
            owner.move(MoverType.SELF, new Vec3(0.0D, 1.1999999F, 0.0D));
        } else {
            Vec3 target = hit.getLocation();

            double distanceX = target.x - owner.getX();
            double distanceY = target.y - owner.getY();
            double distanceZ = target.z - owner.getZ();

            double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ);
            double motionX = distanceX / distance * power;
            double motionY = distanceY / distance * power;
            double motionZ = distanceZ / distance * power;
            owner.setDeltaMovement(motionX, motionY, motionZ);
        }*/
        owner.hurtMarked = true;

        Vec3 pos = owner.position();

        for (int i = 0; i < 32; i++) {
            double xPos = owner.getX() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 1.25F) - owner.getLookAngle().scale(0.35D).x;
            double yPos= owner.getY() + HelperMethods.RANDOM.nextDouble() * (owner.getBbHeight());
            double zPos = owner.getZ() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 1.25F) - owner.getLookAngle().scale(0.35D).z;
            double theta = HelperMethods.RANDOM.nextDouble() * 2 * Math.PI;
            double phi = HelperMethods.RANDOM.nextDouble() * Math.PI;
            double r = HelperMethods.RANDOM.nextDouble() * 0.2D;
            double x = r * Math.sin(phi) * Math.cos(theta);
            double y = r * Math.sin(phi) * Math.sin(theta);
            double z = r * Math.cos(phi);
            Vec3 speed = look.add(x, y, z).reverse();
            level.sendParticles(ParticleTypes.CLOUD, xPos, yPos, zPos, 0, speed.x, speed.y, speed.z, 1.0D);
        }

        for (int i = 0; i < 4; i++) {
            cap.delayTickEvent(() -> {
                for (int a = 0; a < 4; a++) {
                    Vec3 pos2 = owner.getEyePosition();
                    level.sendParticles(ParticleTypes.POOF,
                            pos2.x + (HelperMethods.RANDOM.nextDouble() - 0.5D) * 0.8D,
                            pos2.y + (HelperMethods.RANDOM.nextDouble() - 0.5D) * 0.8D,
                            pos2.z + (HelperMethods.RANDOM.nextDouble() - 0.5D) * 0.8D,
                            0, 0.0D, 0.0D, 0.0D, 1.0D);
                }
            }, i);
        }
    }

    @Override
    public int getRealCooldown(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        if (cap.hasTrait(Trait.HEAVENLY_RESTRICTION_PHYSICAL)) {
            if (owner.isShiftKeyDown()) {
                return 16;
            }
            return 8;
        }
        if (cap.hasTrait(Trait.HEAVENLY_RESTRICTION_CE)) {
            if (!owner.isShiftKeyDown()) {
                return 50;
            }
            return 25;
        }
        if (owner.isShiftKeyDown()) {
            return 25;
        }
        return super.getSuperRealCooldown(owner);
    }
   
}
