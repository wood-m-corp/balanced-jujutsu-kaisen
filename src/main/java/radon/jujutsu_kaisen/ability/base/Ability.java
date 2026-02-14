package radon.jujutsu_kaisen.ability.base;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import radon.jujutsu_kaisen.chant.ChantHandler;
import radon.jujutsu_kaisen.ability.AbilityDisplayInfo;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.entity.base.DomainExpansionEntity;
import radon.jujutsu_kaisen.util.RotationUtil;
import net.minecraft.util.Mth;
import radon.jujutsu_kaisen.capability.data.sorcerer.BindingVow;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedEnergyNature;

import javax.annotation.Nullable;

import radon.jujutsu_kaisen.config.ConfigHolder;
import java.util.List;
import java.util.Locale;


public abstract class Ability {
    public enum ActivationType {
        INSTANT,
        TOGGLED,
        CHANNELED,
        DOMAIN
    }

    public enum Status {
        FAILURE,
        UNUSABLE,
        SUCCESS,
        ENERGY,
        COOLDOWN,
        BURNOUT,
        DISABLE,
        DISARMED,
        SILENCED,
        CHANT,
        THROAT,
        EMPTYINV
    }

    public enum Classification {
        NONE,
        CURSED_SPEECH,
        SLASHING,
        FIRE,
        WATER,
        PLANTS,
        BLUE,
        LIGHTNING,
        PROJECTION
    }
    
    public static float getPower(Ability ability, LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return cap.getAbilityPower() * (1.0F + 0.75F * ChantHandler.getChant(owner, ability));
    }

    public float getPower(LivingEntity owner) {
        return getPower(this, owner);
    }

    public void cooldown(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        if (this.getRealCooldown(owner) > 0) {
            cap.addCooldown(this);
        }
    }

    public boolean isScalable(LivingEntity owner) {
        return this.getActivationType(owner) != ActivationType.TOGGLED;
    }

    public boolean isChantable() {
        return false;
    }

    protected boolean isNotDisabledFromDA() {
        return false;
    }


    public boolean canDisable() {
        return true;
    }

    // Used for skill tree
    public boolean isCursedEnergyColor() {
        return false;
    }

    // Used for skill tree
    public boolean isDisplayed(LivingEntity owner) {
        return this.getPointsCost() > 0;
    }

    protected int getPointsCost() {
        return 0;
    }

    public boolean isDomain() {
        return false;
    }

    public int getRealPointsCost(LivingEntity owner) {
       // ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
      
        /*if (cap.hasTrait(Trait.SIX_EYES)) {
            return this.getPointsCost() / 2;
        }*/
        return this.getPointsCost();
    }

    

    public boolean isUnlocked(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return cap.isUnlocked(this);
    }

    public boolean canUnlock(LivingEntity owner) {
        if (this.isBlocked(owner)) return false;
        if (owner instanceof Player player && player.getAbilities().instabuild) return true;
        if (!this.isUnlockable() || this.getPointsCost() == 0) return false;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return cap.getPoints() >= this.getRealPointsCost(owner);
    }

    public boolean isUnlockable() {
        return this.getPointsCost() > 0;
    }

    public boolean isBlocked(LivingEntity owner) {
        Ability parent = this.getParent(owner);
        return parent != null && !parent.isUnlocked(owner);
    }

    public Vec2 getDisplayCoordinates() {
        return Vec2.ZERO;
    }

    public AbilityDisplayInfo getDisplay(LivingEntity owner) {
        Vec2 coordinates = this.getDisplayCoordinates();
        return new AbilityDisplayInfo(JJKAbilities.getKey(this).getPath(), coordinates.x, coordinates.y);
    }

    @Nullable
    public Ability getParent(LivingEntity owner) {
        return null;
    }

    public Classification getClassification() {
        return Classification.NONE;
    }

    public boolean isMelee() {
        return false;
    }

    public boolean usesHands() {
        return true;
    }

    // Used for AI
    public abstract boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target);

    public abstract ActivationType getActivationType(LivingEntity owner);

    public abstract void run(LivingEntity owner);

    public List<Ability> getRequirements() {
        return List.of();
    }

    protected int getCooldown() {
        return 0;
    }

    public boolean isTechnique() {
        return true;
    }

    public int getRealCooldown(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (this.isMelee() && cap.hasTrait(Trait.HEAVENLY_RESTRICTION_PHYSICAL)) {
            return this.getCooldown() / 2;
        }
        if (this.isMelee() && cap.hasTrait(Trait.HEAVENLY_RESTRICTION_CE)) {
            return this.getCooldown() * 2;
        }
        if (this.isMelee() && cap.getNature() == CursedEnergyNature.DIVERGENT) {
            return this.getCooldown() * 3 / 4;
        }
        return this.getCooldown();
    }

    public MenuType getMenuType() {
        return MenuType.RADIAL;
    }

       public boolean isValid(LivingEntity owner) {
        if (owner == null || (owner instanceof Player player && player.isSpectator()) ) return false;

        if (this.isUnlockable() && !this.isUnlocked(owner)) return false;

         if (!owner.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return false;
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (this.isDomain() && (!(owner instanceof Player player))) {
            return true;
        }

        if (this.isTechnique() && cap.hasToggled(JJKAbilities.DOMAIN_AMPLIFICATION.get())) {
            if (!this.isNotDisabledFromDA() || !cap.hasToggled(this)) {
                return false;
            }
        }
        if (this.isTechnique() && this.canDisable() && cap.hasDisable() ) {
            return false;
        }
        
        if ((this.usesHands() && !this.isDomain() ) && (cap.hasToggled(JJKAbilities.HOLLOW_WICKER_BASKET.get()) || cap.hasDisarmed() ) && !cap.hasTrait(Trait.PERFECT_BODY) ) {
            return false;
        }

        for (Ability ability : this.getRequirements()) {
            if (!ability.isUnlocked(owner)) return false;
        }
        return true;
    }

    public void addDuration(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (this instanceof IDurationable durationable && durationable.getRealDuration(owner) > 0) {
            cap.addDuration(this);
        }
    }

    public void charge(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (!(owner instanceof Player player && player.getAbilities().instabuild)) {
            cap.useEnergy(this.getRealCost(owner));

            if (this.getRealCooldown(owner) > 0) {
                cap.addCooldown(this);
            }
        }
    }

    public Status getStatus(LivingEntity owner) {
        if (this != JJKAbilities.WHEEL.get() && this != JJKAbilities.CURSED_ENERGY_FLOW.get() && owner.hasEffect(JJKEffects.UNLIMITED_VOID.get())) return Status.FAILURE;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (!(owner instanceof Player player && (player.getAbilities().instabuild  || ConfigHolder.SERVER.turboMode.get() ) )) {
            if (this.isTechnique() && cap.hasBurnout()) {
                return Status.BURNOUT;
            }
            
                if (this.isTechnique() && ((this.canDisable() && cap.hasDisable()) || (owner.getHealth() / owner.getMaxHealth() > 0.25F && cap.hasBindingVow(BindingVow.RISK)))) {
                    return Status.DISABLE;
                }
                if ((this.usesHands() && !this.isDomain()) && (cap.hasToggled(JJKAbilities.HOLLOW_WICKER_BASKET.get()) || cap.hasDisarmed()) && !cap.hasTrait(Trait.PERFECT_BODY)) {
                    return Status.DISARMED;
                }



            if (!cap.isCooldownDone(this)) {
                return Status.COOLDOWN;
            }

            if (!this.checkCost(owner)) {
                return Status.ENERGY;
            }
        }
        return Status.SUCCESS;
    }

    public Status isTriggerable(LivingEntity owner) {
        if (!JJKAbilities.getAbilities(owner).contains(this)) return Status.UNUSABLE;

        MobEffectInstance instance = owner.getEffect(JJKEffects.STUN.get());
        
       
        if (instance != null && instance.getAmplifier() > 0 && this != JJKAbilities.HEAL.get() && this != JJKAbilities.RCT1.get() && this != JJKAbilities.RCT2.get() && this != JJKAbilities.RCT3.get() && this != JJKAbilities.CURSED_ENERGY_SHIELD.get() && this != JJKAbilities.HOLLOW_WICKER_BASKET.get()  && this != JJKAbilities.SIMPLE_DOMAIN.get()  && this != JJKAbilities.SIMPLE_DOMAIN_ENLARGEMENT.get()  )  {
            if (this.isDomain() ) {
                ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                if (cap.getType() != JujutsuType.CURSE || cap.hasTrait(Trait.DEATH_PAINTING) ) {
                    return Status.FAILURE;
                }
            } else {
                return Status.FAILURE;
            }
        }

        Status status = this.getStatus(owner);

        /*if (status == Status.SUCCESS && this.getActivationType(owner) == ActivationType.INSTANT) {
            this.charge(owner);
        } moved to abilityhandler */
       	//this.addDuration(owner);
        return status;
    }

    public Status isStillUsable(LivingEntity owner) {
        if (!JJKAbilities.getAbilities(owner).contains(this)) return Status.UNUSABLE;

        if (this instanceof IAttack || this instanceof ICharged) {
            return this.getStatus(owner);
        }

        Status status = this.getStatus(owner);

        if (status == Ability.Status.SUCCESS || status == Ability.Status.COOLDOWN) {
            this.charge(owner);
        }
        return status;
    }

    public boolean checkCost(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (!(owner instanceof Player player && player.getAbilities().instabuild)) {
            float cost = this.getRealCost(owner);
            return cap.getEnergy() >= cost;
        }
        return true;
    }

    public Component getName() {
        ResourceLocation key = JJKAbilities.getKey(this);
        return Component.translatable(String.format("ability.%s.%s", key.getNamespace(), key.getPath()));
    }

    public abstract float getCost(LivingEntity owner);

    public boolean shouldLog(LivingEntity owner) {
        return this.getActivationType(owner) == ActivationType.TOGGLED;
    }

    public Component getEnableMessage() {
        ResourceLocation key = JJKAbilities.getKey(this);

        if (key == null) return Component.empty();

        return Component.translatable(String.format("ability.%s.%s.enable", key.getNamespace(), key.getPath()));
    }

    public Component getDisableMessage() {
        ResourceLocation key = JJKAbilities.getKey(this);

        if (key == null) return Component.empty();

        return Component.translatable(String.format("ability.%s.%s.disable", key.getNamespace(), key.getPath()));
    }


    public @Nullable BlockHitResult getBlockHit(LivingEntity owner, double range) {
        Vec3 start = owner.getEyePosition();
        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);
        Vec3 end = start.add(look.scale(range));
        HitResult result = RotationUtil.getHitResult(owner, start, end);

        if (result.getType() == HitResult.Type.BLOCK) {
            return (BlockHitResult) result;
        } else if (result.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) result).getEntity();
            Vec3 offset = entity.position().subtract(0.0D, 5.0D, 0.0D);
            return owner.level().clip(new ClipContext(entity.position(), offset, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
        }
        return null;
    }

    public float getRealCost(LivingEntity owner, float cost) {
        float output = ChantHandler.getOutput(owner, this);

        if (!owner.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return cost;
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        CursedTechnique copied = cap.getCurrentCopied();

        if (copied != null && List.of(copied.getAbilities()).contains(this)) {
            cost *= 1.5F;
        }
        if (cap.hasTrait(Trait.SIX_EYES) && (this.isTechnique() || this.isDomain() ) ) {
            cost *= ConfigHolder.SERVER.sixEyesMult.get().floatValue();
        }
        else if (!cap.hasTrait(Trait.SIX_EYES) && (this.isTechnique() || this.isDomain()) ) {
            if (JJKAbilities.getTechnique(this) == CursedTechnique.LIMITLESS) {
               cost *= ConfigHolder.SERVER.limitlessNoSixEyesMult.get().floatValue();
            }
        }
        if (output > 0) {
            //System.out.println(output);
            if (output >= 1.0F) {
                output = Mth.clamp(output*0.8F,1.0F,100.0F);
                cost *= (this.isScalable(owner) || this.isChantable() ? output : 1.0F);

             } else {
               float scaledOutput = Mth.clamp(output, 0.1F, 1.0F);
               float costMultiplier = (float)Math.pow(scaledOutput, 0.5);
                cost *= (this.isScalable(owner) || this.isChantable())
                        ? (costMultiplier)
                        : 1.0F;
            }
           
	  
            cost *= (ConfigHolder.SERVER.cursedEnergyCostAmount.get().floatValue() );

        }
        return Float.parseFloat(String.format(Locale.ROOT, "%.2f", cost ));
    }

    public float getRealCost(LivingEntity owner) {
        return this.getRealCost(owner, this.getCost(owner) );
    }

    public interface IDomainAttack {
        default void performEntity(LivingEntity owner, LivingEntity target, DomainExpansionEntity domain) {}
        default void performBlock(LivingEntity owner, DomainExpansionEntity domain, BlockPos pos) {}
    }

    public interface ITenShadowsAttack {
        void perform(LivingEntity owner, @Nullable LivingEntity target);
    }

    public interface IDurationable {
        default int getDuration() {
            return 0;
        }

        default int getRealDuration(LivingEntity owner) {
            int duration = this.getDuration();

            if (duration > 0) {
                duration = (int) (duration * 12);
            }

            return duration;
        }
    }

    public interface IChannelened {
        default void onStart(LivingEntity owner) {}

        default void onStop(LivingEntity owner) {}

        default int getCharge(LivingEntity owner) {
            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            return cap.getCharge((Ability) this );
        }
    }

    public interface ICharged extends IChannelened {
        default boolean onRelease(LivingEntity owner) {
            return true;
        }
    }

    public interface IToggled {
        void onEnabled(LivingEntity owner);

        void onDisabled(LivingEntity owner);

        default void applyModifiers(LivingEntity owner) {}

        default void removeModifiers(LivingEntity owner) {}
    }

    public interface IAttack {
        boolean attack(DamageSource source, LivingEntity owner, LivingEntity target);
    }
}