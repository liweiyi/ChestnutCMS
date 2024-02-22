/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.common.security.aspectj;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.ExcelExportable;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.ServletUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.condition.HeadersRequestCondition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * 通用导出Excel切面
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Aspect
@Component
@Order(-100)
@RequiredArgsConstructor
public class ExcelExportAspect {

    public static final String CONDITION_HEADER = "cc-export";

    private static final HeadersRequestCondition CONDITION = new HeadersRequestCondition(CONDITION_HEADER);

    @Around("@annotation(exportable)")
    public Object around(ProceedingJoinPoint joinPoint, ExcelExportable exportable) throws Throwable {
        Object obj = joinPoint.proceed();
        if (Objects.nonNull(CONDITION.getMatchingCondition(ServletUtils.getRequest()))
            && ServletUtils.getRequest().getMethod().equalsIgnoreCase(HttpMethod.POST.name())) {
            boolean flag = (obj instanceof R<?> r) && r.getData() instanceof TableData;
            Assert.isTrue(flag, () -> new RuntimeException("Unsupported returnType except `R<TableData<?>>` for excel export."));

            TableData<?> tableData = (TableData<?>) ((R<?>) obj).getData();
            exportExcel(tableData.getRows(), exportable.value(), ServletUtils.getResponse());
            return null;
        }
        return obj;
    }

    private void exportExcel(List<?> list, Class<?> clazz, HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());
        String fileName = "Export_" + clazz.getSimpleName() + "_" + DateUtils.dateTimeNow("yyyyMMddHHmmss");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        try (ExcelWriter writer = EasyExcel.write(response.getOutputStream(), clazz).build()) {
            WriteSheet sheet = EasyExcel.writerSheet(clazz.getSimpleName()).build();
            writer.write(list, sheet);
            response.setStatus(HttpStatus.OK.value());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}