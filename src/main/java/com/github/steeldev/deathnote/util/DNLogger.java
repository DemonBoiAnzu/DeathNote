package com.github.steeldev.deathnote.util;

import java.util.logging.Logger;

public class DNLogger extends Logger {
    protected DNLogger(String name, String resourceBundleName) {
        super(name, resourceBundleName);
    }

    public static DNLogger getLogger() {
        return new DNLogger("", null);
    }

    @Override
    public void info(String msg) {
        String prefix = msg.replace("[NBTAPI]", Util.getNbtapiPrefix());
        Util.log(prefix);
    }
}
