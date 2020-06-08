package com.rscgl.ui.util;

import com.badlogic.gdx.graphics.Color;

public class ColorUtil {

    public static String getLevelColor(int myLevel, int opponentLevel) {
        int levelDelta = 0;
        if (myLevel > 0 && opponentLevel > 0) {
            levelDelta = myLevel - opponentLevel;
        }
        String level = "";
        if (levelDelta < 0) {
            level = "@or1@";
        }

        if (levelDelta < -3) {
            level = "@or2@";
        }

        if (levelDelta < -6) {
            level = "@or3@";
        }

        if (levelDelta < -9) {
            level = "@red@";
        }

        if (levelDelta > 0) {
            level = "@gr1@";
        }

        if (levelDelta > 3) {
            level = "@gr2@";
        }

        if (levelDelta > 6) {
            level = "@gr3@";
        }

        if (levelDelta > 9) {
            level = "@gre@";
        }
        level = " " + level + "(level-" + opponentLevel + ")";
        return ColorUtil.parse(level);
    }

    public static Color toColor(int color) {
        float red = ((color >> 16) & 0xff);
        float green = ((color >> 8) & 0xff);
        float blue = ((color) & 0xff);
        return new Color(red / 255F, green / 255F, blue / 255F, 1f);
    }

    public static String parse(String str) {
        String parseCopy = str;
        try {
            for (int i = 0; parseCopy.length() > i; ++i) {
                if (str.charAt(i) == '@' && i + 4 < str.length() && str.charAt(i + 4) == '@') {
                    final String key = str.substring(i + 1, i + 4);
                    String color = "";
                    if (key.equalsIgnoreCase("red")) {
                        color = "#FF0000";
                    } else if (key.equalsIgnoreCase("lre")) {
                        color = "#FF9040";
                    } else if (key.equalsIgnoreCase("yel")) {
                        color = "#FFFF00";
                    } else if (key.equalsIgnoreCase("gre")) {
                        color = "#00FF00";
                    } else if (key.equalsIgnoreCase("blu")) {
                        color = "#0000FF";
                    } else if (key.equalsIgnoreCase("cya")) {
                        color = "#00FFFF";
                    } else if (key.equalsIgnoreCase("mag")) {
                        color = "#FF00FF";
                    } else if (key.equalsIgnoreCase("whi")) {
                        color = "#FFFFFF";
                    } else if (key.equalsIgnoreCase("bla")) {
                        color = "#000000";
                    } else if (key.equalsIgnoreCase("dre")) {
                        color = "#C00000";
                    } else if (key.equalsIgnoreCase("sil")) {
                        color = "#C0C0C0";
                    } else if (key.equalsIgnoreCase("plt")) {
                        color = "#C6DEFF";
                    } else if (key.equalsIgnoreCase("pty")) {
                        color = "#1E90FF";
                    } else if (key.equalsIgnoreCase("gl1")) {
                        color = "#75bb95";//"#75bb95;
                    } else if (key.equalsIgnoreCase("gl2")) {
                        color = "#fe6447";//"#75bb95;
                    } else if (key.equalsIgnoreCase("ora")) {
                        color = "#FF9040";
                    } else if (key.equalsIgnoreCase("ran")) {
                        // color = (int) ("#FFFFFF * Math.random());
                    } else if (key.equalsIgnoreCase("or1")) {
                        color = "#FFB000";
                    } else if (key.equalsIgnoreCase("or2")) {
                        color = "#FF7000";
                    } else if (key.equalsIgnoreCase("or3")) {
                        color = "#FF3000";
                    } else if (key.equalsIgnoreCase("gr1")) {
                        color = "#C0FF00";
                    } else if (key.equalsIgnoreCase("gr2")) {
                        color = "#80FF00";
                    } else if (key.equalsIgnoreCase("gr3")) {
                        color = "#40FF00";
                    }
                    i += 4;
                    str = str.replaceAll("@" + key + "@", "[" + color + "]");
                }
            }
        } catch(Exception e) {
            return str;
        }
        return str;
    }
}
