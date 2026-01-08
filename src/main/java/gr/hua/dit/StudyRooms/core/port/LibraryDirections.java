package gr.hua.dit.StudyRooms.core.port;

import gr.hua.dit.StudyRooms.core.port.impl.LibraryDirectionsImpl;

/**
 * Returns URL with directions from current location towards the library.
 */
public interface LibraryDirections {
    LibraryDirectionsImpl.MapResponse getDirections();
}

