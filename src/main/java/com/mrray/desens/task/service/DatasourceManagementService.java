package com.mrray.desens.task.service;

import com.mrray.desens.task.entity.dto.ExtractDto;
import com.mrray.desens.task.entity.dto.LoadDto;
import com.mrray.desens.task.entity.vo.ExtractRespVo;
import com.mrray.desens.task.entity.vo.RestResponseBody;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("datasource-management-server")
public interface DatasourceManagementService {
    @PostMapping("/extract_task")
    RestResponseBody extract(@RequestBody ExtractDto extractDto);

    @PostMapping("/load_task")
    RestResponseBody load(@RequestBody LoadDto loadDto);

    @GetMapping("/extract/table/{id}")
    RestResponseBody<ExtractRespVo> queryExtractTableInfo(@PathVariable(value = "id") String id);

    @PostMapping("/datasource/table/count")
    RestResponseBody<Long> queryExtractTableCount(@RequestBody ExtractDto extractDto);
}
