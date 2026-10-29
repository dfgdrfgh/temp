# Verification

- Built against Minecraft 26.2, Fabric Loader 0.19.3, Fabric API 0.161.0+26.2, Java 25.
- 13 unit/regression tests pass: Bazaar direction/priority, BIN averages, formatting, logical counts, special IDs, NEU stock/name parsing, zip parsing and scancode resolution.
- Parsed live Hypixel Bazaar (2,201 products) and EliteSkyblock Lowest BIN (4,898 IDs) responses; parsed the current NEU zip and verified enchanted-book names and stock aliases. Counts are a snapshot, not constants.
- Started the 26.2 development client under Xvfb/Mesa, loaded the standalone mod, rendered its resources, and reached the client-started callback with metadata ready. Stopped deliberately after the smoke-check timeout. External sound/language assets were skipped, and this environment has no audio device or authenticated Minecraft session; this was a startup smoke check, not a complete play test.
- The local environment blocks Unix domain sockets, so its cached Loom platform probe was adapted to return unsupported. That tooling-only adaptation is outside the repository and jar. GitHub Actions builds with unmodified Loom and uploads only the default mod jar.
- No authenticated Hypixel session, actual inventory hover test on Hypixel, or modpack compatibility test was performed.

The current NEU book layout includes an anvil hint before the enchantment name. A regression test covers that layout as well as Firmament's older first-line layout.
