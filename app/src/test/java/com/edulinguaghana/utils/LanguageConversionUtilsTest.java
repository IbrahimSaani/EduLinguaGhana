package com.edulinguaghana.utils;

import org.junit.Test;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Unit tests for LanguageConversionUtils
 * Tests language detection, conversion, and utility methods
 */
public class LanguageConversionUtilsTest {

    // ============ Number Conversion Tests ============

    @Test
    public void testEnglishNumberConversion() {
        assertEquals("One", LanguageConversionUtils.convertNumberToWord(1, "en"));
        assertEquals("Two", LanguageConversionUtils.convertNumberToWord(2, "en"));
        assertEquals("Ten", LanguageConversionUtils.convertNumberToWord(10, "en"));
        assertEquals("Eleven", LanguageConversionUtils.convertNumberToWord(11, "en"));
        assertEquals("Twenty", LanguageConversionUtils.convertNumberToWord(20, "en"));
        assertEquals("Twenty-One", LanguageConversionUtils.convertNumberToWord(21, "en"));
        assertEquals("Fifty", LanguageConversionUtils.convertNumberToWord(50, "en"));
        assertEquals("One Hundred", LanguageConversionUtils.convertNumberToWord(100, "en"));
    }

    @Test
    public void testFrenchNumberConversion() {
        // Test that conversions return non-empty strings
        assertNotEquals("", LanguageConversionUtils.convertNumberToWord(1, "fr"));
        assertNotEquals("", LanguageConversionUtils.convertNumberToWord(21, "fr"));
        assertNotEquals("", LanguageConversionUtils.convertNumberToWord(50, "fr"));
        assertNotEquals("", LanguageConversionUtils.convertNumberToWord(100, "fr"));

        // Test that they're different
        assertNotEquals(
            LanguageConversionUtils.convertNumberToWord(1, "fr"),
            LanguageConversionUtils.convertNumberToWord(2, "fr")
        );
    }

    @Test
    public void testTwiNumberConversion() {
        // Test that conversions return non-empty strings
        assertNotEquals("", LanguageConversionUtils.convertNumberToWord(1, "ak"));
        assertNotEquals("", LanguageConversionUtils.convertNumberToWord(21, "ak"));
        assertNotEquals("", LanguageConversionUtils.convertNumberToWord(50, "ak"));
        assertNotEquals("", LanguageConversionUtils.convertNumberToWord(100, "ak"));

        // Test that they're different
        assertNotEquals(
            LanguageConversionUtils.convertNumberToWord(1, "ak"),
            LanguageConversionUtils.convertNumberToWord(2, "ak")
        );
    }

    @Test
    public void testEweNumberConversion() {
        // Test that conversions return non-empty strings
        assertNotEquals("", LanguageConversionUtils.convertNumberToWord(1, "ee"));
        assertNotEquals("", LanguageConversionUtils.convertNumberToWord(21, "ee"));
        assertNotEquals("", LanguageConversionUtils.convertNumberToWord(50, "ee"));
        assertNotEquals("", LanguageConversionUtils.convertNumberToWord(100, "ee"));

        // Test that they're different
        assertNotEquals(
            LanguageConversionUtils.convertNumberToWord(1, "ee"),
            LanguageConversionUtils.convertNumberToWord(2, "ee")
        );
    }

    @Test
    public void testGaNumberConversion() {
        assertEquals("ekome", LanguageConversionUtils.convertNumberToWord(1, "gaa"));
        assertEquals("enyɔ", LanguageConversionUtils.convertNumberToWord(2, "gaa"));
        assertEquals("nyɔŋma", LanguageConversionUtils.convertNumberToWord(10, "gaa"));
        assertEquals("nyɔŋma kɛ ekome", LanguageConversionUtils.convertNumberToWord(11, "gaa"));
        assertEquals("iwuo", LanguageConversionUtils.convertNumberToWord(20, "gaa"));
        assertEquals("iwuo kɛ ekome", LanguageConversionUtils.convertNumberToWord(21, "gaa"));
        assertEquals("iwuo enyɔ kɛ nyɔŋma", LanguageConversionUtils.convertNumberToWord(50, "gaa"));
        assertEquals("ohaa", LanguageConversionUtils.convertNumberToWord(100, "gaa"));
    }

    // ============ Edge Cases ============

    @Test
    public void testNumberConversionEdgeCases() {
        // Out of range - should return empty string
        assertEquals("", LanguageConversionUtils.convertNumberToWord(0, "en"));
        assertEquals("", LanguageConversionUtils.convertNumberToWord(101, "en"));
        assertEquals("", LanguageConversionUtils.convertNumberToWord(-1, "en"));
        assertEquals("", LanguageConversionUtils.convertNumberToWord(200, "en"));
    }

    // ============ Language Code Normalization Tests ============

    @Test
    public void testLanguageCodeNormalization() {
        // Twi variations
        assertEquals("ak", LanguageConversionUtils.normalizeLanguageCode("ak"));
        assertEquals("ak", LanguageConversionUtils.normalizeLanguageCode("twi"));
        assertEquals("ak", LanguageConversionUtils.normalizeLanguageCode("TWI"));
        assertEquals("ak", LanguageConversionUtils.normalizeLanguageCode("Twi"));

        // Ewe variations
        assertEquals("ee", LanguageConversionUtils.normalizeLanguageCode("ee"));
        assertEquals("ee", LanguageConversionUtils.normalizeLanguageCode("ewe"));
        assertEquals("ee", LanguageConversionUtils.normalizeLanguageCode("EWE"));

        // Ga variations
        assertEquals("gaa", LanguageConversionUtils.normalizeLanguageCode("gaa"));
        assertEquals("gaa", LanguageConversionUtils.normalizeLanguageCode("ga"));
        assertEquals("gaa", LanguageConversionUtils.normalizeLanguageCode("GA"));

        // English variations
        assertEquals("en", LanguageConversionUtils.normalizeLanguageCode("en"));

        // French variations
        assertEquals("fr", LanguageConversionUtils.normalizeLanguageCode("fr"));

        // Null should default to English
        assertEquals("en", LanguageConversionUtils.normalizeLanguageCode(null));
    }

    // ============ Ghanaian Language Detection Tests ============

    @Test
    public void testGhanaianLanguageDetection() {
        // Twi is Ghanaian
        assertTrue(LanguageConversionUtils.isGhanaianLanguage("ak"));
        assertTrue(LanguageConversionUtils.isGhanaianLanguage("twi"));
        assertTrue(LanguageConversionUtils.isGhanaianLanguage("Twi"));

        // Ewe is Ghanaian
        assertTrue(LanguageConversionUtils.isGhanaianLanguage("ee"));
        assertTrue(LanguageConversionUtils.isGhanaianLanguage("ewe"));

        // Ga is Ghanaian
        assertTrue(LanguageConversionUtils.isGhanaianLanguage("gaa"));
        assertTrue(LanguageConversionUtils.isGhanaianLanguage("ga"));

        // English is NOT Ghanaian
        assertFalse(LanguageConversionUtils.isGhanaianLanguage("en"));
        assertFalse(LanguageConversionUtils.isGhanaianLanguage("english"));

        // French is NOT Ghanaian
        assertFalse(LanguageConversionUtils.isGhanaianLanguage("fr"));
        assertFalse(LanguageConversionUtils.isGhanaianLanguage("french"));

        // Null is NOT Ghanaian
        assertFalse(LanguageConversionUtils.isGhanaianLanguage(null));
    }

    // ============ Alphabet Retrieval Tests ============

    @Test
    public void testEnglishAlphabet() {
        String[] alphabet = LanguageConversionUtils.getAlphabetForLanguage("en");
        assertEquals(26, alphabet.length);
        assertTrue(contains(alphabet, "A"));
        assertTrue(contains(alphabet, "Z"));
        assertFalse(contains(alphabet, "Ɛ"));
        assertFalse(contains(alphabet, "Ɔ"));
    }

    @Test
    public void testFrenchAlphabet() {
        String[] alphabet = LanguageConversionUtils.getAlphabetForLanguage("fr");
        assertEquals(26, alphabet.length);
        assertTrue(contains(alphabet, "A"));
        assertTrue(contains(alphabet, "Z"));
    }

    @Test
    public void testTwiAlphabet() {
        String[] alphabet = LanguageConversionUtils.getAlphabetForLanguage("ak");
        assertEquals(22, alphabet.length);
        assertTrue(contains(alphabet, "A"));
        assertTrue(contains(alphabet, "Ɛ"));
        assertTrue(contains(alphabet, "Ɔ"));
        assertFalse(contains(alphabet, "Q"));
        assertFalse(contains(alphabet, "V"));
        assertFalse(contains(alphabet, "X"));
        assertFalse(contains(alphabet, "Z"));
    }

    @Test
    public void testEweAlphabet() {
        String[] alphabet = LanguageConversionUtils.getAlphabetForLanguage("ee");
        // Ewe alphabet includes special characters but missing C and J
        // Total: 30 letters (A, B, D, Ɖ, E, Ɛ, F, Ƒ, G, Ɣ, H, I, K, L, M, N, Ŋ, O, Ɔ, P, R, S, T, U, V, Ʋ, W, X, Y, Z)
        assertEquals("Ewe alphabet should have 30 letters (26 - C - J + 5 special = 30)", 30, alphabet.length);
        assertTrue(contains(alphabet, "A"));
        assertTrue(contains(alphabet, "Z"));
        assertFalse("Ewe should not have C (no audio file)", contains(alphabet, "C"));
        assertFalse("Ewe should not have J (no audio file)", contains(alphabet, "J"));
        assertTrue("Ewe should contain Ɛ (E open)", contains(alphabet, "Ɛ"));
        assertTrue("Ewe should contain Ɔ (O open)", contains(alphabet, "Ɔ"));
        assertTrue("Ewe should contain Ɖ (D with hook)", contains(alphabet, "Ɖ"));
        assertTrue("Ewe should contain Ƒ (F with hook)", contains(alphabet, "Ƒ"));
        assertTrue("Ewe should contain Ɣ (G with hook)", contains(alphabet, "Ɣ"));
        assertTrue("Ewe should contain Ŋ (Ng)", contains(alphabet, "Ŋ"));
        assertTrue("Ewe should contain Ʋ (V with hook)", contains(alphabet, "Ʋ"));
    }

    @Test
    public void testGaAlphabet() {
        String[] alphabet = LanguageConversionUtils.getAlphabetForLanguage("gaa");
        // Ga alphabet has 26 letters: A, B, D, E, Ɛ, F, G, H, I, J, K, L, M, N, Ŋ, O, Ɔ, P, R, S, T, U, V, W, Y, Z
        // Missing: C (no audio file)
        // Includes special: Ɛ (E open), Ɔ (O open), Ŋ (Ng)
        // Total: 26 (because we include both E and Ɛ, both O and Ɔ)
        assertEquals("Ga alphabet should have 26 letters (includes E/Ɛ and O/Ɔ variations)", 26, alphabet.length);
        assertTrue(contains(alphabet, "A"));
        assertTrue(contains(alphabet, "Z"));
        assertFalse("Ga should not have C (no audio file)", contains(alphabet, "C"));
        assertFalse(contains(alphabet, "Q"));
        assertFalse(contains(alphabet, "X"));
        assertTrue("Ga should contain Ɛ (E open)", contains(alphabet, "Ɛ"));
        assertTrue("Ga should contain Ɔ (O open)", contains(alphabet, "Ɔ"));
        assertTrue("Ga should contain Ŋ (Ng)", contains(alphabet, "Ŋ"));
    }

    // ============ Locale Mapping Tests ============

    @Test
    public void testLocaleForLanguage() {
        Locale enLocale = LanguageConversionUtils.getLocaleForLanguage("en");
        assertEquals(Locale.US, enLocale);

        Locale frLocale = LanguageConversionUtils.getLocaleForLanguage("fr");
        assertEquals(Locale.FRENCH, frLocale);

        // Ghanaian languages return custom locales
        Locale akLocale = LanguageConversionUtils.getLocaleForLanguage("ak");
        assertNotNull(akLocale);
        assertEquals("ak", akLocale.getLanguage());

        Locale eeLocale = LanguageConversionUtils.getLocaleForLanguage("ee");
        assertNotNull(eeLocale);
        assertEquals("ee", eeLocale.getLanguage());

        Locale gaaLocale = LanguageConversionUtils.getLocaleForLanguage("gaa");
        assertNotNull(gaaLocale);
        assertEquals("gaa", gaaLocale.getLanguage());
    }

    // ============ Speech Locale Code Tests ============

    @Test
    public void testSpeechLocaleCode() {
        assertEquals("en-US", LanguageConversionUtils.getSpeechLocaleCode("en"));
        assertEquals("fr-FR", LanguageConversionUtils.getSpeechLocaleCode("fr"));
        // Ghanaian languages default to en-US for speech recognition
        assertEquals("en-US", LanguageConversionUtils.getSpeechLocaleCode("ak"));
    }

    // ============ Helper Methods ============

    /**
     * Helper method to check if array contains string
     */
    private boolean contains(String[] array, String value) {
        for (String s : array) {
            if (s.equals(value)) {
                return true;
            }
        }
        return false;
    }

    // ============ Integration Tests ============

    @Test
    public void testAllLanguagesHaveNumbers() {
        String[] languages = {"en", "fr", "ak", "ee", "gaa"};
        for (String lang : languages) {
            for (int i = 1; i <= 100; i++) {
                String result = LanguageConversionUtils.convertNumberToWord(i, lang);
                assertNotEquals("Number conversion for " + i + " in " + lang + " should not be empty",
                    "", result);
            }
        }
    }

    @Test
    public void testAllLanguagesHaveAlphabets() {
        String[] languages = {"en", "fr", "ak", "ee", "gaa"};
        for (String lang : languages) {
            String[] alphabet = LanguageConversionUtils.getAlphabetForLanguage(lang);
            assertTrue("Alphabet for " + lang + " should not be empty", alphabet.length > 0);
            assertTrue("Alphabet for " + lang + " should have at least 20 letters", alphabet.length >= 20);
        }
    }

    @Test
    public void testLanguageNormalizationIsIdempotent() {
        String[] languages = {"en", "fr", "ak", "ee", "gaa"};
        for (String lang : languages) {
            String normalized1 = LanguageConversionUtils.normalizeLanguageCode(lang);
            String normalized2 = LanguageConversionUtils.normalizeLanguageCode(normalized1);
            assertEquals("Normalization should be idempotent for " + lang,
                normalized1, normalized2);
        }
    }
}

