package io.github.xpakx.outline.service;

import org.springframework.stereotype.Service;

@Service
public class LinkService {
    private static final String charactersString = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final char[] characters = charactersString.toCharArray();
    private final int base = characters.length;

    public String encode(Long input) {
        StringBuilder encodedString = new StringBuilder();

        if(input == 0) {
            return String.valueOf(characters[0]);
        }

        while(input > 0) {
            encodedString.append(characters[(int) (input % base)]);
            input /= base;
        }

        return encodedString.reverse().toString();
    }

    public long decode(String input) {
        char[] characters = input.toCharArray();
        int length = characters.length;
        int decoded = 0;

        int counter = 1;
        for (char character : characters) {
            decoded += charactersString.indexOf(character) * Math.pow(base, length - counter);
            counter++;
        }
        return decoded;
    }
}
