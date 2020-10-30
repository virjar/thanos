package com.virjar.thanos.api.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.Map;

public class URLEncodedUtils {
    private static final char QP_SEP_A = '&';
    private static final char QP_SEP_S = ';';
    private static final String NAME_VALUE_SEPARATOR = "=";
    private static final int RADIX = 16;

    public static String format(
            final Map<String, String> parameters,
            final Charset charset) {
        return format(parameters, QP_SEP_A, charset);
    }

    public static String format(
            final Map<String, String> parameters,
            final char parameterSeparator,
            final Charset charset) {
        final StringBuilder result = new StringBuilder();
        for (final Map.Entry<String, String> parameter : parameters.entrySet()) {
            final String encodedName = encodeFormFields(parameter.getKey(), charset);
            final String encodedValue = encodeFormFields(parameter.getValue(), charset);
            if (result.length() > 0) {
                result.append(parameterSeparator);
            }
            result.append(encodedName);
            if (encodedValue != null) {
                result.append(NAME_VALUE_SEPARATOR);
                result.append(encodedValue);
            }
        }
        return result.toString();
    }

    private static String encodeFormFields(final String content, final Charset charset) {
        if (content == null) {
            return null;
        }
        return urlEncode(content, charset != null ? charset : StandardCharsets.UTF_8);
    }

    private static String urlEncode(
            final String content,
            final Charset charset) {
        if (content == null) {
            return null;
        }
        final StringBuilder buf = new StringBuilder();
        final ByteBuffer bb = charset.encode(content);
        while (bb.hasRemaining()) {
            final int b = bb.get() & 0xff;
            if (URLEncodedUtils.URLENCODER.get(b)) {
                buf.append((char) b);
            } else if (b == ' ') {
                buf.append('+');
            } else {
                buf.append("%");
                final char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, RADIX));
                final char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, RADIX));
                buf.append(hex1);
                buf.append(hex2);
            }
        }
        return buf.toString();
    }

    private static final BitSet PATHSAFE = new BitSet(256);
    private static final BitSet USERINFO = new BitSet(256);
    private static final BitSet PUNCT = new BitSet(256);
    private static final BitSet UNRESERVED = new BitSet(256);
    private static final BitSet RESERVED = new BitSet(256);
    private static final BitSet URLENCODER = new BitSet(256);
    private static final BitSet URIC = new BitSet(256);

    static {
        // unreserved chars
        // alpha characters
        for (int i = 'a'; i <= 'z'; i++) {
            UNRESERVED.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            UNRESERVED.set(i);
        }
        // numeric characters
        for (int i = '0'; i <= '9'; i++) {
            UNRESERVED.set(i);
        }
        UNRESERVED.set('_'); // these are the charactes of the "mark" list
        UNRESERVED.set('-');
        UNRESERVED.set('.');
        UNRESERVED.set('*');
        URLENCODER.or(UNRESERVED); // skip remaining unreserved characters
        UNRESERVED.set('!');
        UNRESERVED.set('~');
        UNRESERVED.set('\'');
        UNRESERVED.set('(');
        UNRESERVED.set(')');
        // punct chars
        PUNCT.set(',');
        PUNCT.set(';');
        PUNCT.set(':');
        PUNCT.set('$');
        PUNCT.set('&');
        PUNCT.set('+');
        PUNCT.set('=');
        // Safe for userinfo
        USERINFO.or(UNRESERVED);
        USERINFO.or(PUNCT);

        // URL path safe
        PATHSAFE.or(UNRESERVED);
        PATHSAFE.set('/'); // segment separator
        PATHSAFE.set(';'); // param separator
        PATHSAFE.set(':'); // rest as per list in 2396, i.e. : @ & = + $ ,
        PATHSAFE.set('@');
        PATHSAFE.set('&');
        PATHSAFE.set('=');
        PATHSAFE.set('+');
        PATHSAFE.set('$');
        PATHSAFE.set(',');

        RESERVED.set(';');
        RESERVED.set('/');
        RESERVED.set('?');
        RESERVED.set(':');
        RESERVED.set('@');
        RESERVED.set('&');
        RESERVED.set('=');
        RESERVED.set('+');
        RESERVED.set('$');
        RESERVED.set(',');
        RESERVED.set('['); // added by RFC 2732
        RESERVED.set(']'); // added by RFC 2732

        URIC.or(RESERVED);
        URIC.or(UNRESERVED);
    }
}
