package com.taxflow.workplace.api;

import com.taxflow.workplace.WorkplaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workplaces")
@RequiredArgsConstructor
@Tag(name = "Workplaces", description = "사업장")
public class WorkplaceController {

    private final WorkplaceService workplaceService;

    @GetMapping
    @Operation(summary = "사업장 목록")
    public List<WorkplaceDto> list() {
        return workplaceService.list(null);
    }

    @GetMapping("/{id}")
    @Operation(summary = "사업장 상세")
    public WorkplaceDto get(@PathVariable Long id) {
        return workplaceService.get(null, id);
    }

    @PostMapping
    @Operation(summary = "사업장 등록")
    public WorkplaceDto create(@Valid @RequestBody SaveWorkplaceRequest body) {
        return workplaceService.create(null, body);
    }

    @PutMapping("/{id}")
    @Operation(summary = "사업장 수정")
    public WorkplaceDto update(@PathVariable Long id, @Valid @RequestBody SaveWorkplaceRequest body) {
        return workplaceService.update(null, id, body);
    }
}
