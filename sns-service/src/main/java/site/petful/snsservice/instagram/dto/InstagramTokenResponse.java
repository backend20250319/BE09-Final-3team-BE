package site.petful.snsservice.instagram.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class InstagramTokenResponse {

    private final String access_token;
    private final String token_type;
    private final Long expires_in;
}
