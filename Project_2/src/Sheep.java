public class Sheep extends Herbivore{
    private final Types type = Types.SHEEP;
    private final int maxPopulation = 140;
    private final double weight = 70;
    private final int speed = 3;
    private final double maxSaturation = 15;
    private volatile double actSaturation = 15;
    @Override
    public synchronized int getMaxPopulation() {
        return maxPopulation;
    }
    @Override
    public synchronized double getActSaturation() {
        return actSaturation;
    }

    @Override
    public synchronized void setActSaturation(double actSaturation) {
        this.actSaturation = actSaturation;
    }
    @Override
    public synchronized void decreaseSaturation() {
        actSaturation = actSaturation - (maxSaturation/10.0);
    }
    @Override
    public synchronized void eat(Plant plant) {
        actSaturation += plant.getWeight();
        if (actSaturation > maxSaturation) {
            actSaturation = maxSaturation;
        }
    }
    @Override
    public synchronized double getWeight() {
        return weight;
    }
    @Override
    public synchronized Types getType() {
        return type;
    }
    @Override
    public synchronized int getSpeed() {
        return speed;
    }
}
