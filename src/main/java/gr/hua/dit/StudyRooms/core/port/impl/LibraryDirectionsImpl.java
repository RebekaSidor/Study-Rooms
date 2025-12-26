package gr.hua.dit.StudyRooms.core.port.impl;

import gr.hua.dit.StudyRooms.core.port.LibraryDirections;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Service
public class LibraryDirectionsImpl implements LibraryDirections {

    private final String libraryAddress = "Ομήρου 9, Ταύρος, Αθήνα, Ελλάδα";

    @Override
    public String getDirectionsUrl() {
        try {
            String destination = URLEncoder.encode(libraryAddress, "UTF-8");
            return "https://www.google.com/maps/dir/?api=1&origin=Current+Location&destination=" + destination;
        } catch (UnsupportedEncodingException e) {
            return "https://www.google.com/maps";
        }
    }
}