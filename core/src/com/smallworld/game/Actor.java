package com.smallworld.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.smallworld.game.phenotypes.Brain;
import com.smallworld.game.phenotypes.Features;
import com.smallworld.game.phenotypes.Vitals;

import java.util.ArrayList;
import java.util.Iterator;

public class Actor {
    public int id;
    public GameWorld world;
    public Genotype genotype;
    public Body body;
    public Vitals vitals;
    public Features features;
    private Brain brain;
    private Experiment.FitnessEvaluation fitnessEvaluation;
    public MouseJoint mouseJoint = null;

    public Actor(GameWorld world, int id, Experiment.FitnessEvaluation fitnessEvaluation, Genotype genotype, Vector2 position) {
        this.world = world;
        this.id = id;
        this.fitnessEvaluation = fitnessEvaluation;
        if (genotype == null)
            this.genotype = new Genotype(null);
        else
            this.genotype = genotype;

        this.createBody(position);
        this.buildPhenotype();
    }

    private void createBody(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        bodyDef.fixedRotation = true;
        this.body = this.world.physics.createBody(bodyDef);
        this.body.setUserData(this);
    }

    public void buildPhenotype() {
        this.vitals = (Vitals)this.genotype.getPhenotype("vitals", this);
        this.brain = (Brain)this.genotype.getPhenotype("brain", this);
        this.features = (Features)this.genotype.getPhenotype("features", this);

        if (!this.brain.isViable())
            this.vitals.alive = false;

        CircleShape circle = new CircleShape();
        circle.setRadius(1f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.5f;
        //fixtureDef.filter.groupIndex = -1;
        //fixtureDef.isSensor = true;
        this.body.createFixture(fixtureDef);
        circle.dispose();
    }

    public void dispose() {
        if (this.mouseJoint != null) {
            this.world.physics.destroyJoint(this.mouseJoint);
            this.world.screen.inputs.mouseJoint = null;
        }
        this.world.physics.destroyBody(this.body);
    }

    public void render() {
        this.world.shapeRenderer.setColor(this.getHealthColor());
        this.world.shapeRenderer.circle(this.body.getPosition().x, this.body.getPosition().y, 1);
    }

    private Color getHealthColor() {
        float health = this.vitals.getEnergyPercentage();
        float r = 1 - health + 1f/(float)Math.exp(Math.pow((health - 0.5f), 2) / 0.1);
        float g = health + 1f/(float)Math.exp(Math.pow((health - 0.5f), 2) / 0.1);
        float b = 0;
        float a = 1;
        return new Color(r, g, b, a);
    }

    public float getFitness() {
        return this.fitnessEvaluation.evaluate(this);
    }

    public boolean isDead() {
        return !this.vitals.alive;
    }

    public boolean inWater() {
        return this.body.getPosition().x > this.world.tide;
    }

    public void eat(Food f) {
        f.eaten = true;
        this.vitals.addEnergy(f.nutritionalValue);
    }

    public void update() {
        this.vitals.update(this.inWater());
        if (!this.isDead())
            this.applyValuesToActuators(this.brain.think(this.getSensorsValues()));
        this.render();
    }

    private ArrayList<Float> getSensorsValues() {
        ArrayList<Float> values = new ArrayList<Float>();
        for (final Features.Sensor sensor : this.features.sensors)
            values.add(sensor.getValue());
        return values;
    }

    private void applyValuesToActuators(ArrayList<Float> outputs) {
        Iterator<Float> outIt = outputs.iterator();
        Iterator<Features.Actuator> actuatorIt = this.features.actuators.iterator();
        while (outIt.hasNext() && actuatorIt.hasNext()) {
            actuatorIt.next().act(outIt);
        }
    }
}
