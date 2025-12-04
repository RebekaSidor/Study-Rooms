package gr.hua.dit.StudyRooms.core.service;

import gr.hua.dit.StudyRooms.core.service.model.CreatePersonRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreatePersonResult;

/**
 * Service (contract) for managing students/staff.
 */
public interface PersonService {

    CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest, final boolean notify);

    default CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest){
        return this.createPerson(createPersonRequest,true);
    }

    String updateEmail(String libraryId, String newEmail);
    String updatePhone(String libraryId, String newPhone);
    String updatePassword(String libraryId, String newPassword);
}

