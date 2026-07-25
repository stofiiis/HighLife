# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

### Changed
- Reworked seven core item textures into a consistent fantasy alchemy style.
- Replaced legacy-looking herb and infusion icons with clearer, CurseForge-friendly silhouettes.
- Rebuilt every custom block texture in the same fantasy alchemy palette.
- Replaced the flat Seed Mixer and Drying Rack cubes with custom 3D models.
- Updated all crop stages with rounded fantasy leaves and clearer growth progression.
- Seed Mixer inventory rendering now uses the same 3D model as the placed block.
- Block selection and collision shapes now follow the slimmer 3D models.
- Rebuilt all control GUIs with pixel-perfect slot alignment, separated work areas, and restrained device-specific accents.
- Seed Mixer input slots now split stacked parent seeds correctly and identify missing ingredients.
- Added concise usage instructions to every HighLife item tooltip.
- Replaced internal registry-style wording in advancement and gameplay text with localized display names.
- Replaced all four status-effect icons with distinct custom silhouettes.
- Replaced missing custom audio files with reliable built-in Minecraft sound events.

### Fixed
- Fixed five advancement entries being discarded because they referenced nonexistent parent IDs.
- Restored the complete Wand and Alchemy Flask advancement branches.

### Added
- Added the `Master Workshop` challenge for collecting the complete processing setup.
- Added the `Gene Archive` challenge for completing ten seed crosses.

### Development
- Added an archived texture concept sheet for future art direction.
- Added a deterministic PowerShell generator for the final 16x16 item sprites.
- Added deterministic generators for block textures and block models.
- Added deterministic generators for control GUIs and status-effect icons.
- Added two CurseForge item showcase images and a reproducible showcase generator.

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
- Infusion wand and alchemy flask control GUIs with loading workflow.
- Herb roll multi-charge system.
- Expanded advancement set for infusion, loading, hydration, and breeding progression.

### Changed
- Rebranded production name and namespace to `HighLife`.
- Mod metadata updated:
- `mod_id`: `highlife`
- Display name: `HighLife`
- Package namespace: `com.stofiiis.highlife`
- Assets/data namespace moved from `herb` to `highlife`.
- Added bilingual CurseForge description markdown in project root.

### Fixed
- Seed Mixer now gives the finished result immediately even when GUI stays open.
- Infusion wand GUI alignment adjusted to match alchemy flask layout style.
- Infusion wand slot/arrow layout centered and spacing polished.

### Compatibility
- Minecraft: `1.21.11`
- NeoForge: `21.11.38-beta`

