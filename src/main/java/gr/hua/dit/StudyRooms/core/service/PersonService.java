package gr.hua.dit.StudyRooms.core.service;

import gr.hua.dit.StudyRooms.core.model.Person;
import gr.hua.dit.StudyRooms.core.service.model.CreatePersonRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreatePersonResult;


/**
 * Service (contract) for managing students/staff.
 */
public interface PersonService {


    CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest);
}

