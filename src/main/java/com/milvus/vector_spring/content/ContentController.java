package com.milvus.vector_spring.content;

import com.milvus.vector_spring.content.dto.ContentCreateRequestDto;
import com.milvus.vector_spring.content.dto.ContentResponseDto;
import com.milvus.vector_spring.content.dto.ContentUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.milvus.vector_spring.common.Const.CONTENT_ID;
import static com.milvus.vector_spring.common.Const.USER_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/content")
public class ContentController {

    private final ContentService contentService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<ContentResponseDto> findAllContent() {
        List<Content> contentList = contentService.findAllContent();
        return contentList.stream()
                .map(ContentResponseDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ContentResponseDto findOneContentById(@PathVariable Long id) {
        Content content = contentService.findOneContentById(id);
        return ContentResponseDto.from(content);
    }

    @GetMapping("/detail/{key}")
    @ResponseStatus(HttpStatus.OK)
    public ContentResponseDto findOneContentByKey(@PathVariable String key) {
        Content content = contentService.findOneContentByContentKey(key);
        return ContentResponseDto.from(content);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentResponseDto createContenrt(
            @RequestHeader(USER_ID) long userId,
            @Validated @RequestBody ContentCreateRequestDto contentCreateRequestDto
            ) {
        Content content = contentService.createContent(userId, contentCreateRequestDto);
        return ContentResponseDto.from(content);
    }

    @PostMapping("/update")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentResponseDto updateContent(
            @RequestHeader(CONTENT_ID) long id,
            @Validated @RequestBody ContentUpdateRequestDto contentUpdateRequestDto
            ) {
        Content content = contentService.updateContent(id, contentUpdateRequestDto);
        return ContentResponseDto.from(content);
    }
}
