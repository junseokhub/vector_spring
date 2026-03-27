package com.milvus.vector_spring.project;

import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.common.service.EncryptionService;
import com.milvus.vector_spring.content.ContentRepository;
import com.milvus.vector_spring.milvus.MilvusService;
import com.milvus.vector_spring.project.dto.ProjectCreateRequestDto;
import com.milvus.vector_spring.project.dto.ProjectDeleteRequestDto;
import com.milvus.vector_spring.user.User;
import com.milvus.vector_spring.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @InjectMocks
    private ProjectService projectService;

    @Mock private ProjectRepository projectRepository;
    @Mock private UserService userService;
    @Mock private EncryptionService encryptionService;
    @Mock private ContentRepository contentRepository;
    @Mock private MilvusService milvusService;

    private User mockUser;
    private Project mockProject;

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
                .key("test-project-key")
                .openAiKey("encrypted-key")
                .dimensions(1536L)
                .totalToken(0)
                .createdBy(mockUser)
                .updatedBy(mockUser)
                .build();
    }

    @Nested
    @DisplayName("createProject()")
    class CreateProject {

        @Test
        @DisplayName("정상 생성 시 Milvus 스키마 생성 및 기본 Content 저장")
        void createProject_success() {
            // given
            ProjectCreateRequestDto dto = ProjectCreateRequestDto.builder()
                    .name("새 프로젝트")
                    .dimensions(1536L)
                    .createdUserId(1L)
                    .build();

            given(userService.findOneUser(1L)).willReturn(mockUser);
            given(projectRepository.save(any(Project.class))).willReturn(mockProject);

            // when
            Project result = projectService.createProject(dto);

            // then
            assertThat(result).isNotNull();
            verify(milvusService).createSchema(anyLong(), eq(1536));
        }

        @Test
        @DisplayName("Milvus 오류 시 CustomException 발생")
        void createProject_milvusError() {
            // given
            ProjectCreateRequestDto dto = ProjectCreateRequestDto.builder()
                    .name("새 프로젝트")
                    .dimensions(1536L)
                    .createdUserId(1L)
                    .build();

            given(userService.findOneUser(1L)).willReturn(mockUser);
            given(projectRepository.save(any(Project.class))).willReturn(mockProject);
            doThrow(new RuntimeException("Milvus 연결 실패"))
                    .when(milvusService).createSchema(anyLong(), anyInt());

            // when & then
            assertThatThrownBy(() -> projectService.createProject(dto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getBaseCode())
                            .isEqualTo(ErrorStatus.MILVUS_DATABASE_ERROR));
        }
    }

    @Nested
    @DisplayName("deleteProject()")
    class DeleteProject {

        @Test
        @DisplayName("프로젝트 마스터가 삭제 요청 시 성공")
        void deleteProject_success() {
            // given
            ProjectDeleteRequestDto dto = mock(ProjectDeleteRequestDto.class);
            given(dto.getKey()).willReturn("test-project-key");
            given(dto.getUserId()).willReturn(1L);
            given(projectRepository.findProjectByKey("test-project-key"))
                    .willReturn(Optional.of(mockProject));

            // when
            String result = projectService.deleteProject(dto);

            // then
            assertThat(result).isEqualTo("Deleted Success!");
            verify(milvusService).deleteCollection(1L);
            verify(projectRepository).delete(mockProject);
        }

        @Test
        @DisplayName("마스터가 아닌 유저가 삭제 시 CustomException 발생")
        void deleteProject_notMaster() {
            // given
            ProjectDeleteRequestDto dto = mock(ProjectDeleteRequestDto.class);
            given(dto.getKey()).willReturn("test-project-key");
            given(dto.getUserId()).willReturn(99L); // 다른 유저
            given(projectRepository.findProjectByKey("test-project-key"))
                    .willReturn(Optional.of(mockProject));

            // when & then
            assertThatThrownBy(() -> projectService.deleteProject(dto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getBaseCode())
                            .isEqualTo(ErrorStatus.NOT_PROJECT_MASTER_USER));
        }

        @Test
        @DisplayName("존재하지 않는 프로젝트 삭제 시 CustomException 발생")
        void deleteProject_notFound() {
            // given
            ProjectDeleteRequestDto dto = mock(ProjectDeleteRequestDto.class);
            given(dto.getKey()).willReturn("non-exist-key");
            given(projectRepository.findProjectByKey("non-exist-key"))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> projectService.deleteProject(dto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getBaseCode())
                            .isEqualTo(ErrorStatus.NOT_FOUND_PROJECT));
        }
    }

    @Nested
    @DisplayName("decryptOpenAiKey()")
    class DecryptOpenAiKey {

        @Test
        @DisplayName("암호화된 키 복호화 성공")
        void decryptKey_success() {
            // given
            given(encryptionService.decryptData("encrypted-key")).willReturn("sk-real-key");

            // when
            String result = projectService.decryptOpenAiKey(mockProject);

            // then
            assertThat(result).isEqualTo("sk-real-key");
        }

        @Test
        @DisplayName("OpenAI 키 없는 프로젝트 복호화 시 CustomException 발생")
        void decryptKey_noKey() {
            // given
            Project projectWithoutKey = Project.builder()
                    .id(2L)
                    .key("no-key")
                    .openAiKey(null)
                    .build();

            // when & then
            assertThatThrownBy(() -> projectService.decryptOpenAiKey(projectWithoutKey))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getBaseCode())
                            .isEqualTo(ErrorStatus.REQUIRE_OPEN_AI_INFO));
        }
    }

    @Nested
    @DisplayName("plusTotalToken()")
    class PlusTotalToken {

        @Test
        @DisplayName("토큰 누적 업데이트")
        void plusTotalToken_success() {
            // given
            given(projectRepository.findProjectByKeyForUpdate("test-project-key"))
                    .willReturn(mockProject);

            // when
            projectService.plusTotalToken("test-project-key", 500L);

            // then - totalToken이 0 + 500 = 500으로 업데이트
            assertThat(mockProject.getTotalToken()).isEqualTo(500L);
        }
    }
}