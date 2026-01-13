/*
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
 */

package com.hmdm.plugins.devicereset.rest;

import com.hmdm.notification.PushService;
import com.hmdm.notification.persistence.domain.PushMessage;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.rest.json.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * REST resource for device commands (reboot, factory reset, lock, password reset).
 */
@Singleton
@Path("/plugins/devicereset")
@Api(tags = {"Device Commands plugin"})
public class DeviceCommandsResource {

    private static final Logger logger = LoggerFactory.getLogger(DeviceCommandsResource.class);

    private DeviceDAO deviceDAO;
    private PushService pushService;

    @Inject
    public DeviceCommandsResource(DeviceDAO deviceDAO, PushService pushService) {
        this.deviceDAO = deviceDAO;
        this.pushService = pushService;
    }

    /**
     * Reboot a device
     */
    @POST
    @Path("/private/reboot/{number}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Reboot device", notes = "Sends a reboot command to the specified device",
            authorizations = {@Authorization("Bearer Token")})
    public Response reboot(@PathParam("number") String deviceNumber) {
        return sendCommand(deviceNumber, "reboot");
    }

    /**
     * Factory reset a device
     */
    @POST
    @Path("/private/factory-reset/{number}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Factory reset device", notes = "Sends a factory reset command to the specified device",
            authorizations = {@Authorization("Bearer Token")})
    public Response factoryReset(@PathParam("number") String deviceNumber) {
        return sendCommand(deviceNumber, "factoryReset");
    }

    /**
     * Lock a device
     */
    @POST
    @Path("/private/lock/{number}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Lock device", notes = "Sends a lock command to the specified device",
            authorizations = {@Authorization("Bearer Token")})
    public Response lock(@PathParam("number") String deviceNumber) {
        return sendCommand(deviceNumber, "lockDevice");
    }

    /**
     * Reset device password
     */
    @POST
    @Path("/private/password-reset/{number}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Reset device password", notes = "Sends a password reset command to the specified device",
            authorizations = {@Authorization("Bearer Token")})
    public Response passwordReset(@PathParam("number") String deviceNumber,
                                  @QueryParam("password") String password) {
        if (password == null || password.isEmpty()) {
            return Response.ERROR("Password is required");
        }
        Device device = deviceDAO.getDeviceByNumber(deviceNumber);
        if (device == null) {
            return Response.ERROR("Device not found: " + deviceNumber);
        }

        PushMessage message = new PushMessage();
        message.setDeviceId(device.getId());
        message.setMessageType("resetPassword");
        message.setPayload("{\"password\":\"" + password + "\"}");

        pushService.send(message);
        logger.info("Password reset command sent to device: {}", deviceNumber);
        return Response.OK();
    }

    /**
     * Uninstall MDM from a device (for device sale or removal)
     */
    @POST
    @Path("/private/uninstall-mdm/{number}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Uninstall MDM", notes = "Sends an uninstall MDM command to the specified device and removes it from management",
            authorizations = {@Authorization("Bearer Token")})
    public Response uninstallMDM(@PathParam("number") String deviceNumber) {
        try {
            Device device = deviceDAO.getDeviceByNumber(deviceNumber);
            if (device == null) {
                return Response.ERROR("Device not found: " + deviceNumber);
            }

            PushMessage message = new PushMessage();
            message.setDeviceId(device.getId());
            message.setMessageType("uninstallMdm");

            pushService.send(message);
            logger.info("Uninstall MDM command sent to device: {}", deviceNumber);
            return Response.OK();
        } catch (Exception e) {
            logger.error("Failed to send uninstall MDM command to device {}", deviceNumber, e);
            return Response.ERROR("Failed to send uninstall command");
        }
    }

    private Response sendCommand(String deviceNumber, String commandType) {
        try {
            Device device = deviceDAO.getDeviceByNumber(deviceNumber);
            if (device == null) {
                return Response.ERROR("Device not found: " + deviceNumber);
            }

            PushMessage message = new PushMessage();
            message.setDeviceId(device.getId());
            message.setMessageType(commandType);

            pushService.send(message);
            logger.info("Command {} sent to device: {}", commandType, deviceNumber);
            return Response.OK();
        } catch (Exception e) {
            logger.error("Failed to send command {} to device {}", commandType, deviceNumber, e);
            return Response.ERROR("Failed to send command");
        }
    }
}
