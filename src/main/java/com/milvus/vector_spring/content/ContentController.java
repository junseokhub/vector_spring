package com.milvus.vector_spring.content;

import com.milvus.vector_spring.config.jwt.CustomUserDetails;
import com.milvus.vector_spring.content.dto.ContentCreateRequestDto;
import com.milvus.vector_spring.content.dto.ContentResponseDto;
import com.milvus.vector_spring.content.dto.ContentUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/content")
@Slf4j
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContentResponseDto createContent(
            @AuthenticationPrincipal CustomUserDetails user,
            @Validated @RequestBody ContentCreateRequestDto request
    ) {
        return ContentResponseDto.from(
                contentService.create(user.getId(), request.projectKey(), request.title(), request.answer())
        );
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentResponseDto updateContent(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user,
            @Validated @RequestBody ContentUpdateRequestDto request
    ) {
        log.info("id: {}, request: {}", id, request);
        return ContentResponseDto.from(
                contentService.update(id, user.getId(), request.title(), request.answer())
        );
    }
}
