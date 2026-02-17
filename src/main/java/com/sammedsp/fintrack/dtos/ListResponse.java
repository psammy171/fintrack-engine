package com.sammedsp.fintrack.dtos;

import java.util.List;

public record ListResponse<T>(List<T> data) {
}
