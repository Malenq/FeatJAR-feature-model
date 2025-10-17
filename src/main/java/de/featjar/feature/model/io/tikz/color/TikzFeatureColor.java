package de.featjar.feature.model.io.tikz.color;

/**
 * Color in tikz for later implemntation (colors doesnt exists in features in the moment)
 *
 * @author Felix Behme
 * @author Lara Merza
 * @author Jonas Hanke
 */
public enum TikzFeatureColor {

    RED("redColor"),
    ORANGE("orangeColor"),
    YELLOW("yellowColor"),
    DARK_GREEN("darkGreenColor"),
    LIGHT_GREEN("lightGreenColor"),
    CYAN("cyanColor"),
    LIGHT_GRAY("lightGrayColor"),
    BLUE("blueColor"),
    MAGENTA("magentaColor"),
    PINK("pinkColor"),
    NO_COLOR("");

    public static String color(String tikzFeatureColor) {
        for (TikzFeatureColor tikzFeatureColors : values()) {
            if (tikzFeatureColors.getColor().equalsIgnoreCase(tikzFeatureColor)) {
                return tikzFeatureColors.getColor();
            }
        }
        return NO_COLOR.getColor();
    }

    final String color;

    TikzFeatureColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}
