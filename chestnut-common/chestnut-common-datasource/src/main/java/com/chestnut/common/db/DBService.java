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
package com.chestnut.common.db;

import com.chestnut.common.db.domain.DBTable;
import com.chestnut.common.db.domain.DBTableColumn;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 数据库信息获取服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class DBService {

    private static final Pattern SIMPLE_IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_$]*");

    private final DataSource dataSource;

    private volatile String identifierQuoteString;

    /**
     * 获取数据库表元数据信息
     *
     * @param tableName
     * @return
     */
    public List<DBTable> listTables(@Nullable String tableName) {
        List<DBTable> tables = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            try (ResultSet rs = metaData.getTables(connection.getCatalog(), null, tableName, new String[]{"TABLE"})) {
                while (rs.next()) {
                    DBTable dbTable = new DBTable();
                    dbTable.setCatalog(rs.getString("TABLE_CAT"));
                    dbTable.setSchema(rs.getString("TABLE_SCHEM"));
                    dbTable.setName(rs.getString("TABLE_NAME"));
                    dbTable.setType(rs.getString("TABLE_TYPE"));
                    dbTable.setComment(rs.getString("REMARKS"));

                    this.setTableColumns(dbTable, metaData);
                    tables.add(dbTable);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tables;
    }

    /**
     * 精确查找当前数据源中的表，避免将 JDBC tableNamePattern 的通配符结果当成精确匹配。
     */
    public Optional<DBTable> findTable(String tableName) {
        if (tableName == null || tableName.isBlank()) {
            return Optional.empty();
        }
        List<DBTable> tables = this.listTables(tableName);
        Optional<DBTable> match = this.findTable(tables, tableName);
        if (match.isPresent()) {
            return match;
        }

        // 部分数据库的 tableNamePattern 区分大小写。首次查询无匹配时遍历当前 catalog，
        // 以 JDBC 元数据返回的真实名称完成一次兼容性查找。
        return this.findTable(this.listTables(null), tableName);
    }

    private Optional<DBTable> findTable(List<DBTable> tables, String tableName) {
        List<DBTable> exact = tables.stream()
                .filter(table -> table.getName().equals(tableName))
                .toList();
        if (exact.size() == 1) {
            return Optional.of(exact.get(0));
        }
        List<DBTable> caseInsensitive = tables.stream()
                .filter(table -> table.getName().equalsIgnoreCase(tableName))
                .toList();
        return caseInsensitive.size() == 1 ? Optional.of(caseInsensitive.get(0)) : Optional.empty();
    }

    /**
     * 使用当前数据库声明的标识符引用符包装表名或字段名。
     */
    public String quoteIdentifier(String identifier) {
        return quoteIdentifier(identifier, this.getIdentifierQuoteString());
    }

    static String quoteIdentifier(String identifier, String quote) {
        Objects.requireNonNull(identifier, "SQL identifier cannot be null.");
        if (identifier.isBlank() || identifier.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + identifier);
        }
        if (quote.isEmpty()) {
            if (!SIMPLE_IDENTIFIER.matcher(identifier).matches()) {
                throw new IllegalArgumentException("The database does not support quoting this SQL identifier: " + identifier);
            }
            return identifier;
        }
        return quote + identifier.replace(quote, quote + quote) + quote;
    }

    private String getIdentifierQuoteString() {
        String quote = this.identifierQuoteString;
        if (quote == null) {
            synchronized (this) {
                quote = this.identifierQuoteString;
                if (quote == null) {
                    try (Connection connection = dataSource.getConnection()) {
                        quote = connection.getMetaData().getIdentifierQuoteString();
                        quote = quote == null || quote.isBlank() ? "" : quote;
                        this.identifierQuoteString = quote;
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return quote;
    }

    private void setTableColumns(DBTable table, DatabaseMetaData metaData) throws SQLException {
        List<String> primaryKeys = new ArrayList<>();
        try (ResultSet rsPrimaryKeys = metaData.getPrimaryKeys(table.getCatalog(), table.getSchema(), table.getName())) {
            while(rsPrimaryKeys.next()) {
                primaryKeys.add(rsPrimaryKeys.getString("COLUMN_NAME"));
            }
        }

        try (ResultSet columns = metaData.getColumns(table.getCatalog(), table.getSchema(), table.getName(), "%")) {
            while (columns.next()) {
                DBTableColumn column = new DBTableColumn();
                column.setName(columns.getString("COLUMN_NAME"));
                column.setType(columns.getInt("DATA_TYPE"));
                column.setTypeName(columns.getString("TYPE_NAME"));
                column.setSize(columns.getInt("COLUMN_SIZE"));
                column.setDecimalDigits(columns.getInt("DECIMAL_DIGITS"));
                column.setNullable(ResultSetMetaData.columnNullable == columns.getInt("NULLABLE"));
                column.setDefaultValue(columns.getString("COLUMN_DEF"));
                column.setComment(columns.getString("REMARKS"));
                column.setAutoIncrement("YES".equals(columns.getString("IS_AUTOINCREMENT")));
                column.setPrimary(primaryKeys.contains(column.getName()));
                table.getColumns().add(column);
            }
        }
    }
}
