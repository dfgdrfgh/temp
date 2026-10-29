// SPDX-License-Identifier: GPL-3.0-or-later
package moe.nea.firmament.prices.compat;
import com.terraformersmc.modmenu.util.mod.Mod;
import moe.nea.firmament.prices.ModMenuCredits;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.List;

/** Changes only the author summary in our selected-mod header; credits use the real authors. */
@Pseudo
@Mixin(targets="com.terraformersmc.modmenu.gui.ModsScreen",remap=false)
public abstract class ModMenuAuthorSummaryMixin {
    @Redirect(method="extractRenderState",at=@At(value="INVOKE",target="Lcom/terraformersmc/modmenu/util/mod/Mod;getAuthors()Ljava/util/List;"),require=0,remap=false)
    private List<String> firmamentPrices$summaryAuthors(Mod mod) {
        return ModMenuCredits.summaryAuthors(mod.getId(),mod.getAuthors());
    }
}
