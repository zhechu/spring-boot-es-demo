package com.wise.demo.services.elasticsearch;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @description:
 * @author: lingyuwang
 * @create: 2019-10-28 19:00
 **/
@Slf4j
public class StringTest {

    @Test
    public void opencc4jTest() {
        String original = "贾路";
        String result = ZhConverterUtil.convertToSimple(original);
        log.info("result:{}", result);
    }

}
