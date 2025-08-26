package org.example.petservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetRequest {
    private String name;
    private String type;  // breed 대신 type 사용
    private Long age;     // String 대신 Long 사용
    private String gender;
    private Float weight;
    private String imageUrl;
    private Long snsProfileNo;
}
