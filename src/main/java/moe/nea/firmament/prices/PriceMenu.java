// Uses Firmament's MoulConfig editor, restricted to the extracted price options.
// SPDX-License-Identifier: GPL-3.0-or-later
package moe.nea.firmament.prices;

import io.github.notenoughupdates.moulconfig.Config;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;
import io.github.notenoughupdates.moulconfig.gui.*;
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent;
import io.github.notenoughupdates.moulconfig.processor.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;

public final class PriceMenu extends Config {
    @Category(name="Price tooltips",desc="Lowest BIN and Bazaar display options")
    public final PriceSettings tooltips;
    @Category(name="API sources",desc="View or edit the addresses used to retrieve prices and item metadata")
    public final ApiSources apis;
    private final Consumer<PriceSettings> apply;

    public PriceMenu(PriceSettings current,Consumer<PriceSettings> apply) {
        tooltips=current.copy();tooltips.prepareMenu();apis=tooltips.apis;this.apply=apply;
    }
    @Override public StructuredText getTitle() {return StructuredText.of("Firmament Prices");}
    @Override public void saveNow() {
        try {apis.snapshot();}catch(IllegalArgumentException e){return;}
        tooltips.readMenu();apply.accept(tooltips);
    }
    public Screen screen(Screen parent) {
        var processor=MoulConfigProcessor.withDefaults(this);
        var driver=new ConfigProcessorDriver(processor);driver.checkExpose=false;driver.processConfig(this);
        var editor=new MoulConfigEditor<>(processor);
        var screen=new MoulConfigScreenComponent(Component.literal("Firmament Prices"),new GuiContext(new GuiElementComponent(editor)),parent) {
            @Override public void removed() {
                PriceMenu.this.saveNow();
                super.removed();
            }
            @Override public void onClose() {
                try {apis.snapshot();}
                catch(IllegalArgumentException e) {
                    Minecraft.getInstance().gui.setScreen(new AlertScreen(
                        ()->Minecraft.getInstance().gui.setScreen(PriceMenu.this.screen(parent)),
                        Component.literal("Invalid API address"),Component.literal(e.getMessage())));
                    return;
                }
                super.onClose();
                Minecraft.getInstance().gui.setScreen(parent);
            }
        };
        apis.reset=()-> {
            apis.resetDefaults();
            Minecraft.getInstance().gui.setScreen(PriceMenu.this.screen(parent));
        };
        return screen;
    }
}
