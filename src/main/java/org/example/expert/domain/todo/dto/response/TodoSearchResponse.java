package org.example.expert.domain.todo.dto.response;

import lombok.Getter;


@Getter
public class TodoSearchResponse {
    private  String title;
    private  Long managerCnt;
    private  Long commentCnt;

    public TodoSearchResponse(String title, Long managerCnt, Long commentCnt){
        this.title = title;
        this.managerCnt = managerCnt;
        this.commentCnt = commentCnt;
    }
}
