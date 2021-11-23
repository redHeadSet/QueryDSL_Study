package study.querydsl.dto;

import lombok.Data;

@Data
public class SearchCondition {
    private String username;
    private String teamname;
    private Integer ageGoe;
    private Integer ageLoe;

    public SearchCondition(String username, String teamname, Integer ageGoe, Integer ageLoe) {
        this.username = username;
        this.teamname = teamname;
        this.ageGoe = ageGoe;
        this.ageLoe = ageLoe;
    }
}
