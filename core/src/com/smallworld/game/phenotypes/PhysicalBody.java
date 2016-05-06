package com.smallworld.game.phenotypes;

import com.smallworld.game.Actor;
import com.smallworld.game.Genotype;
import com.smallworld.game.Rand;

import java.util.ArrayList;

public class PhysicalBody {
    private Actor actor;

    public PhysicalBody(ArrayList<String> genes, Actor actor) {
        this.actor = actor;
    }

    public static class GeneCoder {
        public static void decode(ArrayList<String> genes) {
        }

        public static String encode(PhysicalBody phenotype) {
            String gene = "";
            return gene.toUpperCase();
        }

        public static String generateRandomDNA() {
            String gene = "";
            return gene;
        }
    }

    public String mutateDNAFromPhenotype() {
        String gene = GeneCoder.encode(this);
        if (Rand.rNorm() > Genotype.GENE_MUTATION_PROB) {
        }
        return gene;
    }
}
