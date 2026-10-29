# Firmament Prices — Fabric 26.2

Standalone extraction of Firmament's Lowest BIN and Bazaar price tooltips. Install **Fabric Loader 0.19.3+**, **Fabric API 0.161.0+26.2** and `FirmamentPrices-1.0.0+26.2.jar` on Minecraft **26.2** with Java **25+**. Full Firmament and Fabric Language Kotlin are not required.

Hover a SkyBlock item to see prices. Bazaar items show buy/sell order prices by default; auction items show Lowest BIN and the three-day average. Hold **Left Shift** to show prices for the whole stack, including quantities shown in sack/order lore. The two keys can be rebound under Options → Controls → Miscellaneous; the optional hold-to-show key is unbound by default.

Settings are saved in `config/firmament-prices.json`. Use these client commands:

- `/firmamentprices` — show settings and help.
- `/firmamentprices enabled true` (or `false`).
- `/firmamentprices bazaar orders` (or `instant`).
- `/firmamentprices average off` (or `1`, `3`, `7`).

The tooltip labels, color/bold styling, Bazaar priority over auction prices, decimal truncation, price sources, two-minute refresh and original settings defaults are retained from Firmament. Pet, rune, enchanted book, potion, attribute shard, case and seasonal hat IDs are retained. NEU metadata supports stock aliases and the original Bazaar/enchantment/Superpairs/dye menu name lookup. The standalone settings use client commands rather than Firmament's full configuration GUI.

Prices use the same public Hypixel Bazaar and EliteSkyblock auction endpoints as Firmament; no API key is needed. NEU metadata downloads in the background on startup and is cached in `firmament-prices/metadata.json`. Network failures keep the last valid data and retry on later price refreshes. Prices may be absent while data loads or while a source is unavailable.

Build: `./gradlew build` with Java 25. Download the normal jar from the **FirmamentPrices-fabric-26.2** artifact in the latest successful [Actions run](https://github.com/dfgdrfgh/temp/actions/workflows/build.yml). This branch builds only the extracted mod; the original Firmament code remains in git history and the `mc-26.1` branch.

Derived from [Firmament](https://github.com/FirmamentMC/Firmament), GPL-3.0-or-later. See [NOTICE](NOTICE) and [license](LICENSES/GPL-3.0-or-later.txt).
