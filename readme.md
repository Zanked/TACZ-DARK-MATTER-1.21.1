# Dark Matter Shimmer (исходники клиентского мода)

Проигрывает 16 готовых кадров «переливания» на текстуре `dmcamo:deagle_dm` (Desert Eagle, 1024x1024).

## Как собрать
1. Возьми официальный MDK для NeoForge 1.21.1 (или свой проект мода на NeoForge 21.1.x).
2. Скопируй `src/main/java/dmshimmer/DmShimmer.java` и всё из `src/main/resources/` в проект
   (если это твой мод, то поменяй MODID/пакет и не дублируй `neoforge.mods.toml`).
3. Собери jar (`./gradlew build`) и положи в `mods/`.
4. Пак `tacz_dark_matter_camo.zip` должен лежать в `.minecraft/tacz/`.

## Настройки (константы в DmShimmer.java)
- `TICKS_PER_FRAME` = 2 → 10 кадров/с. Больше число → медленнее переливание.
- `FRAMES` = 16 — число кадров в `assets/dmshimmer/frames/`.

Код не компилировался и не запускался в игре: возможны мелкие правки под твою версию NeoForge/MDK.
