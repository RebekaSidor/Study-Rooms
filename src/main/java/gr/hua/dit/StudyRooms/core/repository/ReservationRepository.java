package gr.hua.dit.StudyRooms.core.repository;

import gr.hua.dit.StudyRooms.core.model.Person;
import gr.hua.dit.StudyRooms.core.model.PersonType;
import gr.hua.dit.StudyRooms.core.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>{

   //TODO

}
