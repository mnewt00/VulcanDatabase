package com.mnewt00.vulcandatabase.storage;

import com.mnewt00.vulcandatabase.Log;

import java.util.List;
import java.util.UUID;

public interface StorageProvider {

    void createTables();

    void addLog(Log log, UUID uuid);

    List<Log> getLogs(int amount, int offset, UUID uuid);

    int count(UUID uuid);
}
