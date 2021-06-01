package com.mnewt00.vulcandatabase.storage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter
public enum StorageType {
    MYSQL("mysql"), MARIADB("mariadb"), MONGODB("mongodb");

    private final String configName;
}
