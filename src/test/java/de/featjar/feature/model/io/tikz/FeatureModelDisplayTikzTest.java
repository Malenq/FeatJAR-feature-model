package de.featjar.feature.model.io.tikz;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Attribute;
import de.featjar.base.data.Range;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.feature.model.*;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.connective.*;
import de.featjar.formula.structure.term.value.Variable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test the full output with a test feature model and attributes, constrains and more
 *
 * @author Felix Behme
 * @author Lara Merza
 * @author Jonas Hanke
 */
public class FeatureModelDisplayTikzTest {

    private final StringBuilder stringBuilder = new StringBuilder();
    private static IFeatureModel featureModel;

    @BeforeAll
    public static void init() {
        FeatJAR.testConfiguration().initialize();

        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());

        IFeature featureRootS = featureModel.mutate().addFeature("Hello");
        IFeature feature = featureModel.mutate().addFeature("Feature");
        IFeature world1 = featureModel.mutate().addFeature("World1");
        IFeature world2 = featureModel.mutate().addFeature("World2");
        IFeature wonderful1 = featureModel.mutate().addFeature("Wonderful1");
        IFeature beautiful1 = featureModel.mutate().addFeature("Beautiful1");
        IFeature wonderful2 = featureModel.mutate().addFeature("Wonderful2");
        IFeature beautiful2 = featureModel.mutate().addFeature("Beautiful2");

        featureRootS.mutate().setAbstract();

        // first tree
        IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureRootS);
        rootTree.mutate().toAndGroup();

        IFeatureTree firstFeatureTree = rootTree.mutate().addFeatureBelow(feature);
        feature.mutate().setAbstract();
        int group1 = firstFeatureTree.mutate().addAlternativeGroup();
        int group2 = firstFeatureTree.mutate().addOrGroup();

        Attribute<String> attribute = new Attribute<>("any", "test", String.class);
        Attribute<String> attribute2 = new Attribute<>("anyd", "testd", String.class);
        wonderful1.mutate().setAttributeValue(attribute, "value");
        wonderful1.mutate().setAttributeValue(attribute2, "value2");

        firstFeatureTree.mutate().addFeatureBelow(wonderful1, 0, group1);

        firstFeatureTree.mutate().setFeatureCardinality(Range.of(0,2));
        firstFeatureTree.mutate().addFeatureBelow(beautiful1, 1, group1);

        firstFeatureTree.mutate().addFeatureBelow(wonderful2, 2, group2);
        firstFeatureTree.mutate().addFeatureBelow(beautiful2, 3, group2);

        rootTree.mutate().addFeatureBelow(world1);

        IFeatureTree world2FeatureTree = rootTree.mutate().addFeatureBelow(world2);
        world2FeatureTree.mutate().setFeatureCardinality(Range.of(1, 1));

        // Constraints
        featureModel.addConstraint(new And(Expressions.literal("A"), Expressions.literal("B")));
        featureModel.addConstraint(new Implies(Expressions.literal("C"), new ForAll(new Variable("A"), new BiImplies(Expressions.literal("A"), Expressions.literal("C")))));

        FeatureModelDisplayTikzTest.featureModel = featureModel;
    }

    @Test
    public void perform() {
        StringBuilder expectedOutput = loadTestFile();

        if (expectedOutput == null) {
            throw new IllegalStateException("File is null");
        }

        String value = expectedOutput.toString();

        new TikzGraphicalFeatureModelFormat().serialize(featureModel).ifPresent(output -> {
            FeatJAR.log().info("Expected Output: " + value);
            FeatJAR.log().info("Acutally Output: " + output);

            Assertions.assertEquals(value, output);
        });
    }

    // Todo: Add @Test here and remove the other @Test on the method perform
    //@Test
    public void createTestFile() {
        new TikzGraphicalFeatureModelFormat().serialize(featureModel).ifPresent(this::writeToFile);
    }

    /**
     * This method can be used to write the output in a file with ignoring tabs or spaces.
     * (It will be written correctly)
     *
     * @param "output" from the "new" file
     */
    private void writeToFile(String value) {
        Path filePath = Paths.get("src", "main", "resources", "test", "test-output.tex");

        try {
            Files.createDirectories(filePath.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                writer.write(value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method the test-output.tex from the resource (resource/test) file.
     *
     * @return Output of the file as a StringBuilder
     */
    private StringBuilder loadTestFile() {
        StringBuilder stringBuilderFile = new StringBuilder();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test/test-output.tex");

        if (inputStream == null) {
            return null;
        }

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilderFile.append(line).append(System.lineSeparator());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Remove last add (its empty)
        int lastIndex = stringBuilderFile.lastIndexOf("\n");
        if (lastIndex >= 0) {
            stringBuilderFile.delete(lastIndex, stringBuilderFile.length());
        }

        return stringBuilderFile;
    }

}
