package com.milvus.vector_spring.milvus.dto;

import java.util.List;

public record InsertRequestDto(
        long id,
        List<Float> vector,
        String title,
        String answer
) {}
