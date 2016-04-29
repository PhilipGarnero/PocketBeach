package com.smallworld.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Population {
    public ArrayList<Actor> actors = new ArrayList<Actor>();
    private boolean sorted = false;
    private float rankProbability;
    private boolean reverseSort;

    public Population(float rankProbability, boolean reverseSort) {
        this.rankProbability = rankProbability;
        this.reverseSort = reverseSort;
    }

    public int size() {
        return this.actors.size();
    }

    public void append(Actor actor) {
        this.actors.add(actor);
        this.sorted = false;
    }

    public Actor selectBestFitness() {
        this.sort();
        if (this.actors.isEmpty())
            return null;
        return (this.actors.get(0));
    }

    public Actor selectWorstFitness() {
        this.sort();
        if (this.actors.isEmpty())
            return null;
        return (this.actors.get(this.actors.size() - 1));
    }

    public Actor selectByRank() {
        this.sort();
        if (this.actors.isEmpty())
            return null;
        for (Actor actor : this.actors) {
            if (Math.random() < this.rankProbability) {
                return (actor);
            }
        }
        return (this.actors.get(this.actors.size() - 1));
    }

    private void sort() {
        if (!this.sorted) {
            Collections.sort(this.actors, new Comparator<Actor>() {
                @Override
                public int compare(Actor o1, Actor o2) {
                    if (o1.getFitness() < o2.getFitness())
                        return (-1);
                    else if (o1.getFitness() > o2.getFitness())
                        return (1);
                    else
                        return (0);
                }
            });
            if (this.reverseSort)
                Collections.reverse(this.actors);
            this.sorted = true;
        }
    }

    public void clear() {
        for (Actor actor : this.actors) {
            actor.dispose();
        }
        this.actors.clear();
        this.sorted = false;
    }
}
