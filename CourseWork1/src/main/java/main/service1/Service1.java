package main.service1;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedList;

@RestController
@RequestMapping("/service1")
public class Service1 {

    @GetMapping("/timetable")
    public ResponseEntity<ArrayList<LinkedList<Ship>>> getTimetable() {
        return new ResponseEntity<>(TimetableGenerator.generateTimetable(20), HttpStatus.OK);
    }
}
