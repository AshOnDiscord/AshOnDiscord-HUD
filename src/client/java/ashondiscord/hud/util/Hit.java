package ashondiscord.hud.util;

// Literally just a double and a long, will replace with a better data type later, but this works for now.
public class Hit {
    public double distance;
    public long time;

    public Hit(double distance, long time) {
        this.distance = distance;
        this.time = time;
    }
}
