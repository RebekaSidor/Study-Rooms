package gr.hua.dit.StudyRooms.core.port;

import java.time.LocalDate;

public interface HolidayService {
    boolean isHoliday(LocalDate date);
}
