package com.smallworld.game.phenotypes;

import com.smallworld.game.Actor;
import com.smallworld.game.Genotype;
import com.smallworld.game.Rand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Brain {
    private static int NEURON_ID_CODE_LENGTH = 2;
    private static int WEIGHT_CODE_LENGTH = 3;
    private static int CONNECTION_CODE_LENGTH = NEURON_ID_CODE_LENGTH * 2 + WEIGHT_CODE_LENGTH;
    private static float BASE_10_MAX = (float)Math.pow(Genotype.GENE_BASE, WEIGHT_CODE_LENGTH) / 2f;

    private ArrayList<InputNeuron> inputs = new ArrayList<InputNeuron>();
    private ArrayList<Neuron> hiddens = new ArrayList<Neuron>();
    private ArrayList<OutputNeuron> outputs = new ArrayList<OutputNeuron>();
    private ArrayList<Synapse> connections = new ArrayList<Synapse>();
    private HashMap<Integer, ArrayList<ConnectionBuilder>> tree = new HashMap<Integer, ArrayList<ConnectionBuilder>>();
    private Actor actor;

    public Brain(ArrayList<String> genes, Actor actor) {
        this.actor = actor;
        this.netBuilder(GeneCoder.decode(genes));
    }

    public static class GeneCoder {
        public static ArrayList<ConnectionBuilder> decode(ArrayList<String> genes) {
            ArrayList<ConnectionBuilder> connections = new ArrayList<ConnectionBuilder>();
            for (String gene : genes) {
                for (int i = 0; i + CONNECTION_CODE_LENGTH <= gene.length(); i = i + CONNECTION_CODE_LENGTH) {
                    String code = gene.substring(i, i + CONNECTION_CODE_LENGTH);
                    connections.add(new ConnectionBuilder(code));
                }
            }
            return connections;
        }

        public static String encode(Brain phenotype) {
            String gene = "";
            for (final Synapse synapse : phenotype.connections) {
                String inId = Integer.toHexString(synapse.origin.id);
                while (inId.length() < NEURON_ID_CODE_LENGTH)
                    inId = "0" + inId;
                gene += inId;
                String weight = Integer.toHexString(Math.round(synapse.weight * BASE_10_MAX + BASE_10_MAX));
                while (weight.length() < WEIGHT_CODE_LENGTH)
                    weight = "0" + weight;
                gene += weight;
                String outId = Integer.toHexString(synapse.aim.id);
                while (outId.length() < NEURON_ID_CODE_LENGTH)
                    outId = "0" + outId;
                gene += outId;
            }
            return gene;
        }

        public static String generateRandomDNA() {
            String gene = "";
            int nb = Rand.rInt(1, 30);
            for (int i = 0; i < nb; i++)
                gene += Rand.rChoices(Genotype.GENE_CHAR_POOL, CONNECTION_CODE_LENGTH);
            return gene;
        }
    }

    private static class ConnectionBuilder {
        private int inNeuronId;
        private float weight;
        private int outNeuronId;

        public ConnectionBuilder(String code) {
            this.inNeuronId = Integer.parseInt(code.substring(0, NEURON_ID_CODE_LENGTH), Genotype.GENE_BASE);
            this.weight = (Integer.parseInt(code.substring(NEURON_ID_CODE_LENGTH,
                    NEURON_ID_CODE_LENGTH + WEIGHT_CODE_LENGTH), Genotype.GENE_BASE) - BASE_10_MAX) / BASE_10_MAX;
            this.outNeuronId = Integer.parseInt(code.substring(NEURON_ID_CODE_LENGTH + WEIGHT_CODE_LENGTH), Genotype.GENE_BASE);
        }
    }

    private void pathChecker(ArrayList<Integer> path, int check) {
        if (path.contains(check))
            throw new RuntimeException();
        else {
            path.add(check);
            if (this.tree.containsKey(check)) {
                for (ConnectionBuilder con : this.tree.get(check))
                   this.pathChecker(new ArrayList<Integer>(path), con.outNeuronId);
            }
        }
    }

    private Neuron treeBuilder(int neuron_id, ArrayList<Integer> inputs, ArrayList<Integer> outputs) {
        Neuron neuron = this.getNeuron(neuron_id);
        if (neuron == null) {
            if (inputs.contains(neuron_id)) {
                this.inputs.add(new InputNeuron(neuron_id));
                neuron = this.inputs.get(this.inputs.size() - 1);
            } else if (outputs.contains(neuron_id)) {
                this.outputs.add(new OutputNeuron(neuron_id));
                neuron = this.outputs.get(this.outputs.size() - 1);
            } else {
                neuron = new Neuron(neuron_id);
                this.hiddens.add(neuron);
            }
        }
        if (tree.containsKey(neuron_id)) {
            ArrayList<ConnectionBuilder> work = tree.get(neuron_id);
            for (ConnectionBuilder connection : work) {
                Synapse synapse = new Synapse(connection.weight, neuron, treeBuilder(connection.outNeuronId, inputs, outputs));
                this.connections.add(synapse);
                neuron.addConnection(synapse);
            }
        }
        return neuron;
    }

    private void netBuilder(ArrayList<ConnectionBuilder> connections) {
        ArrayList<Integer> ins = new ArrayList<Integer>();
        ArrayList<Integer> outs = new ArrayList<Integer>();

        for (ConnectionBuilder connection : connections) {
            try {
                ArrayList<Integer> path = new ArrayList<Integer>();
                path.add(connection.inNeuronId);
                pathChecker(path, connection.outNeuronId);
            } catch (RuntimeException e) { // A connection is forming a cycle.
                continue;
            }
            if (!this.tree.containsKey(connection.inNeuronId))
                this.tree.put(connection.inNeuronId, new ArrayList<ConnectionBuilder>());
            this.tree.get(connection.inNeuronId).add(connection);
            ins.add(connection.inNeuronId);
            outs.add(connection.outNeuronId);
        }
        ArrayList<Integer> inputs = new ArrayList<Integer>(ins);
        inputs.removeAll(outs);
        ArrayList<Integer> outputs = new ArrayList<Integer>(outs);
        outputs.removeAll(ins);

        for (int neuron_id : inputs)
            treeBuilder(neuron_id, inputs, outputs);
    }

    public boolean isViable() {
        if (this.inputs.isEmpty() || this.outputs.isEmpty())
            return false;
        return true;
    }

    private Neuron getNeuron(int neuron_id) {
        for (Neuron neuron : this.hiddens) {
            if (neuron.id == neuron_id)
                return neuron;
        }
        for (Neuron neuron : this.inputs) {
            if (neuron.id == neuron_id)
                return neuron;
        }
        for (Neuron neuron : this.outputs) {
            if (neuron.id == neuron_id)
                return neuron;
        }
        return null;
    }

    public ArrayList<Float> think(ArrayList<Float> in) {
        ArrayList<Float> out = new ArrayList<Float>();

        Iterator<Float> inIt = in.iterator();
        Iterator<InputNeuron> neuronIt = this.inputs.iterator();
        while (neuronIt.hasNext() && inIt.hasNext())
            neuronIt.next().activate(inIt.next());

        for (OutputNeuron neuron : this.outputs) {
            out.add(neuron.getValue());
            neuron.reset();
        }
        for (InputNeuron neuron : this.inputs)
            neuron.reset();
        for (Neuron neuron : this.hiddens)
            neuron.reset();
        for (Synapse connection : this.connections) {
            connection.alterWeight();
            connection.reset();
        }
        return out;
    }

    public void drawNet() {
        //TODO
    }

    class Neuron {
        private float THRESHOLD_LEVEL = 0.5f;
        private int id;
        protected ArrayList<Synapse> connections = new ArrayList<Synapse>();
        protected float value = 0.0f;
        private boolean hasFired = false;

        public Neuron(int id) {
            this.id = id;
        }

        public void addConnection(Synapse connection) {
            this.connections.add(connection);
        }

        public void activate(float value) {
            this.value += value;
            if (this.value > this.THRESHOLD_LEVEL)
                this.fire();
        }

        protected void fire() {
            if (!this.hasFired) {
                for (Synapse connection : this.connections)
                    connection.propagate();
                this.hasFired = true;
            }
        }

        public void reset() {
            this.hasFired = false;
        }

    }

    class InputNeuron extends Neuron {
        public InputNeuron(int id) {
            super(id);
        }

        @Override
        public void activate(float value) {
            this.value = value;
            this.fire();
        }

        @Override
        protected void fire() {
            for (Synapse connection : this.connections)
                connection.propagate(this.value);
        }
    }

    class OutputNeuron extends Neuron {
        public OutputNeuron(int id) {
            super(id);
        }

        @Override
        public void reset() {
            this.value = 0.0f;
        }

        @Override
        public void activate(float value) {
            this.value += value;
        }

        public float getValue() {
            float val = this.value;
            if (val > 1.0f)
                val = 1.0f;
            else if (val < -1.0f)
                val = -1.0f;
            return val;
        }
    }

    class Synapse {
        private float WEIGHT_ALTERATION_FACTOR = 0.0005f;
        private float weight;
        private Neuron origin;
        private Neuron aim;
        private boolean used;

        public Synapse(float weight, Neuron origin, Neuron aim) {
            this.weight = weight;
            this.origin = origin;
            this.aim = aim;
            this.used = false;
        }

        public void propagate() {
            this.aim.activate(this.weight);
            this.used = this.aim.hasFired;
        }

        public void propagate(float value) {
            this.aim.activate(value * this.weight);
            this.used = this.aim.hasFired;
        }

        public void alterWeight() {
            if ((this.used && this.weight > 0f) || (!this.used && this.weight < 0f))
                this.weight += this.weight * this.WEIGHT_ALTERATION_FACTOR;
            else
                this.weight -= this.weight * this.WEIGHT_ALTERATION_FACTOR;
        }

        public void reset() {
            this.used = false;
        }
    }
}

