package ua.edu.lab2;

/** Незмінний об'єкт, що відповідає одному Item у XML. */
public record Knife(String id, String type, String handy, String origin, Visual visual,
                    boolean value) {
    public record Visual(double lengthCm, double widthMm, String material,
                         String handle, String woodType, boolean fuller) { }
}
