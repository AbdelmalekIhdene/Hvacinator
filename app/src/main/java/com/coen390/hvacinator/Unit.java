package com.coen390.hvacinator;

import java.util.Map;

public class Unit {
    public String ID;
    public String nickname;
    public Long targetTemperature;

    public Unit(Map<String, Object> map) {
        this.ID = (String) map.get("ID");
        this.nickname = (String) map.get("nickname");
        this.targetTemperature = (Long) map.get("targetTemperature");
    }

    public Unit(String ID, String nickname, Long targetTemperature) {
        this.ID = ID;
        this.nickname = nickname;
        this.targetTemperature = targetTemperature;
    }
}
