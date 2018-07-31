package org.dariah.desir.service;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class EntityFishingServiceTest {
    String text = "In computational neuroscience it is generally accepted that human " +
            "motor memory contains neural representations of the physics of the musculoskeletal system " +
            "and the objects in the environment. These representations are called &quot;internal models&quot;. " +
            "Force field studies, in which subjects have to adapt to dynamic perturbations induced by a robotic manipulandum, " +
            "are an established tool to analyze the characteristics of such internal models. " +
            "The aim of the current study was to investigate whether catch trials during force field learning could influence " +
            "the consolidation of motor memory in more complex tasks. Thereby, the force field was more than double " +
            "the force field of previous studies (35 N·s/m). Moreover, the arm of the subjects was not supported. " +
            "A total of 46 subjects participated in this study and performed center-out movements at a robotic manipulandum in two different force fields. " +
            "Two control groups learned force field A on day 1 and were retested in the same force field on day 3 (AA). " +
            "Two test groups additionally learned an interfering force field B (= −A) on day 2 (ABA). The difference between the two test and control groups, " +
            "respectively, was the absence (0%) or presence (19%) of catch trials, in which the force field was turned-off suddenly. " +
            "The results showed consolidation of force field A on day 3 for both control groups. " +
            "Test groups showed no consolidation of force field A (19% catch trials) and even poorer performance on day 3 (0% catch trials). " +
            "In conclusion, it can be stated that catch trials seem to have a positive effect on the performance on day 3 but do not trigger " +
            "a consolidation process as shown in previous studies that used a lower force field viscosity with supported arm. " +
            "These findings indicate that the results of previous studies in which less complex tasks were analyzed, " +
            "cannot be fully transferred to more complex tasks. Moreover, the effects of catch trials in these situations are insufficiently understood " +
            "and further research is needed.";

    String lang = "en";

    Map<String, Double> listTerm = new HashMap<>();


    EntityFishingService entityFishingService = null;

    @Before
    public void setUp(){
        entityFishingService = new EntityFishingService("cloud.science-miner.com/nerd/service");
    }

    @Test
    public void termDisambiguate() throws Exception{
        listTerm.put("computer science", 0.3);
        listTerm.put("engine", 0.1);
        entityFishingService.termDisambiguate(listTerm,lang);
    }

    @Test
    public void textDisambiguate() throws Exception{
        entityFishingService.textDisambiguate(text,lang);
    }
}