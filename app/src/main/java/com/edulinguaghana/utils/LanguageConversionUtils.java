package com.edulinguaghana.utils;

import java.util.Locale;

/**
 * Utility class for language conversion functions.
 * Provides number-to-word conversions for multiple languages
 * and language code normalization.
 */
public class LanguageConversionUtils {

    // Supported language codes
    public static final String LANG_ENGLISH = "en";
    public static final String LANG_FRENCH = "fr";
    public static final String LANG_TWI = "ak";  // ISO 639-1 code for Akan/Twi
    public static final String LANG_EWE = "ee";
    public static final String LANG_GA = "gaa";

    /**
     * Normalizes language code to standard format
     */
    public static String normalizeLanguageCode(String code) {
        if (code == null) return LANG_ENGLISH;
        switch (code.toLowerCase()) {
            case "ak":
            case "twi":
                return LANG_TWI;
            case "ee":
            case "ewe":
                return LANG_EWE;
            case "gaa":
            case "ga":
                return LANG_GA;
            case "fr":
                return LANG_FRENCH;
            case "en":
            default:
                return LANG_ENGLISH;
        }
    }

    /**
     * Checks if the given language code is a Ghanaian language
     */
    public static boolean isGhanaianLanguage(String code) {
        if (code == null) return false;
        String normalized = normalizeLanguageCode(code);
        return normalized.equals(LANG_TWI) ||
               normalized.equals(LANG_EWE) ||
               normalized.equals(LANG_GA);
    }

    /**
     * Converts a number (1-100) to its word representation in the specified language
     */
    public static String convertNumberToWord(int num, String languageCode) {
        String normalized = normalizeLanguageCode(languageCode);
        switch (normalized) {
            case LANG_FRENCH:
                return convertNumberToWordFrench(num);
            case LANG_TWI:
                return convertNumberToWordTwi(num);
            case LANG_EWE:
                return convertNumberToWordEwe(num);
            case LANG_GA:
                return convertNumberToWordGa(num);
            case LANG_ENGLISH:
            default:
                return convertNumberToWordEnglish(num);
        }
    }

    /**
     * Converts number to English words (1-100)
     */
    public static String convertNumberToWordEnglish(int num) {
        if (num < 1 || num > 100) {
            return "";
        }

        String[] units = {
                "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
                "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
        };

        String[] tens = {
                "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
        };

        if (num < 20) {
            return units[num];
        } else if (num == 100) {
            return "One Hundred";
        } else {
            int unit = num % 10;
            int ten = num / 10;
            if (unit == 0) {
                return tens[ten];
            } else {
                return tens[ten] + " " + units[unit];
            }
        }
    }

    /**
     * Converts number to French words (1-100)
     * Uses TTS-friendly format without hyphens or special spacing
     */
    public static String convertNumberToWordFrench(int num) {
        if (num < 1 || num > 100) {
            return "";
        }

        String[] units = {
                "", "Un", "Deux", "Trois", "Quatre", "Cinq", "Six", "Sept", "Huit", "Neuf", "Dix",
                "Onze", "Douze", "Treize", "Quatorze", "Quinze", "Seize", "Dix sept", "Dix huit", "Dix neuf"
        };

        String[] tens = {
                "", "Dix", "Vingt", "Trente", "Quarante", "Cinquante", "Soixante", "Soixante dix", "Quatre vingts", "Quatre vingt dix"
        };

        if (num < 20) {
            return units[num];
        } else if (num == 100) {
            return "Cent";
        } else {
            int unit = num % 10;
            int ten = num / 10;
            if (unit == 0) {
                if (ten == 8) { // 80
                    return "Quatre vingts";
                } else {
                    return tens[ten];
                }
            } else {
                if (ten == 7 || ten == 9) { // 70s, 90s
                    // 71 = 60+11, 91 = 90+1
                    return tens[ten - 1] + " " + units[10 + unit];
                } else if (ten == 8) { // 80s (81-89)
                    return tens[ten] + " " + units[unit];
                } else {
                    // 20-69 (21-29, 31-39, 41-49, 51-59, 61-69)
                    return tens[ten] + " " + units[unit];
                }
            }
        }
    }

    /**
     * Converts number to Twi words (1-100)
     */
    public static String convertNumberToWordTwi(int num) {
        if (num < 1 || num > 100) return "";
        if (num == 100) return "Ɔha";

        String[] standaloneUnits = {"", "Baako", "Mmienu", "Mmiɛnsa", "Ɛnan", "Enum", "Nsia", "Nson", "Nwɔtwe", "Nkron"};
        String[] combinedUnits = {"", "baako", "mmienu", "mmiɛnsa", "nan", "num", "nsia", "nson", "nwɔtwe", "nkron"};
        String[] tens = {"", "Edu", "Aduonu", "Aduasa", "Aduanan", "Aduonum", "Aduosia", "Aduɔson", "Aduɔwɔtwe", "Aduɔkron"};

        if (num < 10) return standaloneUnits[num];
        if (num == 10) return "Edu";

        if (num < 20) {
            int unit = num - 10;
            if (unit == 9) return "Du nkron";
            return "Du" + combinedUnits[unit];
        }

        int ten = num / 10;
        int unit = num % 10;

        if (unit == 0) return tens[ten];

        return tens[ten] + " " + combinedUnits[unit];
    }

    /**
     * Converts number to Ewe words (1-100)
     */
    public static String convertNumberToWordEwe(int num) {
        if (num < 1 || num > 100) return "";
        String[] units = {"", "ɖeka", "eve", "etɔ̃", "ene", "atɔ̃", "adẽ", "adrẽ", "enyi", "asieke"};

        // Handle 1-9
        if (num < 10) return units[num];

        // Handle 10
        if (num == 10) return "ewó";

        // Handle 11-19
        if (num < 20) return "ewóí" + units[num - 10];

        // Handle 20-99
        if (num % 10 == 0) {
            if (num == 20) return "blaeve";
            if (num == 30) return "blaetɔ̃";
            if (num == 40) return "blaene";
            if (num == 50) return "blaatɔ̃";
            if (num == 60) return "blaadẽ";
            if (num == 70) return "blaadrẽ";
            if (num == 80) return "blaenyi";
            if (num == 90) return "blaasieke";
        }

        // Handle 21-99 with remainder
        if (num == 100) return "alakpa ɖeka";
        return convertNumberToWordEwe(num - (num % 10)) + " kple " + units[num % 10];
    }

    /**
     * Converts number to Ga words (1-100)
     */
    public static String convertNumberToWordGa(int num) {
        if (num < 1 || num > 100) return "";
        if (num == 100) return "oha";

        String[] units = {"", "ekome", "enyɔ", "etɛ", "ejwɛ", "enumɔ", "ekpaa", "kpawo", "kpaanyɔ", "nɛɛhu"};

        if (num < 10) return units[num];
        if (num == 10) return "nyɔŋma";
        if (num < 20) return "nyɔŋma kɛ " + units[num - 10];

        int ten = num / 10;
        int unit = num % 10;

        String tenWord;
        if (ten == 2) tenWord = "nyɔŋmai enyɔ";
        else if (ten == 3) tenWord = "nyɔŋmai etɛ";
        else if (ten == 4) tenWord = "nyɔŋmai ejwɛ";
        else if (ten == 5) tenWord = "nyɔŋmai enumɔ";
        else if (ten == 6) tenWord = "nyɔŋmai ekpaa";
        else if (ten == 7) tenWord = "nyɔŋmai kpawo";
        else if (ten == 8) tenWord = "nyɔŋmai kpaanyɔ";
        else tenWord = "nyɔŋmai nɛɛhu";

        if (unit == 0) return tenWord;
        return tenWord + " kɛ " + units[unit];
    }

    /**
     * Gets the appropriate Locale for the given language code
     */
    public static Locale getLocaleForLanguage(String code) {
        String normalized = normalizeLanguageCode(code);
        switch (normalized) {
            case LANG_FRENCH:
                return Locale.FRENCH;
            case LANG_TWI:
                return new Locale("ak");  // Use ISO 639-1 code for Akan/Twi
            case LANG_EWE:
                return new Locale("ee");
            case LANG_GA:
                return new Locale("gaa");
            case LANG_ENGLISH:
            default:
                return Locale.US;
        }
    }

    /**
     * Gets the speech recognition locale code for the given language
     */
    public static String getSpeechLocaleCode(String code) {
        String normalized = normalizeLanguageCode(code);
        switch (normalized) {
            case LANG_FRENCH:
                return "fr-FR";
            case LANG_ENGLISH:
            default:
                return "en-US";
        }
    }

    /**
     * Gets the alphabet/letters for the given language
     */
    public static String[] getAlphabetForLanguage(String code) {
        String normalized = normalizeLanguageCode(code);
        switch (normalized) {
            case LANG_TWI:
                return getTwiAlphabet();
            case LANG_EWE:
                return getEweAlphabet();
            case LANG_GA:
                return getGaAlphabet();
            case LANG_FRENCH:
            case LANG_ENGLISH:
            default:
                return getEnglishAlphabet();
        }
    }

    /**
     * English alphabet (A-Z)
     */
    public static String[] getEnglishAlphabet() {
        return new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    }

    /**
     * Twi alphabet - includes all Twi-specific letters with audio files
     * Audio files available: A, B, D, E, Ɛ, F, G, H, I, K, L, M, N, O, Ɔ, P, R, S, T, U, W, Y
     * Note: No audio for C, J, Q, V, X, Z in Twi
     * Total: 23 letters
     */
    public static String[] getTwiAlphabet() {
        return new String[]{"A", "B", "D", "E", "Ɛ", "F", "G", "H", "I", "K", "L", "M", "N", "O", "Ɔ", "P", "R", "S", "T", "U", "W", "Y"};
    }

    /**
     * Ewe alphabet - includes English letters and Ewe-specific letters with diacritics
     * Based on available audio files in res/raw:
     * A, B, D, Ɖ, E, Ɛ, F, Ƒ, G, Ɣ, H, I, K, L, M, N, Ŋ, O, Ɔ, P, R, S, T, U, V, Ʋ, W, X, Y, Z
     * Missing: C, J (no audio files)
     * Special: Ɖ (D with hook), Ƒ (F with hook), Ɣ (G with hook), Ŋ (Ng), Ʋ (V with hook)
     * Special: Ɛ (E open), Ɔ (O open)
     * Total: 29 letters
     */
    public static String[] getEweAlphabet() {
        return new String[]{
            "A", "B", "D", "Ɖ", "E", "Ɛ", "F", "Ƒ", "G", "Ɣ", "H", "I", "K", "L", "M",
            "N", "Ŋ", "O", "Ɔ", "P", "R", "S", "T", "U", "V", "Ʋ", "W", "X", "Y", "Z"
        };
    }

    /**
     * Ga alphabet - includes English letters and Ga-specific letters
     * Based on available audio files in res/raw:
     * A, B, D, E, Ɛ, F, G, H, I, J, K, L, M, N, Ŋ, O, Ɔ, P, R, S, T, U, V, W, Y, Z
     * Missing: C (no audio file available)
     * Special: Ɛ (E open), Ɔ (O open), Ŋ (Ng)
     * Total: 26 letters (includes both E/Ɛ and O/Ɔ as separate characters)
     */
    public static String[] getGaAlphabet() {
        return new String[]{
            "A", "B", "D", "E", "Ɛ", "F", "G", "H", "I", "J", "K", "L", "M", "N", "Ŋ",
            "O", "Ɔ", "P", "R", "S", "T", "U", "V", "W", "Y", "Z"
        };
    }
}
