package main.service1;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class TimetableGenerator {

    private static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghigklmnopqrstuvwxyz";

    private static String generateRandomString() {
        StringBuilder string = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < random.nextInt(10) + 5; i++) {
            string.append(alphabet.charAt(random.nextInt(52)));
        }
        return string.toString();
    }

    public static ArrayList<LinkedList<Ship>> generateTimetable(int maxShips) {
        if (maxShips < 0)
            throw new IllegalArgumentException();

        ArrayList<LinkedList<Ship>> timetable = new ArrayList<>();
        timetable.add(new LinkedList<>());
        timetable.add(new LinkedList<>());
        timetable.add(new LinkedList<>());

        Random random = new Random();

        for (long i = 0; i < random.nextInt(maxShips) + 20; i++) {
            int cargoType = random.nextInt(3);
            if (cargoType == 0) {
               timetable.get(0).add(new Ship(generateRandomString(),
                       CargoType.CONTAINER,
                       random.nextInt(Ship.maxAmountOfContainers + 1) + 2000,
                       random.nextInt(30) + 1,
                       random.nextInt(1440)));
            } else if (cargoType == 1) {
                timetable.get(1).add(new Ship(generateRandomString(),
                        CargoType.LOOSE,
                        random.nextInt(Ship.maxLooseWeight + 1) + 2000,
                        random.nextInt(30) + 1,
                        random.nextInt(1440)));
            } else {
                timetable.get(2).add(new Ship(generateRandomString(),
                        CargoType.LIQUID,
                        random.nextInt(Ship.maxLiquidWeight + 1) + 2000,
                        random.nextInt(30) + 1,
                        random.nextInt(1440)));
            }
        }

        return timetable;
    }
}
