package de.featjar.feature.model.io.tikz;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Attribute;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.io.tikz.helper.TikzAttributeHelper;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test for displaying the attributes in tikz (filter)
 *
 * @author Felix Behme
 * @author Lara Merza
 * @author Jonas Hanke
 */
public class AttributeFilterTest {

    private static IFeatureModel featureModel;
    private static IFeature feature;

    private final String FIRST_TEST_OUTPUT = "[\\multicolumn{2}{c}{attribute-test} \\\\\\hline\n"
            + "\\small\\texttt{int-attribute (Integer)} &\\small\\texttt{= 1} \\\\\n"
            + ",align=ll";
    private final String SECOND_TEST_OUTPUT = "[\\multicolumn{2}{c}{attribute-test} \\\\\\hline\n"
            + "\\small\\texttt{int-attribute (Integer)} &\\small\\texttt{= 1} \\\\\n"
            + "\\small\\texttt{bool-attribute (Boolean)} &\\small\\texttt{= true} \\\\\n"
            + ",align=ll";
    private final String THIRD_TEST_OUTPUT = "[\\multicolumn{2}{c}{attribute-test} \\\\\\hline\n"
            + "\\small\\texttt{name (String)} &\\small\\texttt{= attribute-test} \\\\\n"
            + "\\small\\texttt{bool-attribute (Boolean)} &\\small\\texttt{= true} \\\\\n"
            + "\\small\\texttt{string-attribute (String)} &\\small\\texttt{= hallo} \\\\\n"
            + ",align=ll";
    private final String FOURTH_TEST_OUTPUT = "[\\multicolumn{2}{c}{attribute-test} \\\\\\hline\n"
            + "\\small\\texttt{name (String)} &\\small\\texttt{= attribute-test} \\\\\n"
            + "\\small\\texttt{string-attribute (String)} &\\small\\texttt{= hallo} \\\\\n"
            + ",align=ll";

    @BeforeAll
    public static void init() {
        FeatJAR.testConfiguration().initialize();

        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());

        IFeature feature = featureModel.addFeature("attribute-test");

        Attribute<Integer> attribute = new Attribute<>("test", "int-attribute", Integer.class);
        Attribute<Boolean> attribute2 = new Attribute<>("test", "bool-attribute", Boolean.class);
        Attribute<String> attribute3 = new Attribute<>("test", "string-attribute", String.class);

        feature.mutate().setAttributeValue(attribute, 1);
        feature.mutate().setAttributeValue(attribute2, true);
        feature.mutate().setAttributeValue(attribute3, "hallo");

        AttributeFilterTest.featureModel = featureModel;
        AttributeFilterTest.feature = feature;
    }

    @Test
    public void test1() {
        StringBuilder stringBuilder = new StringBuilder();

        TikzAttributeHelper tikzAttributeHelper = new TikzAttributeHelper(feature, stringBuilder)
                .setFilterType(TikzAttributeHelper.FilterType.DISPLAY)
                .addFilterValue(List.of("int-attribute"));

        tikzAttributeHelper.build();

        Assertions.assertEquals(stringBuilder.toString(), FIRST_TEST_OUTPUT);
    }

    @Test
    public void test2() {
        StringBuilder stringBuilder = new StringBuilder();

        TikzAttributeHelper tikzAttributeHelper = new TikzAttributeHelper(feature, stringBuilder)
                .setFilterType(TikzAttributeHelper.FilterType.DISPLAY)
                .addFilterValue(List.of("int-attribute", "bool-attribute"));

        tikzAttributeHelper.build();

        Assertions.assertEquals(stringBuilder.toString(), SECOND_TEST_OUTPUT);
    }

    @Test
    public void test3() {
        StringBuilder stringBuilder = new StringBuilder();

        TikzAttributeHelper tikzAttributeHelper = new TikzAttributeHelper(feature, stringBuilder)
                .setFilterType(TikzAttributeHelper.FilterType.WITH_OUT)
                .addFilterValue(List.of("int-attribute"));

        tikzAttributeHelper.build();

        Assertions.assertEquals(stringBuilder.toString(), THIRD_TEST_OUTPUT);
    }

    @Test
    public void test4() {
        StringBuilder stringBuilder = new StringBuilder();

        TikzAttributeHelper tikzAttributeHelper = new TikzAttributeHelper(feature, stringBuilder)
                .setFilterType(TikzAttributeHelper.FilterType.WITH_OUT)
                .addFilterValue(List.of("int-attribute", "bool-attribute"));

        tikzAttributeHelper.build();

        Assertions.assertEquals(stringBuilder.toString(), FOURTH_TEST_OUTPUT);
    }
}
