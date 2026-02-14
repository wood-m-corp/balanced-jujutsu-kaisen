package radon.jujutsu_kaisen.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.VeilHandler;
import radon.jujutsu_kaisen.ability.*;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.block.VeilBlock;
import radon.jujutsu_kaisen.block.VeilRodBlock;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.capability.data.ten_shadows.ITenShadowsData;
import radon.jujutsu_kaisen.capability.data.ten_shadows.TenShadowsDataHandler;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.base.JJKPartEntity;
import radon.jujutsu_kaisen.entity.base.SummonEntity;
import radon.jujutsu_kaisen.entity.curse.base.CursedSpirit;
import radon.jujutsu_kaisen.entity.projectile.ThrownChainProjectile;
import radon.jujutsu_kaisen.entity.sorcerer.HeianSukunaEntity;
import radon.jujutsu_kaisen.entity.sorcerer.SukunaEntity;
import radon.jujutsu_kaisen.entity.sorcerer.base.SorcererEntity;
import radon.jujutsu_kaisen.item.CursedEnergyFleshItem;
import radon.jujutsu_kaisen.item.JJKItems;
import radon.jujutsu_kaisen.item.base.CursedToolItem;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.util.*;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JJKEventHandler {
    @Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class JJKEventHandlerForgeEvents {
        @SubscribeEvent
        public static void onExplosion(ExplosionEvent.Detonate event) {
            Explosion explosion = event.getExplosion();
            LivingEntity instigator = explosion.getIndirectSourceEntity();

            Iterator<BlockPos> iter = explosion.getToBlow().iterator();

            while (iter.hasNext()) {
                BlockPos pos = iter.next();
                Vec3 center = pos.getCenter();
                
                if (!VeilHandler.canDestroy(instigator, event.getLevel(), center.x, center.y, center.z)) {
                    iter.remove();
                }
            }
        }

            @SubscribeEvent
            public static void onEntityTeleport(EntityTeleportEvent event) {
                Level level = event.getEntity().level();
                if (level.isClientSide) return;
                BlockPos from = BlockPos.containing(event.getPrevX(), event.getPrevY(), event.getPrevZ());
                BlockPos to = BlockPos.containing(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        
                if (!VeilHandler.isTeleportValid(level, from) || !VeilHandler.isTeleportValid(level, to) ) {
                    event.setCanceled(true);
                }
                else {
                    ServerLevel serverLevel = (ServerLevel) level;
                    if (!VeilHandler.getDomains(serverLevel, from).isEmpty() || !VeilHandler.getDomains(serverLevel, to).isEmpty()) {
                        event.setCanceled(true);
                    }
                }
            }

        @SubscribeEvent
        public static void onLivingDestroyBlock(LivingDestroyBlockEvent event) {
            LivingEntity entity = event.getEntity();
            Vec3 center = event.getPos().getCenter();
            if (!VeilHandler.canDestroy(event.getEntity(), entity.level(), center.x, center.y, center.z) ) {
                event.setCanceled(true);
            }
        }
        

        @SubscribeEvent
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            Player player = event.getPlayer();
            BlockPos pos = event.getPos();
            Level level = (Level) event.getLevel();
             BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
              if (block instanceof VeilRodBlock || block instanceof VeilBlock) {
                return; 
            }
            Vec3 center = pos.getCenter();
            if (!VeilHandler.canDestroy(player, level, center.x, center.y, center.z, true)) {
                event.setCanceled(true);

                // Optional: re-sync block state to client so it doesn’t look broken
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }


        @SubscribeEvent
        public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
            Entity entity = event.getEntity();
            ServerLevel level = (ServerLevel) event.getLevel();
            BlockPos pos = event.getPos();
            if (!VeilHandler.getDomains(level,pos).isEmpty()) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onSleepFinished(SleepFinishedTimeEvent event) {
            if (!(event.getLevel() instanceof ServerLevel level)) return;

            for (ServerPlayer player : level.players()) {
                if (player.isSleepingLongEnough()) {
                    if (!player.getCapability(SorcererDataHandler.INSTANCE).isPresent()) continue;

                    ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                    if (!cap.hasTrait(Trait.HEAVENLY_RESTRICTION_CE)){
                        cap.setEnergy(cap.getMaxEnergy());
                    }
                    PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
                }
            }
        }

        @SubscribeEvent
        public static void onAttackEntity(AttackEntityEvent event) {
            if (event.getTarget() instanceof JJKPartEntity<?>) {
                Entity parent = ((JJKPartEntity<?>) event.getTarget()).getParent();
                if (parent != null) event.getEntity().attack(parent);
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;

            for (SukunaEntity sukuna : player.level().getEntitiesOfClass(SukunaEntity.class, AABB.ofSize(player.position(),
                    8.0D, 8.0D, 8.0D))) {
                if (sukuna.getOwner() == player) {
                    player.setGameMode(sukuna.getOriginal(player));
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerClone(PlayerEvent.Clone event) {
            Player original = event.getOriginal();
            Player player = event.getEntity();

            original.reviveCaps();

            ISorcererData oldCap = original.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            ISorcererData newCap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            newCap.deserializeNBT(oldCap.serializeNBT());

            if (event.isWasDeath()) {
                if (oldCap.hasTrait(Trait.HEAVENLY_RESTRICTION_CE)){
                    newCap.setEnergy(oldCap.getEnergy());
                }else{
                    newCap.setEnergy(newCap.getMaxEnergy());
                }
                newCap.resetCooldowns();
                newCap.resetBurnout();
                newCap.resetDisable();
                newCap.clearToggled();
                newCap.setCurrentCopied(null);
                newCap.setCurrentStolen(null);
                newCap.resetCopy();
                
                newCap.resetBlackFlash();
                newCap.resetExtraEnergy();
                newCap.resetSpeedStacks();
                newCap.resetDash();
               
                if ( player.getCapability(TenShadowsDataHandler.INSTANCE).isPresent()) {
                    ITenShadowsData shadowCap = player.getCapability(TenShadowsDataHandler.INSTANCE).resolve().orElseThrow();
                    shadowCap.resetAdaptations();
                }
                if (!player.level().isClientSide) {
                    PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(newCap.serializeNBT()), (ServerPlayer) player);
                }
            }
            original.invalidateCaps();
        }

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent event) {
            LivingEntity victim = event.getEntity();

            if (!(event.getSource().getEntity() instanceof LivingEntity owner)) return;

            if (!owner.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return;
            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            if (cap.hasTrait(Trait.DEATH_PAINTING) && owner.getHealth() < owner.getMaxHealth() * 0.3F && HelperMethods.isMelee(event.getSource())  ) {
                victim.addEffect(new MobEffectInstance(MobEffects.POISON, 10 * 20, 4));
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10 * 20, 0));
            }
            // If the target is dead we should not trigger any IAttack's
            if (victim.getHealth() - event.getAmount() <= 0.0F) return;

            cap.attack(event.getSource(), victim);

            // If the target died from the IAttack's then cancel (yes this is very scuffed lmao)
            if (victim.isDeadOrDying()) event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onLivingTick(LivingEvent.LivingTickEvent event) {
            LivingEntity owner = event.getEntity();

            if (owner.isDeadOrDying()) return;

            if (!owner.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return;
            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            cap.tick(owner);

            if ((cap.hasTrait(Trait.SIX_EYES) && (!owner.getItemBySlot(EquipmentSlot.HEAD).is(JJKItems.BLINDFOLD.get()) && !CuriosUtil.findSlot(owner, "head").is(JJKItems.BLINDFOLD.get()) ) ) || cap.hasTrait(Trait.HEAVENLY_RESTRICTION_PHYSICAL)) {
                owner.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, false, false));
            }

            if((cap.hasTrait(Trait.DEATH_PAINTING)) && owner.getHealth() / owner.getMaxHealth() < 0.3F ) {
                double x = owner.getX() + (owner.getRandom().nextDouble() - 0.5D) * owner.getBbWidth();
                double y = owner.getY() + owner.getBbHeight() * 0.5 + (owner.getRandom().nextDouble() - 0.5D) * 0.5;
                double z = owner.getZ() + (owner.getRandom().nextDouble() - 0.5D) * owner.getBbWidth();
                owner.level().addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()),
                    x, y, z,
                    0, 0.05, 0
                );
            }
            if (!owner.level().isClientSide) {
            if (cap.hasToggled(JJKAbilities.SHRINK.get())) {
                  ScaleData baseScale = ScaleTypes.BASE.getScaleData(owner);
                    float targetScale = 0.5F;
                    float currentScale = baseScale.getScale();
                    float newScale = currentScale + (targetScale - currentScale) * 0.1F;
                    //if (baseScale.getScale() != targetScale ) {
                        baseScale.setScale(newScale);
                        baseScale.markForSync(true);
                    //}
            }
            else if((cap.hasTrait(Trait.HEAVENLY_RESTRICTION_CE) )) {
                if (owner instanceof Player) {
                    if (cap != null) {
                        float targetScale = 1.0F;
                        float targetWidth = 0.9F;
                        ScaleData baseScale = ScaleTypes.BASE.getScaleData(owner);
                        ScaleData baseWidth = ScaleTypes.WIDTH.getScaleData(owner);
                        baseScale.setScale(targetScale);
                        baseWidth.setScale(targetWidth);
                    }
                }
            }
            else if((cap.hasTrait(Trait.CURSED_WOMB) )) {
                if (owner instanceof Player) {
                if (cap != null) {
                float targetScale = 0.8F;
                float targetWidth = 1.2F;
                if (cap.checkWombAwakened() ==  true) {
                    targetScale = 1.1F;
                    targetWidth = 1.0F;
                }
                ScaleData baseScale = ScaleTypes.BASE.getScaleData(owner);
                ScaleData baseWidth = ScaleTypes.WIDTH.getScaleData(owner);
                // float currentScale = baseScale.getScale();
                // float currentWidth = baseWidth.getScale();
                baseScale.setScale(targetScale);
                baseWidth.setScale(targetWidth);

                }
            }
            }
            else {
                ScaleData baseScale = ScaleTypes.BASE.getScaleData(owner);
                ScaleData baseWidth = ScaleTypes.WIDTH.getScaleData(owner);
                if (!baseScale.isReset() || !baseWidth.isReset()) {
                    baseWidth.resetScale(true);
                    baseScale.resetScale(true);
                }
            }
            }
            

            if (cap.hasTrait(Trait.HEAVENLY_RESTRICTION_PHYSICAL)) {
                owner.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 2, 1, false, false, false));
            }
            if (!cap.hasTrait(Trait.HEAVENLY_RESTRICTION_CE)) {
                owner.addEffect(new MobEffectInstance(MobEffects.JUMP, 2, 2, false, false, false));
            }
            if (owner instanceof Player player) {
                if ( (cap.getType() == JujutsuType.SORCERER && ConfigHolder.SERVER.sorcererSaturation.get()) || (cap.getType() == JujutsuType.CURSE && ConfigHolder.SERVER.curseSaturation.get()) ) {
                    player.getFoodData().setFoodLevel(20);
                }
               
            }

            if (!owner.getCapability(TenShadowsDataHandler.INSTANCE).isPresent()) return;
            ITenShadowsData shadowCap = owner.getCapability(TenShadowsDataHandler.INSTANCE).resolve().orElseThrow();
            shadowCap.tick(owner);
        }

        @SubscribeEvent
        public static void onLivingFall(LivingFallEvent event) {
            
            LivingEntity victim = event.getEntity();

            event.getEntity().getCapability(SorcererDataHandler.INSTANCE).ifPresent(cap -> {
                if (cap.hasTrait(Trait.HEAVENLY_RESTRICTION_PHYSICAL)) {
                    event.setDistance(event.getDistance() * 0.1F);
                } else if (cap.hasTrait(Trait.HEAVENLY_RESTRICTION_CE)) {
                    event.setDistance(event.getDistance() * 1.5F);
                } else {
                    event.setDistance(event.getDistance() * 0.33F);
                }
            });
        }

        @SubscribeEvent
        public static void onLivingAttack(LivingAttackEvent event) {
            DamageSource source = event.getSource();

            if (!(source.getEntity() instanceof LivingEntity attacker)) return;

            LivingEntity victim = event.getEntity();

            if (victim.level().isClientSide) return;

            if (ConfigHolder.SERVER.realisticCurses.get()) {
                ItemStack stack = source.getDirectEntity() instanceof ThrownChainProjectile chain ? chain.getStack() : attacker.getItemInHand(InteractionHand.MAIN_HAND);

                List<Item> stacks = new ArrayList<>();
                stacks.add(stack.getItem());
                stacks.addAll(CuriosUtil.findSlots(attacker, attacker.getMainArm() == HumanoidArm.RIGHT ? "right_hand" : "left_hand")
                        .stream().map(ItemStack::getItem).toList());
                if (!victim.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return;
                ISorcererData cap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                if (cap.getType() == JujutsuType.CURSE && !cap.hasTrait(Trait.DEATH_PAINTING) ) {
                    boolean cursed = false;

                    if (event.getSource() instanceof JJKDamageSources.JujutsuDamageSource) {
                        cursed = true;
                    } else if (HelperMethods.isMelee(source) && (stacks.stream().anyMatch(item -> item instanceof CursedToolItem))) {
                        cursed = true;
                    } else if (attacker.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
                       // ISorcererData attackerCap = attacker.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                        cursed = true;
                    }

                    if (!cursed) {
                        event.setCanceled(true);
                    }
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onLivingHurt(LivingHurtEvent event) {
            LivingEntity victim = event.getEntity();
            

            if (victim.level().isClientSide) return;
            
            DamageSource source = event.getSource();
           

            // Your own cursed energy doesn't do as much damage
            if (source instanceof JJKDamageSources.JujutsuDamageSource) {
                if (!(source.getEntity() instanceof LivingEntity sourceUser)) return;
                ISorcererData capSelf = sourceUser.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                if (source.getEntity() == victim && !capSelf.hasSelfHit() ) {
                    event.setAmount(event.getAmount() * 0.1F);
                }
            }
            else {
                if (source.getEntity() instanceof LivingEntity sourceUser && HelperMethods.isMelee(source) && sourceUser.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
                        ISorcererData attackerCap = sourceUser.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                        if (attackerCap.getEnergy() <= 0.0F) {  
                            ItemStack stack = source.getDirectEntity() instanceof ThrownChainProjectile chain ? chain.getStack() : sourceUser.getItemInHand(InteractionHand.MAIN_HAND);
                            List<Item> stacks = new ArrayList<>();
                            stacks.add(stack.getItem());
                            stacks.addAll(CuriosUtil.findSlots(sourceUser, sourceUser.getMainArm() == HumanoidArm.RIGHT ? "right_hand" : "left_hand")
                            .stream().map(ItemStack::getItem).toList());
                            if (!(stacks.stream().anyMatch(item -> item instanceof CursedToolItem))) {
                                 ISorcererData victimCap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);
                            if (victimCap != null && victimCap.getType() == JujutsuType.CURSE && !victimCap.hasTrait(Trait.DEATH_PAINTING)) {
                                event.setAmount(0.0F);
                            }
                            }
                        }
                    }
            }
            Entity attackerEntity = source.getEntity();
if (attackerEntity instanceof Projectile projectile) {
    attackerEntity = projectile.getOwner();
}

if (!(attackerEntity instanceof LivingEntity attacker)) return;
if (!(attackerEntity instanceof Player) && attackerEntity instanceof CursedSpirit ) {
    event.setAmount(event.getAmount() * ConfigHolder.SERVER.curseDamageMult.get().floatValue() );
}
if (!(victim instanceof Player) && victim instanceof CursedSpirit ) {
    event.setAmount(event.getAmount() * ConfigHolder.SERVER.curseDefenseMult.get().floatValue() );
}

if (!(attackerEntity instanceof Player) && attackerEntity instanceof SorcererEntity sorc && sorc.getJujutsuType() == JujutsuType.SORCERER ) {
    event.setAmount(event.getAmount() * ConfigHolder.SERVER.sorcererDamageMult.get().floatValue() );
}
if (!(victim instanceof Player) && victim instanceof SorcererEntity sorc && sorc.getJujutsuType() == JujutsuType.SORCERER ) {
    event.setAmount(event.getAmount() * ConfigHolder.SERVER.sorcererDefenseMult.get().floatValue() );
}
if (attackerEntity instanceof Player || (attackerEntity instanceof SummonEntity sum && sum.isTame() && sum.getOwner() instanceof Player) || (attackerEntity instanceof CursedSpirit curse && curse.isTame() && curse.getOwner() instanceof Player ) ) {
    event.setAmount(event.getAmount() * ConfigHolder.SERVER.playerDamageMult.get().floatValue());
}

if (JJKAbilities.hasTrait(attacker, Trait.PERFECT_BODY)) {
    attacker.getCapability(SorcererDataHandler.INSTANCE).ifPresent(capSelf -> {
        if (HelperMethods.isMelee(source) && !capSelf.hasToggled(JJKAbilities.HOLLOW_WICKER_BASKET.get())) {
            event.setAmount(event.getAmount() * ConfigHolder.SERVER.perfectBodyMult.get().floatValue() );
        }
    });
}


            if (victim.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
                ISorcererData victimcap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            if (victimcap != null && victimcap.hasExtraMeleeTaken() && HelperMethods.isMelee(source)) {
                event.setAmount(event.getAmount() * 1.35F);
            }
            }
            

            if (source.is(DamageTypeTags.BYPASSES_ARMOR)) return;

            if (!victim.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return;

            

            ISorcererData cap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            float armor = SorcererUtil.getDefense(cap.getExperience());
      
            if (victim instanceof Player) {
                armor*=ConfigHolder.SERVER.jujutsuDefenseMult.get().floatValue();
            }
            if (cap.hasTrait(Trait.HEAVENLY_RESTRICTION_PHYSICAL)) {
		        armor = SorcererUtil.getDefenseHR(cap.getExperience());
                if (victim instanceof Player) {
                   armor*=ConfigHolder.SERVER.hrDefenseMult.get().floatValue();
                }
            }

            if (!(victim instanceof Player) && (!(attackerEntity instanceof Player)) ) {
                if (!(victim instanceof CursedSpirit curse && curse.isTame())) {
                    if (!(victim instanceof SummonEntity summon && summon.isTame())) {
                        if (!(attackerEntity instanceof CursedSpirit curse && curse.isTame())) {
                            if (!(attackerEntity instanceof SummonEntity summon && summon.isTame())) {
                                event.setAmount(event.getAmount() * ConfigHolder.SERVER.npcvsnpcDamageMult.get().floatValue());
                            }
                        }
                        }
                    }
                }
          

            float blocked = event.getAmount()/armor;
            event.setAmount(blocked);
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            LivingEntity victim = event.getEntity();

            if (victim.level().isClientSide) return;

            if (!victim.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return;
            ISorcererData victimCap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            switch (victimCap.getType()) {
                case SORCERER -> {
                    if (HelperMethods.RANDOM.nextInt(ConfigHolder.SERVER.sorcererFleshRarity.get()) == 0) {
                        ItemStack stack = new ItemStack(JJKItems.SORCERER_FLESH.get());
                        CursedEnergyFleshItem.setGrade(stack, SorcererUtil.getGrade(victimCap.getExperience()));
                        victim.spawnAtLocation(stack);
                    }
                }
                case CURSE -> {
                    if (HelperMethods.RANDOM.nextInt(ConfigHolder.SERVER.curseFleshRarity.get()) == 0) {
                        ItemStack stack = new ItemStack(JJKItems.CURSE_FLESH.get());
                        CursedEnergyFleshItem.setGrade(stack, SorcererUtil.getGrade(victimCap.getExperience()));
                        victim.spawnAtLocation(stack);
                    }
                }
            }
            if (victimCap.hasTrait(Trait.DEATH_PAINTING)) {
            if (victim.level() instanceof ServerLevel serverLevel) {
                for (ServerPlayer player : serverLevel.players()) {
                    if ( player == victim || !player.getCapability(SorcererDataHandler.INSTANCE).isPresent()) continue;
                        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                        if (cap.hasTrait(Trait.DEATH_PAINTING)) {
                             player.sendSystemMessage(Component.translatable(String.format("chat.%s.siblingdeath", JujutsuKaisen.MOD_ID), victim.getName()));
                        }
                }
            }
            }

            DamageSource source = event.getSource();

            if (!(source.getEntity() instanceof LivingEntity attacker)) return;

            if (attacker instanceof ServerPlayer player) {
                if (victim instanceof HeianSukunaEntity && victimCap.getFingers() == 20) {
                    PlayerUtil.giveAdvancement(player, "the_strongest_of_all_time");
                }
            }
        }

        @SubscribeEvent
        public static void onAbilityStop(AbilityStopEvent event) {
            Ability ability = event.getAbility();

            CursedTechnique technique = JJKAbilities.getTechnique(ability);

            LivingEntity owner = event.getEntity();

            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            // Handling removal of absorbed techniques from curse manipulation
            if (technique != null && cap.getAbsorbed().contains(technique)) {
                cap.unabsorb(technique);
            }
        }

        @SubscribeEvent
        public static void onAbilityTrigger(AbilityTriggerEvent.Pre event) {
            Ability ability = event.getAbility();

            CursedTechnique technique = JJKAbilities.getTechnique(ability);

            LivingEntity owner = event.getEntity();

            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            if (ability.getActivationType(owner) == Ability.ActivationType.INSTANT) {
                // Handling removal of absorbed techniques from curse manipulation
                if (technique != null && cap.getAbsorbed().contains(technique)) {
                    cap.unabsorb(technique);
                }
            }
        }
    }
}
