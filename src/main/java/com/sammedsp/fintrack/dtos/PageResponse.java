package com.sammedsp.fintrack.dtos;

import java.util.List;

public record PageResponse<T>(List<T> content, boolean first, boolean last,long totalElements, int totalPages) {
}
