package gr.hua.dit.StudyRooms.core.port;

import java.time.LocalDate;

/**
 * Port to external service for managing national holidays.
 */
public interface HolidayService {
    boolean isHoliday(LocalDate date);
}
