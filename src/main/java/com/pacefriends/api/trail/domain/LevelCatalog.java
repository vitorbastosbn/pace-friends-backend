package com.pacefriends.api.trail.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LevelCatalog {

    private static final String[] LEVEL_NAMES = {
        null,          // index 0 unused
        "Iniciante",   // 1
        "Explorador",  // 2
        "Corredor",    // 3
        "Atleta",      // 4
        "Velocista",   // 5
        "Maratonista", // 6
        "Campeao",     // 7
        "Elite",       // 8
        "Lenda",       // 9
        "Mestre",      // 10
        "Imortal"      // 11
    };

    public static final int MAX_LEVEL = 11;

    private static final List<LevelDefinition> CATALOG = buildCatalog();

    private LevelCatalog() {}

    private static List<LevelDefinition> buildCatalog() {
        List<LevelDefinition> catalog = new ArrayList<>();
        for (int level = 1; level <= MAX_LEVEL; level++) {
            catalog.add(buildLevel(level));
        }
        return List.copyOf(catalog);
    }

    private static LevelDefinition buildLevel(int n) {
        List<TrainingPathItemDefinition> items = List.of(
            new TrainingPathItemDefinition(1,
                String.format("Complete %d atividade(s)", 1 * n),
                ItemCriterion.ACTIVITY_COUNT, 1.0 * n, 10),
            new TrainingPathItemDefinition(2,
                String.format("Complete %d atividades", 3 * n),
                ItemCriterion.ACTIVITY_COUNT, 3.0 * n, 20),
            new TrainingPathItemDefinition(3,
                String.format("Acumule %.0fkm em atividades", 5.0 * n),
                ItemCriterion.TOTAL_DISTANCE_KM, 5.0 * n, 30),
            new TrainingPathItemDefinition(4,
                String.format("Complete %d atividades", 5 * n),
                ItemCriterion.ACTIVITY_COUNT, 5.0 * n, 40),
            new TrainingPathItemDefinition(5,
                String.format("Mantenha ofensiva por %d dias", 3 * n),
                ItemCriterion.STREAK_DAYS, 3.0 * n, 50),
            new TrainingPathItemDefinition(6,
                String.format("Acumule %.0fkm em atividades", 20.0 * n),
                ItemCriterion.TOTAL_DISTANCE_KM, 20.0 * n, 60),
            new TrainingPathItemDefinition(7,
                String.format("Complete %d atividades", 10 * n),
                ItemCriterion.ACTIVITY_COUNT, 10.0 * n, 70),
            new TrainingPathItemDefinition(8,
                String.format("Mantenha ofensiva por %d dias", 7 * n),
                ItemCriterion.STREAK_DAYS, 7.0 * n, 80),
            new TrainingPathItemDefinition(9,
                String.format("Acumule %.0fkm em atividades", 50.0 * n),
                ItemCriterion.TOTAL_DISTANCE_KM, 50.0 * n, 90),
            new TrainingPathItemDefinition(10,
                String.format("Complete %d atividades", 20 * n),
                ItemCriterion.ACTIVITY_COUNT, 20.0 * n, 100)
        );
        return new LevelDefinition(n, LEVEL_NAMES[n], items);
    }

    public static Optional<LevelDefinition> getLevel(int level) {
        if (level < 1 || level > MAX_LEVEL) return Optional.empty();
        return Optional.of(CATALOG.get(level - 1));
    }

    public static String getLevelName(int level) {
        if (level < 1 || level > MAX_LEVEL) return "Desconhecido";
        return LEVEL_NAMES[level];
    }
}
