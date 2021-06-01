package com.mnewt00.vulcandatabase.storage;

import com.mnewt00.vulcandatabase.Log;

import java.util.List;
import java.util.UUID;

public interface AbstractStorageProvider {
    List<Log> getLogs(int amount, UUID uuid);
    void addLog(Log log, UUID uuid);

    void initiateTables(String prefix);
}
