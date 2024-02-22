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
package com.chestnut.common.db.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.chestnut.common.db.DBErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.google.common.collect.ImmutableSortedMap;

import java.util.*;
import java.util.stream.Stream;

public class SqlBuilder {

    ArrayList<Object> params = new ArrayList<>();

    ArrayList<ArrayList<Object>> batches;

    private boolean batchMode;

    StringBuilder sql = new StringBuilder();

    /**
     * 批量操作时，返回所有批量操作的参数列表
     */
    ArrayList<ArrayList<Object>> getBatches() {
        return batches;
    }

    /**
     * 构造一个空的查询，等待使用setSQL()方法设置SQL语句
     */
    public SqlBuilder() {
    }

    /**
     * 根据传入的SQL字符串构造一个SQL查询，参数个数可变
     */
    public SqlBuilder(String sql, Object... args) {
        setSQL(sql);
        if (args != null) {
            for (Object obj : args) {
                add(obj);
            }
        }
    }

    public boolean isBatchMode() {
        return batchMode;
    }

    public void setBatchMode(boolean batchMode) {
        if (batchMode && batches == null) {
            batches = new ArrayList<>();
        }
        this.batchMode = batchMode;
    }

    public void addBatch() {
        if (!batchMode) {
            throw new RuntimeException("This wrapper is not batch mode.");
        }
        batches.add(params);
        params = new ArrayList<>();
    }

    public SqlBuilder add(Object... params) {
        if (Objects.nonNull(params)) {
            for (Object param : params) {
                this.params.add(param);
            }
        }
        return this;
    }

    public SqlBuilder add(Collection<?> params) {
        for (Object param : params) {
            this.params.add(param);
        }
        return this;
    }

    public SqlBuilder set(int index, Object param) {
        params.set(index, param);
        return this;
    }

    public SqlBuilder setSQL(String sql) {
        this.sql.setLength(0);
        this.sql.append(sql);
        return this;
    }

    public SqlBuilder append(String sqlPart, Object... params) {
        sql.append(sqlPart);
        if (Objects.isNull(params)) {
            return this;
        }
        for (Object obj : params) {
            add(obj);
        }
        return this;
    }

    private String getSQL() {
        String sqlStr = sql.toString();
        int index = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sqlStr.length(); i++) {
            if (sqlStr.charAt(i) == '?') {
                sb.append("{").append(index).append("}");
                index++;
            } else {
                sb.append(sqlStr.charAt(i));
            }
        }
        if (index != this.getParams().size()) {
            throw new RuntimeException("The sql params size not equals sql placeholders: "
                    + params.size() + " != " + index);
        }
        return sb.toString();
    }

    public ArrayList<Object> getParams() {
        return params;
    }

    public void setParams(ArrayList<Object> list) {
        this.params = list;
    }

    public void clearBatches() {
        if (batchMode && Objects.nonNull(batches)) {
            batches.clear();
        }
    }

    public SqlBuilder where() {
        append(" WHERE");
        return this;
    }

    public SqlBuilder and() {
        append(" AND");
        return this;
    }

    public SqlBuilder not() {
        append(" NOT");
        return this;
    }

    public SqlBuilder or() {
        append(" OR");
        return this;
    }

    /**
     * in (values)
     */
    public SqlBuilder in(String field, Object... values) {
        return in(field, Stream.of(values).toList());
    }

    /**
     * in (values)
     */
    public SqlBuilder in(String field, Collection<?> values) {
        this.checkSqlInjection(field);
        Assert.isTrue(StringUtils.isNotEmpty(values), DBErrorCode.SQL_IN_PARAMS_EMPTY::exception);

        append(" ");
        append(field);
        append(" IN (");
        boolean first = true;
        for (Object obj : values) {
            if (first) {
                first = false;
            } else {
                append(", ");
            }
            append("?", obj);
        }
        append(")");
        return this;
    }

    /**
     * 等于：field = value
     */
    public SqlBuilder eq(String field, Object value) {
        this.checkSqlInjection(field);
        append(" ");
        append(field);
        append(" = ?", value);
        return this;
    }

    /**
     * 字段等于：field1 = field2
     */
    public SqlBuilder eqField(String field1, String field2) {
        this.checkSqlInjection(field1);
        this.checkSqlInjection(field2);
        append(" ");
        append(field1);
        append("=");
        append(field2);
        return this;
    }

    /**
     * 不等于：field <> value
     */
    public SqlBuilder ne(String field, Object value) {
        this.checkSqlInjection(field);
        append(" ");
        append(field);
        append("<>?", value);
        return this;
    }

    /**
     * 大于： field > value
     */
    public SqlBuilder gt(String field, Object value) {
        this.checkSqlInjection(field);
        append(" ");
        append(field);
        append(">?", value);
        return this;
    }

    /**
     * 小于：field < value
     */
    public SqlBuilder lt(String field, Object value) {
        this.checkSqlInjection(field);
        append(" ");
        append(field);
        append("<?", value);
        return this;
    }

    /**
     * 大于等于：field >= value
     */
    public SqlBuilder ge(String field, Object value) {
        this.checkSqlInjection(field);
        append(" ");
        append(field);
        append(">=?", value);
        return this;
    }

    /**
     * 小于等于：field <= value
     */
    public SqlBuilder le(String field, Object value) {
        this.checkSqlInjection(field);
        append(" ");
        append(field);
        append("<=?", value);
        return this;
    }

    /**
     * Like: field like %value%
     */
    public SqlBuilder like(String field, String value) {
        this.checkSqlInjection(field);
        append(" ");
        append(field);
        append(" like ?", "%" + value + "%");
        return this;
    }

    /**
     * 右like: field like value%
     */
    public SqlBuilder likeRight(String field, String value) {
        this.checkSqlInjection(field);
        append(" ");
        append(field);
        append(" like ?", value + "%");
        return this;
    }

    /**
     * 左like: field like %value
     */
    public SqlBuilder likeLeft(String field, String value) {
        this.checkSqlInjection(field);
        append(" ");
        append(field);
        append(" like ?", "%" + value);
        return this;
    }

    /**
     * 左括号
     */
    public SqlBuilder braceLeft() {
        append(" (");
        return this;
    }

    /**
     * 右括号
     */
    public SqlBuilder braceRight() {
        append(")");
        return this;
    }

    public SqlBuilder orderBy(String field, boolean isAsc) {
        return this.orderBy(ImmutableSortedMap.of(field, isAsc));
    }

    public SqlBuilder orderBy(String field1, boolean isAsc1,
                              String field2, boolean isAsc2) {
        return this.orderBy(ImmutableSortedMap.of(field1, isAsc1, field2, isAsc2));
    }

    public SqlBuilder orderBy(String field1, boolean isAsc1,
                              String field2, boolean isAsc2,
                              String field3, boolean isAsc3) {
        return this.orderBy(ImmutableSortedMap.of(field1, isAsc1, field2, isAsc2, field3, isAsc3));
    }

    /**
     * 增加order by子句
     */
    public SqlBuilder orderBy(Map<String, Boolean> fields) {
        append(" ORDER BY ");
        boolean first = true;
        Iterator<Map.Entry<String, Boolean>> iterator = fields.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Boolean> next = iterator.next();
            String field = next.getKey();
            checkSqlInjection(field);
            boolean isAsc = next.getValue();
            if (first) {
                first = false;
                append(", ");
            }
            append(field).append(" ").append(isAsc ? "ASC" : "DESC");
        }
        return this;
    }

    /**
     * 增加group by子句
     */
    public SqlBuilder groupBy(String... fields) {
        if (Objects.nonNull(fields)) {
            append(" GROUP BY ");
            boolean first = true;
            for (String field : fields) {
                this.checkSqlInjection(field);
                if (first) {
                    first = false;
                } else {
                    append(", ");
                }
                append(field);
            }
        }
        return this;
    }

    /**
     * 增加 having 关键字.
     */
    public SqlBuilder having() {
        append(" HAVING");
        return this;
    }

    /**
     * 增加 exists 关键字.
     */
    public SqlBuilder exists() {
        append(" EXISTS");
        return this;
    }

    public SqlBuilder selectAll() {
        append("SELECT *");
        return this;
    }

    /**
     * 增加 Select 子句
     */
    public SqlBuilder select(String... columns) {
        append("SELECT ");
        if (StringUtils.isEmpty(columns)) {
            append("*");
        } else {
            for (String column : columns) {
                this.checkSqlInjection(column);
            }
            append(StringUtils.join(columns, ","));
        }
        return this;
    }

    /**
     * 增加 Select 子句
     */
    public SqlBuilder select(Collection<String> columns) {
        append("SELECT ");
        if (StringUtils.isEmpty(columns)) {
            append("*");
        } else {
            for (String column : columns) {
                this.checkSqlInjection(column);
            }
            append(StringUtils.join(columns, ","));
        }
        return this;
    }

    /**
     * 增加 FROM 字句
     */
    public SqlBuilder from(String... tables) {
        Assert.isTrue(StringUtils.isNotEmpty(tables), DBErrorCode.SQL_FROM_TABLE::exception);
        append(" FROM ");
        boolean first = true;
        for (String table : tables) {
            this.checkSqlInjection(table);
            if (first) {
                first = false;
            } else {
                append(",");
            }
            append(table);
        }
        return this;
    }

    public SqlBuilder join(String table) {

        return this;
    }


    /**
     * 增加 DELETE 关键字
     */
    public SqlBuilder delete() {
        append("DELETE");
        return this;
    }

    /**
     * 增加 UPDATE 关键字
     */
    public SqlBuilder update(String table) {
        append("UPDATE ");
        this.checkSqlInjection(table);
        append(table);
        return this;
    }

    /**
     * 增加 SET 字句
     */
    public SqlBuilder set(String field, Object value) {
        if (this.sql.toString().toUpperCase().indexOf(" SET ") > 0) {
            append(","); // 前面己有赋值语句
        } else {
            append(" SET ");
        }
        this.checkSqlInjection(field);
        append(field).append(" = ?");
        this.getParams().add(value);
        return this;
    }

    public SqlBuilder addPart(SqlBuilder wherePart) {
        if (Objects.nonNull(wherePart)) {
            this.append(wherePart.getSQL());
            this.getParams().addAll(wherePart.getParams());
        }
        return this;
    }

    public SqlBuilder between(String field, Object start, Object end) {
        this.checkSqlInjection(field);
        append(" ").append(field).append(" between ? and ?", start, end);
        return this;
    }

    /**
     * 增加 INSERT INTO 语句
     */
    public SqlBuilder insertInto(String table, String[] columns, Object[] values) {
        if (StringUtils.isEmpty(columns) || StringUtils.isEmpty(values)) {
            throw new RuntimeException("The sql `INSERT INTO` columns/values cannot be empty.");
        }
        if (columns.length != values.length) {
            throw new RuntimeException("The sql `INSERT INTO` columns length not equals values length.");
        }
        _insertInto(table, columns, values);
        return this;
    }

    /**
     * 增加 INSERT INTO 语句
     */
    public SqlBuilder insertInto(String table, Collection<String> columns, Collection<?> values) {
        if (StringUtils.isEmpty(columns) || StringUtils.isEmpty(values)) {
            throw new RuntimeException("The sql `INSERT INTO` columns/values cannot be empty.");
        }
        if (columns.size() != values.size()) {
            throw new RuntimeException("The sql `INSERT INTO` columns length not equals values length.");
        }
        _insertInto(table, columns.toArray(String[]::new), values.toArray(Object[]::new));
        return this;
    }

    private void _insertInto(String table, String[] columns, Object[] values) {
        append("INSERT INTO ");
        append(table);
        append(" (");
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                append(",");
            }
            append(columns[i]);
        }
        append(") VALUES (");
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                append(",");
            }
            if (values == null) {
                append("?");
            } else {
                append("?", values[i]);
            }
        }
        append(")");
    }

    public SqlBuilder insertInto(String table, String[] columns) {
        if (StringUtils.isEmpty(columns)) {
            throw new RuntimeException("The sql `INSERT INTO` columns cannot be empty.");
        }
        _insertInto(table, columns, null);
        return this;
    }

    private void checkSqlInjection(String target) {

    }

    public List<Map<String, Object>> selectList() {
        return SqlRunner.db().selectList(this.getSQL(), this.getParams().toArray(Object[]::new));
    }

    public IPage<Map<String, Object>> selectPage(IPage<Map<String, Object>> page) {
        return SqlRunner.db().selectPage(page, this.getSQL(), this.getParams().toArray(Object[]::new));
    }

    public long selectCount() {
        return SqlRunner.db().selectCount(this.getSQL(), this.getParams().toArray(Object[]::new));
    }

    public Map<String, Object> selectOne() {
        return SqlRunner.db().selectOne(this.getSQL(), this.getParams().toArray(Object[]::new));
    }

    public void execInsert() {
        if (this.isBatchMode()) {
            String sqlString = this.getSQL();
            // TODO 优化 insert into () values (),(),()...
            this.getBatches().forEach(args -> SqlRunner.db().insert(sqlString, args));
        } else {
            SqlRunner.db().insert(this.getSQL(), this.getParams().toArray(Object[]::new));
        }
    }

    public void executeUpdate() {
        SqlRunner.db().update(this.getSQL(), this.getParams().toArray(Object[]::new));
    }

    public void executeDelete() {
        SqlRunner.db().delete(this.getSQL(), this.getParams().toArray(Object[]::new));
    }
}