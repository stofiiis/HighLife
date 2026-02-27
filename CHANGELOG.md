# Changelog

All notable changes to this project are documented in this file.

## [1.0.0] - 2026-02-27

### Added
- Tiered seed categories from `common` to `premium`.
- Seed stacking rules based on matching strain + seed class.
- Seed breeding gameplay with inheritance/mutation logic.
- `Seed Mixer` as a placeable block with 4-slot GUI:
- Input: 2x seeds + dirt, optional bonemeal boost.
- Timed mixing with visual progress.
- Result collection directly from the block.
- Non-destructive mature crop harvesting (plant stays in farmland after harvest).
- Pipe and bong control GUIs with loading workflow.
- Joint multi-puff system.
- Expanded advancement set for smoking, loading, hydration, and breeding progression.

### Changed
- Rebranded production name and namespace to `HighLife`.
- Mod metadata updated:
- `mod_id`: `highlife`
- Display name: `HighLife`
- Package namespace: `com.stofiiis.highlife`
- Assets/data namespace moved from `weed` to `highlife`.
- Added bilingual CurseForge description markdown in project root.

### Fixed
- Seed Mixer now gives the finished result immediately even when GUI stays open.
- Pipe GUI alignment adjusted to match bong layout style.
- Pipe slot/arrow layout centered and spacing polished.

### Compatibility
- Minecraft: `1.21.11`
- NeoForge: `21.11.38-beta`

