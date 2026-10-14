
package com.school.enrollment;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class GradingCalculator {

    private GradingCalculator() {}

    private static void validate(double grade) {
        if (!Double.isFinite(grade) || grade < 0 || grade > 100) {
            throw new IllegalArgumentException(
                    "Each component score must be between 0 and 100.");
        }
    }

    public static double calculateLecture(
            double writtenWorks,
            double unitTest,
            double termTest) {

        validate(writtenWorks);
        validate(unitTest);
        validate(termTest);

        return round(
                writtenWorks * 0.30
                + unitTest * 0.40
                + termTest * 0.30);
    }

    public static double calculateLaboratory(
            double writtenWorks,
            double unitTest,
            double termTest,
            double performanceTasks) {

        validate(writtenWorks);
        validate(unitTest);
        validate(termTest);
        validate(performanceTasks);

        return round(
                writtenWorks * 0.20
                + unitTest * 0.25
                + termTest * 0.20
                + performanceTasks * 0.35);
    }

    // Project assumption: 60% lecture + 40% laboratory.
    public static double finalRawGrade(
            double lectureGrade,
            double laboratoryGrade,
            boolean hasLaboratory) {

        validate(lectureGrade);
        validate(laboratoryGrade);

        return hasLaboratory
                ? round(lectureGrade * 0.60
                        + laboratoryGrade * 0.40)
                : round(lectureGrade);
    }

    public static double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static String equivalentGrade(double rawGrade) {
        validate(rawGrade);

        if (rawGrade >= 98) return "1.00";
        if (rawGrade >= 91) return "1.25";
        if (rawGrade >= 85) return "1.50";
        if (rawGrade >= 79) return "1.75";
        if (rawGrade >= 73) return "2.00";
        if (rawGrade >= 67) return "2.25";
        if (rawGrade >= 61) return "2.50";
        if (rawGrade >= 55) return "2.75";
        if (rawGrade >= 50) return "3.00";

        return "5.00";
    }
}
