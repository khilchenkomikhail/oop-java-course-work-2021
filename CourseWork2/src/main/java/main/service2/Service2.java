package main.service2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

@RestController
@RequestMapping("/service2")
public class Service2 {
    private RestTemplate restTemplate = new RestTemplate();
    private String firstServiceURL = "http://localhost:8090/service1/timetable/";
    private String resourcesPath = "src/main/resources/";

    @GetMapping(value = {"/generateTimetable", "/generateTimetable/{extentionId}"})
    public ResponseEntity<String> getTimetable(@PathVariable Optional<String> extentionId) {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(firstServiceURL, String.class);
        Type listOfShips = new TypeToken<ArrayList<LinkedList<Ship>>>() {}.getType();
        Gson gson = new Gson();
        ArrayList<LinkedList<Ship>> timetable = gson.fromJson(responseEntity.getBody(), listOfShips);
        StringBuilder builder = new StringBuilder(resourcesPath + "timetable-");
        if (extentionId.isPresent()) {
            builder.append(extentionId.get());
        } else {
            builder.append(UUID.randomUUID());
        }
        String fileName = builder.append(".json").toString();
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(gson.toJson(timetable));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(fileName, HttpStatus.OK);
    }

    @GetMapping(value = "/addShipToTimetable/{filename}")
    public ResponseEntity<Boolean> addShip(@PathVariable String filename)
    {
        try (FileReader fileReader = new FileReader(resourcesPath + filename))
        {
            Type listOfShips = new TypeToken<ArrayList<LinkedList<Ship>>>() {}.getType();
            Gson gson = new Gson();
            ArrayList<LinkedList<Ship>> timetable = gson.fromJson(fileReader, listOfShips);

            Scanner in = new Scanner(System.in);

            System.out.println('\n');

                System.out.print("?????????????? ???????????????????? ???????????????? (?????????? ?????????????????????????? ??????????): ");
                int amountOfShips = in.nextInt();

                for (int i = 0; i < amountOfShips; i++) {
                    System.out.print("?????????????? ???????????????? ??????????????: ");
                    String name = in.next();

                    System.out.print("?????????????? ?????? ?????????? (??????????????????, ??????????????, ????????????): ");
                    String cargo = in.next();
                    while (!cargo.equals("??????????????????") && !cargo.equals("??????????????") && !cargo.equals("????????????")) {
                        System.out.println("???????????????????????? ????????, ?????????????????? ??????????????");
                        cargo = in.next();
                    }
                    CargoType cargoType;
                    if (cargo.equals("??????????????????")) {
                        cargoType = CargoType.CONTAINER;
                    } else if (cargo.equals("??????????????")) {
                        cargoType = CargoType.LOOSE;
                    } else {
                        cargoType = CargoType.LIQUID;
                    }

                    System.out.print("?????????????? ???????????????????? ?????????? (?????????? ?????????????????????????? ?????????? ???? 200 ???? 2000): ");
                    int amountOfUnits = in.nextInt();
                    while (amountOfUnits < 200 || amountOfUnits > 2000) {
                        System.out.println("???????????????????????? ????????, ?????????????????? ??????????????");
                        amountOfUnits = in.nextInt();
                    }

                    System.out.print("?????????????? ???????? ???????????????? (?????????? ?????????????????????????? ?????????? ???? 1 ???? 30): ");
                    int arrivalDay = in.nextInt();
                    while (arrivalDay < 1 || arrivalDay > 30) {
                        System.out.println("???????????????????????? ????????, ?????????????????? ??????????????");
                        arrivalDay = in.nextInt();
                    }

                    System.out.print("?????????????? ?????????? ???????????????? ?? ?????????????? (?????????? ?????????????????????????? ?????????? ???? 0 ???? 1439): ");
                    int arrivalTime = in.nextInt();
                    while (arrivalTime < 0 || arrivalTime > 1439) {
                        System.out.println("???????????????????????? ????????, ?????????????????? ??????????????");
                        arrivalTime = in.nextInt();
                    }

                    if (cargoType == CargoType.CONTAINER) {
                        timetable.get(0).add(new Ship(name, cargoType, amountOfUnits, arrivalDay, arrivalTime));
                    } else if (cargoType == CargoType.LOOSE) {
                        timetable.get(1).add(new Ship(name, cargoType, amountOfUnits, arrivalDay, arrivalTime));
                    } else {
                        timetable.get(2).add(new Ship(name, cargoType, amountOfUnits, arrivalDay, arrivalTime));
                    }
                }
            System.out.println('\n');

            String fileName = resourcesPath + filename;
            try (FileWriter fileWriter = new FileWriter(fileName)) {
                fileWriter.write(gson.toJson(timetable));
            } catch (Exception ex) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @GetMapping(value = "/timetable/{filename}")
    public ResponseEntity<String> getTimetableByFileName(@PathVariable String filename) {
        if (!filename.endsWith(".json")) {
            filename = filename + ".json";
        }
        try {
            File file = new File(resourcesPath + filename);
            if (file.exists()) {
                StringBuilder stringBuilder = new StringBuilder();
                Scanner scanner = new Scanner(file);
                while (scanner.hasNext()) {
                    stringBuilder.append(scanner.nextLine());
                }
                return new ResponseEntity<>(stringBuilder.toString(), HttpStatus.OK);
            } else {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/getReport/{filename}")
    public ResponseEntity<String> getReportByFileName(@PathVariable String filename) {
        if (!filename.endsWith(".json")) {
            filename = filename + ".json";
        }
        try {
            File file = new File(resourcesPath + filename);
            if (file.exists()) {
                StringBuilder stringBuilder = new StringBuilder();
                Scanner scanner = new Scanner(file);
                while (scanner.hasNext()) {
                    stringBuilder.append(scanner.nextLine());
                }
                return new ResponseEntity<>(stringBuilder.toString(), HttpStatus.OK);
            } else {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/report/{filename}")
    public ResponseEntity<String> postSimulationReport(@RequestBody String simulationReport, @PathVariable String filename) {
        String fileName = resourcesPath + filename + ".json";
        try {
            FileWriter fileWriter = new FileWriter(fileName);
            fileWriter.write(simulationReport);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(fileName, HttpStatus.OK);
    }

    @GetMapping(value = "/checkTimetables")
    public ResponseEntity<String> checkTimetables() {
        String[] filenames;
        File file = new File(resourcesPath);
        filenames = file.list();
        StringBuilder stringBuilder = new StringBuilder();

        for (String currentFile : filenames) {
            if (currentFile.startsWith("timetable")) {
                stringBuilder.append(currentFile + '\n');
            }
        }

        return new ResponseEntity<>(stringBuilder.toString(), HttpStatus.OK);
    }

    @GetMapping(value = "/checkReports")
    public ResponseEntity<String> checkReports() {
        String[] filenames;
        File file = new File(resourcesPath);
        filenames = file.list();
        StringBuilder stringBuilder = new StringBuilder();

        for (String currentFile : filenames) {
            if (currentFile.startsWith("report")) {
                stringBuilder.append(currentFile + '\n');
            }
        }

        return new ResponseEntity<>(stringBuilder.toString(), HttpStatus.OK);
    }
}
