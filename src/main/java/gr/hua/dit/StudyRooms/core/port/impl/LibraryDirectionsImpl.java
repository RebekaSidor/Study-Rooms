package gr.hua.dit.StudyRooms.core.port.impl;

import gr.hua.dit.StudyRooms.core.port.LibraryDirections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LibraryDirectionsImpl implements LibraryDirections {

    private final RestTemplate restTemplate;

    @Value("${app.api.directions.base-url}")
    private String baseUrl;

    public LibraryDirectionsImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public MapResponse getDirections() {
        try {
            MapResponse response = restTemplate.getForObject(baseUrl, MapResponse.class);
            return response != null ? response : new MapResponse();
        } catch (Exception e) {
            return new MapResponse();
        }
    }

    public static class MapResponse {
        private String origin;
        private String destination;
        private String directionsUrl;
        private String provider;

        public String getDirectionsUrl() { return directionsUrl; }
        public void setDirectionsUrl(String directionsUrl) { this.directionsUrl = directionsUrl; }
        public String getOrigin() { return origin; }
        public void setOrigin(String origin) { this.origin = origin; }
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
    }
}


