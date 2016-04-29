package com.smallworld.game;

import com.badlogic.gdx.Gdx;
import com.smallworld.game.phenotypes.Brain;
import com.smallworld.game.phenotypes.PhysicalBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class Genotype {
    private final static float GENE_MUTATION_PROB = 0.70f;
    private final static float GENE_IF_MUTATION_ADD_PROB = 0.1f;
    private final static float GENE_IF_MUTATION_NO_ADD_DEL_PROB = 0.50f;
    private final static float GENE_CROSSOVER_PROB = 0.70f;
    private final static float GENE_IF_CROSSOVER_DOUBLE_PROB = 0.50f;
    private final static float GENE_IF_CROSSOVER_AND_DOUBLE_UNBALANCED_PROB = 0.30f;
    private final static String GENE_CHAR_POOL = "0123456789ABCDEF";
    public final static int GENE_BASE = 16;
    private final static int[] GENE_LENGTH = {100, 200};
    private final static int[] GENE_IMPORTANT_CODE_NUMBER = {5, 10};
    private final static int GENE_STEP = 2;
    private final static String GENE_START = "00";
    private final static String GENE_STOP = "FF";
    private final HashMap<String, String> GENE_PHENOTYPES_IDS = new HashMap<String, String>();

    public String dna;
    private HashMap<String, ArrayList<String>> genes = new HashMap<String, ArrayList<String>>();

    public Genotype(String dna) {
        this.GENE_PHENOTYPES_IDS.put("body", "01");
        this.GENE_PHENOTYPES_IDS.put("brain", "02");
        this.GENE_PHENOTYPES_IDS.put("properties", "03");

        this.dna = dna;
        if (this.dna == null)
            this.generateDna();
        this.genes = this.extractGenes(this.dna);
    }

    private void generateDna() {
        String[] importantCodes = {GENE_START, GENE_STOP};
        ArrayList<String> geneCodes = new ArrayList<String>(this.GENE_PHENOTYPES_IDS.values());
        this.dna = "";
        while (!this.isViable()) {
            this.dna = "";
            int length = Rand.rInt(GENE_LENGTH[0], GENE_LENGTH[1]);
            for (int i = 0; i < length; i++)
                this.dna = this.dna + Rand.rChoice(GENE_CHAR_POOL);
            length = Rand.rInt(GENE_IMPORTANT_CODE_NUMBER[0], GENE_IMPORTANT_CODE_NUMBER[1]);
            for (int i = 0; i < length; i++) {
                int where = Rand.rInt(GENE_STEP, this.dna.length() - 2 * GENE_STEP);
                where -= where % GENE_STEP;
                String code = Rand.rChoice(Arrays.asList(importantCodes));
                this.dna = this.dna.substring(0, where) + code + this.dna.substring(where + GENE_STEP);
                if (code.equals(GENE_START))
                    this.dna = this.dna.substring(0, where + GENE_STEP)
                            + Rand.rChoice(geneCodes)
                            + this.dna.substring(where + GENE_STEP * 2);
            }
        }
    }

    private boolean isViable() {
        if (this.dna.isEmpty())
            return false;
        HashMap<String, ArrayList<String>> newGenes = this.extractGenes(this.dna);
        if (newGenes.get(this.GENE_PHENOTYPES_IDS.get("brain"))  != null
                && newGenes.get(this.GENE_PHENOTYPES_IDS.get("body")) != null) {
            return true;
        }
        return false;
    }

    private HashMap<String, ArrayList<String>> extractGenes(String dna) {
        HashMap<String, ArrayList<String>> genes = new HashMap<String, ArrayList<String>>();
        ArrayList<String> dnaSeq = new ArrayList<String>();
        for (int i = 0; i < dna.length(); i = i + GENE_STEP)
            dnaSeq.add(dna.substring(i, Math.min(i + GENE_STEP, dna.length())));
        String gene = "";
        String geneId = "";
        boolean inGeneSequence = false;
        while (!dnaSeq.isEmpty()) {
            String code = dnaSeq.get(0);
            dnaSeq.remove(0);
            if (code.equals(GENE_START) && !inGeneSequence) {
                inGeneSequence = true;
                geneId = "code";
                gene = "";
            } else if (code.equals(GENE_STOP) && inGeneSequence) {
                if (!geneId.isEmpty() && !gene.isEmpty())
                    genes.get(geneId).add(gene);
                inGeneSequence = false;
            } else if (inGeneSequence && geneId.equals("code")) {
                geneId = code;
                if (!genes.containsKey(geneId))
                    genes.put(geneId, new ArrayList<String>());
            } else if (inGeneSequence) {
                gene = gene + code;
            }
        }
        return genes;
    }

    private ArrayList<String> getGenesForPhenotype(String name) {
        ArrayList<String> pGenes = this.genes.get(this.GENE_PHENOTYPES_IDS.get(name));
        if (pGenes == null)
            pGenes = new ArrayList<String>();
        return pGenes;
    }

    public Object getPhenotype(String name) {
        if (name.equals("brain")) {
            return new Brain(this.getGenesForPhenotype("brain"));
        } else if (name.equals("body")) {
            return new PhysicalBody(this.getGenesForPhenotype("body"));
        }
        return null;
    }

    private void mutate() {
        if (Rand.rNorm() < GENE_MUTATION_PROB) {
            int i;
            if (Rand.rNorm() < GENE_IF_MUTATION_ADD_PROB) {
                i = Rand.rInt(0, this.dna.length() - 1);
                this.dna = this.dna.substring(0, i) + Rand.rChoice(GENE_CHAR_POOL) + this.dna.substring(i);
            } else if (Rand.rNorm() < GENE_IF_MUTATION_NO_ADD_DEL_PROB) {
                i = Rand.rInt(1, this.dna.length() - 1);
                this.dna = this.dna.substring(0, i - 1) + this.dna.substring(i);
            } else {
                i = Rand.rInt(1, this.dna.length() - 1);
                this.dna = this.dna.substring(0, i - 1) + Rand.rChoice(GENE_CHAR_POOL) + this.dna.substring(i);
            }
            this.genes = this.extractGenes(this.dna);
        }
    }

    private static String crossover(String fatherDna, String motherDna) {
        String childDna;
        int max_cut = Math.min(fatherDna.length(), motherDna.length()) - 1;
        int cut1, cut2, cut3;
        if (Rand.rNorm() < GENE_CROSSOVER_PROB) {
            cut1 = Rand.rInt(0, max_cut);
            if (Rand.rNorm() < GENE_IF_CROSSOVER_DOUBLE_PROB) {
                cut2 = Rand.rInt(0, max_cut);
                if (cut2 < cut1) {
                    cut1 = cut1 + cut2;
                    cut2 = cut1 - cut2;
                    cut1 = cut1 - cut2;
                }
                if (Rand.rNorm() < GENE_IF_CROSSOVER_AND_DOUBLE_UNBALANCED_PROB) {
                    cut3 = Rand.rInt(0, max_cut);
                    if (cut3 < cut1) {
                        cut1 = cut1 + cut3;
                        cut3 = cut1 - cut3;
                        cut1 = cut1 - cut3;
                    }
                    childDna = fatherDna.substring(0, cut1) + motherDna.substring(cut1, cut2) + fatherDna.substring(cut3);
                } else
                    childDna = fatherDna.substring(0, cut1) + motherDna.substring(cut1, cut2) + fatherDna.substring(cut2);
            } else
                childDna = fatherDna.substring(0, cut1) + motherDna.substring(cut1);
        }
        else {
            String[] ch = {fatherDna, motherDna};
            childDna = Rand.rChoice(Arrays.asList(ch));
        }
        return childDna;
    }

    public static Genotype reproduce(Genotype father, Genotype mother) {
        Genotype child = new Genotype(crossover(father.dna, mother.dna));
        child.mutate();
        if (child.isViable())
            return child;
        else
            return new Genotype(null);
    }
}
