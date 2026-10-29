# Firmament Prices configuration menu

Use Firmament’s MoulConfig menu, restricted to the extracted price features. Keep Minecraft 26.2 Fabric only and migrate existing settings.

- Add independent toggles for Lowest BIN, average BIN, Bazaar buy, Bazaar sell, stack totals and stack hint, plus existing enable/mode/window settings.
- Show and edit the full URLs for Bazaar, Lowest BIN, each average and NEU metadata; validate HTTP(S) URLs and provide reset defaults. Compatible JSON/zip formats remain required.
- Open via /firmamentprices, an optional key binding and Mod Menu. Embed the GUI library so no full Firmament installation is required.
- Apply sources atomically, clear data when providers change, reject late old-provider responses, refresh immediately off-thread and retain valid data on failure.
- Verify settings migration/persistence, URL validation, line toggles, provider switching, GUI startup/rendering and Actions build.
