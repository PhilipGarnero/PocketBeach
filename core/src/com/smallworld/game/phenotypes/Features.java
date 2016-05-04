package com.smallworld.game.phenotypes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
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
        this.actuators.add(new RotateActuator(this.actor));
        this.actuators.add(new LayEggsActuator(this.actor));

        this.sensors.add(new OrientationSensor(this.actor));
        this.sensors.add(new RadarSensor(this.actor));
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

    public class OrientationSensor extends Sensor {
        public OrientationSensor(Actor actor) {
            super(actor);
        }

        @Override
        public float getValue() {
            return actor.body.getAngle() / 2 * (float)Math.PI;
        }
    }

    public class RadarSensor extends Sensor {
        public RadarSensor(Actor actor) {
            super(actor);

            float radius = 3;
            Vector2[] vertices = new Vector2[8];
            vertices[0] = new Vector2(0, 0);
            for (int i = 0; i <= 6; i++) {
                float angle = i / 6.0f * (float)(Math.PI / 4f) - (float)(Math.PI / 8f);
                vertices[i + 1] = new Vector2(radius * (float)Math.cos(angle), radius * (float)Math.sin(angle));
            }
            FixtureDef f = new FixtureDef();
            PolygonShape a = new PolygonShape();
            a.set(vertices);
            f.shape = a;
            f.isSensor = true;
            actor.body.createFixture(f).setUserData(this);
            a.dispose();
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

    public class RotateActuator extends Actuator {
        public RotateActuator(Actor actor) {
            super(actor);
        }

        @Override
        public void act(Iterator<Float> it) {
            float[] outputs = this.getOutputs(it, 1);
            this.actor.body.setTransform(this.actor.body.getPosition(), outputs[0] * 2 * (float)Math.PI);
        }
    }

    public class LayEggsActuator extends Actuator {
        private long lastLay;

        public LayEggsActuator(Actor actor) {
            super(actor);
            this.lastLay = System.nanoTime() / 1000000000;
        }

        @Override
        public void act(Iterator<Float> it) {
            float[] outputs = this.getOutputs(it, 1);
            long currentTime = System.nanoTime() / 1000000000;
            if (outputs[0] > 0f && currentTime - this.lastLay > Actor.EGG_SPAN) {
                this.lastLay = currentTime;
                this.actor.layEggs();
            }
        }
    }
}