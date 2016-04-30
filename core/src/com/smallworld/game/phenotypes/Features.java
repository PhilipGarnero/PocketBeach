package com.smallworld.game.phenotypes;

import com.badlogic.gdx.math.Vector2;
import com.smallworld.game.Actor;

import java.util.ArrayList;
import java.util.Iterator;

public class Features {
    public ArrayList<Sensor> sensors = new ArrayList<Sensor>();
    public ArrayList<Actuator> actuators = new ArrayList<Actuator>();
    private Actor actor;

    public Features(ArrayList<String> genes, Actor actor) {
        this.actor = actor;
        this.actuators.add(new MoveActuator(this.actor));
        this.sensors.add(new TideSensor(this.actor));
        this.sensors.add(new WaterSensor(this.actor));
        this.sensors.add(new EnergySensor(this.actor));
        this.sensors.add(new TemperatureSensor(this.actor));
    }

    public class Sensor {
        protected float value = 0f;
        protected Actor actor;

        public Sensor(Actor actor) {
            this.actor = actor;
        }

        public void setValue(float value) {
            this.value = value;
        }

        public float getValue() {
            return this.value;
        }
    }

    public class TideSensor extends Sensor {
        public TideSensor(Actor actor) {
            super(actor);
        }

        @Override
        public float getValue() {
            return (actor.body.getPosition().x - actor.world.tide > 0) ? 1 : -1;
        }
    }

    public class WaterSensor extends Sensor {
        public WaterSensor(Actor actor) {
            super(actor);
        }

        @Override
        public float getValue() {
            return actor.inWater() ? 1 : 0;
        }
    }

    public class TemperatureSensor extends Sensor {
        public TemperatureSensor(Actor actor) {
            super(actor);
        }

        @Override
        public float getValue() {
            return actor.vitals.getTemperaturePercentage();
        }
    }

    public class EnergySensor extends Sensor {
        public EnergySensor(Actor actor) {
            super(actor);
        }

        @Override
        public float getValue() {
            return actor.vitals.getEnergyPercentage();
        }
    }

    public abstract class Actuator {
        protected Actor actor;

        public Actuator(Actor actor) {
            this.actor = actor;
        }

        protected float[] getOutputs(Iterator<Float> it, int nb) {
            float[] outputs = new float[nb];
            int i = 0;
            while (i < nb && it.hasNext())
                outputs[i++] = it.next();
            while (i < nb)
                outputs[i++] = 0;
            return outputs;
        }

        public abstract void act(Iterator<Float> it);
    }

    public class MoveActuator extends Actuator {
        public MoveActuator(Actor actor) {
            super(actor);
        }

        @Override
        public void act(Iterator<Float> it) {
            float[] outputs = this.getOutputs(it, 2);
            Vector2 v = new Vector2(outputs[0], outputs[1]).scl(3);
            if (this.actor.inWater())
                this.actor.body.applyForceToCenter(v.x, v.y, true);
            else {
                this.actor.body.setLinearVelocity(v);
            }
        }
    }
}