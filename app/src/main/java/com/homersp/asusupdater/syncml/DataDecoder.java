package com.homersp.asusupdater.syncml;

public class DataDecoder {
    public static String decode(String str) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '%' && i + 1 < str.length()) {
                switch (str.charAt(i + 1)) {
                    case '%':
                        sb.append(c);
                        i++;
                        break;
                    case 'u':
                        if (i + 5 >= str.length()) {
                            break;
                        }

                        sb.append((char) Long.parseLong(str.substring(i + 2, i + 6), 16));

                        i += 5;

                        break;
                    default:
                        if (i + 3 >= str.length()) {
                            break;
                        }

                        sb.append((char) Integer.parseInt(str.substring(i + 1, i + 3), 16));

                        i += 2;
                        break;
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
