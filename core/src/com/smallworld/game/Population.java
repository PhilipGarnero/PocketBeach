package com.smallworld.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class Population {
    public ArrayList<Actor> actors = new ArrayList<Actor>();
    private boolean sorted = false;
    private float rankProbability;
    private boolean reverseSort;
    private int maxPop;
    private Experiment experiment;

    public Population(Experiment exp) {
        this.experiment = exp;
        this.maxPop = exp.MAX_POP_SIZE;
        this.rankProbability = exp.RANK_PROBABILITY_CONSTANT;
        this.reverseSort = exp.REVERSE_RANK;
    }

    public int size() {
        return this.actors.size();
    }

    public void append(Actor actor) {
        if (this.actors.size() <= this.maxPop) {
            this.actors.add(actor);
            this.sorted = false;
        }
    }

    public void removeDeads() {
        Iterator<Actor> iterator = this.actors.iterator();
        while (iterator.hasNext()) {
            Actor currentActor = iterator.next();
            if (currentActor.isDead()) {
                currentActor.dispose();
                iterator.remove();
            }
        }
    }

    public void fillPop() {
        for (int i = this.size(); i < this.maxPop; i++)
            this.append(this.experiment.createActor(null));
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
