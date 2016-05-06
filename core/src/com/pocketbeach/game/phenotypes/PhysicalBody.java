package com.pocketbeach.game.phenotypes;

import com.pocketbeach.game.Rand;

import java.util.ArrayList;

public class PhysicalBody {
    private com.pocketbeach.game.Actor actor;

    public PhysicalBody(ArrayList<String> genes, com.pocketbeach.game.Actor actor) {
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
        if (Rand.rNorm() > com.pocketbeach.game.Genotype.GENE_MUTATION_PROB) {
        }
        return gene;
    }
}
