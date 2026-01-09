package com.hmdm.plugins.devicereset.guice.module;

import com.google.inject.AbstractModule;
import com.hmdm.plugins.devicereset.rest.DeviceCommandsResource;

/**
 * Guice module for Device Commands plugin.
 */
public class DeviceResetModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DeviceCommandsResource.class);
    }
}
