package com.gangverk.phonebook;

import java.util.HashMap;

public class StringAccentRemover {

    @SuppressWarnings("serial")
	private static final HashMap<Character, Character> accents  = new HashMap<Character, Character>(){
        {
            put('Á', 'A');
            put('É', 'E');
            put('Í', 'I');
            put('Ó', 'O');
            put('Ú', 'U');
            put('Ý', 'Y');
            put('Ö', 'O');
            
            put('á', 'a');
            put('é', 'e');
            put('í', 'i');
            put('ó', 'o');
            put('ú', 'u');
            put('ý', 'y');
            put('ö', 'o');
        }
    };
    /**
     * remove accented from a string and replace with ascii equivalent
     */
    public static String removeAccents(String s) {
        char[] result = s.toCharArray();
        for(int i=0; i<result.length; i++) {
            Character replacement = accents.get(result[i]);
            if (replacement!=null) result[i] = replacement;
        }
        return new String(result);
    }

}
