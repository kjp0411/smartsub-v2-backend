package com.smartsub.guide.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChatCategoryTest {

    @Test
    @DisplayName("정상적인 카테고리 값은 대소문자 무관하게 매칭된다")
    void from_validValue_returnsMatchedCategory() {
        assertThat(ChatCategory.from("MENU")).isEqualTo(ChatCategory.MENU);
        assertThat(ChatCategory.from("menu")).isEqualTo(ChatCategory.MENU);
        assertThat(ChatCategory.from("Parking")).isEqualTo(ChatCategory.PARKING);
    }

    @Test
    @DisplayName("앞뒤 공백이 있어도 정상 매칭된다")
    void from_valueWithWhitespace_returnsMatchedCategory() {
        assertThat(ChatCategory.from(" PARKING ")).isEqualTo(ChatCategory.PARKING);
        assertThat(ChatCategory.from("EVENT\n")).isEqualTo(ChatCategory.EVENT);
    }

    @Test
    @DisplayName("null 값은 ETC로 폴백된다")
    void from_nullValue_returnsEtc() {
        assertThat(ChatCategory.from(null)).isEqualTo(ChatCategory.ETC);
    }

    @Test
    @DisplayName("목록에 없는 값은 ETC로 폴백된다")
    void from_unknownValue_returnsEtc() {
        assertThat(ChatCategory.from("FOOD")).isEqualTo(ChatCategory.ETC);
        assertThat(ChatCategory.from("주차")).isEqualTo(ChatCategory.ETC);
    }

    @Test
    @DisplayName("빈 문자열은 ETC로 폴백된다")
    void from_emptyValue_returnsEtc() {
        assertThat(ChatCategory.from("")).isEqualTo(ChatCategory.ETC);
        assertThat(ChatCategory.from("   ")).isEqualTo(ChatCategory.ETC);
    }
}