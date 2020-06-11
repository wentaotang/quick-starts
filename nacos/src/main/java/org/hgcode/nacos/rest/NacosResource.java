package org.hgcode.nacos.rest;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;

@RestController
public class NacosResource {

    @NacosValue(value = "${abc.def:}", autoRefreshed = true)
    private String value;


    @NacosInjected
    private ConfigService configService;

    @GetMapping(value = "/get")
    public String  get() {
        return value;
    }


    @GetMapping(value = "/set")
    public String  set() throws NacosException {
        String dataId = "app";
        String group = DEFAULT_GROUP;
        String content =  configService.getConfig(dataId,group,5000L);
        return content;
    }
}
