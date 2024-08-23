import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class PolarGrid {
    private int size; // Number of parallels, which also determines the planet size
    private PolarPoint start; // Starting point
    private PolarPoint goal; // Goal point

    // Constructor
    public PolarGrid(int size, PolarPoint start, PolarPoint goal) {
        this.size = size;
        this.start = start;
        this.goal = goal;
    }

    /**
     * Check if a given point is within the bounds of the grid
     */
    public boolean isValidPoint(PolarPoint p) {
        if (p.distance <= 0) return false; // the pole is not a valid point
        if (p.distance >= size) return false; // beyond the last parallel
        return true;
    }

    /**
     * Generate possible movements from a given point
     */
    public List<PolarPoint> getNeighbors(PolarPoint current) {
        List<PolarPoint> neighbors = new ArrayList<>();
        // Movement towards the pole (North)
        if (current.distance > 1) {
            neighbors.add(new PolarPoint(current.distance - 1, current.angle));
        }
        // Movement away from the pole (South)
        if (current.distance < size) {
            neighbors.add(new PolarPoint(current.distance + 1, current.angle));
        }
        // Movement clockwise (East)
        int eastAngle = (current.angle + 45) % 360;
        neighbors.add(new PolarPoint(current.distance, eastAngle));
        // Movement counter-clockwise (West)
        int westAngle = (current.angle - 45 + 360) % 360;
        neighbors.add(new PolarPoint(current.distance, westAngle));
        // Filter out invalid points
        neighbors.removeIf(p -> !isValidPoint(p));
        return neighbors;
    }

    /**
     * Calculate the distance from the start point to another point
     */
    public double distanceTo(PolarPoint other) {
        double parallelDistance = Math.abs(this.start.distance - other.distance);
        double meridianDistance = Math.abs(this.start.angle - other.angle) / 45.0 * (2 * Math.PI * this.start.distance / 8);
        return parallelDistance + meridianDistance;
    }

    /**
     * Calculate the cost to move from one point to another
     */
    public double cost(PolarPoint current, PolarPoint next) {
        // Cost based on radial distance
        double radialCost = Math.abs(current.distance - next.distance);
    
        // Cost based on angular distance, only if on the same parallel
        double angularCost = 0;
        if (current.distance == next.distance) {
            int angleDiff = Math.abs(current.angle - next.angle);
            angleDiff = Math.min(angleDiff, 360 - angleDiff); // Account for the shortest path around the circle
            angularCost = (angleDiff / 45.0) * (Math.PI * current.distance / 4); // Calculate part of circumference
        }
    
        return radialCost + angularCost;
    }

    // Class to represent a point in the polar grid
    public static class PolarPoint {
        int distance; // Distance from the pole
        int angle; // Angle in degrees

        public PolarPoint(int distance, int angle) {
            this.distance = distance;
            this.angle = angle;
        }

        public int getDistance() {
            return distance;
        }

        public int getAngle() {
            return angle;
        }

        // Override equals and hashCode for correct functionality in collections
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof PolarPoint)) return false;
            PolarPoint other = (PolarPoint) obj;
            return this.distance == other.distance && this.angle == other.angle;
        }

        @Override
        public int hashCode() {
            return Objects.hash(distance, angle);
        }

        @Override
        public String toString() {
            return "PolarPoint{" +
                    "distance=" + distance +
                    ", angle=" + angle +
                    '}';
        }

        // Custom method for specific output format
        public String toSimpleString() {
            return "(" + distance + ":" + angle + ")";
        }
    }

    // Getters
    public PolarPoint getStart() {
        return start;
    }

    public PolarPoint getGoal() {
        return goal;
    }

    public int getSize() {
        return size;
    }
}