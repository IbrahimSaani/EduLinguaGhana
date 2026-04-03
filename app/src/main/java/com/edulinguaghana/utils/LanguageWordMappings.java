package com.edulinguaghana.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Language-specific word mappings for the matching quiz.
 * Maps letters to words that start with that letter in each language.
 */
public class LanguageWordMappings {

    // English letter-word mappings
    private static final Map<String, String> ENGLISH_MAPPINGS = new LinkedHashMap<String, String>() {{
        put("A", "Apple");
        put("B", "Ball");
        put("C", "Cat");
        put("D", "Dog");
        put("E", "Egg");
        put("F", "Fish");
        put("G", "Girl");
        put("H", "House");
        put("I", "Ice");
        put("J", "Jellyfish");
        put("K", "Kite");
        put("L", "Lion");
        put("M", "Monkey");
        put("N", "Nest");
        put("O", "Orange");
        put("P", "Pig");
        put("Q", "Queen");
        put("R", "Rabbit");
        put("S", "Sun");
        put("T", "Tiger");
        put("U", "Umbrella");
        put("V", "Violin");
        put("W", "Water");
        put("X", "Xylophone");
        put("Y", "Yo-yo");
        put("Z", "Zebra");
    }};

    // French letter-word mappings
    private static final Map<String, String> FRENCH_MAPPINGS = new LinkedHashMap<String, String>() {{
        put("A", "Arbre");      // Tree
        put("B", "Balle");      // Ball
        put("C", "Chat");       // Cat
        put("D", "Dogue");      // Dog
        put("E", "Œuf");        // Egg
        put("F", "Poisson");    // Fish
        put("G", "Fille");      // Girl
        put("H", "Maison");     // House
        put("I", "Glace");      // Ice
        put("J", "Méduse");     // Jellyfish
        put("K", "Cerf-volant");// Kite
        put("L", "Lion");       // Lion
        put("M", "Singe");      // Monkey
        put("N", "Nid");        // Nest
        put("O", "Orange");     // Orange
        put("P", "Cochon");     // Pig
        put("Q", "Reine");      // Queen
        put("R", "Lapin");      // Rabbit
        put("S", "Soleil");     // Sun
        put("T", "Tigre");      // Tiger
        put("U", "Parapluie");  // Umbrella
        put("V", "Violon");     // Violin
        put("W", "Eau");        // Water
        put("X", "Xylophone");  // Xylophone
        put("Y", "Yo-yo");      // Yo-yo
        put("Z", "Zèbre");      // Zebra
    }};

    // Twi letter-word mappings
    private static final Map<String, String> TWI_MAPPINGS = new LinkedHashMap<String, String>() {{
        put("A", "Adowa");      // A traditional dance
        put("B", "Bɔ");         // Goat
        put("C", "Cyɛ");        // Light/brightness
        put("D", "Dɔm");        // Darkness
        put("E", "Ɛbɛ");        // Sore
        put("F", "Frɔ");        // From/away
        put("G", "Gyae");       // Leave/abandon
        put("H", "Hyɛ");        // Climb
        put("I", "Irigyina");   // Lightning
        put("J", "Juani");      // A personal name
        put("K", "Kyɛw");       // Hide
        put("L", "Lɔ");         // Fish
        put("M", "Mma");        // Palms
        put("N", "Nananom");    // Chiefs
        put("O", "Obi");        // Chief
        put("P", "Paa");        // Father
        put("Q", "Ɔdam");       // Shield
        put("R", "Rana");       // Frog
        put("S", "Safoa");      // Kola nut
        put("T", "Toa");        // War
        put("U", "Ɔdɔ");        // Love
        put("V", "Ɔva");        // House
        put("W", "Warabɔ");     // Warrior
        put("X", "Ɔnsɔ");       // Agreement
        put("Y", "Yaw");        // A day name
        put("Z", "Zane");       // Arrow
    }};

    // Ewe letter-word mappings
    private static final Map<String, String> EWE_MAPPINGS = new LinkedHashMap<String, String>() {{
        put("A", "Asi");        // Room/house
        put("B", "Bolo");       // Goat
        put("C", "Ciele");      // Light
        put("D", "Dze");        // Darkness
        put("E", "Eƒo");        // Sore
        put("F", "Fo");         // Away
        put("G", "Gbe");        // Climb
        put("H", "Hehe");       // Hide
        put("I", "Ilo");        // Lightning
        put("J", "Jome");       // Juniper
        put("K", "Kpe");        // Catch
        put("L", "Lo");         // Fish
        put("M", "Mmɛ");        // Palms
        put("N", "Nɔ");         // Chief
        put("O", "Ɔƒe");        // Chief
        put("P", "Papa");       // Father
        put("Q", "Qɔ");         // Agreement
        put("R", "Ra");         // Frog
        put("S", "Soxo");       // Kola nut
        put("T", "To");         // War
        put("U", "Ɔdɔ");        // Love
        put("V", "Vɔ");         // House
        put("W", "Wɔlɔ");       // Warrior
        put("X", "Xe");         // Break
        put("Y", "Yaw");        // A day name
        put("Z", "Za");         // Arrow
    }};

    // Ga letter-word mappings
    private static final Map<String, String> GA_MAPPINGS = new LinkedHashMap<String, String>() {{
        put("A", "Akai");       // Friend
        put("B", "Bɔ");         // Goat
        put("C", "Cie");        // Light
        put("D", "Dim");        // Darkness
        put("E", "Efi");        // Sore
        put("F", "Fi");         // Away
        put("G", "Ge");         // Give
        put("H", "Hi");         // Do
        put("I", "Ilo");        // Lightning
        put("J", "Jo");         // Drink
        put("K", "Kɔ");         // Go
        put("L", "Lo");         // Fish
        put("M", "Mmɔ");        // Mother
        put("N", "Naa");        // Chief
        put("O", "Obi");        // Chief
        put("P", "Paa");        // Father
        put("Q", "Qɔ");         // Agreement
        put("R", "Ra");         // Frog
        put("S", "Safo");       // Kola nut
        put("T", "Tɔ");         // War
        put("U", "Ɔdɔ");        // Love
        put("V", "Vi");         // House
        put("W", "Wɔ");         // They
        put("X", "Xe");         // Break
        put("Y", "Ya");         // A day name
        put("Z", "Za");         // Arrow
    }};

    /**
     * Gets the letter-word mapping for a given language
     */
    public static Map<String, String> getLetterWordMapping(String languageCode) {
        String normalized = LanguageConversionUtils.normalizeLanguageCode(languageCode);
        switch (normalized) {
            case LanguageConversionUtils.LANG_FRENCH:
                return new HashMap<>(FRENCH_MAPPINGS);
            case LanguageConversionUtils.LANG_TWI:
                return new HashMap<>(TWI_MAPPINGS);
            case LanguageConversionUtils.LANG_EWE:
                return new HashMap<>(EWE_MAPPINGS);
            case LanguageConversionUtils.LANG_GA:
                return new HashMap<>(GA_MAPPINGS);
            case LanguageConversionUtils.LANG_ENGLISH:
            default:
                return new HashMap<>(ENGLISH_MAPPINGS);
        }
    }

    /**
     * Gets a sample of letters and words for the matching quiz
     * Returns up to 3 pairs per language
     */
    public static java.util.List<Map.Entry<String, String>> getSampleMatchingPairs(String languageCode, int count) {
        Map<String, String> mapping = getLetterWordMapping(languageCode);
        java.util.List<Map.Entry<String, String>> result = new java.util.ArrayList<>();
        int i = 0;
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            if (i >= count) break;
            result.add(entry);
            i++;
        }
        return result;
    }
}

