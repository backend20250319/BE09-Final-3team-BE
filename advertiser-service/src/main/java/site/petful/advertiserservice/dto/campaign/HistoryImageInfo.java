package site.petful.advertiserservice.dto.campaign;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryImageInfo {
    private Long id;
    private String url;
    private String originalName;
    private String savedName;
    private Long historyNo;
}
