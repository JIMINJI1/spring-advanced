package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class TodoCustomRepositoryImpl implements TodoCustomRepository {
    private final JPAQueryFactory jpaqueryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {

        QTodo todo = QTodo.todo;

        Todo result = jpaqueryFactory.selectFrom(todo)
                .leftJoin(todo.user, user)
                .fetchJoin() //영속화 -> n+1 문제 해결
                .where(todo.id.eq(todoId))
                .fetchOne(); //결과 반환 역할
        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> searchTodo(String title, LocalDateTime start, LocalDateTime end, String nickname, Pageable pageable) {
        QTodo todo = QTodo.todo;
        QComment comment = QComment.comment;
        QUser user = QUser.user;
        QManager manager = QManager.manager;

        BooleanBuilder builder = new BooleanBuilder();

        // 조건
        if (title != null && !title.isEmpty()) {
            builder.and(todo.title.like("%" + title + "%"));
        }

        if (start != null) {
            builder.and(todo.createdAt.after(start));
        }

        if (end != null) {
            builder.and(todo.createdAt.before(end));
        }

        if (nickname != null && !nickname.isEmpty()) {
            builder.and(user.nickname.like("%" + nickname + "%"));
        }

        // 쿼리 실행
        List<TodoSearchResponse> result = jpaqueryFactory.select(Projections.constructor(
                        TodoSearchResponse.class,
                        todo.title,
                        manager.count().as("managerCnt"),
                        comment.count().as("commentCnt")
                ))
                .from(todo)
                .leftJoin(todo.comments, comment)
                .leftJoin(todo.managers, manager)
                .leftJoin(todo.user, user)
                .where(builder)
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 페이지 반환
        return new PageImpl<>(result, pageable, result.size());
    }

    /*
    select t.title, count(m.comment_id), count(c.comment_id)
    from todo t
    join comment c on t.user_id = c.user_id
    join manager m on m.user_id = t.user_id
    group by todo_id
    order by created_at desc
    */
}
