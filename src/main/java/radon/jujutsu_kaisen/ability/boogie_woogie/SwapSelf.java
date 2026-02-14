package radon.jujutsu_kaisen.ability.boogie_woogie;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.entity.projectile.base.JujutsuProjectile;
import radon.jujutsu_kaisen.entity.projectile.CursedEnergyImbuedItemProjectile;
import radon.jujutsu_kaisen.item.base.CursedToolItem;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

public class SwapSelf extends Ability {
    public static final double RANGE = 50.0D;
    public Entity enemy = null;

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return this.getTarget(owner) == target && HelperMethods.RANDOM.nextInt(3) == 0;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
    }

    public static boolean canSwap(Entity target) {
        return (target.isPickable() || target instanceof ItemEntity item && item.getItem().getItem() instanceof CursedToolItem || target instanceof CursedEnergyImbuedItemProjectile
                || target instanceof JujutsuProjectile) && (!(target instanceof LivingEntity living) || !JJKAbilities.hasTrait(living, Trait.HEAVENLY_RESTRICTION_PHYSICAL));
    }

    
    private @Nullable Entity getTarget(LivingEntity owner) {
        LivingEntity target = (RotationUtil.getExpandedLookAt(owner, RANGE));
        if (target != null) {
            return canSwap(target) ? target : null;
        }
        return null;
    }

    @Override
    public void run(LivingEntity owner) {
        owner.swing(InteractionHand.MAIN_HAND);

        if (owner.level().isClientSide) return;

        Entity target = enemy;

        if (target != null) {
            owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), JJKSounds.CLAP.get(), SoundSource.MASTER, 2.0F, 1.0F);
            owner.level().playSound(null, target.getX(), target.getY(), target.getZ(), JJKSounds.CLAP.get(), SoundSource.MASTER, 1.0F, 1.0F);

            Vec3 pos = target.position();

            Vec2 ownerRot = owner.getRotationVector();
            Vec2 targetRot = target.getRotationVector();
            target.getCapability(SorcererDataHandler.INSTANCE).ifPresent(cap -> {
                    cap.setSelfHit(15);
                    cap.setExtraMeleeTaken(20);
                });
      

            target.teleportTo(owner.getX(), owner.getY(), owner.getZ());
            owner.teleportTo(pos.x, pos.y, pos.z);
           
            target.setYRot(ownerRot.y);
            target.setXRot(ownerRot.x);

            owner.setYRot(targetRot.y);
            owner.setXRot(targetRot.x);
        }
        enemy = null;
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 20.0F;
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        Entity target = this.getTarget(owner);
        enemy = target;

        if (target == null) {
            return Status.FAILURE;
        }
        return super.isTriggerable(owner);
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.J2TSU;
    }

    @Override
    public int getCooldown() {
        return 2;
    }
}
