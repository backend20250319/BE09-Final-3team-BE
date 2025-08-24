package site.petful.notificationservice.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public final class SliceResponseDto<T>{
    private final List<T> content;
    private final int page;
    private final int size;
    private final boolean hasNext;
}
