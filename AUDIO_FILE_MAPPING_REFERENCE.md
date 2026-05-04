# Audio File Mapping Reference
## Special Character Normalization Guide

This document provides a complete mapping of Ghanaian language special characters to their Android-compatible filename representations.

---

## Ewe Language - Letter Mappings

| Original Character | Unicode | IPA | Safe Filename | File Name | Status |
|---|---|---|---|---|---|
| a | U+0061 | /a/ | a | ewe_letter_a.mp3 | ✅ |
| b | U+0062 | /b/ | b | ewe_letter_b.mp3 | ✅ |
| ... | ... | ... | ... | ewe_letter_*.mp3 | ✅ |
| d | U+0064 | /d/ | d | ewe_letter_d.mp3 | ✅ |
| **ɖ** | **U+0256** | **[ɖ]** | **d_caron** | **ewe_letter_d_caron.mp3** | ✅ RENAMED |
| e | U+0065 | /e/ | e | ewe_letter_e.mp3 | ✅ |
| **ɛ** | **U+0190** | **[ɛ]** | **e_open** | **ewe_letter_e_open.mp3** | ✅ RENAMED |
| f | U+0066 | /f/ | f | ewe_letter_f.mp3 | ✅ |
| **ƒ** | **U+0192** | **[ƒ]** | **f_hook** | **ewe_letter_f_hook.mp3** | ✅ RENAMED |
| ... | ... | ... | ... | ewe_letter_*.mp3 | ✅ |
| **ɣ** | **U+0263** | **[ɣ]** | **g_hook** | **ewe_letter_g_hook.mp3** | ✅ RENAMED |
| ... | ... | ... | ... | ewe_letter_*.mp3 | ✅ |
| **ŋ** | **U+014B** | **[ŋ]** | **ng** | **ewe_letter_ng.mp3** | ✅ RENAMED |
| **ɔ** | **U+0254** | **[ɔ]** | **o_open** | **ewe_letter_o_open.mp3** | ✅ RENAMED |
| ... | ... | ... | ... | ewe_letter_*.mp3 | ✅ |
| **ʋ** | **U+028B** | **[ʋ]** | **v_hook** | **ewe_letter_v_hook.mp3** | ✅ RENAMED |
| ... | ... | ... | ... | ewe_letter_*.mp3 | ✅ |

**Total Ewe Letter Files**: 30
- Regular letters: 23
- Special characters: 7 (d_caron, e_open, f_hook, g_hook, ng, o_open, v_hook)

---

## Gaa Language - Letter Mappings

| Original Character | Unicode | IPA | Safe Filename | File Name | Status |
|---|---|---|---|---|---|
| a | U+0061 | /a/ | a | gaa_letter_a.mp3 | ✅ |
| ... | ... | ... | ... | gaa_letter_*.mp3 | ✅ |
| **ɛ** | **U+0190** | **[ɛ]** | **e_open** | **gaa_letter_e_open.mp3** | ✅ RENAMED |
| ... | ... | ... | ... | gaa_letter_*.mp3 | ✅ |
| **ŋ** | **U+014B** | **[ŋ]** | **ng** | **gaa_letter_ng.mp3** | ✅ RENAMED |
| **ɔ** | **U+0254** | **[ɔ]** | **o_open** | **gaa_letter_o_open.mp3** | ✅ RENAMED |
| ... | ... | ... | ... | gaa_letter_*.mp3 | ✅ |

**Total Gaa Letter Files**: 26
- Regular letters: 23
- Special characters: 3 (e_open, ng, o_open)

---

## Twi Language - Letter Mappings

| Original Character | Unicode | IPA | Safe Filename | File Name | Status |
|---|---|---|---|---|---|
| a | U+0061 | /a/ | a | twi_letter_a.mp3 | ✅ |
| ... | ... | ... | ... | twi_letter_*.mp3 | ✅ |
| **ɛ** | **U+0190** | **[ɛ]** | **e_open** | **twi_letter_e_open.mp3** | ✅ RENAMED |
| ... | ... | ... | ... | twi_letter_*.mp3 | ✅ |
| **ɔ** | **U+0254** | **[ɔ]** | **o_open** | **twi_letter_o_open.mp3** | ✅ RENAMED |
| ... | ... | ... | ... | twi_letter_*.mp3 | ✅ |

**Total Twi Letter Files**: 22
- Regular letters: 20
- Special characters: 2 (e_open, o_open)

---

## Complete File Rename Log

### Ewe Language (7 files renamed)

```
Original Filename              →  New Filename
────────────────────────────────────────────────────
ewe_letter_ŋ.mp3             →  ewe_letter_ng.mp3
ewe_letter_ɔ.mp3             →  ewe_letter_o_open.mp3
ewe_letter_ɖ,.mp3            →  ewe_letter_d_caron.mp3
ewe_letter_ɛ.mp3             →  ewe_letter_e_open.mp3
ewe_letter_ʋ.mp3             →  ewe_letter_v_hook.mp3
ewe_letter_ƒ.mp3             →  ewe_letter_f_hook.mp3
ewe_letter_ɣ.mp3             →  ewe_letter_g_hook.mp3
```

### Gaa Language (3 files renamed)

```
Original Filename              →  New Filename
────────────────────────────────────────────────────
gaa_letter_ŋ.mp3             →  gaa_letter_ng.mp3
gaa_letter_ɔ.mp3             →  gaa_letter_o_open.mp3
gaa_letter_ɛ.mp3             →  gaa_letter_e_open.mp3
```

### Twi Language (2 files renamed)

```
Original Filename              →  New Filename
────────────────────────────────────────────────────
twi_letter_ɔ.mp3             →  twi_letter_o_open.mp3
twi_letter_ɛ.mp3             →  twi_letter_e_open.mp3
```

### Summary

| Language | Files Renamed | Details |
|---|---|---|
| Ewe | 7 | d_caron, e_open, f_hook, g_hook, ng, o_open, v_hook |
| Gaa | 3 | e_open, ng, o_open |
| Twi | 2 | e_open, o_open |
| **TOTAL** | **12** | |

---

## Sanitization Algorithm

The `OfflineGhanaLPTtsService.sanitizeForFilename()` method applies these transformations:

### Step 1: Convert to Lowercase
```
Input: "Ŋ" → Output: "ŋ"
```

### Step 2: Replace Special Ghanaian Characters
```
"ɛ"  → "e_open"      // Open e (Ewe, Twi)
"ɔ"  → "o_open"      // Open o (Ewe, Twi)
"ɖ"  → "d_caron"     // D with hook (Ewe)
"ƒ"  → "f_hook"      // F with hook (Ewe)
"ɣ"  → "g_hook"      // G with hook (Ewe)
"ŋ"  → "ng"          // Engma/Ng (Ga, Ewe)
"ʋ"  → "v_hook"      // V with hook (Ewe)
"ɲ"  → "ny"          // Ny digraph
```

### Step 3: Replace Diacritics
```
"ã" → "a"
"õ" → "o"
"ũ" → "u"
"ĩ" → "i"
"ẽ" → "e"
"ā" → "a"
"ē" → "e"
"ī" → "i"
"ō" → "o"
"ū" → "u"
```

### Step 4: Replace Spaces & Special Characters
```
" "  → "_"
"-"  → "_"
"'"  → ""  (remove)
"\"" → ""  (remove)
```

### Step 5: Remove Non-Alphanumeric (except underscore)
```
regex: [^a-z0-9_] → removed
```

---

## Usage Example

### Scenario: Learn Letter "ŋ" in Ewe

1. User selects Ewe language and letter "ŋ"
2. QuizActivity calls: `speakPrompt("ŋ", "ewe")`
3. Routes to: `speakWithGhanaLP("ŋ")`
4. Offline TTS service:
   - `sanitizeForFilename("ŋ")` → `"ng"`
   - `normalizeLanguageForAudio("ewe")` → `"ewe"`
   - Builds patterns: `"ewe_letter_ng"`, `"ewe_ng"`, etc.
   - Lookup: `R.getIdentifier("ewe_letter_ng", "raw", packageName)`
   - Finds: `ewe_letter_ng.mp3` ✅
   - Plays: `MediaPlayer.create(context, R.raw.ewe_letter_ng).start()`

---

## Android Resource System Integration

### How R.java Maps To Files

When Gradle builds the APK:

1. **Scan Phase**: Reads all files in `app/src/main/res/raw/`
2. **Validation Phase**: Ensures all filenames contain only `[a-z0-9_.]`
3. **Generation Phase**: Creates R.java with resource IDs:
   ```java
   public static final int ewe_letter_ng = 0x7f120345;
   public static final int ewe_letter_e_open = 0x7f120346;
   public static final int ewe_letter_o_open = 0x7f120347;
   // ... etc for all 383 files
   ```
4. **Runtime Phase**: Code uses R.raw constants or dynamic lookup:
   ```java
   // Direct reference
   MediaPlayer.create(context, R.raw.ewe_letter_ng);
   
   // Dynamic lookup
   int resId = context.getResources().getIdentifier(
       "ewe_letter_ng", "raw", context.getPackageName()
   );
   MediaPlayer.create(context, resId);
   ```

---

## Validation Checklist

✅ **All original character files renamed**: 12 files
✅ **Sanitization algorithm verified**: Matches all renamed files
✅ **Build successful**: `./gradlew clean assembleDebug`
✅ **R.java regenerated**: All resource IDs present
✅ **No invalid filenames**: All contain only `[a-z0-9_]`
✅ **Audio playback working**: OfflineGhanaLPTtsService tested
✅ **Language code normalization working**: Correct mapping applied

---

*Document Generated*: May 3, 2026
*Total Files Restored*: 383 audio files
*Files Renamed*: 12 files with special characters
*Build Status*: ✅ SUCCESSFUL

