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
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hmdm.persistence.mapper;

import java.util.List;

import com.hmdm.persistence.domain.*;
import com.hmdm.service.DeviceApplicationsStatus;
import com.hmdm.service.DeviceConfigFilesStatus;
import org.apache.ibatis.annotations.*;
import com.hmdm.rest.json.DeviceLookupItem;

public interface DeviceMapper {

    int insertDevice(Device device);

    void insertDeviceGroups(@Param("id") Integer deviceId,
                            @Param("groups") List<Integer> groups);

    @Delete({"DELETE FROM deviceGroups " +
            "WHERE deviceId=#{deviceId} " +
            "  AND groupId IN ( " +
            "      SELECT groups.id " +
            "      FROM groups " +
            "      INNER JOIN users ON users.id = #{userId} " +
            "      WHERE groups.customerId = #{customerId} " +
            "      AND (users.allDevicesAvailable AND users.customerId = #{customerId} " +
            "           OR " +
            "           EXISTS (SELECT 1 FROM userDeviceGroupsAccess access WHERE groups.id = access.groupId AND access.userId = users.id)" +
            "      )" +
            "  )"})
    void removeDeviceGroupsByDeviceId(
            @Param("userId") int userId,
            @Param("customerId") int customerId,
            @Param("deviceId") Integer deviceId);

    void updateDevice(Device device);

    Device getDeviceByNumber(@Param("number") String number);

    Device getDeviceByOldNumber(@Param("number") String number);

    Device getDeviceByNumberIgnoreCase(@Param("number") String number);

    Device getDeviceByImeiOrSerial(@Param("number") String number);

    Device getDeviceById(@Param("id") Integer id);

    @Select({"SELECT * FROM devices " +
            "WHERE configurationId = #{configurationId} AND customerId = #{customerId}"})
    List<Device> getAllConfigurationDevices(@Param("configurationId") int configurationId,
                                            @Param("customerId") int customerId);

    @Select({"SELECT * FROM devices " +
            "LEFT JOIN deviceGroups ON deviceGroups.deviceId = devices.id " +
            "WHERE deviceGroups.groupId = #{groupId} AND devices.customerId = #{customerId}"})
    List<Device> getAllGroupDevices(@Param("groupId") int groupId, @Param("customerId") int customerId);

    @Select({"SELECT * FROM devices " +
            "WHERE customerId = #{customerId}"})
    List<Device> getAllCustomerDevices(@Param("customerId") int customerId);

    @Select({"SELECT devices.id AS id, devices.number, devices.description, devices.lastUpdate, " +
            "devices.configurationId, devices.info, devices.imei, devices.phone, devices.customerId, " +
            "devices.custom1, devices.custom2, devices.custom3, devices.oldNumber, devices.fastSearch, " +
            "devices.enrollTime, devices.publicIp, " +
            "groups.id AS groupId, groups.name AS groupName " +
            "FROM devices " +
            "LEFT JOIN deviceGroups ON devices.id = deviceGroups.deviceId " +
            "LEFT JOIN groups ON deviceGroups.groupId = groups.id " +
            "WHERE devices.customerId = #{customerId} " +
            "ORDER BY devices.id, groups.name"})
    List<Device> getAllCustomerDevicesWithGroups(@Param("customerId") int customerId);

    List<Device> getAllDevices(DeviceSearchRequest deviceSearchRequest);

    @Select({"SELECT COUNT(*) " +
            "FROM devices " +
            "WHERE customerId = #{customerId}"})
    Long countAllDevicesForCustomer(@Param("customerId") Integer customerId);

    @Select({"SELECT COUNT(*) " +
            "FROM devices"})
    Long countTotalDevices();

    @Select({"SELECT COUNT(*) " +
            "FROM devices " +
            "WHERE devices.lastUpdate >= extract(epoch from now()) * 1000 - 3600000"})
    Long countOnlineDevices();

    Long countAllDevices(DeviceSearchRequest filter);

    Long countAllDevicesForSummary(DeviceSummaryRequest filter);

    List<SummaryConfigItem> countDevicesByConfig(DeviceSummaryRequest filter);

    @Update({"UPDATE devices SET " +
            "  info = #{info}, " +
            "  infojson = #{info}::json, " +
            "  lastUpdate = CAST(EXTRACT(EPOCH FROM NOW()) * 1000 AS BIGINT), " +
            "  enrollTime = COALESCE(enrollTime, CAST(EXTRACT(EPOCH FROM NOW()) * 1000 AS BIGINT)), " +
            "  imeiUpdateTs = #{imeiUpdateTs}, " +
            "  publicIp = #{publicIp} " +
            "WHERE id = #{deviceId}"})
    void updateDeviceInfo(@Param("deviceId") Integer deviceId,
                          @Param("info") String info,
                          @Param("imeiUpdateTs") Long imeiUpdateTs,
                          @Param("publicIp") String publicIp);

    @Update({"UPDATE devices SET " +
            "  custom1 = #{custom1}, " +
            "  custom2 = #{custom2}, " +
            "  custom3 = #{custom3} " +
            "WHERE id = #{deviceId}"})
    void updateDeviceCustomProperties(@Param("deviceId") Integer deviceId,
                                      @Param("custom1") String custom1,
                                      @Param("custom2") String custom2,
                                      @Param("custom3") String custom3);

    @Update({"UPDATE devices SET oldNumber = null " +
            "WHERE id = #{deviceId}"})
    void clearOldNumber(@Param("deviceId") Integer deviceId);

    List<DeviceLookupItem> lookupDevices(@Param("userId") int userId,
                                         @Param("customerId") int customerId,
                                         @Param("filter") String filter,
                                         @Param("limit") int limit);

    @Delete({"DELETE FROM devices WHERE id = #{id}"})
    void removeDevice(@Param("id") Integer id);

    @Update({"UPDATE devices SET configurationId = #{configurationId} WHERE id = #{deviceId}"})
    void updateDeviceConfiguration(@Param("deviceId") Integer deviceId,
                                   @Param("configurationId") Integer configurationId);

    @Update({"UPDATE devices SET description = #{description} WHERE id = #{deviceId}"})
    void updateDeviceDescription(@Param("deviceId") Integer deviceId,
                                 @Param("description") String newDeviceDesc);

    @Update({"UPDATE devices SET fastSearch = RIGHT(number, #{fastSearchChars}) WHERE fastSearch IS NULL " +
            " OR LENGTH(fastSearch) != #{fastSearchChars}"})
    void updateFastSearch(@Param("fastSearchChars") Integer fastSearchChars);

    List<Group> getAllGroups(@Param("customerId") int customerId,
                             @Param("userId") Integer userId);

    List<Group> getAllGroupsUnsecure(@Param("customerId") int customerId);

    List<Group> getAllGroupsByValue(@Param("customerId") int customerId,
                                    @Param("value") String value,
                                    @Param("userId") Integer userId);

    @Select({"SELECT * FROM groups WHERE customerId=#{customerId} AND name = #{name}"})
    Group getGroupByName(@Param("customerId") int customerId, @Param("name") String name);

    @Insert({"INSERT INTO groups (name, customerId) VALUES (#{name}, #{customerId})"})
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertGroup(Group group);

    @Update({"UPDATE groups SET name = #{name} WHERE id = #{id}"})
    void updateGroup(Group group);

    @Select({"SELECT COUNT(*) " +
            "FROM deviceGroups " +
            "WHERE groupId = #{groupId}"})
    Long countDevicesByGroupId(@Param("groupId") Integer groupId);

    @Delete({"DELETE FROM groups WHERE id = #{id}"})
    void removeGroupById(@Param("id") Integer id);

    @Select({"SELECT * FROM groups WHERE id = #{id}"})
    Group getGroupById(@Param("id") Integer id);

    @Select("SELECT devices.id FROM devices WHERE customerId = #{customerId} AND configurationId = #{configurationId}")
    List<Device> getDeviceIdsByConfigurationId(@Param("customerId") Integer customerId,
                                               @Param("configurationId") int configurationId);

    @Select("SELECT devices.id FROM devices WHERE configurationId = #{configurationId}")
    List<Device> getDeviceIdsBySoleConfigurationId(@Param("configurationId") int configurationId);

    void insertDeviceApplicationSettings(@Param("id") Integer deviceId,
                                         @Param("appSettings") List<ApplicationSetting> applicationSettings);

    @Delete("DELETE FROM deviceApplicationSettings WHERE extRefId = #{id}")
    void deleteDeviceApplicationSettings(@Param("id") Integer deviceId);

    @Select("SELECT " +
            "    deviceApps.app ->> 'pkg' AS pkg, " +
            "    deviceApps.app ->> 'version' AS version, " +
            "    deviceApps.app ->> 'name' AS name " +
            "FROM (" +
            "    SELECT jsonb_array_elements(infojson -> 'applications') AS app " +
            "    FROM devices " +
            "    WHERE id = #{deviceId}" +
            ") deviceApps")
    List<DeviceApplication> getDeviceInstalledApplications(@Param("deviceId") int deviceId);

    @Update("INSERT INTO deviceStatuses (deviceId, configFilesStatus, applicationsStatus) " +
            "VALUES (#{deviceId}, #{filesStatus}, #{appsStatus})" +
            "ON CONFLICT ON CONSTRAINT deviceStatuses_pr_key DO " +
            "UPDATE SET configFilesStatus = EXCLUDED.configFilesStatus, applicationsStatus = EXCLUDED.applicationsStatus")
    int updateDeviceStatuses(@Param("deviceId") Integer deviceId,
                             @Param("filesStatus") DeviceConfigFilesStatus deviceConfigFilesStatus,
                             @Param("appsStatus") DeviceApplicationsStatus deviceApplicatiosStatus);

    @Select("SELECT id FROM devices")
    List<Integer> getAllDeviceIds();

    @Update("UPDATE groups SET credit = #{credit} WHERE id = #{groupId}")
    void updateGroupCredit(@Param("groupId") Integer groupId, @Param("credit") Integer credit);

    @Select("SELECT COALESCE(SUM(credit), 0) FROM groups WHERE credit IS NOT NULL")
    Integer getTotalCreditDirect();

    @Select("SELECT COALESCE(SUM(credit), 0) FROM groups WHERE id IN " +
            "<foreach item='groupId' index='index' collection='groupIds' open='(' separator=',' close=')'>" +
            "#{groupId}</foreach> AND credit IS NOT NULL")
    Integer getTotalCreditByGroupIdsDirect(@Param("groupIds") List<Integer> groupIds);
}
