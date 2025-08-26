package site.petful.snsservice.instagram.insight.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@Embeddable
@NoArgsConstructor
public class InstagramFollowerHistoryId implements Serializable {

    private Long instagramId;
    private LocalDate month;

}