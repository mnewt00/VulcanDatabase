package com.mnewt00.vulcandatabase;

import lombok.Data;

import java.util.UUID;

@Data
public class Log {
    private final UUID uuid;
    private final String playerName;
    private final long timestamp;
    private final String info;
    private final String checkName;
    private final String checkType;
    private final int vl;
    private final String version;
    private final int ping;
    private final double tps;
}
