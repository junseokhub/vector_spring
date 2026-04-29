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
        return contentService.findAllContent().stream()
                .map(ContentResponseDto::from)
                .toList();
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public List<ContentResponseDto> findAllContent(@RequestParam("projectKey") String projectKey) {
        return ContentResponseDto.from(contentService.findAllContentByProject(projectKey));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ContentResponseDto findOneContentById(@PathVariable Long id) {
        return ContentResponseDto.from(contentService.findOneContentById(id));
    }

    @GetMapping("/detail/{key}")
    @ResponseStatus(HttpStatus.OK)
    public ContentResponseDto findOneContentByKey(@PathVariable String key) {
        return ContentResponseDto.from(contentService.findOneContentByContentKey(key));
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentResponseDto createContent(
            @RequestHeader(USER_ID) long userId,
            @Validated @RequestBody ContentCreateRequestDto request
    ) {
        return ContentResponseDto.from(
                contentService.create(userId, request.projectKey(), request.title(), request.answer())
        );
    }

    @PostMapping("/update")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentResponseDto updateContent(
            @RequestHeader(CONTENT_ID) long id,
            @Validated @RequestBody ContentUpdateRequestDto request
    ) {
        return ContentResponseDto.from(
                contentService.update(id, request.updatedUserId(), request.title(), request.answer())
        );
    }
}
