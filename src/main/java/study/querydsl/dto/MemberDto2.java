package study.querydsl.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDto2 {
    private String name;
    private int age;

    public MemberDto2(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
