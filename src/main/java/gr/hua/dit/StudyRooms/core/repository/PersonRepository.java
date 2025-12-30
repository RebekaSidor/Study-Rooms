package gr.hua.dit.StudyRooms.core.repository;

import gr.hua.dit.StudyRooms.core.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    Person findTopByOrderByLibraryIdDesc();

    Optional<Person> findByLibraryId(String libraryId);

    boolean existsByEmailAddressIgnoreCase(final String emailAddress);
    boolean existsByMobilePhoneNumber(final String mobilePhoneNumber);

    // τελευταία εγγραφή προσωπικού
    @Query("SELECT p FROM Person p WHERE p.libraryId LIKE 's%' ORDER BY p.libraryId DESC")
    Person findTopByOrderByLibraryIdDescForStaff();

    // τελευταία εγγραφή φοιτητή
    @Query("SELECT p FROM Person p WHERE p.libraryId LIKE 'lib%' ORDER BY p.libraryId DESC")
    Person findTopByOrderByLibraryIdDescForStudents();

}
