package radon.jujutsu_kaisen.capability.data.sorcerer;

import net.minecraft.network.chat.Component;
import radon.jujutsu_kaisen.JujutsuKaisen;

public enum Trait {
    SIX_EYES,
    HEAVENLY_RESTRICTION_PHYSICAL,
    HEAVENLY_RESTRICTION_CE,
    VESSEL,
    RCT_OUTPUT,
    INCARNATED,
    PERFECT_BODY,
    CURSED_WOMB,
    DEATH_PAINTING,
    PRODIGY,
    SIMURIAN;

    public Component getName() {
        return Component.translatable(String.format("trait.%s.%s", JujutsuKaisen.MOD_ID, this.name().toLowerCase()));
    }

    public String getRawName() {
        return this.name().toLowerCase();
    }
}
