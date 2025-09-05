package site.petful.snsservice.instagram.insight.entity;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InstagramMonthlyId {

    private Long instagramId;
    private LocalDate month;

}
