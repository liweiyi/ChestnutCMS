/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.contentcore.service.impl;

import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.config.properties.CMSProperties;
import com.chestnut.contentcore.core.AbstractContent;
import com.chestnut.contentcore.dao.CmsContentDAO;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.ContentDTO;
import com.chestnut.contentcore.listener.event.BeforeContentSaveEvent;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.service.ISiteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.support.StaticApplicationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 内容保存入口路径校验测试
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
class ContentSavePathTest {
    @TempDir
    Path temporary;
    StaticApplicationContext application;
    Object previousFactory;
    Object previousContext;
    AtomicInteger writes;
    TestContent content;
    List<CmsPublishPipe> publishPipes;

    @BeforeEach
    void setUp() throws Exception {
        previousFactory = springField("beanFactory").get(null);
        previousContext = springField("applicationContext").get(null);
        application = new StaticApplicationContext();
        application.getStaticMessageSource().setUseCodeAsDefaultMessage(true);
        application.refresh();
        SpringUtils spring = new SpringUtils();
        spring.postProcessBeanFactory(application.getBeanFactory());
        spring.setApplicationContext(application);
        CMSProperties properties = new CMSProperties();
        properties.setResourceRoot(temporary.toRealPath().toString());
        new CMSConfig(properties, null, List.of());
        CmsSite site = new CmsSite();
        site.setSiteId(1L);
        site.setPath("siteA");
        CmsPublishPipe pipe = new CmsPublishPipe();
        pipe.setCode("pc");
        CmsPublishPipe mobile = new CmsPublishPipe();
        mobile.setCode("mobile");
        publishPipes = List.of(pipe, mobile);
        ISiteService sites = (ISiteService) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{ISiteService.class}, (proxy, method, args) -> {
                    assertEquals("getSite", method.getName());
                    assertEquals(1L, args[0]);
                    return site;
                });
        IPublishPipeService pipes = (IPublishPipeService) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{IPublishPipeService.class}, (proxy, method, args) -> {
                    assertEquals("getPublishPipes", method.getName());
                    assertEquals(1L, args[0]);
                    return publishPipes;
                });
        writes = new AtomicInteger();
        CmsContentDAO dao = new CmsContentDAO() {
            @Override
            public boolean saveOrUpdate(CmsContent entity) {
                writes.incrementAndGet();
                return true;
            }
        };
        ContentServiceImpl service = new ContentServiceImpl(null, sites, null, pipes, null, null, dao, null);
        application.getBeanFactory().registerSingleton("contentService", service);
        content = new TestContent();
        CmsContent entity = new CmsContent();
        entity.setSiteId(1L);
        content.setContentEntity(entity);
    }

    @AfterEach
    void tearDown() throws Exception {
        springField("beanFactory").set(null, previousFactory);
        springField("applicationContext").set(null, previousContext);
        application.close();
    }

    @ParameterizedTest
    @ValueSource(strings = { "../siteB_pc/audit.shtml", "a/../../siteB_pc/a", "a/../b", "./a", "/tmp/a",
            "C:/a", "C:a", "\\\\server\\share\\a", "\\tmp\\a", "a\\..\\b", "a\0.html", "a/", "a//b",
            "template/a.shtml", "Template/a", "include/a", "INCLUDE/a", "assets/a", "Assets/a", "assets",
            "template\\a", "assets\\nested\\a" })
    void rejectsInvalidPathsAtBothInsertAndUpdateBoundary(String path) {
        for (boolean insert : List.of(true, false)) {
            content.getContentEntity().setStaticPath(path);
            assertThrows(GlobalException.class, () -> content.persist(insert));
        }
        assertEquals(0, writes.get());
        assertFalse(Files.exists(temporary.resolve("siteA_pc")));
    }

    @Test
    void validatesAfterBeforeSaveListenersHaveModifiedPath() {
        content.getContentEntity().setStaticPath("news/a.shtml");
        application.addApplicationListener(event -> {
            if (event instanceof BeforeContentSaveEvent) {
                content.getContentEntity().setStaticPath("assets/a.shtml");
            }
        });
        assertThrows(GlobalException.class, () -> content.persist(false));
        assertEquals(0, writes.get());
    }

    @Test
    void persistsCanonicalPathWithoutCreatingOutputDirectories() {
        content.getContentEntity().setStaticPath("news\\a.json");
        content.persist(true);
        assertEquals("news/a.json", content.getContentEntity().getStaticPath());
        assertEquals(1, writes.get());
        assertFalse(Files.exists(temporary.resolve("siteA_pc")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " \t", "a", "a.xml", "a.json", "news/中文.shtml", "news/include/a", "assets-old/a" })
    void acceptsBlankAndSafeRelativePathsWithoutSuffixRestrictions(String path) {
        content.getContentEntity().setStaticPath(path);
        content.persist(true);
        assertEquals(1, writes.get());
        assertFalse(Files.exists(temporary.resolve("siteA_pc")));
        assertFalse(Files.exists(temporary.resolve("siteA_mobile")));
    }

    @Test
    void rejectsLinkedChannelRootBeforeSaving() throws Exception {
        Path outside = Files.createDirectory(temporary.resolve("siteB_pc"));
        Files.createSymbolicLink(temporary.resolve("siteA_mobile"), outside);
        content.getContentEntity().setStaticPath("a.shtml");
        assertThrows(GlobalException.class, () -> content.persist(true));
        assertEquals(0, writes.get());
        assertFalse(Files.exists(outside.resolve("a.shtml")));
    }

    @Test
    void rejectsLinkedAncestorAndTargetIncludingDanglingLinks() throws Exception {
        Path root = Files.createDirectory(temporary.resolve("siteA_pc"));
        Path outside = Files.createDirectory(temporary.resolve("siteB_pc"));
        Files.createSymbolicLink(root.resolve("news"), outside);
        content.getContentEntity().setStaticPath("news/a.shtml");
        assertThrows(GlobalException.class, () -> content.persist(false));
        Files.createSymbolicLink(root.resolve("a.shtml"), outside.resolve("missing"));
        content.getContentEntity().setStaticPath("a.shtml");
        assertThrows(GlobalException.class, () -> content.persist(false));
        Files.writeString(outside.resolve("missing"), "keep");
        assertThrows(GlobalException.class, () -> content.persist(false));
        assertEquals("keep", Files.readString(outside.resolve("missing")));
        assertEquals(0, writes.get());
    }

    @Test
    void rejectsDirectoryAsStaticFileBeforeSaving() throws Exception {
        Files.createDirectories(temporary.resolve("siteA_pc/a.shtml"));
        content.getContentEntity().setStaticPath("a.shtml");
        assertThrows(GlobalException.class, () -> content.persist(true));
        assertEquals(0, writes.get());
    }

    @Test
    void checksSyntaxEvenWhenNoPublishChannelIsEnabled() {
        publishPipes = List.of();
        content.getContentEntity().setStaticPath("assets/a.shtml");
        assertThrows(GlobalException.class, () -> content.persist(true));
        assertEquals(0, writes.get());
    }

    private static Field springField(String name) throws Exception {
        Field field = SpringUtils.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    /**
     * 保存入口测试使用的内容实现
     *
     * @author 兮玥
     * @email 190785909@qq.com
     */
    private static class TestContent extends AbstractContent<Object> {
        void persist(boolean insert) { saveToDB(insert); }
        @Override protected void add0() { }
        @Override protected void save0(ContentDTO dto) { }
        @Override protected void saveToDB0() { }
        @Override protected void delete0() { }
        @Override protected void copyTo0(CmsContent target, Integer copyType) { }
    }
}
