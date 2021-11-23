package study.querydsl.dto;


import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDto_qp {
    private String name;
    private int age;

    @QueryProjection
    public MemberDto_qp(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
