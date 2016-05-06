package com.smallworld.game.phenotypes;

import com.badlogic.gdx.Gdx;
import com.smallworld.game.Actor;
import com.smallworld.game.Genotype;
import com.smallworld.game.Rand;

import java.util.ArrayList;

public class Vitals {
    public float energy;
    public float temperature;
    public boolean alive;
    private Actor actor;

    public Vitals(ArrayList<String> genes, Actor actor) {
        this.actor = actor;
        this.alive = true;
        this.energy = 100f;
        this.temperature = 36f;
    }

    public static class GeneCoder {
        public static void decode(ArrayList<String> genes) {
        }

        public static String encode(Vitals phenotype) {
            String gene = "";
            return gene.toUpperCase();
        }

        public static String generateRandomDNA() {
            String gene = "";
            return gene;
        }
    }

    public String mutateDNAFromPhenotype() {
        String gene = GeneCoder.encode(this);
        if (Rand.rNorm() > Genotype.GENE_MUTATION_PROB) {
        }
        return gene;
    }

    public void update(boolean inWater) {
        float dt = Gdx.graphics.getDeltaTime();
        this.energy -= dt * (inWater ? 0.8 : 1) * 5;
        this.temperature += dt * (inWater ? -0.5 : 0.3);
        while (this.temperature < 32) {
            this.energy -= 1;
            this.temperature += 0.5;
        }
        while (this.temperature > 40) {
            this.energy -= 1;
            this.temperature -= 0.5;
        }
        if (this.energy <= 0) {
            this.alive = false;
        }
    }

    public void giveBirth() {
        this.energy -= 20;
        this.temperature += 2;
    }

    public void addEnergy(float value) {
        this.energy += value;
        this.energy = Math.min(this.energy, 300f);
    }

    public float getEnergyPercentage() {
        return this.energy / 100f;
    }

    public float getTemperaturePercentage() {
        return this.temperature / 100f;
    }
}
