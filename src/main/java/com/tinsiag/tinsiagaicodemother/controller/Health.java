package com.tinsiag.tinsiagaicodemother.controller;

import com.tinsiag.tinsiagaicodemother.common.BaseResponse;
import com.tinsiag.tinsiagaicodemother.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class Health {
    @GetMapping("/")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}
