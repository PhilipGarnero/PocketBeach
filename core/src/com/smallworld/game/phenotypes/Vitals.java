package com.smallworld.game.phenotypes;

import com.badlogic.gdx.Gdx;
import com.smallworld.game.Actor;

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

    public void update(boolean inWater) {
        float dt = Gdx.graphics.getDeltaTime();
        this.energy -= dt * (inWater ? -0.1 : 0.4);
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

    public float getEnergyPercentage() {
        return this.energy / 100f;
    }

    public float getTemperaturePercentage() {
        return this.temperature / 100f;
    }
}