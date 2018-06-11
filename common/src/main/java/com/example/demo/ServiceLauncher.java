package com.example.demo;


import io.vertx.core.Launcher;

public class ServiceLauncher extends Launcher {

    public ServiceLauncher(){
    }



    /**
     * Utility method to execute a specific command.
     *
     * @param cmd  the command
     * @param args the arguments
     */
    public static void executeCommand(String cmd, String... args) {
        new ServiceLauncher().execute(cmd, args);
    }
}
