package de.featjar.feature.model.io.tikz.helper;

import de.featjar.base.FeatJAR;
import org.junit.jupiter.api.Test;

public class MatrixHelperTest {

    @Test
    public void matrix() {
        FeatJAR.testConfiguration().initialize();

        MatrixHelper matrixHelper = new MatrixHelper(MatrixType.LEGEND);
        matrixHelper
                .writeNode("[abstract,label=right:Abstract Feature] {}")
                .writeNode("[concrete,label=right:Concrete Feature] {}")
                .writeNode("[abstract,label=right:Feature] {}")
                .writeNode("[concrete,label=right:Feature] {}")
                .writeNode("[mandatory,label=right:Mandatory] {}")
                .writeNode("[optional,label=right:Optional] {}")
                // Or Group
                .writeFillDraw("filldraw[drawColor] (0.1,0) -- +(-0,-0.2) -- +(0.2,-0.2) -- +(0.1,0)")
                .writeDraw("draw[drawColor] (0.1,0) -- +(-0.2, -0.4)")
                .writeDraw("draw[drawColor] (0.1,0) -- +(0.2,-0.4)")
                .writeFill("fill[drawColor] (0,-0.2) arc (240:300:0.2)")
                .writeNode("[or,label=right:Or Group] {}");

                // Alternative Group
                //.writeDraw("draw[drawColor] (0.1,0) -- +(-0.2, -0.4)")
                //.writeDraw("draw[drawColor] (0.1,0) -- +(0.2,-0.4)")
                //.writeDraw("draw[drawColor] (0,-0.2) arc (240:300:0.2)")
        //.writeNode("[alternative,label=right:Alternative Group] {}");

        FeatJAR.log().info(matrixHelper.build());
    }

}
