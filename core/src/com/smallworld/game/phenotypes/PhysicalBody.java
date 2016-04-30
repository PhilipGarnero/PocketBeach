package com.smallworld.game.phenotypes;

import com.smallworld.game.Actor;

import java.util.ArrayList;

public class PhysicalBody {
    private Actor actor;

    public PhysicalBody(ArrayList<String> genes, Actor actor) {
        this.actor = actor;
    }
}
