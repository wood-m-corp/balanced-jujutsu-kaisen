package radon.jujutsu_kaisen.capability.data.sorcerer;

import com.mojang.authlib.GameProfile;

import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.ServerLifecycleHooks;
import radon.jujutsu_kaisen.JJKConstants;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.AbilityStopEvent;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.misc.Slam;
import radon.jujutsu_kaisen.client.particle.ParticleColors;
import radon.jujutsu_kaisen.client.visual.ClientVisualHandler;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.item.JJKItems;
import radon.jujutsu_kaisen.network.packet.c2s.UnstealAbilityC2SPacket;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.network.packet.s2c.SyncVisualDataS2CPacket;
import radon.jujutsu_kaisen.util.CuriosUtil;
import radon.jujutsu_kaisen.util.EntityUtil;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.PlayerUtil;
import radon.jujutsu_kaisen.util.SorcererUtil;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class SorcererData implements ISorcererData {
    private boolean initialized;

    private int cursedEnergyColor;
    

    private int points;
    private final Set<Ability> unlocked;

    private float domainSize;

    private @Nullable CursedTechnique technique;

    //private Set<CursedTechnique> add;
    private @Nullable CursedTechnique additional;

    private Set<CursedTechnique> copied;
    private @Nullable CursedTechnique currentCopied;

    private Set<CursedTechnique> stolen;
    private @Nullable CursedTechnique currentStolen;

    private final Set<CursedTechnique> absorbed;
    private @Nullable CursedTechnique currentAbsorbed;


    // private GameProfile stolenSkinProfile;
    // private ResourceLocation stolenSkinTexture;

    private int transfiguredSouls;

    private CursedEnergyNature nature;

    private float experience;
    private float output;

    private float energy;
    private int lives;
    private int dashes = 0;
    private int shieldTicks = 0;
    private float maxEnergy;
    private float extraEnergy;
    private float additionalEnergy;

    private JujutsuType type;
    private int silenced;
    private int disarmed;
    private int disable;

    private int selfHit;
    private int extraMeleeTaken;
    private int burnout;
    private int brainDamage;
    private int brainDamageTimer;
    private int throatDamage;
    
    private long lastBlackFlashTime;
    private boolean addBlackFlash;
    private boolean hasWombAwakened;

    private @Nullable Ability channeled;
    private int charge;

    private final Set<Ability> toggled;

    private final Set<Trait> traits;
    private final List<DelayedTickEvent> delayedTickEvents;
    private final Map<Ability, Integer> cooldowns;
    private final Map<Ability, Integer> durations;
    private final Map<Ability, Integer> disrupted;
    private final Set<SummonData> summons;
    private final Map<UUID, Set<Pact>> acceptedPacts;
    private final Map<UUID, Set<Pact>> requestedPactsCreations;
    private final Map<UUID, Integer> createRequestExpirations;
    private final Map<UUID, Set<Pact>> requestedPactsRemovals;
    private final Set<BindingVow> bindingVows;
    private final Map<BindingVow, Integer> bindingVowCooldowns;
    private final Map<Ability, Set<String>> chants;

    // Curse Manipulation
    private final List<AbsorbedCurse> curses;

    // Projection Sorcery
    private final List<AbstractMap.SimpleEntry<Vec3, Float>> frames;
    private int speedStacks;
    private int noMotionTime;

    private int fingers;

    private static final UUID MAX_HEALTH_UUID = UUID.fromString("72ff5080-3a82-4a03-8493-3be970039cfe");
    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("4979087e-da76-4f8a-93ef-6e5847bfa2ee");
    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("a2aef906-ed31-49e8-a56c-decccbfa2c1f");
    private static final UUID MOVEMENT_SPEED_UUID = UUID.fromString("9fe023ca-f22b-4429-a5e5-c099387d5441");
    private static final UUID STEP_HEIGHT_UUID = UUID.fromString("654c65b5-dc0f-4092-8423-59cbe3d19682");
    private static final UUID ARMOR_UUID = UUID.fromString("486fd273-fdbc-4876-b0b8-af5a64bfb08a");
    private static final UUID ARMOR_TOUGHNESS_UUID = UUID.fromString("0be71dde-8aeb-4c5d-955f-d37325c31a94");
    private static final UUID PROJECTION_SORCERY_MOVEMENT_SPEED_UUID = UUID.fromString("23ecaba3-fbe8-44c1-93c4-5291aa9ee777");
    private static final UUID PROJECTION_ATTACK_SPEED_UUID = UUID.fromString("18cd1e25-656d-4172-b9f7-2f1b3daf4b89");
    private static final UUID PROJECTION_STEP_HEIGHT_UUID = UUID.fromString("1dbcbef7-8193-406a-b64d-8766ea505fdb");

    private LivingEntity owner;

    public SorcererData() {
        this.domainSize = 1.0F;

        this.unlocked = new HashSet<>();

        this.nature = CursedEnergyNature.BASIC;

        this.type = JujutsuType.SORCERER;

        //this.add = new LinkedHashSet<>();
        this.copied = new LinkedHashSet<>();
        this.absorbed = new LinkedHashSet<>();
        this.stolen = new LinkedHashSet<>();


        this.output = 1.0F;

        this.lives = ConfigHolder.SERVER.livesconfig.get();
        this.charge = 0;
        this.lastBlackFlashTime = -1;
        this.addBlackFlash = false;
        
        this.toggled = new HashSet<>();
        this.traits = new HashSet<>();
        this.delayedTickEvents = new ArrayList<>();
        this.cooldowns = new HashMap<>();
        this.disrupted = new HashMap<>();
        this.durations = new HashMap<>();
        this.summons = new HashSet<>();
        this.acceptedPacts = new HashMap<>();
        this.requestedPactsCreations = new HashMap<>();
        this.createRequestExpirations = new HashMap<>();
        this.requestedPactsRemovals = new HashMap<>();
        this.bindingVows = new HashSet<>();
        this.bindingVowCooldowns = new HashMap<>();
        this.chants = new HashMap<>();

        this.curses = new ArrayList<>();

        this.frames = new ArrayList<>();
    }

    private void sync() {
        if (!this.owner.level().isClientSide) {
            ClientVisualHandler.ClientData data = new ClientVisualHandler.ClientData(this.getToggled(), this.channeled, this.getTraits(), this.getTechniques(), this.getTechnique(), this.getType(),
                    this.getExperience(), this.getCursedEnergyColor());
            PacketHandler.broadcast(new SyncVisualDataS2CPacket(this.owner.getUUID(), data.serializeNBT()));
        }
    }

    private void updateCooldowns() {
        Iterator<Map.Entry<Ability, Integer>> iter = this.cooldowns.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<Ability, Integer> entry = iter.next();

            int remaining = entry.getValue();

            if (remaining > 0) {
                this.cooldowns.put(entry.getKey(), --remaining);
            } else {
                iter.remove();
            }
        }
    }

    private void updateDurations() {
        Iterator<Map.Entry<Ability, Integer>> iter = this.durations.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<Ability, Integer> entry = iter.next();

            Ability ability = entry.getKey();

            if (!this.isChanneling(ability)) {
                iter.remove();
                continue;
            }

            int remaining = entry.getValue();

            if (remaining >= 0) {
                this.durations.put(entry.getKey(), --remaining);
            } else {
                if (ability instanceof Ability.IToggled) {
                    if (this.hasToggled(ability)) {
                        this.toggle(ability);
                    }
                } else if (ability instanceof Ability.IChannelened) {
                    if (this.isChanneling(ability)) {
                        this.channel(ability);
                    }
                }
                iter.remove();
            }
        }
    }

    private void updateTickEvents() {
        this.delayedTickEvents.removeIf(DelayedTickEvent::finished);

        for (DelayedTickEvent current : new ArrayList<>(this.delayedTickEvents)) {
            current.tick();

            if (current.finished()) {
                current.run();
            }
        }
    }

    private void updateToggled() {
        List<Ability> remove = new ArrayList<>();

        for (Ability ability : new ArrayList<>(this.toggled)) {
            if (this.disrupted.containsKey(ability)) {
                remove.add(ability);
                continue;
            }
            Ability.Status status = ability.isStillUsable(this.owner);

            if (status == Ability.Status.SUCCESS || status == Ability.Status.COOLDOWN || (status == Ability.Status.ENERGY && ability instanceof Ability.IAttack)) {
                ability.run(this.owner);

                ((Ability.IToggled) ability).applyModifiers(this.owner);
            } else {
                remove.add(ability);
            }
        }

        for (Ability ability : remove) {
            this.toggle(ability);
        }
    }

    private void updateChanneled() {
        if (this.channeled != null) {
            //if (this.disrupted.containsKey(this.channeled)) return;
            Ability.Status status = this.channeled.isStillUsable(this.owner);
            if (status == Ability.Status.SUCCESS || status == Ability.Status.COOLDOWN || (status == Ability.Status.ENERGY && this.channeled instanceof Ability.IAttack)) {
                this.channeled.run(this.owner);
            } else {
                this.channel(this.channeled);
            }
            this.charge++;
        } else {
            this.charge = 0;
        }
    }

     private void updateSummons() {
        if (!(this.owner.level() instanceof ServerLevel level)) return;

        boolean dirty = false;

        Iterator<SummonData> iter = this.summons.iterator();

        while (iter.hasNext()) {
            SummonData data = iter.next();

            if (!level.areEntitiesLoaded(data.getChunkPos())) continue;

            Entity entity = level.getEntity(data.getIdentifier());

            if (entity == null || !entity.isAlive() || entity.isRemoved()) {
                dirty = true;
                iter.remove();
                continue;
            }
            data.setChunkPos(entity.chunkPosition().toLong());
        }

        if (dirty && this.owner instanceof ServerPlayer player) {
            PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(this.serializeNBT()), player);
        }
    }

    private void updateRequestExpirations() {
        Iterator<Map.Entry<UUID, Integer>> iter = this.createRequestExpirations.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<UUID, Integer> entry = iter.next();

            int remaining = entry.getValue();

            if (remaining > 0) {
                this.createRequestExpirations.put(entry.getKey(), --remaining);
            } else {
                iter.remove();
                this.requestedPactsCreations.remove(entry.getKey());
            }
        }
    }

    private void updateBindingVowCooldowns() {
        Iterator<Map.Entry<BindingVow, Integer>> iter = this.bindingVowCooldowns.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<BindingVow, Integer> entry = iter.next();

            int remaining = entry.getValue();

            if (remaining > 0) {
                this.bindingVowCooldowns.put(entry.getKey(), --remaining);
            } else {
                iter.remove();
                this.bindingVowCooldowns.remove(entry.getKey());
            }
        }
    }

    private void updateBrainDamage() {
        if (this.brainDamage == 0) return;

        if (!this.owner.level().isClientSide) {
            this.owner.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 10 * 20, this.brainDamage - 1, false, false, false));
        }

        this.brainDamageTimer++;

        if (this.brainDamageTimer >= JJKConstants.DECREASE_BRAIN_DAMAGE_INTERVAL) {
            this.brainDamageTimer = 0;
            this.brainDamage--;

            this.output = this.getMaximumOutput();
        }
    }

    private void checkAdvancements(ServerPlayer player) {
        if (this.traits.contains(Trait.SIX_EYES)) PlayerUtil.giveAdvancement(player, "six_eyes");
        if (this.traits.contains(Trait.HEAVENLY_RESTRICTION_PHYSICAL)) PlayerUtil.giveAdvancement(player, "heavenly_restriction_physical");
        if (this.traits.contains(Trait.HEAVENLY_RESTRICTION_CE)) PlayerUtil.giveAdvancement(player, "heavenly_restriction_ce");
        if (this.traits.contains(Trait.VESSEL)) PlayerUtil.giveAdvancement(player, "vessel");
        if (this.unlocked.contains(JJKAbilities.RCT1.get()))
            PlayerUtil.giveAdvancement(player, "reverse_cursed_technique");
        if (this.traits.contains(Trait.PERFECT_BODY))
            PlayerUtil.giveAdvancement(player, "perfect_body");
        if (this.hasWombAwakened)
            PlayerUtil.giveAdvancement(player, "cursed_womb_awakening");
    }

    @Override
    public void attack(DamageSource source, LivingEntity target) {
        if (this.channeled instanceof Ability.IAttack attack) {
            if (this.channeled.getStatus(this.owner) == Ability.Status.SUCCESS && attack.attack(source, this.owner, target)) {
                this.channeled.charge(this.owner);
                this.charge = 0;
            }
        }

        for (Ability ability : this.toggled) {
            // In-case any of IAttack's kill the target just break the loop
            if (target.isDeadOrDying()) break;

            if (!(ability instanceof Ability.IAttack attack)) continue;
            if (ability.getStatus(this.owner) != Ability.Status.SUCCESS) continue;
            if (!attack.attack(source, this.owner, target)) continue;

            ability.charge(this.owner);
        }

        if (this.owner instanceof ServerPlayer player) {
            PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(this.serializeNBT()), player);
        }
    }

    private void updateDisrupted() {
        Iterator<Map.Entry<Ability, Integer>> iter = this.disrupted.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<Ability, Integer> entry = iter.next();

            int remaining = entry.getValue();

            if (remaining > 0) {
                this.disrupted.put(entry.getKey(), --remaining);
            } else {
                iter.remove();
            }
        }
    }

    @Override
    public void tick(LivingEntity owner) {
        if (this.owner == null) {
            this.owner = owner;
        }

        this.updateSummons();

        this.updateCooldowns();
        this.updateDurations();
        this.updateTickEvents();
        this.updateToggled();
        this.updateChanneled();
        this.updateDisrupted();

        this.updateRequestExpirations();
        this.updateBindingVowCooldowns();

        this.updateBrainDamage();

        if (Slam.TARGETS.containsKey(owner.getUUID()) && (owner.onGround() || owner.isInWater())) {
            Slam.onHitGround(owner, 0);
        }

        if (!this.owner.level().isClientSide) {
            if (this.speedStacks > 0) {
                EntityUtil.applyModifier(this.owner, Attributes.MOVEMENT_SPEED, PROJECTION_SORCERY_MOVEMENT_SPEED_UUID, "Movement speed", this.speedStacks * 0.25D, AttributeModifier.Operation.MULTIPLY_TOTAL);
                EntityUtil.applyModifier(this.owner, Attributes.ATTACK_SPEED, PROJECTION_ATTACK_SPEED_UUID, "Attack speed", this.speedStacks, AttributeModifier.Operation.MULTIPLY_TOTAL);
                EntityUtil.applyModifier(this.owner, ForgeMod.STEP_HEIGHT_ADDITION.get(), PROJECTION_STEP_HEIGHT_UUID, "Step height addition", 2.0F, AttributeModifier.Operation.ADDITION);
                
                this.noMotionTime++;

                if (this.noMotionTime > 20*20) {
                    this.lowerSpeedStacks();
                }
            } else {
                EntityUtil.removeModifier(this.owner, Attributes.MOVEMENT_SPEED, PROJECTION_SORCERY_MOVEMENT_SPEED_UUID);
                EntityUtil.removeModifier(this.owner, Attributes.ATTACK_SPEED, PROJECTION_ATTACK_SPEED_UUID);
                EntityUtil.removeModifier(this.owner, ForgeMod.STEP_HEIGHT_ADDITION.get(), PROJECTION_STEP_HEIGHT_UUID);
            }

            if (this.owner instanceof ServerPlayer player) {
                if (!this.initialized) {
                    this.initialized = true;
                    this.generate(player);
                }
                this.checkAdvancements(player);
            }
        }

        if (this.burnout > 0) {
            this.burnout--;
        }
        if (this.disable > 0) {
            this.disable--;
        }
        if (this.throatDamage > 0) {
            this.throatDamage--;
        }
        if (this.selfHit > 0) {
            this.selfHit--;
        }
        if (this.extraMeleeTaken > 0) {
            this.extraMeleeTaken--;
        }
        if (this.disarmed > 0) {
            this.disarmed--;
        }
        if (this.silenced > 0) {
            this.silenced--;
        }

        this.energy = Math.min(this.energy + (ConfigHolder.SERVER.cursedEnergyRegenerationAmount.get().floatValue()
                * (traits.contains(Trait.HEAVENLY_RESTRICTION_CE) ? ConfigHolder.SERVER.cehrCEMult.get().floatValue() : 1.0F)
                * ((this.owner instanceof Player player && ConfigHolder.SERVER.foodCERegen.get()) ? (player.getFoodData().getFoodLevel() / 20.0F) : 1.0F)
                ), this.getMaxEnergy());

        double health = (Math.ceil(((this.getRealPower() - 1.0F) * ConfigHolder.SERVER.npcHPMult.get().floatValue() ) / 20) * 20) + ConfigHolder.SERVER.npcHPMin.get();
        if (this.traits.contains(Trait.HEAVENLY_RESTRICTION_PHYSICAL)) {
            if (this.owner instanceof Player player) {
                health = (Math.ceil(((this.getRealPower() - 1.0F) * ConfigHolder.SERVER.phrHPMult.get().floatValue() ) / 20) * 20) + ConfigHolder.SERVER.phrHPMin.get();
            }
            if (this.owner.getMaxHealth() < health && EntityUtil.applyModifier(this.owner, Attributes.MAX_HEALTH, MAX_HEALTH_UUID, "Max health", health, AttributeModifier.Operation.ADDITION)) {
                this.owner.setHealth(this.owner.getMaxHealth());
            }

            double damage = this.getRealPower() * 3.9D;
            EntityUtil.applyModifier(this.owner, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_UUID, "Attack damage", damage, AttributeModifier.Operation.ADDITION);

            double speed = this.getRealPower();
            EntityUtil.applyModifier(this.owner, Attributes.ATTACK_SPEED, ATTACK_SPEED_UUID, "Attack speed", speed, AttributeModifier.Operation.ADDITION);
            EntityUtil.applyArmorBoost(owner);

            float ratio = owner.getHealth()/owner.getMaxHealth();


            double movement = this.getRealPower() * 0.05D;
            double realmovement = ConfigHolder.SERVER.HRMaxSpeed.get().floatValue();


            if (this.getOutput() < 1) {
                movement *= (0.5 * this.getOutput());
                realmovement *= (0.5 * this.getOutput());
                damage *= this.getOutput();
            }

            if (ratio <= 0.5 && ratio > 0.35) {
                movement *= 0.4;
                realmovement *= 0.4;
            }

            if (ratio <= 0.35 && ratio > 0.2) {
                movement *= 0.25;
                realmovement *= 0.25;
            }

            if (ratio <= 0.2) {
                movement *= 0.15;
                realmovement *= 0.15;
            }


            EntityUtil.applyModifier(this.owner, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_UUID, "Movement speed", Math.min(realmovement, movement), AttributeModifier.Operation.ADDITION);

            if (this.owner.getHealth() != this.owner.getMaxHealth()) {
                this.owner.heal(0.45F / 20);
            }

            EntityUtil.applyModifier(this.owner, ForgeMod.STEP_HEIGHT_ADDITION.get(), PROJECTION_STEP_HEIGHT_UUID, "Step height addition", 2.0F, AttributeModifier.Operation.ADDITION);

            if (!owner.level().getBlockState(owner.blockPosition()).getFluidState().isEmpty()) {
                Vec3 motion = owner.getDeltaMovement();

                if (motion.y < 0.0D) {
                    double amount = 0.01D;
                    if (owner.isInWater()) {
                        amount = 0.15D;
                    }
                    owner.setDeltaMovement(motion.x, amount, motion.z);
                }
                owner.setOnGround(true);
            }

            if (owner instanceof Player player) {
                float f;

                if (owner.onGround() && !owner.isDeadOrDying() && !owner.isSwimming()) {
                    f = Math.min(0.1F, (float) owner.getDeltaMovement().horizontalDistance());
                } else {
                    f = 0.0F;
                }
                player.bob += (f - player.bob) * 0.4F;
            }

        }
        else if (this.traits.contains(Trait.HEAVENLY_RESTRICTION_CE)) {
            if (this.owner.getHealth() > ConfigHolder.SERVER.cehrHP.get()){
                this.owner.setHealth(ConfigHolder.SERVER.cehrHP.get());
            }
            double movement = 1 - this.getRealPower() * 0.05D;
            movement *= 0.1;
            EntityUtil.applyModifier(this.owner, Attributes.MAX_HEALTH, MAX_HEALTH_UUID, "Max health", -10, AttributeModifier.Operation.ADDITION);
            EntityUtil.applyModifier(this.owner, Attributes.ATTACK_SPEED, ATTACK_SPEED_UUID, "Attack speed", 0.5F, AttributeModifier.Operation.MULTIPLY_TOTAL);
            EntityUtil.applyModifier(this.owner, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_UUID, "Attack damage", 0.5F, AttributeModifier.Operation.MULTIPLY_TOTAL);
            EntityUtil.applyModifier(this.owner, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_UUID, "Movement speed", -movement, AttributeModifier.Operation.ADDITION);
            EntityUtil.applyModifier(this.owner, ForgeMod.STEP_HEIGHT_ADDITION.get(), STEP_HEIGHT_UUID, "Step height addition", -5.0F, AttributeModifier.Operation.ADDITION);
            EntityUtil.applyModifier(this.owner, Attributes.ARMOR, ARMOR_UUID, "Armor", -20F, AttributeModifier.Operation.ADDITION);
            EntityUtil.applyModifier(this.owner, Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS_UUID, "Armor toughness", 0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        else {
            double damage = this.getRealPower() * 1.0D;
            if (this.owner instanceof Player player) {
                health = (Math.ceil(((this.getRealPower() - 1.0F) * ConfigHolder.SERVER.playerHPMult.get().floatValue()) / 20) * 20) +  ConfigHolder.SERVER.playerHPMin.get();
            
                damage = this.getRealPower() * ConfigHolder.SERVER.playerM1Mult.get().floatValue();
            }
            EntityUtil.applyModifier(this.owner, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_UUID, "Attack damage", damage, AttributeModifier.Operation.ADDITION);

            if (this.owner.getMaxHealth() != health && EntityUtil.applyModifier(this.owner, Attributes.MAX_HEALTH, MAX_HEALTH_UUID, "Max health", health, AttributeModifier.Operation.ADDITION)) {
                this.owner.setHealth(this.owner.getMaxHealth());
            }
        }
    }

    @Override
    public void init(LivingEntity owner) {
        this.owner = owner;
    }

    @Override
    public int getCursedEnergyColor() {
        return this.cursedEnergyColor == 0 ? HelperMethods.getRGB24(ParticleColors.getCursedEnergyColor(this.type)) : this.cursedEnergyColor;
    }

    @Override
    public void setCursedEnergyColor(int color) {
        this.cursedEnergyColor = color;
        this.sync();
    }

    @Override
    public void disrupt(Ability ability, int duration) {
        this.disrupted.put(ability, duration);
    }

    @Override
    public void hurtThroat(int damage) {
        this.throatDamage = damage;
    }

    @Override
    public int getThroatDamage() {
        return this.throatDamage;
    }

    @Override
    public boolean isThroatDamaged() {
        return this.throatDamage > 0;
    }

    @Override
    public float getMaximumOutput() {
        float output = 1.0F;

        if (this.traits.contains(Trait.HEAVENLY_RESTRICTION_CE )){
            output = ConfigHolder.SERVER.cehrOutputMax.get().floatValue() / 100F;
        }
        else if (this.toggled.contains(JJKAbilities.MYTHICAL_BEAST_AMBER.get() )) {
            output = 1.5F;
        }
        else if (this.isInZone())  {
            output = 1.2F;
        }
        else if (this.traits.contains(Trait.SIMURIAN)) {
            output = 1.1F;
        }
        else {
            ItemStack stack = owner.getItemInHand(InteractionHand.MAIN_HAND);
            List<Item> stacks = new ArrayList<>();
            stacks.add(stack.getItem());
            stacks.addAll(CuriosUtil.findSlots(owner, owner.getMainArm() == HumanoidArm.RIGHT ? "right_hand" : "left_hand")
                    .stream().map(ItemStack::getItem).toList());
            if (stacks.contains(JJKItems.HITEN_STAFF.get()) && !this.traits.contains(Trait.HEAVENLY_RESTRICTION_PHYSICAL)) {
                output = 1.1F;
            }
        }


        return output * (1.0F - ((float) this.brainDamage / JJKConstants.MAX_BRAIN_DAMAGE));
    }

    @Override
    public void increaseOutput() {
        this.output = Math.min(this.getMaximumOutput(), this.output + 0.1F);
    }

    @Override
    public void decreaseOutput() {
        this.output = Math.max(0.1F, this.output - 0.1F);
    }

    @Override
    public void maxOutput() {
        this.output = this.getMaximumOutput();
    }

    @Override
    public int getPoints() {
        return this.points;
    }

    @Override
    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public void addPoints(int points) {
        this.points += points;
    }

    @Override
    public void usePoints(int count) {
        this.points -= count;
    }

    @Override
    public boolean isUnlocked(Ability ability) {
        return this.unlocked.contains(ability);
    }

    @Override
    public void lock(Ability ability) {
        this.unlocked.remove(ability);
    }
    
    @Override
    public void unlock(Ability ability) {
        this.unlocked.add(ability);
    }

    @Override
    public void unlockAll(List<Ability> abilities) {
        this.unlocked.addAll(abilities);
    }

    @Override
    public void lockAll() {
        this.unlocked.clear();
    }

    @Override
    public void createPact(UUID recipient, Pact pact) {
        if (!this.acceptedPacts.containsKey(recipient)) {
            this.acceptedPacts.put(recipient, new HashSet<>());
        }
        this.acceptedPacts.get(recipient).add(pact);
    }
    // @Override
    // public void setStolenSkinProfile(GameProfile profile) {
    //     this.stolenSkinProfile = profile;
    // }

    // @Override
    // public GameProfile getStolenSkinProfile() {
    //     return this.stolenSkinProfile;
    // }

    //  @Override
    // public void setStolenSkinTexture(ResourceLocation skin) {
    //     this.stolenSkinTexture = skin;
    // }

    // @Override
    // public ResourceLocation getStolenSkinTexture() {
    //     return this.stolenSkinTexture;
    // }


    @Override
    public boolean hasPact(UUID recipient, Pact pact) {
        return this.acceptedPacts.getOrDefault(recipient, Set.of()).contains(pact);
    }

    @Override
    @Nullable
    public Player getPactPartner(UUID recipient, Pact pact) {
        if (!this.acceptedPacts.getOrDefault(recipient, Set.of()).contains(pact)) {
            return null;
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;

        ServerPlayer partner = server.getPlayerList().getPlayer(recipient);
        return partner;
    }

    @Override
    public Map<UUID, Set<Pact>> getAcceptedPacts() {
        return this.acceptedPacts;
    }

    @Override
    public void removePact(UUID recipient, Pact pact) {
        this.acceptedPacts.get(recipient).remove(pact);

        if (this.acceptedPacts.get(recipient).isEmpty()) {
            this.acceptedPacts.remove(recipient);
        }
    }

    @Override
    public void createPactCreationRequest(UUID recipient, Pact pact) {
        if (!this.requestedPactsCreations.containsKey(recipient)) {
            this.requestedPactsCreations.put(recipient, new HashSet<>());
        }
        this.requestedPactsCreations.get(recipient).add(pact);
        this.createRequestExpirations.put(recipient, 30 * 20);
    }

    @Override
    public void createPactRemovalRequest(UUID recipient, Pact pact) {
        if (!this.requestedPactsRemovals.containsKey(recipient)) {
            this.requestedPactsRemovals.put(recipient, new HashSet<>());
        }
        this.requestedPactsRemovals.get(recipient).add(pact);
    }

  
    @Override
    public int getShieldTicks() {
        return shieldTicks;
    }

    @Override
    public void setShieldTicks(int ticks) {
        this.shieldTicks = ticks;
    }

    @Override
    public void removePactCreationRequest(UUID recipient, Pact pact) {
        this.requestedPactsCreations.getOrDefault(recipient, new HashSet<>()).remove(pact);
        this.createRequestExpirations.remove(recipient);
    }

    @Override
    public void removePactRemovalRequest(UUID recipient, Pact pact) {
        this.requestedPactsRemovals.getOrDefault(recipient, new HashSet<>()).remove(pact);
    }

    @Override
    public boolean hasRequestedPactCreation(UUID recipient, Pact pact) {
        return this.requestedPactsCreations.getOrDefault(recipient, Set.of()).contains(pact);
    }

    @Override
    public boolean hasRequestedPactRemoval(UUID recipient, Pact pact) {
        return this.requestedPactsRemovals.getOrDefault(recipient, Set.of()).contains(pact);
    }

    @Override
    public void addBindingVow(BindingVow vow) {
        this.bindingVows.add(vow);
    }

    @Override
    public void removeBindingVow(BindingVow vow) {
        this.bindingVows.remove(vow);
    }

    @Override
    public boolean hasBindingVow(BindingVow vow) {
        return this.bindingVows.contains(vow);
    }

    @Override
    public void addBindingVowCooldown(BindingVow vow) {
        this.bindingVowCooldowns.put(vow, 20 * ConfigHolder.SERVER.bindingVowCooldown.get() );
    }

    @Override
    public void resetVows() {
        this.bindingVows.clear();
        this.bindingVowCooldowns.clear();
    }

    @Override
    public int getRemainingCooldown(BindingVow vow) {
        return this.bindingVowCooldowns.get(vow);
    }

    @Override
    public boolean isCooldownDone(BindingVow vow) {
        return !this.bindingVowCooldowns.containsKey(vow);
    }

    @Override
    public void addChant(Ability ability, String chant) {
        if (!this.chants.containsKey(ability)) {
            this.chants.put(ability, new LinkedHashSet<>());
        }
        this.chants.get(ability).add(chant);
    }

    @Override
    public void addChants(Ability ability, Set<String> chants) {
        this.chants.put(ability, chants);
    }

    @Override
    public void removeChant(Ability ability, String chant) {
        if (this.chants.containsKey(ability)) {
            this.chants.get(ability).remove(chant);

            if (this.chants.get(ability).isEmpty()) {
                this.chants.remove(ability);
            }
        }
    }

    @Override
    public boolean hasChant(Ability ability, String chant) {
        List<String> chants = new ArrayList<>(this.chants.getOrDefault(ability, Set.of()));

        if (chants.contains(chant)) return true;

        chants.add(chant);

        for (Map.Entry<Ability, Set<String>> entry : this.chants.entrySet()) {
            if (entry.getKey() == ability) continue;

            List<String> current = new ArrayList<>(entry.getValue());

            for (int i = 0; i < chants.size(); i++) {
                if (i > current.size() - 1) break;
                if (chants.get(i).equals(current.get(i))) return true;
            }
        }
        return false;
    }

    @Override
    public boolean isChantsAvailable(Set<String> chants) {
        for (Set<String> entry : this.chants.values()) {
            if (entry.containsAll(chants)) return false;
        }
        return true;
    }

    @Override
    public @Nullable Ability getAbility(String chant) {
        for (Map.Entry<Ability, Set<String>> entry : this.chants.entrySet()) {
            if (entry.getValue().contains(chant)) return entry.getKey();
        }
        return null;
    }

    @Override
    public @Nullable Ability getAbility(Set<String> chants) {
        for (Map.Entry<Ability, Set<String>> entry : this.chants.entrySet()) {
            if (entry.getValue().equals(chants)) return entry.getKey();
        }
        return null;
    }

    @Override
    public Set<String> getFirstChants() {
        return this.chants.values().stream().map(set -> set.stream().findFirst().orElseThrow()).collect(Collectors.toSet());
    }

    @Override
    public Set<String> getFirstChants(Ability ability) {
        return this.chants.getOrDefault(ability, Set.of());
    }

    @Override
    public float getOutput() {
        return Math.min(this.getMaximumOutput(), this.output);
    }

    @Override
    public float getAbilityPower() {
        float power = this.getRealPower() * this.getOutput();
        CursedTechnique tech = this.technique;
        if (tech != null) {
                Ability domain = this.technique.getDomain();

                if (tech == CursedTechnique.BRAIN_TRANSPLANT && this.getCurrentStolen() != null ) {
                    domain = this.getCurrentStolen().getDomain();
                }

                if (domain != null && this.toggled.contains(domain)) {
                    power *= 1.2F;
                }
            
        }
        return power;
    }

    @Override
    public float getRealPower() {
        return SorcererUtil.getPower(this.experience);
    }

    @Override
    public float getRealPower(float exp) {
        return SorcererUtil.getPower(exp);
    }

    @Override
    public float getExperience() {
        return this.experience;
    }

    @Override
    public void setExperience(float experience) {
        this.experience = experience;
        this.sync();
    }

    @Override
    public boolean addExperience(float amount) {
        SorcererGrade previous = SorcererUtil.getGrade(this.experience);

        if (this.experience >= ConfigHolder.SERVER.maximumExperienceAmount.get().floatValue()) {
            return false;
        }

        this.experience = Math.min(ConfigHolder.SERVER.maximumExperienceAmount.get().floatValue(), this.experience + amount);

        SorcererGrade current = SorcererUtil.getGrade(this.experience);

        if (!this.owner.level().isClientSide && this.owner instanceof Player) {
            if (previous != current) {
                this.owner.sendSystemMessage(Component.translatable(String.format("chat.%s.rank_up", JujutsuKaisen.MOD_ID), current.getName()));
            }
        }
        this.sync();
        return true;
    }

    @Override
    public float getDomainSize() {
        return this.domainSize;
    }

    @Override
    public void setDomainSize(float domainSize) {
        this.domainSize = domainSize;
    }

    public @Nullable CursedTechnique getTechnique() {
        return this.technique;
    }

    @Override
    public Set<CursedTechnique> getTechniques() {
        Set<CursedTechnique> techniques = new HashSet<>();

        if (this.getTechnique() != null) techniques.add(this.getTechnique());
        if (this.getCurrentCopied() != null) techniques.add(this.getCurrentCopied());
        if (this.getCurrentAbsorbed() != null) techniques.add(this.getCurrentAbsorbed());
        if (this.getCurrentStolen() != null) techniques.add(this.getCurrentStolen());
        if (this.getAdditional() != null) techniques.add(this.getAdditional());

        return techniques;
    }

    @Override
    public boolean hasTechnique(CursedTechnique technique) {
        return this.technique == technique || this.additional == technique || this.getCurrentCopied() == technique || this.currentAbsorbed == technique || this.getCurrentStolen() == technique;
    }

    @Override
    public void setTechnique(@Nullable CursedTechnique technique) {
        this.technique = technique;
        this.sync();
        for (Ability ability : new ArrayList<>(this.unlocked)) {
            if (!ability.isDisplayed(owner) && ability != JJKAbilities.WORLD_SLASH.get() ) {
                this.lock(ability);
            }
        }
        
    }

    @Override
    public CursedEnergyNature getNature() {
        return this.nature;
    }

    @Override
    public void setNature(CursedEnergyNature nature) {
        this.nature = nature;
    }

    @Override
    public void setGrade(SorcererGrade grade) {
        this.experience = grade.getRequiredExperience();
    }

    @Override
    public boolean hasTrait(Trait trait) {
        return this.traits.contains(trait);
    }

    @Override
    public void addTrait(Trait trait) {
        this.traits.add(trait);
        this.sync();
    }

    @Override
    public void addTraits(List<Trait> traits) {
        this.traits.addAll(traits);
        this.sync();
    }

    @Override
    public void removeTrait(Trait trait) {
        this.traits.remove(trait);
        if (trait == Trait.CURSED_WOMB) {
            this.hasWombAwakened = false;
        }
        this.sync();
    }

    @Override
    public Set<Trait> getTraits() {
        return this.traits;
    }

    @Override
    public void setTraits(Set<Trait> traits) {
        this.traits.clear();
        this.traits.addAll(traits);
        this.sync();
    }

    @Override
    public void setType(JujutsuType type) {
        this.type = type;
        this.sync();
    }

    @Override
    public JujutsuType getType() {
        return this.type;
    }

    public void toggle(Ability ability) {
        if (!this.owner.level().isClientSide && this.owner instanceof Player) {
            if (ability.shouldLog(this.owner)) {
                if (this.hasToggled(ability)) {
                    this.owner.sendSystemMessage(ability.getDisableMessage());
                } else {
                    this.owner.sendSystemMessage(ability.getEnableMessage());
                }
            }
        }

        if (this.toggled.contains(ability)) {
            this.toggled.remove(ability);

            //ability.cooldown(this.owner);

            ((Ability.IToggled) ability).onDisabled(this.owner);

            ((Ability.IToggled) ability).removeModifiers(this.owner);

            MinecraftForge.EVENT_BUS.post(new AbilityStopEvent(this.owner, ability));
        } else {
            this.toggled.add(ability);
            ((Ability.IToggled) ability).onEnabled(this.owner);
            ability.run(this.owner);
        }

        this.sync();
    }

    @Override
    public void clearToggled() {
        this.toggled.clear();
    }

    @Override
    public Set<Ability> getToggled() {
        return this.toggled;
    }

    @Override
    public void addCooldown(Ability ability) {
        this.cooldowns.put(ability, ability.getRealCooldown(this.owner));
    }

     @Override
    public void setCooldown(Ability ability, int time) {
        this.cooldowns.put(ability, time);
    }

    @Override
    public void clearCooldown(Ability ability) {
        this.cooldowns.put(ability, 1);
    }

    @Override
    public int getRemainingCooldown(Ability ability) {
        return this.cooldowns.getOrDefault(ability, 0);
    }

    @Override
    public boolean isCooldownDone(Ability ability) {
        return !this.cooldowns.containsKey(ability);
    }

    @Override
    public void addDuration(Ability ability) {
        this.durations.put(ability, ((Ability.IDurationable) ability).getRealDuration(this.owner));
    }

    @Override
    public void increaseBrainDamage() {
        this.brainDamage = Math.min(JJKConstants.MAX_BRAIN_DAMAGE, this.brainDamage + 1);
    }

    @Override
    public int getBrainDamage() {
        return this.brainDamage;
    }

    @Override
    public void setBurnout(int duration) {
        this.burnout = duration;
    }

     @Override
    public void setDisable(int duration) {
        if (this.getDisable() >= duration) return;
        this.disable = duration;
    }
    
    @Override
    public void setDisarmed(int disarmed) {
        if (this.getDisarmed() >= disarmed) return;
        this.disarmed = disarmed;
    }

    @Override
    public void setSilenced(int silenced) {
        if (this.getSilenced() >= silenced) return;
        this.silenced = silenced;
    }

     @Override
    public void setSelfHit(int duration) {
        this.selfHit = duration;
    }

    @Override
    public void setExtraMeleeTaken(int duration) {
        this.extraMeleeTaken = duration;
    }

    @Override
    public int getBurnout() {
        return this.burnout;
    }

    @Override
    public int getDisable() {
        return this.disable;
    }

    @Override
    public int getDisarmed() {
        return this.disarmed;
    }

    @Override
    public int getSilenced() {
        return this.silenced;
    }

    @Override
    public int getSelfHit() {
        return this.selfHit;
    }

    @Override
    public int getExtraMeleeTaken() {
        return this.extraMeleeTaken ;
    }

    @Override
    public boolean hasBurnout() {
        return this.burnout > 0;
    }

    @Override
    public boolean hasDisable() {
        return this.disable > 0;
    }

    @Override
    public boolean hasDisarmed() {
        return this.disarmed > 0;
    }

    @Override
    public boolean hasSelfHit() {
        return this.selfHit > 0;
    }

    @Override
    public boolean hasExtraMeleeTaken() {
        return this.extraMeleeTaken > 0;
    }

    @Override
    public boolean hasSilenced() {
        return this.silenced > 0;
    }


    @Override
    public void resetCooldowns() {
        this.cooldowns.clear();
    }

    @Override
    public void resetBurnout() {
        this.burnout = 0;
    }

    @Override
    public void resetDisable() {
        this.disable = 0;
    }

     @Override
    public void resetDisarmed() {
        this.disarmed = 0;
    }

    @Override
    public void resetSilenced() {
        this.silenced = 0;
    }

    @Override
    public void resetSelfHit() {
        this.selfHit = 0;
    }

    @Override
    public float getEnergy() {
        if (this.traits.contains(Trait.HEAVENLY_RESTRICTION_PHYSICAL)) {
            return 0.0F;
        }
        return this.energy;
    }

    @Override
    public void addEnergy(float amount) {
        this.energy = Math.min(this.getMaxEnergy(), this.energy + amount);
        this.sync();
    }

   @Override
    public float getMaxEnergy() {
    long time = this.owner.level().getLevelData().getDayTime();
    boolean night = time >= 13000 && time < 24000;

    float baseEnergy = (this.maxEnergy == 0.0F)
            ? ConfigHolder.SERVER.cursedEnergyAmount.get().floatValue()
            : this.maxEnergy;


    float baseCapacity = baseEnergy * this.getRealPower() * ((float) Math.log(this.getRealPower() + 1)) * 0.5F;
    float maxEXP = this.getRealPower(ConfigHolder.SERVER.maximumExperienceAmount.get().floatValue() );
    float maxCapacity = baseEnergy * maxEXP  * ((float) Math.log(maxEXP + 1)) * 0.5F;
    float timeMultiplier = (this.bindingVows.contains(BindingVow.OVERTIME))
            ? (night ? 1.2F : 0.9F)
            : 1.0F;
    
    float finalEnergy = Math.min(maxCapacity * timeMultiplier, (baseCapacity * timeMultiplier + this.extraEnergy) );
    finalEnergy += this.additionalEnergy;
    finalEnergy = this.hasTrait(Trait.HEAVENLY_RESTRICTION_CE) ? Float.MAX_VALUE : finalEnergy;

    return finalEnergy;
}

    @Override
    public void setMaxEnergy(float maxEnergy) {
        this.maxEnergy = maxEnergy;
    }

    @Override
    public void useEnergy(float amount) {
        this.energy -= Math.max(0,amount);
    }

    @Override
    public void setEnergy(float energy) {
        this.energy = energy;
    }

    @Override
    public float getAdditionalEnergy() {
        return this.additionalEnergy;
    }

    @Override
    public void setAdditionalEnergy(float additionalEnergy) {
        this.additionalEnergy = additionalEnergy;
    }

    @Override
    public float getExtraEnergy() {
        return this.extraEnergy;
    }

    @Override
    public void addExtraEnergy(float amount) {
        this.extraEnergy += amount;
    }

    @Override
    public void resetExtraEnergy() {
        this.extraEnergy = 0.0F;
        this.extraEnergy = Math.min(this.maxEnergy*0.25F, this.extraEnergy);
    }

    @Override
    public void onBlackFlash() {
        this.lastBlackFlashTime = this.owner.level().getGameTime();

        this.output = this.getMaximumOutput();
        this.energy += Math.max(750.0f, (this.getMaxEnergy() - (this.energy * 2.0f)) * 0.2f);
        if (this.owner instanceof ServerPlayer player) {
            PlayerUtil.giveAdvancement(player, "black_flash");
        }
    }

    @Override
    public long getLastBlackFlashTime() {
        return this.lastBlackFlashTime;
    }

    @Override
    public boolean addBlackFlash() {
        return this.addBlackFlash;
    }

    @Override
    public void moreBlackFlash(boolean bool) {
        this.addBlackFlash = bool;
    }

    @Override
    public void setWombAwakened(boolean bool) {
        this.hasWombAwakened = bool;
    }

    @Override
    public boolean checkWombAwakened() {
        return this.hasWombAwakened;
    }

    @Override
    public void resetBlackFlash() {
        this.lastBlackFlashTime = -1;
    }

    @Override
    public boolean isInZone() {
        return this.lastBlackFlashTime != -1 && ((this.owner.level().getGameTime() - this.lastBlackFlashTime) / 20) < (75);
    }

    @Override
    public void delayTickEvent(Runnable task, int delay) {
        this.delayedTickEvents.add(new DelayedTickEvent(task, delay));
    }

    @Override
    public void uncopy(CursedTechnique technique) {
        this.copied.remove(technique);
    }

    @Override
    public void unsteal(CursedTechnique technique) {
        this.stolen.remove(technique);
    }

    @Override
    public void resetCopy() {
        this.copied = new LinkedHashSet<>();
        this.sync();
    }


     @Override
    public void resetSteal() {
        this.stolen = new LinkedHashSet<>();
        this.sync();
    }

    @Override
    public void copy(@Nullable CursedTechnique technique) {
        this.copied.add(technique);
    }

    @Override
    public void setCopies(Set<CursedTechnique> copiedTechs) {
        this.copied = copiedTechs;
    } 

     @Override
    public void steal(@Nullable CursedTechnique technique) {
        this.stolen.add(technique);
    }



    @Override
    public Set<CursedTechnique> getCopied() {
        if (!this.hasToggled(JJKAbilities.RIKA.get()) || !this.hasTechnique(CursedTechnique.MIMICRY)) {
            return Set.of();
        }
        return this.copied;
    }

    @Override
    public Set<CursedTechnique> getStolen() {
        if (!this.hasTechnique(CursedTechnique.BRAIN_TRANSPLANT)) {
            return Set.of();
        }
        return this.stolen;
    }

    @Override
    public void setStolen(Set<CursedTechnique> stolenTechs) {
        this.stolen = stolenTechs;
    } 

    @Override
    public CursedTechnique getLastStolen() {
        if (stolen.isEmpty()) return null;
        CursedTechnique last = null;
        for (CursedTechnique tech : stolen) {
            last = tech; 
        }
        return last;
    }


    @Override
    public CursedTechnique getLatestStolen() {
        if (stolen.isEmpty()) 
            return null;
        return stolen.iterator().next();
    }

   @Override
    public void addStolen(CursedTechnique technique) {
        
        if (stolen.contains(technique)) {
            if (this.owner.level().isClientSide()) { 
                PacketHandler.sendToServer(new UnstealAbilityC2SPacket(technique));
            }
           
            stolen.remove(technique);
        }
        stolen.add(technique);
        if (stolen.size() > ConfigHolder.SERVER.maximumStolenTechniques.get()) { 
            CursedTechnique first = stolen.iterator().next(); 
            if (this.owner.level().isClientSide()) { 
                PacketHandler.sendToServer(new UnstealAbilityC2SPacket(first));
            }
            stolen.remove(first); 
        }
    }

    @Override
    public boolean hasStolen(CursedTechnique technique) {
        if (stolen.contains(technique)) {
            return true;
        }
        return false;
    }

    @Override
    public void setCurrentCopied(@Nullable CursedTechnique technique) {
        this.currentCopied = this.currentCopied == technique ? null : technique;
        this.sync();
    }

    @Override
    public void setCurrentStolen(@Nullable CursedTechnique technique) {
        this.currentStolen = this.currentStolen == technique ? null : technique;
        this.sync();
    }

    @Override
    public @Nullable CursedTechnique getCurrentCopied() {
        if (!this.toggled.contains(JJKAbilities.RIKA.get())) {
            return null;
        }
        return this.currentCopied;
    }

    @Override
    public @Nullable CursedTechnique getCurrentStolen() {
         if (this.getTechnique() != CursedTechnique.BRAIN_TRANSPLANT && !this.getCopied().contains(CursedTechnique.BRAIN_TRANSPLANT) ) {
            return null;
        }
        return this.currentStolen;
    }

    @Override
    public void absorb(@Nullable CursedTechnique technique) {
        this.absorbed.add(technique);
    }

    @Override
    public void unabsorb(CursedTechnique technique) {
        this.absorbed.remove(technique);
        this.currentAbsorbed = null;
    }

    @Override
    public Set<CursedTechnique> getAbsorbed() {
        if (!this.hasTechnique(CursedTechnique.CURSE_MANIPULATION)) {
            return Set.of();
        }
        return this.absorbed;
    }

    @Override
    public void setCurrentAbsorbed(@Nullable CursedTechnique technique) {
        this.currentAbsorbed = this.currentAbsorbed == technique ? null : technique;
        this.sync();
    }

    @Override
    public @Nullable CursedTechnique getCurrentAbsorbed() {
        if (!this.hasTechnique(CursedTechnique.CURSE_MANIPULATION)) {
            return null;
        }
        return this.currentAbsorbed;
    }

    @Override
    public int getTransfiguredSouls() {
        return this.transfiguredSouls;
    }

    @Override
    public void increaseTransfiguredSouls() {
        this.transfiguredSouls++;
    }

    @Override
    public void decreaseTransfiguredSouls() {
        this.transfiguredSouls--;
    }

    @Override
    public void useTransfiguredSouls(int amount) {
        this.transfiguredSouls -= amount;
    }

    @Override
    public @Nullable Ability getChanneled() {
        return this.channeled;
    }

    @Override
    public void channel(@Nullable Ability ability) {
        if (this.channeled != null) {
            ((Ability.IChannelened) this.channeled).onStop(this.owner);

            if (this.channeled instanceof Ability.ICharged charged) {
                if (charged.onRelease(this.owner)) {
                    this.channeled.charge(this.owner);
                }
            }
            if (!this.owner.level().isClientSide && this.channeled.shouldLog(this.owner)) {
                this.owner.sendSystemMessage(this.channeled.getDisableMessage());
            }
              // this.channeled.cooldown(this.owner);
            MinecraftForge.EVENT_BUS.post(new AbilityStopEvent(this.owner, ability));
        }

        if (this.channeled == ability) {
            this.channeled = null;
        } else {
            this.channeled = ability;
           
            if (this.channeled != null) {
                  ((Ability.IChannelened) this.channeled).onStart(this.owner);
                if (!this.owner.level().isClientSide && this.channeled.shouldLog(this.owner)) {
                    this.owner.sendSystemMessage(this.channeled.getEnableMessage());
                }
            }
        }
        this.sync();
    }

    @Override
    public boolean isChanneling(Ability ability) {
        return this.channeled == ability;
    }

    @Override
    public int getCharge(Ability ability) {
         return this.channeled == ability ? this.charge : 0;
    }

    // @Override
    // public void unlockDomain() {
    //     if (this.getTechnique() != null && this.getTechnique().getDomain() != null) {
    //         this.unlock(this.getTechnique().getDomain());
    //     }
    // }

    @Override
    public void removeLife() {
        this.lives -= 1;
        if (ConfigHolder.SERVER.livesconfig.get() > 0 && this.owner instanceof ServerPlayer play ) {
            if (this.lives <= 0) {
                this.wipe(play);
            }
            else {
                owner.sendSystemMessage(Component.translatable(String.format("chat.%s.lives", JujutsuKaisen.MOD_ID), this.lives ));
            }
            
        }
    }
    

    @Override 
    public void wipe(ServerPlayer owner) {
        this.setExperience(0.0F);
        PlayerUtil.removeAdvancement(owner, "six_eyes");
        PlayerUtil.removeAdvancement(owner, "heavenly_restriction_physical");
        PlayerUtil.removeAdvancement(owner, "heavenly_restriction_ce");
        PlayerUtil.removeAdvancement(owner, "vessel");
        PlayerUtil.removeAdvancement(owner, "perfect_body");
        this.resetCopy();
        this.setCurrentCopied(null);
        this.clearToggled();
        this.resetSteal();
        this.lockAll();
        this.setPoints(0);
        this.generate(owner);
    }

    @Override
    public int getLives() {
        return this.lives;
    }

    @Override
    public void setLives(int count) {
        this.lives = count;
    }

    @Override
    public void addSummon(Entity entity) {
        this.summons.add(new SummonData(entity));
    }

    @Override
    public void removeSummon(Entity entity) {
        this.summons.removeIf(data -> data.getIdentifier().equals(entity.getUUID()));
    }

        @Override
    public List<Entity> getSummons() {
        if (!(this.owner.level() instanceof ServerLevel level)) return List.of();

        List<Entity> entities = new ArrayList<>();

        for (SummonData data : this.summons) {
            Entity entity = level.getEntity(data.getIdentifier());

            if (entity == null) continue;

            entities.add(entity);
        }
        return entities;
    }

     @Override
    public <T extends Entity> @Nullable T getSummonByClass(Class<T> clazz) {
        if (!(this.owner.level() instanceof ServerLevel level)) return null;

        EntityTypeTest<Entity, T> test = EntityTypeTest.forClass(clazz);

        for (SummonData data : this.summons) {
            Entity entity = level.getEntity(data.getIdentifier());

            if (entity == null) continue;

            T summon = test.tryCast(entity);

            if (summon != null) {
                return summon;
            }
        }
        return null;
    }

    @Override
    public <T extends Entity> List<T> getSummonsByClass(Class<T> clazz) {
        if (!(this.owner.level() instanceof ServerLevel level)) return List.of();

        List<T> entities = new ArrayList<>();

        EntityTypeTest<Entity, T> test = EntityTypeTest.forClass(clazz);

        for (SummonData data : this.summons) {
            Entity entity = level.getEntity(data.getIdentifier());

            if (entity == null) continue;

            T summon = test.tryCast(entity);

            if (summon != null) {
                entities.add(summon);
            }
        }
        return entities;
    }

    @Override
    public <T extends Entity> void unsummonByClass(Class<T> clazz) {
        if (!(this.owner.level() instanceof ServerLevel level)) return;

        EntityTypeTest<Entity, T> test = EntityTypeTest.forClass(clazz);

        Iterator<SummonData> iter = this.summons.iterator();

        while (iter.hasNext()) {
            SummonData data = iter.next();

            Entity entity = level.getEntity(data.getIdentifier());

            if (entity == null) continue;

            T summon = test.tryCast(entity);

            if (summon != null) {
                iter.remove();
                summon.discard();
            }
        }
    }

   @Override
    public <T extends Entity> void removeSummonByClass(Class<T> clazz) {
        if (!(this.owner.level() instanceof ServerLevel level)) return;

        EntityTypeTest<Entity, T> test = EntityTypeTest.forClass(clazz);

        Iterator<SummonData> iter = this.summons.iterator();

        while (iter.hasNext()) {
            SummonData data = iter.next();

            Entity entity = level.getEntity(data.getIdentifier());

            if (entity == null) continue;

            T summon = test.tryCast(entity);

            if (summon != null) {
                iter.remove();
            }
        }
    }
    
    @Override
    public <T extends Entity> boolean hasSummonOfClass(Class<T> clazz) {
        if (!(this.owner.level() instanceof ServerLevel level)) return false;
        EntityTypeTest<Entity, T> test = EntityTypeTest.forClass(clazz);

        for (SummonData data : this.summons) {
            Entity entity = level.getEntity(data.getIdentifier());
            if (entity == null) continue;
            T summon = test.tryCast(entity);
            if (summon != null) {
                return true;
            }
        }

        return false;
    }


    @Override
    public void addCurse(AbsorbedCurse curse) {
        this.curses.add(curse);
    }

    @Override
    public void removeCurse(AbsorbedCurse curse) {
        this.curses.remove(curse);
    }

    @Override
    public List<AbsorbedCurse> getCurses() {
        if (!this.hasTechnique(CursedTechnique.CURSE_MANIPULATION)) return List.of();

        List<AbsorbedCurse> sorted = new ArrayList<>(this.curses);
        return sorted;
    }

    @Override
    public @Nullable AbsorbedCurse getCurse(EntityType<?> type) {
        for (AbsorbedCurse curse : this.getCurses()) {
            if (curse.getType() == type) return curse;
        }
        return null;
    }

    @Override
    public boolean hasCurse(EntityType<?> type) {
        for (AbsorbedCurse curse : this.getCurses()) {
            if (curse.getType() == type) return true;
        }
        return false;
    }

    @Override
    public List<AbstractMap.SimpleEntry<Vec3, Float>> getFrames() {
        return this.frames;
    }

    @Override
    public void addFrame(Vec3 frame, float yaw) {
        this.frames.add(new AbstractMap.SimpleEntry<>(frame, yaw));
    }

    @Override
    public void removeFrame(AbstractMap.SimpleEntry<Vec3, Float> frame) {
        this.frames.remove(frame);
    }

    @Override
    public void resetFrames() {
        this.frames.clear();
    }

    private boolean isIncompatible(Trait newTrait, Set<Trait> existingTraits, List<String> incompatibilities) {
        for (String pair : incompatibilities) {
            String[] split = pair.split(",");
            if (split.length != 2) continue;

            Trait t1 = Trait.valueOf(split[0].trim());
            Trait t2 = Trait.valueOf(split[1].trim());

            if ((newTrait == t1 && existingTraits.contains(t2)) || 
                (newTrait == t2 && existingTraits.contains(t1))) {
                return true;
            }
        }
        return false;
    }

    private boolean isUniqueTraitAllowed(Trait trait) {
        List<Trait> uniqueTraits = ConfigHolder.SERVER.getUniqueTraits();
        return !ConfigHolder.SERVER.uniqueTraits.get() || !uniqueTraits.contains(trait);
    }

    @Override
    public void generate(ServerPlayer owner) {
        this.initialized = true;

        this.technique = null;
        this.hasWombAwakened = false;

        this.nature = CursedEnergyNature.BASIC;
        this.traits.clear();
        // this.traits.remove(Trait.SIX_EYES);
        // this.traits.remove(Trait.HEAVENLY_RESTRICTION);
        // this.traits.remove(Trait.VESSEL);
        // this.traits.remove(Trait.PERFECT_BODY);
        // this.traits.remove(Trait.RCT_OUTPUT);
        // this.traits.remove(Trait.INCARNATED);
        Set<CursedTechnique> taken = new HashSet<>();
        Set<Trait> traits = new HashSet<>();

        if (ConfigHolder.SERVER.uniqueTraits.get()) {
            GameProfileCache cache = owner.server.getProfileCache();

            if (cache == null) throw new NullPointerException();

            for (GameProfileCache.GameProfileInfo info : cache.load()) {
                GameProfile profile = info.getProfile();

                if (profile.getId() == owner.getUUID()) continue;

                ServerPlayer player;

                if ((player = owner.server.getPlayerList().getPlayerByName(profile.getName())) == null) {
                    player = owner.server.getPlayerList().getPlayerForLogin(profile);
                    owner.server.getPlayerList().load(player);
                }

                if (!player.getCapability(SorcererDataHandler.INSTANCE).isPresent()) continue;

                ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                taken.addAll(cap.getTechniques());
                traits.addAll(cap.getTraits());
            }
        }
        if ((isUniqueTraitAllowed(Trait.HEAVENLY_RESTRICTION_CE)  || (!traits.contains(Trait.HEAVENLY_RESTRICTION_PHYSICAL) && !traits.contains(Trait.HEAVENLY_RESTRICTION_CE))) &&
                HelperMethods.RANDOM.nextInt(ConfigHolder.SERVER.heavenlyRestrictionRarity.get()) == 0) {
            this.addTrait(Trait.HEAVENLY_RESTRICTION_CE);
        }
        else if ((isUniqueTraitAllowed(Trait.HEAVENLY_RESTRICTION_PHYSICAL)  || (!traits.contains(Trait.HEAVENLY_RESTRICTION_PHYSICAL) && !traits.contains(Trait.HEAVENLY_RESTRICTION_CE))) &&
            HelperMethods.RANDOM.nextInt(ConfigHolder.SERVER.heavenlyRestrictionRarity.get()) == 0) {
            this.addTrait(Trait.HEAVENLY_RESTRICTION_PHYSICAL);
        } else {
            this.type = HelperMethods.RANDOM.nextInt(ConfigHolder.SERVER.curseRarity.get()) == 0 ? JujutsuType.CURSE : JujutsuType.SORCERER;
           
            List<CursedTechnique> unlockable = ConfigHolder.SERVER.getUnlockableTechniques(type);

            if (ConfigHolder.SERVER.uniqueTechniques.get()) {
                unlockable.removeAll(taken);
            }
            this.technique = unlockable.get(HelperMethods.RANDOM.nextInt(unlockable.size()));
            int rollCount = 0;
            int weightMult = 1;
            
            if (this.technique == CursedTechnique.MYTHICAL_BEAST_AMBER) {
                this.nature = CursedEnergyNature.LIGHTNING;
                rollCount += ConfigHolder.SERVER.natureTraitCost.get();
            }
            else {
                if (HelperMethods.RANDOM.nextInt(ConfigHolder.SERVER.cursedEnergyNatureRarity.get()) == 0) {
                    this.nature = HelperMethods.randomEnum(CursedEnergyNature.class, Set.of(CursedEnergyNature.BASIC));
                }
                if (this.nature != CursedEnergyNature.BASIC) {
                    rollCount += ConfigHolder.SERVER.natureTraitCost.get();
                    weightMult *= ConfigHolder.SERVER.natureTraitModifier.get();
                }
            }
            owner.sendSystemMessage(Component.translatable(String.format("chat.%s.nature", JujutsuKaisen.MOD_ID), this.nature.getName()));
           
            
          
            Map<Trait, Integer> weights = ConfigHolder.SERVER.getTraits(type);
            //weights.put(null, ConfigHolder.SERVER.noTraitWeight.get());
       

            while (rollCount < ConfigHolder.SERVER.traitRolls.get() && !weights.isEmpty()) {
                for (Map.Entry<Trait, Integer> entry : weights.entrySet()) {
                    weights.put(entry.getKey(), (int) (entry.getValue() / weightMult));
                }
                if (rollCount < ConfigHolder.SERVER.minTraits.get() ) {
                    weights.remove(null);
                } else {
                    weights.put(null, ConfigHolder.SERVER.noTraitWeight.get() * weightMult);
                }
                
                int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();
                int roll = HelperMethods.RANDOM.nextInt(totalWeight);
                int cumulative = 0;
                Trait chosen = null;
               

                for (Map.Entry<Trait, Integer> entry : weights.entrySet()) {
                    cumulative += entry.getValue();
                    if (roll < cumulative) {
                        chosen = entry.getKey();
                        break;
                    }
                }
                List<String> incompatibilities = ConfigHolder.SERVER.incompatibleTraits.get();
                if (chosen != null && !isIncompatible(chosen, traits, incompatibilities) && (isUniqueTraitAllowed(chosen) || !traits.contains(chosen))  && rollCount < ConfigHolder.SERVER.maxTraits.get() && !this.hasTrait(chosen) ) {
                    this.addTrait(chosen);
                    rollCount++;
                    weightMult *= ConfigHolder.SERVER.traitScalingModifier.get();
                }
                else if (chosen == null) {
                    rollCount++;
                }
            }

            
            
            // while(traitCount < ConfigHolder.SERVER.maxTraits.get() )
            // if ((!ConfigHolder.SERVER.uniqueTraits.get() || !traits.contains(Trait.VESSEL)) && this.type == JujutsuType.SORCERER &&
            //         HelperMethods.RANDOM.nextInt(ConfigHolder.SERVER.vesselRarity.get()) == 0) {
            //     this.addTrait(Trait.VESSEL);
            //     traitCount += 1;
            // }

            // if ((!ConfigHolder.SERVER.uniqueTraits.get() || !traits.contains(Trait.SIX_EYES)) && this.type == JujutsuType.SORCERER  &&
            //         HelperMethods.RANDOM.nextInt(ConfigHolder.SERVER.sixEyesRarity.get()) == 0) {
            //     this.addTrait(Trait.SIX_EYES);
            //     traitCount += 1;
            // }

            // if ((!ConfigHolder.SERVER.uniqueTraits.get() || !traits.contains(Trait.PERFECT_BODY)) &&
            //         HelperMethods.RANDOM.nextInt(ConfigHolder.SERVER.perfectBodyRarity.get()) == 0) {
            //     this.addTrait(Trait.PERFECT_BODY);
            //     traitCount += 1;
            // }

            // if ((!ConfigHolder.SERVER.uniqueTraits.get() || !traits.contains(Trait.RCT_OUTPUT)) &&
            //         HelperMethods.RANDOM.nextInt(ConfigHolder.SERVER.rctOutputRarity.get()) == 0) {
            //     this.addTrait(Trait.RCT_OUTPUT);
            //     traitCount += 1;
            // }

            // if ((!ConfigHolder.SERVER.uniqueTraits.get() || !traits.contains(Trait.INCARNATED)) &&
            //         HelperMethods.RANDOM.nextInt(ConfigHolder.SERVER.incarnatedRarity.get()) == 0) {
            //     this.addTrait(Trait.INCARNATED);
            //     traitCount += 1;
            // }
        

            assert this.technique != null;

            if (this.technique == null || this.technique == CursedTechnique.TECHNIQUELESS) {
                owner.sendSystemMessage(Component.translatable(String.format("chat.%s.technique.none", JujutsuKaisen.MOD_ID)));
            }
            else {
                 owner.sendSystemMessage(Component.translatable(String.format("chat.%s.technique", JujutsuKaisen.MOD_ID), this.technique.getName()));
            }
 
            for (Trait t : this.traits ) {
                  owner.sendSystemMessage(Component.translatable(String.format("chat.%s.trait.%s", JujutsuKaisen.MOD_ID, t.getRawName())));
            }
          
            if (this.type == JujutsuType.CURSE) {
                owner.sendSystemMessage(Component.translatable(String.format("chat.%s.curse", JujutsuKaisen.MOD_ID)));
            } else {
                owner.sendSystemMessage(Component.translatable(String.format("chat.%s.sorcerer", JujutsuKaisen.MOD_ID)));
            }
        }

        this.energy = this.traits.contains(Trait.HEAVENLY_RESTRICTION_CE) ? 0.0F : this.getMaxEnergy();
        if (ConfigHolder.SERVER.livesconfig.get() > 0) {
            owner.sendSystemMessage(Component.translatable(String.format("chat.%s.lives", JujutsuKaisen.MOD_ID), this.lives ));
        }
        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(this.serializeNBT()), owner);
    }

    @Override
    public int getSpeedStacks() {
        return this.speedStacks;
    }

    public int getDash() {
        return this.dashes;
    }

    public void addDash() {
        this.dashes++;
    }

    public void subDash() {
        this.dashes--;
    }

    public void resetDash() {
        this.dashes = 0;
    }

    @Override
    public void addSpeedStack() {
        this.noMotionTime = 0;
        this.speedStacks = Math.min(JJKConstants.MAX_PROJECTION_SORCERY_STACKS, this.speedStacks + 1);
    }

    @Override
    public void resetSpeedStacks() {
        this.speedStacks = 0;
        this.noMotionTime = 0;
    }

    @Override
    public void lowerSpeedStacks() {
       this.speedStacks = Math.min(0, this.speedStacks - 1);
        this.noMotionTime = 0;
    }

    @Override
    public int getFingers() {
        return this.fingers;
    }

    @Override
    public void setFingers(int count) {
        this.fingers = count;
    }

    @Override
    public int addFingers(int count) {
        int real = Math.min(count, 20 - this.fingers);
        this.fingers += real;
        return real;
    }

    public boolean hasToggled(Ability ability) {
        return this.toggled.contains(ability)  && !this.disrupted.containsKey(ability);
    }

    @Override
    public @Nullable CursedTechnique getAdditional() {
        return this.additional;
    }

    @Override
    public void setAdditional(@Nullable CursedTechnique technique) {
        this.additional = technique;
        this.sync();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("initialized", this.initialized);
        nbt.putInt("cursed_energy_color", this.cursedEnergyColor);
        nbt.putInt("points", this.points);
        nbt.putFloat("domain_size", this.domainSize);

        if (this.technique != null) {
            nbt.putInt("technique", this.technique.ordinal());
        }
        if (this.additional != null) {
            nbt.putInt("additional", this.additional.ordinal());
        }
        if (this.currentCopied != null) {
            nbt.putInt("current_copied", this.currentCopied.ordinal());
        }
        if (this.currentStolen != null) {
            nbt.putInt("current_stolen", this.currentStolen.ordinal());
        }
        if (nbt.contains("current_absorbed")) {
            this.currentAbsorbed = CursedTechnique.values()[nbt.getInt("current_absorbed")];
        }
        nbt.putInt("transfigured_souls", this.transfiguredSouls);
        nbt.putInt("nature", this.nature.ordinal());
        nbt.putFloat("experience", this.experience);
        nbt.putFloat("output", this.output);
        nbt.putFloat("energy", this.energy);
        nbt.putFloat("max_energy", this.maxEnergy);
        nbt.putFloat("extra_energy", this.extraEnergy);
        nbt.putFloat("additional_energy", this.additionalEnergy);
        nbt.putInt("type", this.type.ordinal());
        nbt.putInt("burnout", this.burnout);
        nbt.putInt("disable", this.disable);
        nbt.putInt("disarmed", this.disarmed);
        nbt.putInt("silenced", this.silenced);
        nbt.putInt("self_hit", this.selfHit);
        nbt.putInt("throat_damage", this.throatDamage);
        nbt.putInt("brain_damage", this.brainDamage);
        nbt.putInt("brain_damage_timer", this.brainDamageTimer);
       // nbt.putInt("charge", this.charge);
        nbt.putLong("last_black_flash_time", this.lastBlackFlashTime);
        nbt.putInt("speed_stacks", this.speedStacks);
        nbt.putInt("fingers", this.fingers);
        nbt.putInt("lives", this.lives);
        nbt.putBoolean("hasWombAwakened", this.hasWombAwakened);
        // if (this.stolenSkinProfile != null) {
        //     CompoundTag prof = new CompoundTag();
        //     NbtUtils.writeGameProfile(prof, this.stolenSkinProfile);
        //     nbt.put("stolen_profile", prof);
        // }

        ListTag unlockedTag = new ListTag();

        for (Ability ability : this.unlocked) {
            ResourceLocation key = JJKAbilities.getKey(ability);

            if (key == null) continue;

            unlockedTag.add(StringTag.valueOf(key.toString()));
        }
        nbt.put("unlocked", unlockedTag);

        ListTag copiedTag = new ListTag();

        for (CursedTechnique technique : this.copied) {
            copiedTag.add(IntTag.valueOf(technique.ordinal()));
        }
        nbt.put("copied", copiedTag);

         ListTag stolenTag = new ListTag();

        for (CursedTechnique technique : this.stolen) {
            stolenTag.add(IntTag.valueOf(technique.ordinal()));
        }
        nbt.put("stolen", stolenTag);




        ListTag absorbedTag = new ListTag();

        for (CursedTechnique technique : this.absorbed) {
            absorbedTag.add(IntTag.valueOf(technique.ordinal()));
        }
        nbt.put("absorbed", absorbedTag);

        ListTag toggledTag = new ListTag();

        for (Ability ability : this.toggled) {
            ResourceLocation key = JJKAbilities.getKey(ability);

            if (key == null) continue;

            toggledTag.add(StringTag.valueOf(key.toString()));
        }
        nbt.put("toggled", toggledTag);

        ListTag traitsTag = new ListTag();

        for (Trait trait : this.traits) {
            traitsTag.add(IntTag.valueOf(trait.ordinal()));
        }
        nbt.put("traits", traitsTag);

        ListTag cooldownsTag = new ListTag();

        for (Map.Entry<Ability, Integer> entry : this.cooldowns.entrySet()) {
            ResourceLocation key = JJKAbilities.getKey(entry.getKey());

            if (key == null) continue;

            CompoundTag data = new CompoundTag();
            data.putString("identifier", key.toString());
            data.putInt("cooldown", entry.getValue());
            cooldownsTag.add(data);
        }
        nbt.put("cooldowns", cooldownsTag);

       
        ListTag summonsTag = new ListTag();

        for (SummonData data : this.summons) {
            summonsTag.add(data.serializeNBT());
        }
        nbt.put("summons", summonsTag);

        ListTag acceptedPactsTag = new ListTag();

        for (Map.Entry<UUID, Set<Pact>> entry : this.acceptedPacts.entrySet()) {
            CompoundTag data = new CompoundTag();
            data.putUUID("recipient", entry.getKey());

            ListTag pacts = new ListTag();

            for (Pact pact : entry.getValue()) {
                pacts.add(IntTag.valueOf(pact.ordinal()));
            }
            data.put("entries", pacts);

            acceptedPactsTag.add(data);
        }
        nbt.put("accepted_pacts", acceptedPactsTag);

        ListTag bindingVowsTag = new ListTag();

        for (BindingVow vow : this.bindingVows) {
            bindingVowsTag.add(IntTag.valueOf(vow.ordinal()));
        }
        nbt.put("binding_vows", bindingVowsTag);

        ListTag bindingVowCooldownsTag = new ListTag();

        for (Map.Entry<BindingVow, Integer> entry : this.bindingVowCooldowns.entrySet()) {
            CompoundTag data = new CompoundTag();
            data.putInt("vow", entry.getKey().ordinal());
            data.putInt("cooldown", entry.getValue());
        }
        nbt.put("binding_vow_cooldowns", bindingVowCooldownsTag);

        ListTag chantsTag = new ListTag();

        for (Map.Entry<Ability, Set<String>> entry : this.chants.entrySet()) {
            ResourceLocation key = JJKAbilities.getKey(entry.getKey());

            if (key == null) continue;

            CompoundTag data = new CompoundTag();
            data.putString("ability", key.toString());

            ListTag chants = new ListTag();

            for (String chant : entry.getValue()) {
                chants.add(StringTag.valueOf(chant));
            }
            data.put("entries", chants);

            chantsTag.add(data);
        }
        nbt.put("chants", chantsTag);

        ListTag cursesTag = new ListTag();

        for (AbsorbedCurse curse : this.curses) {
            cursesTag.add(curse.serializeNBT());
        }
        nbt.put("curses", cursesTag);

        ListTag disruptedTag = new ListTag();

        for (Map.Entry<Ability, Integer> entry : this.disrupted.entrySet()) {
            ResourceLocation key = JJKAbilities.getKey(entry.getKey());

            if (key == null) continue;

            CompoundTag data = new CompoundTag();
            data.putString("identifier", key.toString());
            data.putInt("duration", entry.getValue());
            disruptedTag.add(data);
        }
        nbt.put("disrupted", disruptedTag);

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.initialized = nbt.getBoolean("initialized");

        this.cursedEnergyColor = nbt.getInt("cursed_energy_color");

        this.points = nbt.getInt("points");
        this.domainSize = nbt.getFloat("domain_size");

        if (nbt.contains("technique")) {
            this.technique = CursedTechnique.values()[nbt.getInt("technique")];
        }
        if (nbt.contains("additional")) {
            this.additional = CursedTechnique.values()[nbt.getInt("additional")];
        }
        if (nbt.contains("current_copied")) {
            this.currentCopied = CursedTechnique.values()[nbt.getInt("current_copied")];
        }
         if (nbt.contains("current_stolen")) {
            this.currentStolen = CursedTechnique.values()[nbt.getInt("current_stolen")];
        }
        if (nbt.contains("current_absorbed")) {
            this.currentAbsorbed = CursedTechnique.values()[nbt.getInt("current_absorbed")];
        }
        // if (nbt.contains("stolen_profile")) {
        //     this.stolenSkinProfile = NbtUtils.readGameProfile(nbt.getCompound("stolen_profile"));
        // } else {
        //     this.stolenSkinProfile = null;
        // }
        this.transfiguredSouls = nbt.getInt("transfigured_souls");
        this.nature = CursedEnergyNature.values()[nbt.getInt("nature")];
        this.experience = nbt.getFloat("experience");
        this.output = nbt.getFloat("output");
        this.energy = nbt.getFloat("energy");
        this.maxEnergy = nbt.getFloat("max_energy");
        this.extraEnergy = nbt.getFloat("extra_energy");
        this.additionalEnergy = nbt.getFloat("additional_energy");
        this.type = JujutsuType.values()[nbt.getInt("type")];
        this.burnout = nbt.getInt("burnout");
        this.disable = nbt.getInt("disable");
        this.throatDamage = nbt.getInt("throat_damage");
        this.brainDamage = nbt.getInt("brain_damage");
        this.brainDamageTimer = nbt.getInt("brain_damage_timer");
        //this.charge = nbt.getInt("charge");
        this.lastBlackFlashTime = nbt.getLong("last_black_flash_time");
        this.speedStacks = nbt.getInt("speed_stacks");
        this.fingers = nbt.getInt("fingers");
        this.lives = nbt.getInt("lives");
        if (ConfigHolder.SERVER.livesconfig.get() > 0 && this.lives <= 0) {
            this.lives = ConfigHolder.SERVER.livesconfig.get();
        }
        this.hasWombAwakened = nbt.getBoolean("hasWombAwakened");

        this.unlocked.clear();

        for (Tag tag : nbt.getList("unlocked", Tag.TAG_STRING)) {
            this.unlocked.add(JJKAbilities.getValue(new ResourceLocation(tag.getAsString())));
        }

        this.copied.clear();

        for (Tag tag : nbt.getList("copied", Tag.TAG_INT)) {
            this.copied.add(CursedTechnique.values()[((IntTag) tag).getAsInt()]);
        }

        this.stolen.clear();

        for (Tag tag : nbt.getList("stolen", Tag.TAG_INT)) {
            this.stolen.add(CursedTechnique.values()[((IntTag) tag).getAsInt()]);
        }

        this.absorbed.clear();

        for (Tag tag : nbt.getList("absorbed", Tag.TAG_INT)) {
            this.absorbed.add(CursedTechnique.values()[((IntTag) tag).getAsInt()]);
        }

        this.toggled.clear();

        for (Tag key : nbt.getList("toggled", Tag.TAG_STRING)) {
            this.toggled.add(JJKAbilities.getValue(new ResourceLocation(key.getAsString())));
        }

        this.traits.clear();

        for (Tag key : nbt.getList("traits", Tag.TAG_INT)) {
            this.traits.add(Trait.values()[((IntTag) key).getAsInt()]);
        }

        this.cooldowns.clear();

        for (Tag key : nbt.getList("cooldowns", Tag.TAG_COMPOUND)) {
            CompoundTag data = (CompoundTag) key;
            this.cooldowns.put(JJKAbilities.getValue(new ResourceLocation(data.getString("identifier"))),
                    data.getInt("cooldown"));
        }

        this.disrupted.clear();

        for (Tag key : nbt.getList("disrupted", Tag.TAG_COMPOUND)) {
            CompoundTag data = (CompoundTag) key;
            this.disrupted.put(JJKAbilities.getValue(new ResourceLocation(data.getString("identifier"))),
                    data.getInt("duration"));
        }

        this.summons.clear();

        for (Tag tag : nbt.getList("summons", Tag.TAG_COMPOUND)) {
            this.summons.add(new SummonData((CompoundTag) tag));
        }

        this.acceptedPacts.clear();

        for (Tag key : nbt.getList("accepted_pacts", Tag.TAG_COMPOUND)) {
            CompoundTag data = (CompoundTag) key;

            Set<Pact> pacts = new HashSet<>();

            for (Tag entry : data.getList("entries", Tag.TAG_INT)) {
                pacts.add(Pact.values()[((IntTag) entry).getAsInt()]);
            }
            this.acceptedPacts.put(data.getUUID("recipient"), pacts);
        }

        this.bindingVows.clear();

        for (Tag key : nbt.getList("binding_vows", Tag.TAG_INT)) {
            this.bindingVows.add(BindingVow.values()[((IntTag) key).getAsInt()]);
        }

        this.bindingVowCooldowns.clear();

        for (Tag key : nbt.getList("binding_vow_cooldowns", Tag.TAG_COMPOUND)) {
            CompoundTag data = (CompoundTag) key;
            this.bindingVowCooldowns.put(BindingVow.values()[data.getInt("vow")], data.getInt("cooldown"));
        }

        this.chants.clear();

        for (Tag key : nbt.getList("chants", Tag.TAG_COMPOUND)) {
            CompoundTag data = (CompoundTag) key;

            Set<String> chants = new LinkedHashSet<>();

            for (Tag entry : data.getList("entries", Tag.TAG_STRING)) {
                chants.add(entry.getAsString());
            }
            this.chants.put(JJKAbilities.getValue(new ResourceLocation(data.getString("ability"))), chants);
        }

        this.curses.clear();

        for (Tag key : nbt.getList("curses", Tag.TAG_COMPOUND)) {
            CompoundTag curse = (CompoundTag) key;
            this.curses.add(new AbsorbedCurse(curse));
        }
    }
}
