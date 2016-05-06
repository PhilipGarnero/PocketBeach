package com.smallworld.game;

import com.smallworld.game.phenotypes.Brain;
import com.smallworld.game.phenotypes.Features;
import com.smallworld.game.phenotypes.PhysicalBody;
import com.smallworld.game.phenotypes.Vitals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class Genotype {
    public final static float GENE_MUTATION_PROB = 0.40f;
    private final static float GENE_CROSSOVER_PROB = 0.70f;
    private final static float GENE_IF_CROSSOVER_DOUBLE_PROB = 0.50f;
    private final static float GENE_IF_CROSSOVER_AND_DOUBLE_UNBALANCED_PROB = 0.30f;
    public final static String GENE_CHAR_POOL = "0123456789ABCDEF";
    public final static int GENE_BASE = GENE_CHAR_POOL.length();
    private final static String GENE_SEP = "Z";
    private final HashMap<String, String> GENE_PHENOTYPES_IDS = new HashMap<String, String>();

    public String dna;
    private HashMap<String, ArrayList<String>> genes = new HashMap<String, ArrayList<String>>();
    public Actor individual = null;

    public Genotype(String dna) {
        this.GENE_PHENOTYPES_IDS.put("body", "1");
        this.GENE_PHENOTYPES_IDS.put("brain", "2");
        this.GENE_PHENOTYPES_IDS.put("vitals", "3");
        this.GENE_PHENOTYPES_IDS.put("features", "4");

        this.dna = dna;
        if (this.dna == null)
            this.dna = this.generateDna();
        this.genes = this.extractGenes(this.dna);
    }

    private String generateDna() {
        String dna = "";
//        dna += GENE_SEP + this.GENE_PHENOTYPES_IDS.get("body") + PhysicalBody.GeneCoder.generateRandomDNA() + GENE_SEP;
        dna += GENE_SEP + this.GENE_PHENOTYPES_IDS.get("brain") + Brain.GeneCoder.generateRandomDNA() + GENE_SEP;
        dna += GENE_SEP + this.GENE_PHENOTYPES_IDS.get("vitals") + Vitals.GeneCoder.generateRandomDNA() + GENE_SEP;
        dna += GENE_SEP + this.GENE_PHENOTYPES_IDS.get("features") + Features.GeneCoder.generateRandomDNA() + GENE_SEP;
        return dna;
    }

    private HashMap<String, ArrayList<String>> extractGenes(String dna) {
        HashMap<String, ArrayList<String>> genes = new HashMap<String, ArrayList<String>>();
        ArrayList<String> dnaSeq = new ArrayList<String>(Arrays.asList(dna.split("")));
        dnaSeq.remove(0);
        String gene = "";
        String geneId = "";
        boolean inGeneSequence = false;
        while (!dnaSeq.isEmpty()) {
            String code = dnaSeq.get(0);
            dnaSeq.remove(0);
            if (code.equals(GENE_SEP) && !inGeneSequence) {
                inGeneSequence = true;
                geneId = "";
                gene = "";
            } else if (code.equals(GENE_SEP) && inGeneSequence) {
                if (!geneId.isEmpty() && !gene.isEmpty())
                    genes.get(geneId).add(gene);
                inGeneSequence = false;
            } else if (inGeneSequence && geneId.isEmpty()) {
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

    public Object getPhenotype(String name, Actor actor) {
        if (name.equals("brain")) {
            return new Brain(this.getGenesForPhenotype("brain"), actor);
        } else if (name.equals("body")) {
            return new PhysicalBody(this.getGenesForPhenotype("body"), actor);
        } else if (name.equals("vitals")) {
            return new Vitals(this.getGenesForPhenotype("vitals"), actor);
        } else if (name.equals("features")) {
            return new Features(this.getGenesForPhenotype("features"), actor);
        }
        return null;
    }

    private void mutate() {
        if (this.individual != null) {
            this.dna = "";
//          this.dna += GENE_SEP + this.GENE_PHENOTYPES_IDS.get("body") + this.individual.body.mutateDNAFromPhenotype() + GENE_SEP;
            this.dna += GENE_SEP + this.GENE_PHENOTYPES_IDS.get("brain") + this.individual.brain.mutateDNAFromPhenotype() + GENE_SEP;
            this.dna += GENE_SEP + this.GENE_PHENOTYPES_IDS.get("vitals") + this.individual.vitals.mutateDNAFromPhenotype() + GENE_SEP;
            this.dna += GENE_SEP + this.GENE_PHENOTYPES_IDS.get("features") + this.individual.features.mutateDNAFromPhenotype() + GENE_SEP;
            this.genes = this.extractGenes(this.dna);
        }
    }

    private static String crossover(String fatherDna, String motherDna) {
        String childDna;
        int max_cut = Math.min(fatherDna.length(), motherDna.length()) - 1;
        int cut1, cut2, cut3;
        if (Rand.rNorm() > GENE_CROSSOVER_PROB) {
            cut1 = Rand.rInt(0, max_cut);
            if (Rand.rNorm() > GENE_IF_CROSSOVER_DOUBLE_PROB) {
                cut2 = Rand.rInt(0, max_cut);
                if (cut2 < cut1) {
                    cut1 = cut1 + cut2;
                    cut2 = cut1 - cut2;
                    cut1 = cut1 - cut2;
                }
                if (Rand.rNorm() > GENE_IF_CROSSOVER_AND_DOUBLE_UNBALANCED_PROB) {
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
        father.mutate();
        mother.mutate();
        return new Genotype(crossover(father.dna, mother.dna));
    }
}
