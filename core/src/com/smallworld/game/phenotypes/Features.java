package com.smallworld.game.phenotypes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.smallworld.game.Actor;
import com.smallworld.game.Genotype;
import com.smallworld.game.Rand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class Features {
    private static int TRANSDUCER_CODE_LENGTH = 2;
    public ArrayList<Sensor> sensors = new ArrayList<Sensor>();
    public ArrayList<Actuator> actuators = new ArrayList<Actuator>();
    private ArrayList<Transducer> transducers = new ArrayList<Transducer>();
    private Actor actor;

    public Features(ArrayList<String> genes, Actor actor) {
        this.actor = actor;
        for (Class<? extends Transducer> cls : GeneCoder.decode(genes)) {
           try {
               this.transducers.add((cls.getConstructor(Features.class, Actor.class).newInstance(this, this.actor)));
               if (Sensor.class.isAssignableFrom(cls))
                   this.sensors.add((Sensor)this.transducers.get(this.transducers.size() - 1));
               else if (Actuator.class.isAssignableFrom(cls))
                   this.actuators.add((Actuator)this.transducers.get(this.transducers.size() - 1));
           } catch (Exception e) {
               Gdx.app.log("IMPOSSIBLE ERROR", "A new instance of a Transducer subclass couldn't be created in Features' constructor.");
               Gdx.app.log("IMPOSSIBLE ERROR", e.toString());
           }
        }
    }

    public static class GeneCoder {
        public static final Map<String, Class<? extends Transducer>> table;
        static {
            Map<String, Class<? extends Transducer>> tmp = new HashMap<String, Class<? extends Transducer>>();
            tmp.put("01", MoveActuator.class);
            tmp.put("02", RotateActuator.class);
            tmp.put("03", LayEggsActuator.class);
            tmp.put("81", OrientationSensor.class);
            tmp.put("82", RadarSensor.class);
            tmp.put("83", TideSensor.class);
            tmp.put("84", WaterSensor.class);
            tmp.put("85", EnergySensor.class);
            tmp.put("86", TemperatureSensor.class);
            table = Collections.unmodifiableMap(tmp);
        }

        public static ArrayList<Class<? extends Transducer>> decode(ArrayList<String> genes) {
            ArrayList<Class<? extends Transducer>> transducers = new ArrayList<Class<? extends Transducer>>();
            for (final String gene : genes) {
                for (int i = 0; i + TRANSDUCER_CODE_LENGTH <= gene.length(); i = i + TRANSDUCER_CODE_LENGTH) {
                    String code = gene.substring(i, i + TRANSDUCER_CODE_LENGTH);
                    if (table.containsKey(code))
                        transducers.add(table.get(code));
                }
            }
            return transducers;
        }

        public static String encode(Features phenotype) {
            String gene = "";
            for (final Transducer transducer : phenotype.transducers)
                gene += tableGetValue(transducer.getClass());
            return gene.toUpperCase();
        }

        public static String generateRandomDNA() {
            String gene = "";
            for (final Object f : Rand.rChoices(Arrays.asList(table.keySet().toArray()), Rand.rInt(3, 10)))
                gene += (String)f;
            return gene;
        }

        private static String tableGetValue(Class<? extends Transducer> value) {
            for (Map.Entry<String, Class<? extends Transducer>> entry : table.entrySet()) {
                if (Objects.equals(value, entry.getValue())) {
                    return entry.getKey();
                }
            }
            return "";
        }
    }

    public String mutateDNAFromPhenotype() {
        Features clone = new Features(new ArrayList<String>(Arrays.asList(GeneCoder.encode(Features.this))), Features.this.actor);
        String add = "";
        if (!clone.transducers.isEmpty() && Rand.rNorm() > Genotype.GENE_MUTATION_PROB) {
            if (Rand.rNorm() > 0.5f)
                clone.transducers.remove(Rand.rInt(0, clone.transducers.size() - 1));
            else
                add = (String) Rand.rChoice(Arrays.asList(GeneCoder.table.keySet().toArray()));
        }
        return GeneCoder.encode(clone) + add;
    }

    abstract class Transducer {
    }

    public class Sensor extends Transducer {
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


    public abstract class Actuator extends Transducer {
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