package site.petful.campaignservice.dto.advertisement;

import lombok.Getter;
import lombok.Setter;
import site.petful.campaignservice.entity.advertisement.Keyword;

@Getter
@Setter
public class KeywordResponse {
    private Long keywordNo;
    private String content;

    public static KeywordResponse from(Keyword keyword) {
        KeywordResponse res = new KeywordResponse();
        res.setKeywordNo(keyword.getKeywordNo());
        res.setContent(keyword.getContent());
        return res;
    }
}
