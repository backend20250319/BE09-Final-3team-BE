package site.petful.campaignservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstagramProfileDto {

    private Long id;
    private String username;
    private String name;
    private String profile_picture_url;
    private Long followers_count;
    private Long follows_count;
    private Long media_count;
    private Boolean auto_delete;
}
