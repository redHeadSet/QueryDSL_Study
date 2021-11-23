package study.querydsl.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // (access = AccessLevel.PROTECTED) // 엔티티가 아닌 애들은 public으로 해줘야 하는건가...?
public class MemberDto {
    private String username;
    private int age;

    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
