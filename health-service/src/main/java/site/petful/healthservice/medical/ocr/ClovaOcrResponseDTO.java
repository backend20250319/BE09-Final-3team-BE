package site.petful.healthservice.medical.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ClovaOcrResponseDTO {
    private String uid;
    private String name;
    private String inferResult;
    private String message;
    private MatchedTemplate matchedTemplate;
    private ValidationResult validationResult;
    private List<Field> fields;
    private Title title;

    @Data
    public static class MatchedTemplate {
        private Long id;
        private String name;
    }

    @Data
    public static class ValidationResult {
        private String result;
    }

    @Data
    public static class Field {
        private String name;
        private String valueType;
        private BoundingPoly boundingPoly;
        private String inferText;
        private Double inferConfidence;
        private String type;
        private List<SubField> subFields;
    }

    @Data
    public static class SubField {
        private BoundingPoly boundingPoly;
        private String inferText;
        private Double inferConfidence;
        private Boolean lineBreak;
    }

    @Data
    public static class BoundingPoly {
        private List<Vertex> vertices;
    }

    @Data
    public static class Vertex {
        private Double x;
        private Double y;
    }

    @Data
    public static class Title {
        private String name;
        private BoundingPoly boundingPoly;
        private String inferText;
        private Double inferConfidence;
        private List<SubField> subFields;
    }
}
