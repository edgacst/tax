package com.taxflow.workplace.api;

import com.taxflow.workplace.WorkplaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
    public List<WorkplaceDto> list(@RequestParam(name = "tenantId", required = false) Long tenantId) {
        return workplaceService.list(tenantId);
    }
}
