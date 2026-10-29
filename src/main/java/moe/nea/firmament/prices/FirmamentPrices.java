// Price tooltip behavior extracted from Firmament (GPL-3.0-or-later).
// SPDX-FileCopyrightText: Firmament contributors
// SPDX-License-Identifier: GPL-3.0-or-later
package moe.nea.firmament.prices;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import java.nio.file.Path;
import java.util.List;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;

public final class FirmamentPrices implements ClientModInitializer {
    private static FirmamentPrices instance;
    private PriceSettings settings;
    private boolean pendingConfig;
    private KeyMapping openConfig;
    public static FirmamentPrices instance() {return instance;}
    public Screen configScreen(Screen parent) {return new PriceMenu(settings,this::applySettings).screen(parent);}
    private void applySettings(PriceSettings updated) {
        var endpoints=updated.apis.snapshot();settings=updated;settings.save(settingsPath);prices.configure(endpoints);
    }
    private Path settingsPath;
    private PriceRepository prices;
    private KeyMapping showPrices;
    private KeyMapping stackPrices;

    @Override public void onInitializeClient() {
        instance=this;
        var loader = FabricLoader.getInstance();
        settingsPath = loader.getConfigDir().resolve("firmament-prices.json");
        settings = PriceSettings.load(settingsPath);
        prices = new PriceRepository(loader.getGameDir().resolve("firmament-prices/metadata.json"));
        prices.configure(settings.apis.snapshot());
        openConfig=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.firmament_prices.config",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_UNKNOWN,KeyMapping.Category.MISC));
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            if(pendingConfig||openConfig.consumeClick()){pendingConfig=false;client.gui.setScreen(configScreen(client.gui.screen()));}
        });
        showPrices = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.firmament_prices.show", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, KeyMapping.Category.MISC));
        stackPrices = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.firmament_prices.stack", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_SHIFT, KeyMapping.Category.MISC));
        ItemTooltipCallback.EVENT.register((stack, context, flags, lines) -> tooltip(stack, lines));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> {
            var root = literal("firmamentprices").executes(command -> {
                pendingConfig=true;
                return 1;
            });
            root.then(literal("enabled").then(argument("value", BoolArgumentType.bool()).executes(command -> {
                settings.enabled = BoolArgumentType.getBool(command, "value"); settings.save(settingsPath); return 1;
            })));
            var bazaar = literal("bazaar");
            for (boolean instant : new boolean[] {false, true}) bazaar.then(literal(instant ? "instant" : "orders").executes(command -> {
                settings.instantPrices = instant; settings.save(settingsPath); return 1;
            }));
            root.then(bazaar);
            var average = literal("average");
            for (int days : new int[] {0, 1, 3, 7}) average.then(literal(days == 0 ? "off" : Integer.toString(days)).executes(command -> {
                settings.averageDays = days; settings.save(settingsPath); return 1;
            }));
            root.then(average); dispatcher.register(root);
        });
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> prices.start());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> prices.close());
    }

    private void tooltip(ItemStack stack, List<Component> lines) {
        if (!settings.enabled || (!showPrices.isUnbound() && !pressed(showPrices))) return;
        String id = identify(stack);
        if (id == null) return;
        long count = PriceLogic.logicalCount(lore(stack), stack.getCount());
        boolean wholeStack = settings.stackPrices && pressed(stackPrices);
        var result = PriceLogic.visibleLines(prices.bazaar(id), prices.auction(id, 0),
            settings.averageDays == 0 ? null : prices.auction(id, settings.averageDays), settings, wholeStack ? count : 1);
        if (result.isEmpty()) return;
        lines.add(Component.empty());
        if (settings.stackPrices && count > 1 && (wholeStack || settings.stackHint)) lines.add((wholeStack
            ? Component.literal("Showing prices for x" + count)
            : Component.literal("[").append(stackPrices.getTranslatedKeyMessage()).append("] to show x" + count))
            .withStyle(ChatFormatting.DARK_GRAY));
        for (var line : result) lines.add(formatPrice(line));
    }

    private static MutableComponent formatPrice(PriceLogic.Line line) {
        return Component.empty().withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
            .append(line.label()).append(": ")
            .append(Component.literal(PriceLogic.format(line.price()) + (line.price() == 1 ? " coin" : " coins"))
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
    }

    private String identify(ItemStack stack) {
        var custom = stack.get(DataComponents.CUSTOM_DATA);
        if (custom != null) {
            String id = ItemIds.fromData(NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, custom.copyTag()).getAsJsonObject());
            if (id != null) return id;
        }
        Screen screen = Minecraft.getInstance().gui.screen();
        if (screen == null) return null;
        String title = clean(screen.getTitle());
        boolean bazaar = isBazaar(screen);
        boolean dye = title.contains("Dye Compendium");
        boolean enchants = title.endsWith("Enchantments Guide") || title.contains("Experimentation Table RNG");
        boolean pairs = title.startsWith("Superpairs");
        if (!bazaar && !dye && !enchants && !pairs) return null;
        String name = clean(stack.getHoverName());
        if (bazaar || dye) {
            if (name.startsWith("SELL ")) name = name.substring(5);
            if (name.startsWith("BUY ")) name = name.substring(4);
        }
        if (pairs) {
            var lore = lore(stack);
            for (int i = 0; i + 1 < lore.size(); i++) if (lore.get(i).isBlank()) { name = lore.get(i + 1); break; }
        }
        return prices.find(name, stack.is(Items.ENCHANTED_BOOK) || enchants || pairs);
    }

    private static boolean isBazaar(Screen screen) {
        if (!(screen instanceof ContainerScreen container)) return false;
        var menu = container.getMenu();
        int end = menu.getRowCount() * 9;
        return end >= 5 && (clean(menu.getSlot(end - 4).getItem().getHoverName()).equals("Manage Orders")
            || lore(menu.getSlot(end - 5).getItem()).contains("To Bazaar"));
    }
    private static List<String> lore(ItemStack stack) {
        var lore = stack.get(DataComponents.LORE);
        return lore == null ? List.of() : lore.lines().stream().map(FirmamentPrices::clean).toList();
    }
    private static String clean(Component text) { return PriceLogic.clean(text.getString()); }
    private static boolean pressed(KeyMapping mapping) {
        var key = KeyMappingHelper.getBoundKeyOf(mapping);
        long window = Minecraft.getInstance().getWindow().handle();
        if (key.getType() == InputConstants.Type.MOUSE) return GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
        if (key.getType() == InputConstants.Type.SCANCODE) {
            int symbol = KeyInput.keyForScancode(key.getValue(), GLFW::glfwGetKeyScancode);
            return symbol >= 0 && GLFW.glfwGetKey(window, symbol) == GLFW.GLFW_PRESS;
        }
        return key.getValue() >= 0 && GLFW.glfwGetKey(window, key.getValue()) == GLFW.GLFW_PRESS;
    }
}
