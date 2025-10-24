package de.featjar.feature.model.transformer;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Attribute;
import de.featjar.base.data.Range;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.io.IO;
import de.featjar.feature.model.Feature;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.io.tikz.TikzGraphicalFeatureModelFormat;
import de.featjar.feature.model.io.tikz.helper.TikzAttributeHelper;
import de.featjar.formula.structure.predicate.LessThan;
import de.featjar.formula.structure.term.aggregate.AttributeSum;
import de.featjar.formula.structure.term.value.Constant;
import org.w3c.dom.Attr;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class TikzShow {

    public static void main(String[] args) {
        FeatJAR.testConfiguration().initialize(); // init FeatJAR

        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier()); // create feature model

        // todo 4

        // todo 6

        // todo 5

        IFeature featureRoot = featureModel.mutate().addFeature("Hello");
        featureRoot.mutate().setAbstract();

        IFeature feature = featureModel.mutate().addFeature("Feature");
        feature.mutate().setAbstract();

        IFeature world = featureModel.mutate().addFeature("World");
        IFeature wonderful = featureModel.mutate().addFeature("Wonderful");
        IFeature beautiful = featureModel.mutate().addFeature("Beautiful");

        IFeatureTree rootTree = featureModel.mutate().addFeatureTreeRoot(featureRoot); // create tree and make it to an and group
        rootTree.mutate().toAndGroup();

        IFeatureTree firstFeatureTree = rootTree.mutate().addFeatureBelow(feature);
        firstFeatureTree.mutate().toAlternativeGroup();
        firstFeatureTree.mutate().setFeatureCardinality(Range.of(0, 1)); // todo: change to 0, 2
        // rootTree.mutate().setFeatureCardinality(Range.of(0, 3));

        rootTree.mutate().addFeatureBelow(world);

        TikzGraphicalFeatureModelFormat tikzGraphicalFeatureModelFormat = new TikzGraphicalFeatureModelFormat(
                TikzAttributeHelper.FilterType.WITH_OUT,
                List.of("name", "abstract")
        );

        // todo 1

        // todo 2

        firstFeatureTree.mutate().addFeatureBelow(wonderful);
        firstFeatureTree.mutate().addFeatureBelow(beautiful);

        // todo 3
        try {
            // write class output to a file
            IO.save(featureModel, Paths.get("src", "test", "java", "show", "test-output-show.tex"), tikzGraphicalFeatureModelFormat);
            FeatJAR.log().info("Build run successfully");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 1
     */
    /*
    Attribute<Double> first = new Attribute<>("cost", "cost", Double.class);

        wonderful.mutate().setAttributeValue(first, (double) 123); 1
        beautiful.mutate().setAttributeValue(first, (double) 321);
        world.mutate().setAttributeValue(first, (double) 312);
     */

    /**
     * 2
     */
    /*
    featureModel.addConstraint(new LessThan(new AttributeSum("cost"), new Constant(10.0))); 2
     */

    /**
     * 3
     */
    /*
    IFeature newF = featureModel.mutate().addFeature("abc");
        IFeatureTree secTree = rootTree.mutate().addFeatureBelow(newF);
        secTree.mutate().setFeatureCardinality(Range.of(0, 1));

    int id = secTree.mutate().addOrGroup();
        int idD = secTree.mutate().addAlternativeGroup();



        IFeature a = featureModel.addFeature("a");
        IFeature b = featureModel.addFeature("b");
        IFeature c = featureModel.addFeature("c");
        IFeature d = featureModel.addFeature("d");

        secTree.mutate().addFeatureBelow(a, 1, id);
        secTree.mutate().addFeatureBelow(b,2,id);
        secTree.mutate().addFeatureBelow(c,1,idD);
        secTree.mutate().addFeatureBelow(c,2,idD);
     */

    /**
     * 4
     */
    /*
        IFeature secRoot = featureModel.mutate().addFeature("Sec-Root");
        IFeatureTree secTreeRoot = featureModel.mutate().addFeatureTreeRoot(secRoot);

        IFeature featureOne = featureModel.addFeature("1");
        IFeature featureSecond = featureModel.addFeature("2");

        secTreeRoot.mutate().addFeatureBelow(featureOne);
        secTreeRoot.mutate().addFeatureBelow(featureSecond);
     */

    /**
     * 5
     */
    /*
    Attribute<String> firstS = new Attribute<>("abc", "key", String.class);
        featureOne.mutate().setAttributeValue(firstS, "First");
        featureSecond.mutate().setAttributeValue(firstS, "Second");
        featureSecond.mutate().setAbstract();5
     */

    /**
     * 6
     */
    /*
    secTreeRoot.mutate().toOrGroup();
        secTreeRoot.mutate().setFeatureCardinality(Range.of(0, 2));
     */
}
