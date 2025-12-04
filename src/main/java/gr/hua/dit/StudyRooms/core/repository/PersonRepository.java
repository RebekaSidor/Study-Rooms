package gr.hua.dit.StudyRooms.core.repository;

import gr.hua.dit.StudyRooms.core.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    Person findTopByOrderByLibraryIdDesc();

    Optional<Person> findByLibraryId(String libraryId);

    boolean existsByEmailAddressIgnoreCase(final String emailAddress);
    boolean existsByMobilePhoneNumber(final String mobilePhoneNumber);
}
