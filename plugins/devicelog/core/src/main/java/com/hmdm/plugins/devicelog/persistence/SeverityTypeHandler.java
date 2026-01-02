/*
 *
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hmdm.plugins.devicelog.persistence;

import com.hmdm.plugins.devicelog.model.LogLevel;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p>A type handler for converting between database severity column and LogLevel enum.</p>
 */
public class SeverityTypeHandler extends BaseTypeHandler<LogLevel> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LogLevel parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter != null ? parameter.name() : null);
    }

    @Override
    public LogLevel getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (value == null) {
            return null;
        }
        try {
            return LogLevel.valueOf(value);
        } catch (IllegalArgumentException e) {
            // Try to convert from severityOrder if stored as number
            return LogLevel.NONE;
        }
    }

    @Override
    public LogLevel getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        try {
            return LogLevel.valueOf(value);
        } catch (IllegalArgumentException e) {
            return LogLevel.NONE;
        }
    }

    @Override
    public LogLevel getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        try {
            return LogLevel.valueOf(value);
        } catch (IllegalArgumentException e) {
            return LogLevel.NONE;
        }
    }
}
