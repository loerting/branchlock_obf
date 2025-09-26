package net.branchlock.commons.asm;

import net.branchlock.structure.BClass;
import net.branchlock.structure.provider.MapDataProvider;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DependencyCycleDetectorTest {

    @Test
    public void testDetectorNoCycle() {
        MapDataProvider dataProvider = new MapDataProvider();

        dataProvider.classes.put("A", dummyWithParent("A", "B", dataProvider));
        dataProvider.classes.put("B", dummyWithParent("B", "C", dataProvider));
        dataProvider.classes.put("C", dummyWithParent("C", "D", dataProvider));
        dataProvider.classes.put("D", dummyWithParent("D", null, dataProvider));
        DependencyCycleDetector detector = new DependencyCycleDetector(dataProvider);
        List<DependencyCycleDetector.DependencyCycle> detect = detector.detect();
        assertEquals(0, detect.size());
    }

    @Test
    public void testDetectorCycle() {
        MapDataProvider dataProvider = new MapDataProvider();

        dataProvider.classes.put("A", dummyWithParent("A", "B", dataProvider));
        dataProvider.classes.put("B", dummyWithParent("B", "C", dataProvider));
        dataProvider.classes.put("C", dummyWithParent("C", "D", dataProvider));
        dataProvider.classes.put("D", dummyWithParent("D", "A", dataProvider));
        DependencyCycleDetector detector = new DependencyCycleDetector(dataProvider);
        assertEquals(1, detector.detect().size());
    }

    @Test
    public void testDetectorTwoCycles() {
        MapDataProvider dataProvider = new MapDataProvider();

        dataProvider.classes.put("A", dummyWithParent("A", "B", dataProvider));
        dataProvider.classes.put("B", dummyWithParent("B", "C", dataProvider));
        dataProvider.classes.put("C", dummyWithParent("C", "D", dataProvider));
        dataProvider.classes.put("D", dummyWithParent("D", "A", dataProvider));
        dataProvider.classes.put("E", dummyWithParent("E", "F", dataProvider));
        dataProvider.classes.put("F", dummyWithParent("F", "G", dataProvider));
        dataProvider.classes.put("G", dummyWithParent("G", "E", dataProvider));
        DependencyCycleDetector detector = new DependencyCycleDetector(dataProvider);
        assertEquals(2, detector.detect().size());
    }

    @Test
    public void testDetectorTwoCyclesWithSharedNode() {
        MapDataProvider dataProvider = new MapDataProvider();

        dataProvider.classes.put("A", dummyWithParent("A", "B", dataProvider));
        dataProvider.classes.put("B", dummyWithParent("B", "C", dataProvider));
        dataProvider.classes.put("C", dummyWithParent("C", "D", dataProvider));
        dataProvider.classes.put("D", dummyWithParent("D", "A", dataProvider));
        dataProvider.classes.put("E", dummyWithParent("E", "F", dataProvider));
        dataProvider.classes.put("F", dummyWithParent("F", "G", dataProvider));
        dataProvider.classes.put("G", dummyWithParent("G", "E", dataProvider));
        dataProvider.classes.put("H", dummyWithParent("H", "G", dataProvider));
        DependencyCycleDetector detector = new DependencyCycleDetector(dataProvider);
        assertEquals(2, detector.detect().size());
    }

    @Test
    public void testDetectorInterfaceCycle() {
        MapDataProvider dataProvider = new MapDataProvider();

        dataProvider.classes.put("A", dummyWithInterfaces("A", List.of("B", "C"), dataProvider));
        dataProvider.classes.put("B", dummyWithInterfaces("B", List.of("C", "D"), dataProvider));
        dataProvider.classes.put("C", dummyWithInterfaces("C", List.of("D", "A"), dataProvider));
        dataProvider.classes.put("D", dummyWithInterfaces("D", List.of("A", "B"), dataProvider));
        DependencyCycleDetector detector = new DependencyCycleDetector(dataProvider);
        assertEquals(1, detector.detect().size());
    }

    private BClass dummyWithParent(String name, String superClassName, MapDataProvider dataProvider) {
        ClassNode node = new ClassNode();
        node.name = name;
        node.superName = superClassName;
        return BClass.createBClass(dataProvider, node);
    }

    private BClass dummyWithInterfaces(String name, List<String> interfaces, MapDataProvider dataProvider) {
        ClassNode node = new ClassNode();
        node.name = name;
        node.interfaces = interfaces;
        return BClass.createBClass(dataProvider, node);
    }
}
