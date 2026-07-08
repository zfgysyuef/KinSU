package com.mikokernel.ui.theme.beautify

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Centralized wallpaper / beautification state.
 * All fields are Compose-observable so UI recomposes automatically.
 * Persisted via SharedPreferences ("beautify").
 */
object BackgroundConfig {

    // --- Global wallpaper ---
    var globalUri: String? by mutableStateOf(null);          private set
    var isGlobalEnabled: Boolean by mutableStateOf(false);   private set

    // --- Per-page wallpapers (kept for backward compat, not exposed in UI) ---
    var homeUri: String? by mutableStateOf(null);            private set
    var superuserUri: String? by mutableStateOf(null);       private set
    var moduleUri: String? by mutableStateOf(null);          private set
    var settingsUri: String? by mutableStateOf(null);        private set

    // --- Appearance tuning ---
    var cardOpacity: Float by mutableFloatStateOf(0.85f);    private set
    var backgroundDim: Float by mutableFloatStateOf(0.30f);  private set
    var isDualBackgroundDimEnabled: Boolean by mutableStateOf(true); private set
    var backgroundDayDim: Float by mutableFloatStateOf(0.0f); private set
    var backgroundNightDim: Float by mutableFloatStateOf(0.50f); private set
    var backgroundBlur: Float by mutableFloatStateOf(0f);    private set
    var cardBgOpacity: Float by mutableFloatStateOf(1.0f);   private set
    var cardBgDim: Float by mutableFloatStateOf(0.26f);      private set
    var cardHeightScale: Float by mutableFloatStateOf(1.0f); private set

    // Legacy module card default background. Kept only for reading old settings.
    var moduleCardBgUri: String? by mutableStateOf(null);       private set
    var isModuleCardBgEnabled: Boolean by mutableStateOf(false); private set
    var moduleCardBgOpacity: Float by mutableFloatStateOf(1.0f); private set

    // Per-card individual backgrounds (cardId -> uri)
    val cardBgMap = mutableStateMapOf<String, String>()

    // --- Mutators (called from UI) ---

    fun setGlobal(context: Context, uri: String?) {
        globalUri = uri
        isGlobalEnabled = uri != null
        save(context)
    }

    fun setGlobalEnabled(context: Context, enabled: Boolean) {
        isGlobalEnabled = enabled && globalUri != null
        save(context)
    }

    fun setHome(context: Context, uri: String?) {
        homeUri = uri; save(context)
    }

    fun setSuperuser(context: Context, uri: String?) {
        superuserUri = uri; save(context)
    }

    fun setModule(context: Context, uri: String?) {
        moduleUri = uri; save(context)
    }

    fun setSettings(context: Context, uri: String?) {
        settingsUri = uri; save(context)
    }

    fun setCardOpacity(context: Context, v: Float) {
        cardOpacity = v.coerceIn(0.1f, 1f); save(context)
    }

    fun setBackgroundDim(context: Context, v: Float) {
        backgroundDim = v.coerceIn(0f, 1f); save(context)
    }

    fun setDualBackgroundDimEnabled(context: Context, enabled: Boolean) {
        isDualBackgroundDimEnabled = enabled; save(context)
    }

    fun setBackgroundDayDim(context: Context, v: Float) {
        backgroundDayDim = v.coerceIn(0f, 1f); save(context)
    }

    fun setBackgroundNightDim(context: Context, v: Float) {
        backgroundNightDim = v.coerceIn(0f, 1f); save(context)
    }

    fun effectiveBackgroundDim(isDarkTheme: Boolean): Float {
        return if (isDualBackgroundDimEnabled) {
            if (isDarkTheme) backgroundNightDim else backgroundDayDim
        } else {
            backgroundDim
        }
    }

    fun setModuleCardBg(context: Context, uri: String?) {
        moduleCardBgUri = uri; isModuleCardBgEnabled = uri != null; save(context)
    }
    fun setModuleCardBgOpacity(context: Context, v: Float) {
        moduleCardBgOpacity = v.coerceIn(0.1f, 1f); save(context)
    }
    fun clearModuleCardBg(context: Context) {
        moduleCardBgUri = null; isModuleCardBgEnabled = false; save(context)
    }

    fun setBackgroundBlur(context: Context, v: Float) {
        backgroundBlur = v.coerceIn(0f, 25f); save(context)
    }

    fun setCardBgOpacity(context: Context, v: Float) {
        cardBgOpacity = v.coerceIn(0f, 1f); save(context)
    }

    fun setCardBgDim(context: Context, v: Float) {
        cardBgDim = v.coerceIn(0.18f, 0.78f); save(context)
    }

    fun setCardHeightScale(context: Context, v: Float) {
        cardHeightScale = v.coerceIn(0.75f, 1.60f); save(context)
    }

    // Per-card backgrounds
    fun setCardBackground(context: Context, cardId: String, uri: String?) {
        if (uri != null) cardBgMap[cardId] = uri
        else cardBgMap.remove(cardId)
        saveCardBgMap(context)
    }
    fun getCardBackground(cardId: String): String? = cardBgMap[cardId]
    fun clearCardBackground(context: Context, cardId: String) {
        cardBgMap.remove(cardId)
        saveCardBgMap(context)
    }

    fun moduleCardId(moduleId: String): String = "module:$moduleId"

    fun setModuleCardBgForModule(context: Context, moduleId: String, uri: String?) {
        setCardBackground(context, moduleCardId(moduleId), uri)
    }
    fun getModuleCardBgForModule(moduleId: String): String? = getCardBackground(moduleCardId(moduleId))
    fun clearModuleCardBgForModule(context: Context, moduleId: String) {
        clearCardBackground(context, moduleCardId(moduleId))
    }

    /** Resolve the effective wallpaper URI for a given page. */
    fun resolveUri(pageKey: String): String? {
        val specific = when (pageKey) {
            KEY_HOME      -> homeUri
            KEY_SUPERUSER -> superuserUri
            KEY_MODULE    -> moduleUri
            KEY_SETTINGS  -> settingsUri
            else          -> null
        }
        return specific ?: if (isGlobalEnabled) globalUri else null
    }

    // --- Persistence ---

    private const val PREFS = "beautify"
    private const val K_GLOBAL       = "global_uri"
    private const val K_GLOBAL_ON    = "global_on"
    private const val K_HOME         = "home_uri"
    private const val K_SU           = "su_uri"
    private const val K_MOD          = "mod_uri"
    private const val K_SET          = "set_uri"
    private const val K_OPACITY      = "card_opacity"
    private const val K_DIM          = "bg_dim"
    private const val K_DUAL_DIM     = "bg_dual_dim"
    private const val K_DAY_DIM      = "bg_day_dim"
    private const val K_NIGHT_DIM    = "bg_night_dim"
    private const val K_BLUR         = "bg_blur"
    private const val K_CARD_BG_OPACITY = "card_bg_opacity"
    private const val K_CARD_BG_DIM = "card_bg_dim"
    private const val K_CARD_HEIGHT_SCALE = "card_height_scale"
    private const val K_MODCARD_BG  = "modcard_bg_uri"
    private const val K_MODCARD_BG_ON = "modcard_bg_on"
    private const val K_MODCARD_BG_OP = "modcard_bg_opacity"
    private const val K_PER_MODULE_BG_MAP = "per_module_bg_map"
    private const val K_CARD_BG_MAP = "card_bg_map"

    const val KEY_HOME      = "home"
    const val KEY_SUPERUSER = "superuser"
    const val KEY_MODULE    = "module"
    const val KEY_SETTINGS  = "settings"

    fun load(context: Context) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        globalUri       = p.getString(K_GLOBAL, null)
        isGlobalEnabled = p.getBoolean(K_GLOBAL_ON, false)
        homeUri         = p.getString(K_HOME, null)
        superuserUri    = p.getString(K_SU, null)
        moduleUri       = p.getString(K_MOD, null)
        settingsUri     = p.getString(K_SET, null)
        cardOpacity     = p.getFloat(K_OPACITY, 0.85f)
        backgroundDim   = p.getFloat(K_DIM, 0.30f)
        isDualBackgroundDimEnabled = p.getBoolean(K_DUAL_DIM, true)
        backgroundDayDim = p.getFloat(K_DAY_DIM, 0.0f)
        backgroundNightDim = p.getFloat(K_NIGHT_DIM, 0.50f)
        backgroundBlur  = p.getFloat(K_BLUR, 0f)
        cardBgOpacity   = p.getFloat(K_CARD_BG_OPACITY, 1.0f)
        cardBgDim       = p.getFloat(K_CARD_BG_DIM, 0.26f).coerceIn(0.18f, 0.78f)
        cardHeightScale = p.getFloat(K_CARD_HEIGHT_SCALE, 1.0f)
        moduleCardBgUri = p.getString(K_MODCARD_BG, null)
        isModuleCardBgEnabled = p.getBoolean(K_MODCARD_BG_ON, false)
        moduleCardBgOpacity = p.getFloat(K_MODCARD_BG_OP, 1.0f)
        // Load per-card backgrounds, then migrate older per-module entries.
        cardBgMap.clear()
        val cardBgSet = p.getStringSet(K_CARD_BG_MAP, emptySet()) ?: emptySet()
        cardBgSet.forEach { entry ->
            val parts = entry.split("|||", limit = 2)
            if (parts.size == 2) cardBgMap[parts[0]] = parts[1]
        }
        val perModuleBgSet = p.getStringSet(K_PER_MODULE_BG_MAP, emptySet()) ?: emptySet()
        perModuleBgSet.forEach { entry ->
            val parts = entry.split("|||", limit = 2)
            if (parts.size == 2) cardBgMap.putIfAbsent(moduleCardId(parts[0]), parts[1])
        }
    }

    /** Returns true if any wallpaper (global or page-specific) is active. */
    val hasAnyWallpaper: Boolean
        get() = isGlobalEnabled || homeUri != null || superuserUri != null || moduleUri != null || settingsUri != null

    fun saveCardBgMap(context: Context) {
        val set = cardBgMap.map { "${it.key}|||${it.value}" }.toSet()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putStringSet(K_CARD_BG_MAP, set)
            .apply()
    }

    fun saveModuleBgMap(context: Context) = saveCardBgMap(context)

    fun save(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            putString(K_GLOBAL, globalUri)
            putBoolean(K_GLOBAL_ON, isGlobalEnabled)
            putString(K_HOME, homeUri)
            putString(K_SU, superuserUri)
            putString(K_MOD, moduleUri)
            putString(K_SET, settingsUri)
            putFloat(K_OPACITY, cardOpacity)
            putFloat(K_DIM, backgroundDim)
            putBoolean(K_DUAL_DIM, isDualBackgroundDimEnabled)
            putFloat(K_DAY_DIM, backgroundDayDim)
            putFloat(K_NIGHT_DIM, backgroundNightDim)
            putFloat(K_BLUR, backgroundBlur)
            putFloat(K_CARD_BG_OPACITY, cardBgOpacity)
            putFloat(K_CARD_BG_DIM, cardBgDim)
            putFloat(K_CARD_HEIGHT_SCALE, cardHeightScale)
            putString(K_MODCARD_BG, moduleCardBgUri)
            putBoolean(K_MODCARD_BG_ON, isModuleCardBgEnabled)
            putFloat(K_MODCARD_BG_OP, moduleCardBgOpacity)
            apply()
        }
    }

    fun clearPage(context: Context, pageKey: String) {
        when (pageKey) {
            KEY_HOME      -> homeUri = null
            KEY_SUPERUSER -> superuserUri = null
            KEY_MODULE    -> moduleUri = null
            KEY_SETTINGS  -> settingsUri = null
        }
        save(context)
    }

    fun clearAll(context: Context) {
        globalUri = null; isGlobalEnabled = false
        homeUri = null; superuserUri = null; moduleUri = null; settingsUri = null
        cardOpacity = 0.85f; backgroundDim = 0.30f
        isDualBackgroundDimEnabled = true; backgroundDayDim = 0.0f; backgroundNightDim = 0.50f
        backgroundBlur = 0f
        cardBgOpacity = 1.0f; cardBgDim = 0.26f; cardHeightScale = 1.0f
        moduleCardBgUri = null; isModuleCardBgEnabled = false; moduleCardBgOpacity = 1.0f
        cardBgMap.clear()
        save(context)
        saveCardBgMap(context)
    }
}
