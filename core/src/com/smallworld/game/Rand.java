package com.smallworld.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Rand {
    private static Random r = new Random();

    public static int rInt(int min, int max) {
        return r.nextInt((max - min) + 1) + min;
    }

    public static float rFloat(float min, float max) {
        return r.nextFloat() * (max - min) + min;
    }

    public static float rNorm() {
        return r.nextFloat();
    }

    public static char rChoice(String from) {
        return from.charAt(r.nextInt(from.length()));
    }

    public static <T> T rChoice(ArrayList<T> from) {
        return from.get(r.nextInt(from.size()));
    }

    public static <T> T rChoice(List<T> from) {
        return from.get(r.nextInt(from.size()));
    }
}
