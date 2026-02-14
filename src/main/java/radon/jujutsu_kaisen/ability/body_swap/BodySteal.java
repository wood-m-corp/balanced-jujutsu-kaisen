package radon.jujutsu_kaisen.ability.body_swap;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.jetbrains.annotations.Nullable;

import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedEnergyNature;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.util.HelperMethods;

public class BodySteal extends Ability implements Ability.IToggled {
    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) { 
        if (target == null) return false; 
        if (!(target instanceof Player player) &&  (ConfigHolder.SERVER.playerBodySteal.get())  ) return false;
        if (!target.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return false; 
        ISorcererData cap = target.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow(); 
        return cap.getType() == JujutsuType.SORCERER && cap.getExperience() >= ConfigHolder.SERVER.minimumBodyStealEXP.get(); 
    }

    private static boolean canSteal(LivingEntity owner, LivingEntity target) {
        //if (!owner.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return false;
        if (!target.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return false;

        //ISorcererData ownerCap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        ISorcererData targetCap = target.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        return ((target instanceof Player player) || (!ConfigHolder.SERVER.playerBodySteal.get())   ) && ( targetCap.getType() == JujutsuType.SORCERER) &&
                (target.isDeadOrDying()) && targetCap.getExperience() >= ConfigHolder.SERVER.minimumBodyStealEXP.get();
    }


    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.TOGGLED;
    }

    @Override
    public void run(LivingEntity owner) {

    }

    // @Override
    // public boolean isValid(LivingEntity owner) {
    //     //ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
    //     //return cap.getCopied().size() < ConfigHolder.SERVER.maximumCopiedTechniques.get() && JJKAbilities.hasToggled(owner, JJKAbilities.RIKA.get()) && super.isValid(owner);
    //     return true;
    // }

    @Override
    public float getCost(LivingEntity owner) {
        return 0.0F;
    }

    @Override
    public void onEnabled(LivingEntity owner) {

    }

    @Override
    public void onDisabled(LivingEntity owner) {

    }

    @Override
    public int getCooldown() {
        return 5 * 20;
    }

      private static void check(LivingEntity victim, DamageSource source) {
      //if (owner.level().isClientSide) return false;
      //  if (!HelperMethods.isMelee(source)) return false;
        //if (!(target instanceof Player player)) return false;
        if (!(victim instanceof Player player) &&  (ConfigHolder.SERVER.playerBodySteal.get()) ) return;

        if (!victim.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return ;

        if (!HelperMethods.isMelee(source)) return;

        if (!(source.getEntity() instanceof LivingEntity attacker)) return;

        if (!canSteal(attacker, victim)) return;

        if (!JJKAbilities.hasToggled(attacker, JJKAbilities.BODY_STEAL.get())) return;

       // ISorcererData victimCap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        attacker.swing(InteractionHand.MAIN_HAND, true);

        ISorcererData ownerCap = attacker.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        
        ISorcererData targetCap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        //ownerCap.deserializeNBT(ownerCap.serializeNBT());
        //CompoundTag targetnbt = targetCap.serializeNBT();
        // Require target to have enough exp
        if (targetCap.getExperience() <= ConfigHolder.SERVER.minimumBodyStealEXP.get()) {
            return;
        }

        CursedTechnique current = ownerCap.getTechnique();
        CursedTechnique steal = targetCap.getTechnique();
        CursedEnergyNature nature = targetCap.getNature();

        if (current == null) return;
        if (current == steal || (steal == CursedTechnique.MIMICRY && !ConfigHolder.SERVER.mimicryBodyStealCompat.get() ) ) return;

        
        if (steal != null) {
            ownerCap.addStolen(steal);
        }
        //ownerCap.steal(steal);
        ownerCap.setNature(nature);

        if (ConfigHolder.SERVER.bodyStealTraits.get() && ownerCap.getTraits() != null && targetCap.getTraits() != null) { 
            for (Trait t : ownerCap.getTraits()) {
                if (t != Trait.RCT_OUTPUT ) {
                    ownerCap.removeTrait(t);
                }
            }
            for (Trait t : targetCap.getTraits()) {
                if (t != Trait.RCT_OUTPUT && t != Trait.HEAVENLY_RESTRICTION_PHYSICAL && t != Trait.HEAVENLY_RESTRICTION_CE) {
                    ownerCap.addTrait(t);
                }
            }
        }

        //ownerCap.setExperience(targetCap.getExperience());
        if (ConfigHolder.SERVER.bodyStealEXPReset.get()) {
            targetCap.setExperience(0);
        }
        
       
       
        //NbtUtils.writeGameProfile(nbt, player.getGameProfile());
        //GameProfile profile = NbtUtils.readGameProfile(targetnbt);
       // 
        if (attacker instanceof ServerPlayer servOwner ) {
            if (ConfigHolder.SERVER.bodyStealReroll.get()) {
                targetCap.wipe(servOwner);
            }
        
            attacker.sendSystemMessage(Component.translatable(
                String.format("chat.%s.bodysteal", JujutsuKaisen.MOD_ID),
                victim.getName()
            ));
            // PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(ownerCap.serializeNBT()), servOwner);
             //GameProfile profile = player.getGameProfile();
            // ownerCap.setStolenSkinProfile(profile);
             SyncSorcererDataS2CPacket packet = new SyncSorcererDataS2CPacket(ownerCap.serializeNBT());
            PacketHandler.sendToClient(packet, servOwner);
            //  for (ServerPlayer online : servOwner.server.getPlayerList().getPlayers()) {
            //      PacketHandler.sendToClient(packet, online);
            // }
        }



      
        // if (target instanceof ServerPlayer servTarget) {
        //     PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(targetCap.serializeNBT()), servTarget);
        // }

        //ItemStack stack = new ItemStack(JJKItems.CURSED_SPIRIT_ORB.get());

        //if (!(victim instanceof Player)) {
       //     victim.discard();
      //  } else {
           // victim.kill();
      //  }
    }


     @Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class BodyStealForgeEvents {
        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            check(event.getEntity(), event.getSource());
        }
    }

}
