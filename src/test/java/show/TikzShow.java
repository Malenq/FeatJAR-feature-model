package show;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.io.IO;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.io.tikz.TikzGraphicalFeatureModelFormat;
import de.featjar.feature.model.io.tikz.helper.TikzAttributeHelper;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class TikzShow {

    public static void main(String[] args) {
        FeatJAR.testConfiguration().initialize(); // init FeatJAR

        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier()); // create feature model

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
        firstFeatureTree.mutate().toOrGroup();

        firstFeatureTree.mutate().addFeatureBelow(wonderful);
        firstFeatureTree.mutate().addFeatureBelow(beautiful);

        rootTree.mutate().addFeatureBelow(world);

        TikzGraphicalFeatureModelFormat tikzGraphicalFeatureModelFormat = new TikzGraphicalFeatureModelFormat(
                TikzAttributeHelper.FilterType.WITH_OUT,
                List.of("name")
        );

        try {
            // write class output to a file
            IO.save(featureModel, Paths.get("src", "test", "java", "show", "test-output-show.tex"), tikzGraphicalFeatureModelFormat);
            FeatJAR.log().info("Build run successfully");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
