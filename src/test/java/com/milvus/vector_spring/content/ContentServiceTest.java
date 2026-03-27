package com.milvus.vector_spring.content;

import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.common.service.EncryptionService;
import com.milvus.vector_spring.content.dto.ContentCreateRequestDto;
import com.milvus.vector_spring.content.dto.ContentUpdateRequestDto;
import com.milvus.vector_spring.libraryopenai.OpenAiLibraryService;
import com.milvus.vector_spring.milvus.MilvusService;
import com.milvus.vector_spring.milvus.dto.InsertRequestDto;
import com.milvus.vector_spring.project.Project;
import com.milvus.vector_spring.project.ProjectService;
import com.milvus.vector_spring.user.User;
import com.milvus.vector_spring.user.UserService;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.Embedding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @InjectMocks
    private ContentService contentService;

    @Mock private ContentRepository contentRepository;
    @Mock private UserService userService;
    @Mock private ProjectService projectService;
    @Mock private EncryptionService encryptionService;
    @Mock private OpenAiLibraryService openAiLibraryService;
    @Mock private MilvusService milvusService;

    private User mockUser;
    private Project mockProject;
    private Content mockContent;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .username("테스터")
                .role("ROLE_USER")
                .build();

        mockProject = Project.builder()
                .id(1L)
                .name("테스트 프로젝트")
                .key("project-key")
                .openAiKey("encrypted-key")
                .embedModel("text-embedding-3-large")
                .dimensions(1536L)
                .build();

        mockContent = Content.builder()
                .id(1L)
                .key("content-key")
                .title("테스트 제목")
                .answer("테스트 답변")
                .project(mockProject)
                .createdBy(mockUser)
                .updatedBy(mockUser)
                .build();
    }

    @Nested
    @DisplayName("findOneContentById()")
    class FindById {

        @Test
        @DisplayName("존재하는 id로 조회 성공")
        void findById_success() {
            given(contentRepository.findById(1L)).willReturn(Optional.of(mockContent));
            Content result = contentService.findOneContentById(1L);
            assertThat(result.getTitle()).isEqualTo("테스트 제목");
        }

        @Test
        @DisplayName("존재하지 않는 id 조회 시 CustomException 발생")
        void findById_notFound() {
            given(contentRepository.findById(999L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> contentService.findOneContentById(999L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getBaseCode())
                            .isEqualTo(ErrorStatus.NOT_FOUND_CONTENT));
        }
    }

    @Nested
    @DisplayName("createContent()")
    class CreateContent {

        @Test
        @DisplayName("정상적으로 content 생성 및 Milvus upsert 호출")
        void createContent_success() {
            // given
            ContentCreateRequestDto dto = ContentCreateRequestDto.builder()
                    .title("새 제목")
                    .answer("새 답변")
                    .projectKey("project-key")
                    .build();

            CreateEmbeddingResponse mockEmbedResp = mock(CreateEmbeddingResponse.class);
            Embedding mockEmbedding = mock(Embedding.class);
            given(mockEmbedding.embedding()).willReturn(List.of(0.1f, 0.2f, 0.3f));
            given(mockEmbedResp.data()).willReturn(List.of(mockEmbedding));

            given(userService.findOneUser(1L)).willReturn(mockUser);
            given(projectService.findOneProjectByKey("project-key")).willReturn(mockProject);
            given(encryptionService.decryptData("encrypted-key")).willReturn("real-api-key");
            given(openAiLibraryService.embedding(anyString(), anyString(), anyLong(), anyString()))
                    .willReturn(mockEmbedResp);
            given(contentRepository.save(any(Content.class))).willReturn(mockContent);

            // when
            Content result = contentService.createContent(1L, dto);

            // then
            assertThat(result).isNotNull();
            verify(milvusService).upsertCollection(anyLong(), any(InsertRequestDto.class), anyLong());
        }

        @Test
        @DisplayName("OpenAI Key 없는 프로젝트에서 content 생성 시 CustomException 발생")
        void createContent_noOpenAiKey() {
            // given
            Project projectWithoutKey = Project.builder()
                    .id(2L)
                    .key("no-key-project")
                    .openAiKey("")
                    .embedModel("text-embedding-3-large")
                    .dimensions(1536L)
                    .build();

            ContentCreateRequestDto dto = ContentCreateRequestDto.builder()
                    .title("제목")
                    .answer("답변")
                    .projectKey("no-key-project")
                    .build();

            given(userService.findOneUser(1L)).willReturn(mockUser);
            given(projectService.findOneProjectByKey("no-key-project")).willReturn(projectWithoutKey);

            // when & then
            assertThatThrownBy(() -> contentService.createContent(1L, dto))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    @DisplayName("updateContent()")
    class UpdateContent {

        @Test
        @DisplayName("answer 변경 시 embedding 재생성 및 Milvus upsert 호출")
        void updateContent_answerChanged() {
            // given
            ContentUpdateRequestDto dto = mock(ContentUpdateRequestDto.class);
            given(dto.getUpdatedUserId()).willReturn(1L);
            given(dto.getAnswer()).willReturn("변경된 답변");

            CreateEmbeddingResponse mockEmbedResp = mock(CreateEmbeddingResponse.class);
            Embedding mockEmbedding = mock(Embedding.class);
            given(mockEmbedding.embedding()).willReturn(List.of(0.1f, 0.2f));
            given(mockEmbedResp.data()).willReturn(List.of(mockEmbedding));

            given(userService.findOneUser(1L)).willReturn(mockUser);
            given(contentRepository.findByIdWithProjectAndUser(1L)).willReturn(mockContent);
            given(encryptionService.decryptData(anyString())).willReturn("real-api-key");
            given(openAiLibraryService.embedding(anyString(), anyString(), anyLong(), anyString()))
                    .willReturn(mockEmbedResp);

            // when
            contentService.updateContent(1L, dto);

            // then
            verify(milvusService).upsertCollection(anyLong(), any(), anyLong());
        }

        @Test
        @DisplayName("answer 미변경 시 embedding 재생성 안 함")
        void updateContent_answerNotChanged() {
            // given
            ContentUpdateRequestDto dto = mock(ContentUpdateRequestDto.class);
            given(dto.getUpdatedUserId()).willReturn(1L);
            given(dto.getAnswer()).willReturn("테스트 답변"); // 기존과 동일

            given(userService.findOneUser(1L)).willReturn(mockUser);
            given(contentRepository.findByIdWithProjectAndUser(1L)).willReturn(mockContent);

            // when
            contentService.updateContent(1L, dto);

            // then
            verify(openAiLibraryService, never()).embedding(anyString(), anyString(), anyLong(), anyString());
            verify(milvusService, never()).upsertCollection(anyLong(), any(), anyLong());
        }

        @Test
        @DisplayName("존재하지 않는 content 수정 시 CustomException 발생")
        void updateContent_notFound() {
            // given
            ContentUpdateRequestDto dto = mock(ContentUpdateRequestDto.class);
            given(dto.getUpdatedUserId()).willReturn(1L);
            given(userService.findOneUser(1L)).willReturn(mockUser);
            given(contentRepository.findByIdWithProjectAndUser(999L)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> contentService.updateContent(999L, dto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getBaseCode())
                            .isEqualTo(ErrorStatus.NOT_FOUND_CONTENT));
        }
    }
}