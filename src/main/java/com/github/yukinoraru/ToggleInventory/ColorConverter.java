package com.github.yukinoraru.ToggleInventory;

/**
 * ColorConverter.
 */
public class ColorConverter {

        /**
         * To hex.
         *
         * @param r the r
         * @param g the g
         * @param b the b
         * @return the string
         */
        public static String toHex(int r, int g, int b) {
            return "0x" + toBrowserHexValue(r) + toBrowserHexValue(g)
                    + toBrowserHexValue(b);
        }

        /**
         * To browser hex value.
         *
         * @param number the number
         * @return the string
         */
        private static String toBrowserHexValue(int number) {
            StringBuilder builder = new StringBuilder(
                    Integer.toHexString(number & 0xff));
            while (builder.length() < 2) {
                builder.append("0");
            }
            return builder.toString().toUpperCase();
        }

}
