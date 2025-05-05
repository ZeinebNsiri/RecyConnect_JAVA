package controllers.Events;

/**
 * Static class to share map selection results between components
 */
public class MapPickerResult {
    // These variables store the results of the map picker selection
    public static String selectedCoordinates = "";
    public static String selectedAddress = "";

    // For debugging
    public static void printCurrentValues() {
        System.out.println("MapPickerResult current values:");
        System.out.println("- selectedCoordinates: " + selectedCoordinates);
        System.out.println("- selectedAddress: " + selectedAddress);
    }

    // Clear all values
    public static void reset() {
        selectedCoordinates = "";
        selectedAddress = "";
        System.out.println("MapPickerResult values reset");
    }
}