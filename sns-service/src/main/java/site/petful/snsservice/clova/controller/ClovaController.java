package site.petful.snsservice.clova.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import site.petful.snsservice.clova.service.ClovaApiService;
import site.petful.snsservice.common.ApiResponse;
import site.petful.snsservice.common.ApiResponseGenerator;
import site.petful.snsservice.instagram.comment.entity.Sentiment;

@Controller
@RequestMapping("/clova")
@RequiredArgsConstructor
public class ClovaController {


    private final ClovaApiService clovaApiService;

    @PostMapping("/sentiment")
    public ResponseEntity<ApiResponse<Sentiment>> analyzeSentiment(@RequestBody String text) {

        return ResponseEntity.ok(
            ApiResponseGenerator.success(clovaApiService.analyzeSentiment(text)));
    }
}
