# Standalone Firmament Prices
Extract the price tooltip feature from Firmament commit 4d1ed0013927328d91892252b596c0f66ceb5d10 for Minecraft 26.2 Fabric only. Keep original labels, styling, Bazaar priority, order/instant selection, optional 1/3/7-day averages, enable/hold bindings, logical stack counts, special item IDs and contextual name lookup. Defaults remain enabled, order prices, three-day average, Left Shift for stack prices.

Use a small independent client mod with Fabric API, the same Hypixel/EliteSkyblock price endpoints and NEU item/stock metadata. Preserve upstream copyright and GPL licensing. No Firmament runtime dependency or unrelated gameplay features. New mod identity: firmament_prices; default jar name FirmamentPrices-1.0.0+26.2.jar. Settings available through /firmamentprices and normal Controls key bindings.

A wholesale port would retain unrelated features and their dependencies. Keeping the feature in an independent source set would leave two competing mod builds. The chosen extraction replaces the build on a dedicated branch while retaining the original fork branch and git history.

Network work must stay off the client thread, failures must retain the last valid snapshot, and refresh jobs must survive failed requests. Build/test and launch on 26.2; report separately what cannot be checked on live Hypixel.
