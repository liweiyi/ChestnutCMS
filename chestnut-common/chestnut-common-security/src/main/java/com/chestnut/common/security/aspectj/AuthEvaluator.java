package com.chestnut.common.security.aspectj;

import com.chestnut.common.utils.StringUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SA-TOKEN认证SpEL处理类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
public class AuthEvaluator {

    /**
     * SpEL解析器
     */
    private final SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * 表达式缓存
     */
    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

    /**
     * 表达式模板
     */
    private final ParserContext template = new TemplateParserContext("${", "}");

    /**
     * 参数解析器
     */
    static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    /**
     * 解析EL表达式
     *
     * @param expression 字符串表达式
     * @return 翻译后表达式
     */
    Expression parseExpression(final String expression) {
        if (StringUtils.isBlank(expression)) {
            return null;
        }
        return expressionCache.computeIfAbsent(expression, expressionStr ->
            parser.parseExpression(expression.trim(), template)
        );
    }
}