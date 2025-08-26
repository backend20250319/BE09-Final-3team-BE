package site.petful.snsservice.instagram.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;


@Getter
public class InstagramApiInsightsResponseDto {

    @JsonProperty("data")
    private List<InsightData> data;
    private Paging paging;


    @Getter
    public static class InsightData {

        @JsonProperty("name")
        private String name;
        @JsonProperty("total_value")
        private TotalValue totalValue;
    }


    @Getter
    public static class TotalValue {

        @JsonProperty("value")
        private long value;
    }

    @Getter
    public static class Paging {

        @JsonProperty("previous")
        private String previous;
        @JsonProperty("next")
        private String next;
    }
}

