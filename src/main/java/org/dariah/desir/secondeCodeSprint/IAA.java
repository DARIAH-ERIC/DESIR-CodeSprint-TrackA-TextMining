package org.dariah.desir.secondeCodeSprint;

import de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.FleissKappaAgreement;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.KrippendorffAlphaAgreement;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.PercentageAgreement;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.distance.NominalDistanceFunction;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.unitizing.KrippendorffAlphaUnitizingAgreement;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.unitizing.UnitizingAnnotationStudy;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.visualization.CoincidenceMatrixPrinter;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class IAA {

    public static void main(String[] args) {
        CodingAnnotationStudy codingStudy = new CodingAnnotationStudy(3);
        codingStudy.addItem(1, 1, 1);
        codingStudy.addItem(1, 2, 2);
        codingStudy.addItem(2, 2, 2);
        codingStudy.addItem(4, 4, 4);
        codingStudy.addItem(1, 4, 4);
        codingStudy.addItem(2, 2, 2);
        codingStudy.addItem(1, 2, 3);
        codingStudy.addItem(3, 3, 3);
        codingStudy.addItem(2, 2, 2);

        PercentageAgreement pa = new PercentageAgreement(codingStudy);
        System.out.println("Agreement percentage : " + pa.calculateAgreement());

        FleissKappaAgreement kappa = new FleissKappaAgreement(codingStudy);
        System.out.println("Kappa : " +  kappa.calculateAgreement());

        KrippendorffAlphaAgreement alpha = new KrippendorffAlphaAgreement(codingStudy, new NominalDistanceFunction());
        System.out.println("Obeserved disagreement : " + alpha.calculateObservedDisagreement());
        System.out.println("Expected disagreement :" + alpha.calculateExpectedDisagreement());
        System.out.println("Agreement : " + alpha.calculateAgreement());
        System.out.println("Agreement Category 1 : " + alpha.calculateCategoryAgreement(1));
        System.out.println("Agreement Category 2 : " + alpha.calculateCategoryAgreement(2));
        new CoincidenceMatrixPrinter().print(System.out, codingStudy);

        // r0: -11111111-
        // r1: --1-1-1---
//        UnitizingAnnotationStudy unitizingStudy = new UnitizingAnnotationStudy(2, 10);
//        unitizingStudy.addUnit(1, 8, 0, "researchInstitution");
//        unitizingStudy.addUnit(1, 8, 1, "researchInstitution");
        //unitizingStudy.addUnit(2, 1, 1, "researchInstitution");
        //unitizingStudy.addUnit(4, 1, 1, "researchInstitution");
        //unitizingStudy.addUnit(6, 1, 1, "researchInstitution");


//        KrippendorffAlphaUnitizingAgreement alpha = new KrippendorffAlphaUnitizingAgreement(unitizingStudy);
//        System.out.println("Agreement : " + alpha.calculateAgreement());
//        System.out.println("Agreement of researchInstitution : " + alpha.calculateCategoryAgreement("researchInstitution"));
//        System.out.println("Agreement of affiliation : " + alpha.calculateCategoryAgreement("affiliation"));


        // use Kappa, agreement pourcentage
    }
}
