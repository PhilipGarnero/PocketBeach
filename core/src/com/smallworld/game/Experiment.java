package com.smallworld.game;

import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;
import java.util.List;

public class Experiment {
    public final int MAX_POP_SIZE = 100;
    public final int RANDOM_ACTORS_NUMBER = 10;
    public final float RANK_PROBABILITY_CONSTANT = 0.2f;
    public final boolean REVERSE_RANK = false;
    public final int TIME_BETWEEN_GEN = 15;
    public int popIndex;
    public int currentGen;
    private GameWorld world;
    public Population population;
    public long nextGen = System.nanoTime() / 1000000000;

    public Experiment(GameWorld world) {
        this.world = world;
        this.population = new Population(this);
    }

    public void start() {
        this.popIndex = 1;
        this.currentGen = 1;
        this.population.fillPop();
    }

    public void stop() {
        this.population.clear();
    }

    interface FitnessEvaluation {
        float evaluate(Actor actor);
    }

    public Actor createActor(Genotype genotype) {
        Vector2 pos = new Vector2(Rand.rInt(1, (int)this.world.width - 1),
                                  Rand.rInt(1, (int)this.world.height - 1));
        Actor actor = new Actor(this.world, this.popIndex, new FitnessEvaluation() {
            public float evaluate(Actor actor) {
                if (actor.isDead())
                    return 0xFFFF;
                return Math.abs(actor.body.getPosition().x - actor.world.tide);
            }
        }, genotype, pos);
        this.popIndex += 1;
        return (actor);
    }

    public void update() {
        for (Actor actor : this.population.actors)
            actor.update();
        if (this.TIME_BETWEEN_GEN == 0) {
            this.population.removeDeads();
            if (this.population.size() == 0)
                this.population.fillPop();
        }
    }

    public void nextGeneration() {
        this.nextGen = System.nanoTime() / 1000000000;
        Population newPop = new Population(this);
        for (int i = 0; i < this.RANDOM_ACTORS_NUMBER; i++) {
            newPop.append(this.createActor(null));
        }
        for (int i = 0; i < this.MAX_POP_SIZE - this.RANDOM_ACTORS_NUMBER; i++) {
            Genotype new_genotype = Genotype.reproduce(this.population.selectByRank().genotype, this.population.selectByRank().genotype);
            newPop.append(this.createActor(new_genotype));
        }
        this.population.clear();
        this.population = newPop;
        this.currentGen += 1;
    }
}



