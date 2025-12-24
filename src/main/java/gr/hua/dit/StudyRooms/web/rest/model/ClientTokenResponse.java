package gr.hua.dit.StudyRooms.web.rest.model;

import gr.hua.dit.StudyRooms.web.rest.ClientAuthResource;

/**
 * @see ClientAuthResource
 */
public record ClientTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
