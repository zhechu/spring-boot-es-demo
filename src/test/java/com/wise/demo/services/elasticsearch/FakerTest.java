package com.wise.demo.services.elasticsearch;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Locale;

/**
 * @description:
 * @author: lingyuwang
 * @create: 2019-10-28 19:00
 **/
@Slf4j
public class FakerTest {

    @Test
    public void fakerTest() {
        Faker faker = new Faker(new Locale("zh-CN"));
        log.info("title:{}", faker.name().title());
        log.info("name:{}", faker.name().name());
    }

}
