package gr.hua.dit.StudyRooms.core.port.impl;

import gr.hua.dit.StudyRooms.core.port.HolidayService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Service
public class HolidayServiceImpl implements HolidayService {

    private final RestTemplate restTemplate;
    private final String holidayApiUrl = "http://localhost:8082/api/bookings?date=";

    public HolidayServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        try {
            // Κάνουμε POST στο holiday-API
            String response = restTemplate.postForObject(holidayApiUrl + date, null, String.class);

            // Αν το response περιέχει ❌, θεωρούμε ότι είναι αργία
            return response != null && response.contains("❌");
        } catch (Exception e) {
            // Αν πέσει το API, θεωρούμε ότι δεν είναι αργία
            return false;
        }
    }
}
