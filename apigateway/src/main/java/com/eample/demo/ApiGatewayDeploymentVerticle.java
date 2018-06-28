package com.eample.demo;

import com.example.demo.ServiceLauncher;
import com.example.demo.constant.Endpoints;
import com.example.demo.verticle.DepoymenetVerticle;

public class ApiGatewayDeploymentVerticle extends DepoymenetVerticle {

    public static void main(final String[] args) {
        ServiceLauncher.executeCommand("run", ApiGatewayDeploymentVerticle.class.getName());
    }

    @Override
    protected Class getVerticleClass() {
        return ApiGatewayVerticle.class;
    }

    @Override
    public String getServiceName() {
        return "Api Gateway";
    }

    @Override
    public String getEventBusAddress() {
        return Endpoints.API_GATEWAY;
    }
}
