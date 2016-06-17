package org.grobid.core.utilities;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splits a string using regex delimiters, optionally keeping the delimiters.
 *
 * In response to a <a href="http://stackoverflow.com/questions/275768">Stackoverflow challenge</a>.
 */
public class Splitter {
    /** Default pattern. */
    private static final Pattern DEFAULT_PATTERN = Pattern.compile("\\s+");

    /** Chosen pattern */
    private Pattern pattern;

    /** Flag for keeping the delimiters */
    private boolean keep_delimiters;

    /**
     * Constructs a new Splitter object.
     *  
     * @param pattern
     *          Pattern to use. Default is '\s+', meaning any whitespace.
     * @param keep_delimiters
     *          Flag to keep delimiters. Default is 'true'.
     */
    public Splitter(Pattern pattern, boolean keep_delimiters) {
        this.pattern = pattern;
        this.keep_delimiters = keep_delimiters;
    }
    public Splitter(String pattern, boolean keep_delimiters) {
        this(Pattern.compile(pattern==null?"":pattern), keep_delimiters);
    }
    public Splitter(Pattern pattern) { this(pattern, true); }
    public Splitter(String pattern) { this(pattern, true); }
    public Splitter(boolean keep_delimiters) { this(DEFAULT_PATTERN, keep_delimiters); }
    public Splitter() { this(DEFAULT_PATTERN); }

    /**
     * Splits a string using the pattern.
     * 
     * @return  Array of strings with each part. If keep_delimiters is active,
     * the indices will contain the matched delimiters.
     */
    public String[] split(String text) {
        if (text == null) {
            text = "";
        }

        int last_match = 0;
        LinkedList<String> splitted = new LinkedList<String>();

        Matcher m = this.pattern.matcher(text);

        // Iterate trough each match
        while (m.find()) {
            // Text since last match
            splitted.add(text.substring(last_match,m.start()));

            // The delimiter itself
            if (this.keep_delimiters) {
                splitted.add(m.group());
            }

            last_match = m.end();
        }
        // Trailing text
        splitted.add(text.substring(last_match));

        return splitted.toArray(new String[splitted.size()]);
    }
    
    public static void run_tests() {
        String[][] test_cases = {
            // Limit cases:
            // 'null' to be splitted with regexp 'null' gives []
            { null, null },
            // '' to be splitted with regexp 'null' gives []
            { "", null },
            // 'null' to be splitted with regexp '' gives []
            { null, "" },
            // '' to be splitted with regexp '' gives []
            { "", "" },

            // Border cases:
            // 'abcd' to be splitted with regexp 'ab' gives [ab], 'cd', []
            { "abcd", "ab" },
            // 'abcd' to be splitted with regexp 'cd' gives [], 'ab', [cd]
            { "abcd", "cd" },
            // 'abcd' to be splitted with regexp 'abcd' gives [abcd]
            { "abcd", "abcd" },
            // 'abcd' to be splitted with regexp 'bc' gives [], 'a', [bc], 'd', []
            { "abcd", "bc" },

            // Real cases:
            // 'abcd    efg  hi   j' to be splitted with regexp '[ \t\n\r\f]+'
            //   gives [], 'abcd', [   ], 'efg', [  ], 'hi', [   ], 'j', []
            { "abcd    efg  hi   j", "[ \\t\\n\\r\\f]+" }, 
            // ''ab','cd','eg'' to be splitted with regexp '\W+'
            //   gives ['], 'ab', [','], 'cd', [','], 'eg', [']
            { "'ab','cd','eg'", "\\W+" },

            // Split-like cases:
            // 'boo:and:foo' to be splitted with regexp ':'
            //     gives [], 'boo', [:], 'and', [:], 'foo', []
            { "boo:and:foo", ":" },
            // 'boo:and:foo' to be splitted with regexp 'o'
            //     gives [], 'b', [o], '', [o], ':and:f', [o], '', [o]
            { "boo:and:foo", "o" },
            // 'boo:and:foo' to be splitted with regexp 'o+'
            //     gives [], 'b', [oo], ':and:f', [oo]
            { "boo:and:foo", "o+" }
        };

        int test_counter = 1;
        for (String[] test : test_cases) {
            String text = test[0];
            String pattern = test[1];

            System.out.printf("Test case #%d:\n", test_counter++);
            System.out.printf("  Text:    '%s'\n", text);
            System.out.printf("  Pattern: /%s/\n", pattern);
            System.out.printf("  Parts:\n");

            Splitter splitter = new Splitter(pattern, true);

            int part_counter = 1;
            for (String part : splitter.split(text)) {
                System.out.printf("    %2d) '%s'\n", part_counter++, part);
            }

            System.out.println();
        }
    }
}