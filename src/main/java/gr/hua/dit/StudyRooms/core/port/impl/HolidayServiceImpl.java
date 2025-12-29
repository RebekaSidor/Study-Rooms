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
            //POST to holiday-API
            String response = restTemplate.postForObject(holidayApiUrl + date, null, String.class);

            //if response περιέχει ❌ -> id a holiday
            return response != null && response.contains("❌");
        } catch (Exception e) {
            //if the API doesn't work consider it not a holiday
            return false;
        }
    }
}
