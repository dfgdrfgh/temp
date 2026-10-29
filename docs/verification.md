# Verification

- Built against Minecraft 26.2, Fabric Loader 0.19.3, Fabric API 0.161.0+26.2, Java 25.
- 20 unit/regression tests pass: Bazaar direction/priority, BIN averages, formatting, logical counts, special IDs, NEU stock/name parsing, zip parsing and scancode resolution.
- Parsed live Hypixel Bazaar (2,201 products) and EliteSkyblock Lowest BIN (4,898 IDs) responses; parsed the current NEU zip and verified enchanted-book names and stock aliases. Counts are a snapshot, not constants.
- Started the 26.2 development client under Xvfb/Mesa, loaded the standalone mod, rendered its resources, and reached the client-started callback with metadata ready. Stopped deliberately after the smoke-check timeout. External sound/language assets were skipped, and this environment has no audio device or authenticated Minecraft session; this was a startup smoke check, not a complete play test.
- The local environment blocks Unix domain sockets, so its cached Loom platform probe was adapted to return unsupported. That tooling-only adaptation is outside the repository and jar. GitHub Actions builds with unmodified Loom and uploads only the default mod jar.
- No authenticated Hypixel session, actual inventory hover test on Hypixel, or modpack compatibility test was performed.

The current NEU book layout includes an anvil hint before the enchantment name. A regression test covers that layout as well as Firmament's older first-line layout.

## Configuration menu update

- MoulConfig 4.8.0 for Minecraft 26.2 is bundled and relocated to avoid sharing GUI classes with other mods. Its Kotlin runtime is included as a discoverable nested library.
- The additional tests cover older settings migration, independent toggle persistence, custom source persistence and URL validation, averages independent of current BIN data, retained good prices after network failure, and late-response rejection after changing API sources.
- Launched the actual shaded `FirmamentPrices-1.1.0+26.2.jar` from a fresh game directory with only Fabric API and a temporary smoke-test mod. Kotlin was loaded from the mod's nested jar, without a separate Kotlin installation.
- The smoke test opened the MoulConfig screen, let it render, changed a price toggle, closed the menu and asserted that the save callback ran exactly once with the edited value. Result: `CONFIG_MENU_SMOKE_OK rendered configuration and saved toggle on close`.
- API URL changes and reset use the same persisted configuration. Custom services must preserve the documented response formats.
