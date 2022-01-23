package main.service3;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/service3")
public class Service3 {
    private RestTemplate restTemplate = new RestTemplate();
    private String URL = "http://localhost:8091/service2/";

    /*
    @GetMapping("/simulate/{timetableFilename}/{reportFilename}")
    public ResponseEntity<Boolean> simulate(@PathVariable String timetableFilename, @PathVariable String reportFilename) {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(URL + "timetable/" + timetableFilename, String.class);
        SimulationReport simulationReport = PortSimulation.computeOptimum(responseEntity.getBody());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        restTemplate.postForEntity(URL + "report/" + reportFilename, gson.toJson(simulationReport), String.class);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

     */

    @GetMapping(value = {"/simulate/{timetableFilename}", "/simulate/{timetableFilename}/{reportFileExtension}"})
    public ResponseEntity<String> simulate(@PathVariable String timetableFilename, @PathVariable Optional<String> reportFileExtension) {
        ResponseEntity<String> responseEntity;
        try {
             responseEntity = restTemplate.getForEntity(URL + "timetable/" + timetableFilename, String.class);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        String outputFile;
        if (reportFileExtension.isEmpty()) {
            outputFile =  "report-" + UUID.randomUUID();
        } else {
            outputFile = "report-" + reportFileExtension.get();
        }
        SimulationReport simulationReport = PortSimulation.computeOptimum(responseEntity.getBody());
        Gson gson = new Gson();
        restTemplate.postForEntity(URL + "report/" + outputFile, gson.toJson(simulationReport), String.class);
        return new ResponseEntity<>(outputFile + ".json", HttpStatus.OK);
    }
}