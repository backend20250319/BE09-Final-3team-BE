package site.petful.advertiserservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvertiserRequest {

    private String name;
    private String phone;
    private String website;
    private String email;
    private String description;
    private String reason;
    private Long profileNo;
}
