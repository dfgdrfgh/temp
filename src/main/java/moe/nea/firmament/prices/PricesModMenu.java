// SPDX-License-Identifier: GPL-3.0-or-later
package moe.nea.firmament.prices;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
public final class PricesModMenu implements ModMenuApi {
    @Override public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent->FirmamentPrices.instance().configScreen(parent);
    }
}
