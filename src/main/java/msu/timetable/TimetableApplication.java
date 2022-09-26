package msu.timetable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

/** Main class of application */
@SpringBootApplication
@RestController
public class TimetableApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimetableApplication.class, args);
	}

}
