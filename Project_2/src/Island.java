import java.util.*;
import java.util.concurrent.*;

public class Island {
    static int a = (int) (Math.random() * 20 + 20);// рандомное число стороны поля а
    static int b = (int) (Math.random() * 20 + 20);// рандомное число стороны поля в
    static int c = (int) (Math.random() * 130 + 20);//рандомное число для заполнения поля животными/растениями
    static List<ArrayList<ArrayList<? extends Animal>>> fieldArray = Collections.synchronizedList(new ArrayList<>()); //размер одной стороны поля
    static protected volatile int[][][] animalsCount = new int[a][b][16];
    static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(7);
    static class RandomEating implements Runnable {
        int getA1 = 0;
        int getA2 = 0;
        int count = 0;

        @Override
        public void run() {
            try {
                fieldArray
                        .forEach(n -> n
                                .forEach(m -> {
                                    while (getA1 == getA2 || m.get(getA1).getType() == Types.PLANT && m.get(getA2).getType() == Types.PLANT) {
                                        getA1 = ThreadLocalRandom.current().nextInt(0, m.size());
                                        getA2 = ThreadLocalRandom.current().nextInt(0, m.size());
                                    } // trying until both not plants and not same animal
                                    if (ChanceToEat.getChance(m.get(getA1).getType().ordinal(), m.get(getA2).getType().ordinal()) >
                                            ChanceToEat.getChance(m.get(getA2).getType().ordinal(), m.get(getA1).getType().ordinal())) {
                                        if (ThreadLocalRandom.current().nextInt(0, 100) >=
                                                ChanceToEat.getChance(m.get(getA1).getType().ordinal(), m.get(getA2).getType().ordinal())) {
                                            m.get(getA1).eat(m.remove(getA2));
                                            count++;
                                        }
                                    } else {
                                        if (ThreadLocalRandom.current().nextInt(0, 100) >=
                                                ChanceToEat.getChance(m.get(getA2).getType().ordinal(), m.get(getA1).getType().ordinal())) {
                                            m.get(getA2).eat(m.remove(getA1));
                                            count++;
                                        }
                                    }
                                    getA1 = 0; // reset for next field
                                    getA2 = 0; // reset for next field
                                }));
            } catch (Exception e) {
                System.out.println("Exception in eating ");
                e.printStackTrace();
            }
            System.out.println(count + " animals ate other animals");
            count = 0;
        }
    }
    static class AllAnimalsSlowlyDying implements Runnable {
        int count = 0;
        Iterator<? extends Animal> it;
        Animal animal;
        @Override
        public void run() {
            try {
                fieldArray
                        .forEach(n -> n
                                .forEach(m -> m.forEach(k -> {
                                    if (k.getType() != Types.PLANT && k.getActSaturation() > 0) {
                                        k.decreaseSaturation();
                                    }
                                }))); // slowly animals dying
//                System.out.println("thread decreasing health is work");
            } catch (Exception e) {
                System.out.println("Exception in health decrease ");
                e.printStackTrace();
            }
            try {
                fieldArray
                        .forEach(n -> n
                                .forEach(m -> {
                                    it = m.iterator();
                                    while (it.hasNext()) {
                                        animal = it.next();
                                        if (animal.getType() != Types.PLANT && animal.getActSaturation() <= 0.0000001) {
                                            animalsCount[fieldArray.indexOf(n)][n.indexOf(m)][animal.getType().ordinal()]--;
                                            it.remove();
                                            count++;
                                        }
                                    }
                                })); //Check if dead
                System.out.println(count + " animals died");
            } catch (Exception e) {
                System.out.println("Exception in killing animals ");
                e.printStackTrace();
            }
            count = 0;
        }
    }
    static class GrowingPlants implements Runnable {
        int a;
        int b;
        final int randFieldsCount = (int) ((fieldArray.size() * fieldArray.get(0).size()) * 0.15);
        Set<String> set = new HashSet<String>();
        int count = 0;

        @Override
        public void run() {
            try {
                for (int i = 0; i < randFieldsCount; i++) {
                    a = ThreadLocalRandom.current().nextInt(0, fieldArray.size());
                    b = ThreadLocalRandom.current().nextInt(0, fieldArray.get(a).size());
                    if (!set.contains(a + "" + b)) {
                        set.add(a + "" + b); // to not repeat field
                        fieldArray.get(a).get(b).add(0, Animal.getAnimal(15)); // planting plant
                        count++;
                    } else {
                        i--;
                    }
                }
                set.clear(); // clear set for next iteration
                System.out.println(count + " plants grown");
                count = 0;
            } catch (Exception e) {
                System.out.println("Exception in growing plants");
                e.printStackTrace();
            }
        }
    }
    static class RandomAnimalMove implements Runnable {
        int pos = 0;
        int step = 0;
        String direction = "";
        int count = 0;

        @Override
        public void run() {
            try {
                fieldArray
                        .forEach(n -> n
                                .forEach(m -> {
                                    if (m.size() > 0) {
                                        for (int i = m.size() - 1; i >= 0; i--) {
                                            if (i < ThreadLocalRandom.current().nextInt(0, (m.size() - 1))) {
                                                if (m.get(i).getType() != Types.PLANT) {
                                                    step = ThreadLocalRandom.current().nextInt(0, (m.get(i).getSpeed() + 1));
                                                }
                                                direction = Animal.getMoveDirection();
                                                if (step > 0) {
                                                    if (direction.equalsIgnoreCase("down")) {
                                                        pos = fieldArray.indexOf(n) + step;
                                                        if (pos >= fieldArray.size())
                                                            pos = fieldArray.size() - 1;
                                                        if (animalsCount[pos][n.indexOf(m)][m.get(i).getType().ordinal()] < m.get(i).getMaxPopulation()) {
                                                            fieldArray.get(pos).get(n.indexOf(m)).add(Animal.converter(m.remove(i)));
                                                            count++;
                                                        }
                                                    } else if (direction.equalsIgnoreCase("up")) {
                                                        pos = fieldArray.indexOf(n) - step;
                                                        if (pos < 0)
                                                            pos = 0;
                                                        if (animalsCount[pos][n.indexOf(m)][m.get(i).getType().ordinal()] < m.get(i).getMaxPopulation()) {
                                                            fieldArray.get(pos).get(n.indexOf(m)).add(Animal.converter(m.remove(i)));
                                                            count++;
                                                        }
                                                    } else if (direction.equalsIgnoreCase("left")) {
                                                        pos = n.indexOf(m) - step;
                                                        if (pos < 0)
                                                            pos = 0;
                                                        if (animalsCount[fieldArray.indexOf(n)][pos][m.get(i).getType().ordinal()] < m.get(i).getMaxPopulation()) {
                                                            n.get(pos).add(Animal.converter(m.remove(i)));
                                                            count++;
                                                        }
                                                    } else if (direction.equalsIgnoreCase("right")) {
                                                        pos = n.indexOf(m) + step;
                                                        if (pos >= n.size())
                                                            pos = n.size() - 1;
                                                        if (animalsCount[fieldArray.indexOf(n)][pos][m.get(i).getType().ordinal()] < m.get(i).getMaxPopulation()) {
                                                            n.get(pos).add(Animal.converter(m.remove(i)));
                                                            count++;
                                                        }
                                                    }
                                                }
                                            }
                                            step = 0;
                                        }
                                    }
                                }));
                System.out.println(count + " animals moved");
                pos = 0;
                count = 0;
            } catch (Exception e) {
                System.out.println("Exception in moving");
                e.printStackTrace();
            }
        }
    }
    static class MakingBabies implements Runnable {
        int getA1 = 0;
        int getA2 = 0;
        int count = 0;

        @Override
        public void run() {
            try {
                fieldArray
                        .forEach(n -> n
                                .forEach(m -> {
                                    if (m.size() > 0) {
                                        getA1 = ThreadLocalRandom.current().nextInt(0, m.size());
                                        getA2 = ThreadLocalRandom.current().nextInt(0, m.size());
                                        if (m.get(getA1).getType() == m.get(getA2).getType()
                                                && m.get(getA1).getType() != Types.PLANT && m.get(getA2).getType() != Types.PLANT
                                                && getA1 != getA2
                                                && animalsCount[fieldArray.indexOf(n)][n.indexOf(m)][m.get(getA1).getType().ordinal()] < m.get(getA1).getMaxPopulation()) {
                                            m.add(Animal.getAnimal(m.get(getA2).getType().ordinal()));
                                            animalsCount[fieldArray.indexOf(n)][n.indexOf(m)][m.get(getA1).getType().ordinal()]++;
                                            count++;
                                        }
                                        getA1 = 0; // reset for next field
                                        getA2 = 0; // reset for next field
                                    }
                                }));
                System.out.println(count + " babies was born");
            } catch (Exception e) {
                System.out.println("Exception in making babies ");
                e.printStackTrace();
            }
            count = 0;
        }
    }
    static class CheckEnd implements Runnable {
        int totalAnimalsCount = 0;
        int totalPlantsCount = 0;
        int iterations = 0;
    @Override
        public void run() {
            try {
                fieldArray.forEach(n -> n.forEach(m -> m.forEach(k -> {
                    if (k.getType() != Types.PLANT) {
                        totalAnimalsCount++;
                    } else {
                        totalPlantsCount++;
                    }
                })));
                if (totalAnimalsCount <= 0) {
                    System.out.println("Total animals count is " + totalAnimalsCount);
                    System.out.println("Total plants count is " + totalPlantsCount);
                    System.out.println("SIMULATION IS FINISHED ALL ANIMALS DEAD");
                    totalAnimalsCount = 0;
                    totalPlantsCount = 0;
                    executorService.shutdown();
                } else {
                    System.out.println("Iteration " + ++iterations);
                    System.out.println("Total animals count is " + totalAnimalsCount);
                    System.out.println("Total plants count is " + totalPlantsCount);
                    //System.out.println(fieldArray.get(0).get(0).get(16).getType() + " " + fieldArray.get(0).get(0).get(16).getActSaturation());
                    totalAnimalsCount = 0;
                    totalPlantsCount = 0;
                }
            } catch (Exception e) {
                System.out.println("Exception in check end");
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {

        int type;
        // INITIALIZATION
        for (int i = 0; i < a; i++) {
            fieldArray.add(new ArrayList<>()); // размер второй стороны поля и заполнение первой стороны
            for (int j = 0; j < b; j++) {
                fieldArray.get(i).add(new ArrayList<>()); //размер поля и заполнение полей
                for (int k = 0; k < c; k++) {
                    if (k < (c / 10)) {
                        fieldArray.get(i).get(j).add(Animal.getAnimal(15));
                        animalsCount[i][j][15]++;//заполнение травой каждую ячейку на 10%
                    } else {
                        type = ThreadLocalRandom.current().nextInt(0, 15);
                        while (animalsCount[i][j][type] >= Animal.getAnimal(type).getMaxPopulation()) {
                            type = ThreadLocalRandom.current().nextInt(0, 15);
                        } // проверка перенаселения
                        fieldArray.get(i).get(j).add(Animal.getAnimal(type));
                        animalsCount[i][j][type]++;//заполнение животными
                    }
                }
            }
        }
        System.out.println("Island created size is " + a + "*" + b + "*" + c);

        final int msTime = 100;
            executorService.scheduleAtFixedRate(new AllAnimalsSlowlyDying(), 10, msTime, TimeUnit.MILLISECONDS);
            executorService.scheduleAtFixedRate(new RandomEating(), 10, msTime, TimeUnit.MILLISECONDS);
            executorService.scheduleAtFixedRate(new GrowingPlants(), 10, msTime, TimeUnit.MILLISECONDS);
            executorService.scheduleAtFixedRate(new RandomAnimalMove(), 10, msTime, TimeUnit.MILLISECONDS);
            executorService.scheduleAtFixedRate(new MakingBabies(), 10, msTime, TimeUnit.MILLISECONDS);
            executorService.scheduleAtFixedRate(new CheckEnd(), 10, msTime, TimeUnit.MILLISECONDS);
    }
}
