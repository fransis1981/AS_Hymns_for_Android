package com.fransis1981.Android_Hymns;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by Fransis on 01/09/2014.
 * Class introduced to surrogate Normalizer class which is only available on API level 9 and higher.
 */
public class SupportNormalizer {
    private static final Pattern STRANGE = Pattern.compile("[^a-zA-Z0-9-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private static final String DIACRITIC_CHARS = "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9"
            + "\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD"
            + "\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177"
            + "\u00C3\u00E3\u00D5\u00F5\u00D1\u00F1"
            + "\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF"
            + "\u00C5\u00E5" + "\u00C7\u00E7" + "\u0150\u0151\u0170\u0171";

    private static final String PLAIN_CHARS = "AaEeIiOoUu" // grave
            + "AaEeIiOoUuYy" // acute
            + "AaEeIiOoUuYy" // circumflex
            + "AaOoNn" // tilde
            + "AaEeIiOoUuYy" // umlaut
            + "Aa" // ring
            + "Cc" // cedilla
            + "OoUu"; // double acute

    private static char[] lookup = new char[0x180];

    static {
        Arrays.fill(lookup, (char) 0);
        for (int i = 0; i < DIACRITIC_CHARS.length(); i++)
            lookup[DIACRITIC_CHARS.charAt(i)] = PLAIN_CHARS.charAt(i);
    }

    /*
    public static String slugify(String s) {
        String nowhitespace = WHITESPACE.matcher(s).replaceAll("-");
        String unaccented = unaccentify(nowhitespace);
        String slug = STRANGE.matcher(unaccented).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
    */

    public static String unaccentify(String s) {
        StringBuilder sb = new StringBuilder(s);
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (c > 126 && c < lookup.length) {
                char replacement = lookup[c];
                if (replacement > 0)
                    sb.setCharAt(i, replacement);
            }
        }
        return sb.toString();
    }
}