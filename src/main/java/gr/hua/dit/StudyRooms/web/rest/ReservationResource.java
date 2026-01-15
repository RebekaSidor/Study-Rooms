package gr.hua.dit.StudyRooms.web.rest;

import gr.hua.dit.StudyRooms.core.service.ReservationDataService;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationRequest;
import gr.hua.dit.StudyRooms.core.service.model.ReservationView;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing {@code Reservation} resource.
 */
@RestController
@RequestMapping(value = "/api/v1/reservation", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReservationResource {

    private final ReservationDataService reservationDataService;

    public ReservationResource(final ReservationDataService reservationDataService) {
        this.reservationDataService = reservationDataService;
    }

    //Get all reservations (for staff/integration)
    @GetMapping
    @PreAuthorize("hasRole('INTEGRATION_READ')")
    public List<ReservationView> getAllReservations() {
        return this.reservationDataService.getAllReservations();
    }

    //Create a new reservation (student)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    public ReservationView createNewReservation(
            @RequestBody CreateReservationRequest request
    ) {
        return this.reservationDataService.createReservation(request);
    }

    // Cancel a reservation (student or library staff)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('STUDENT','LIBRARY_STAFF')")
    public void cancelExistingReservation(@PathVariable Long id) {
        this.reservationDataService.cancelReservation(id);
    }

    //Get reservations of the currently authenticated student
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public List<ReservationView> getMyReservations() {
        return this.reservationDataService.getMyReservations();
    }
}
