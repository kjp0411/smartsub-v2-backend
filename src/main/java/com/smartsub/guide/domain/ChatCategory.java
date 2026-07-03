package com.smartsub.guide.domain;

import java.util.Arrays;

public enum ChatCategory {
    MENU,       // 메뉴
    PARKING,    // 주차
    FACILITY,   // 시설
    HOURS,      // 운영시간
    EVENT,      // 이벤트
    LOCATION,   // 위치
    ETC;        //

    public static ChatCategory from(String value) {
        if (value == null) {
            return ETC;
        }
        return Arrays.stream(values())
            .filter(category -> category.name().equalsIgnoreCase(value.trim()))
            .findFirst()
            .orElse(ETC);
    }
}
