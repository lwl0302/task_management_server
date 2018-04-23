package com.mrray.desens.task.service;

import com.mrray.desens.task.entity.dto.DatabaseInfo;
import com.mrray.desens.task.entity.dto.ScanDto;
import com.mrray.desens.task.entity.vo.RestResponseBody;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("data-desensitive-server")
public interface DataDesensitiveService {
    @PostMapping("/scan")
    RestResponseBody scan(@RequestBody ScanDto scanDto);

    @PostMapping("/desensitive")
    RestResponseBody desensitive(@RequestBody DatabaseInfo targetInfo, @RequestParam("ruleId") String ruleId, @RequestParam("scan") String scan);

    @GetMapping("/desensitive/{taskUuid}")
    RestResponseBody<DatabaseInfo> getDatabaseInfo(@PathVariable(value = "taskUuid") String taskUuid);
}
