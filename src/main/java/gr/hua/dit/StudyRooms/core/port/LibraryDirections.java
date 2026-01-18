package gr.hua.dit.StudyRooms.core.port;

import gr.hua.dit.StudyRooms.core.port.impl.LibraryDirectionsImpl;
import gr.hua.dit.StudyRooms.core.port.impl.dto.LibraryDirectionsDto;

/**
 * Returns URL with directions from current location towards the library.
 */
public interface LibraryDirections {
    LibraryDirectionsDto getDirections();
}

