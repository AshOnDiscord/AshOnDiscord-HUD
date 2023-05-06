package ashondiscord.hud.util;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static ashondiscord.hud.ExampleModClient.LOGGER;

public class DistanceCalculator {
    public static double getAttackDistance(Entity attacking, Entity receiving) {
        Vec3d camera = attacking.getCameraPosVec(1);
        Vec3d rotation = attacking.getRotationVec(1);

        Box box = receiving.getBoundingBox();

        double result = RayRectangularPrismIntersection.calc(camera, rotation, box);
//        LOGGER.info("RESULT: "+result);
        return result;
    }
}

class RayRectangularPrismIntersection {
    public static double calc(Vec3d Camera, Vec3d Rotation, Box box) {

        double[] rayStart = {Camera.x, Camera.y, Camera.z};
        double[] rayRotation = {Rotation.x, Rotation.y, Rotation.z};

        double[] prismMin = {box.minX, box.minY, box.minZ};
        double[] prismMax = {box.maxX, box.maxY, box.maxZ};

        // Calculate the intersection point
        double[] intersectionPoint = rayRectangularPrismIntersection(rayStart, rayRotation, prismMin, prismMax);

        if (intersectionPoint != null) {
            System.out.println("Intersection at (" + intersectionPoint[0] + ", " + intersectionPoint[1] + ", " + intersectionPoint[2] + ")");
            // calculate distance from camera to intersection point
            return Math.sqrt(Math.pow(intersectionPoint[0] - rayStart[0], 2) + Math.pow(intersectionPoint[1] - rayStart[1], 2) + Math.pow(intersectionPoint[2] - rayStart[2], 2));
        }

        System.out.println("No intersection");
        return -1;
    }

    private static double[] rayRectangularPrismIntersection(double[] rayStart, double[] rayRotation, double[] prismMin, double[] prismMax) {
        // A BUNCH OF MATH THAT I DON'T UNDERSTAND. C: (like tf? inverse direction?, t values?)
        // However, it's pretty accurate and consistent, so I'm not going to touch it

        // Calculate the inverse direction of the ray
        double[] invDir = {1 / rayRotation[0], 1 / rayRotation[1], 1 / rayRotation[2]};

        // Calculate the t values for the x planes
        double t1x = (prismMin[0] - rayStart[0]) * invDir[0];
        double t2x = (prismMax[0] - rayStart[0]) * invDir[0];

        // Calculate the t values for the y planes
        double t1y = (prismMin[1] - rayStart[1]) * invDir[1];
        double t2y = (prismMax[1] - rayStart[1]) * invDir[1];

        // Calculate the t values for the z planes
        double t1z = (prismMin[2] - rayStart[2]) * invDir[2];
        double t2z = (prismMax[2] - rayStart[2]) * invDir[2];

        // Find the maximum of the minimum t values
        double tmin = Math.max(Math.max(Math.min(t1x, t2x), Math.min(t1y, t2y)), Math.min(t1z, t2z));

        // Find the minimum of the maximum t values
        double tmax = Math.min(Math.min(Math.max(t1x, t2x), Math.max(t1y, t2y)), Math.max(t1z, t2z));

        // Check if there is an intersection
        if (tmax < 0 || tmin > tmax) {
            return null;
        }

        // Calculate the intersection point
        return new double[]{
                rayStart[0] + tmin * rayRotation[0],
                rayStart[1] + tmin * rayRotation[1],
                rayStart[2] + tmin * rayRotation[2]
        };
    }
}