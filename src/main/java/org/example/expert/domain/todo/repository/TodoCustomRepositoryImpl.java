package org.example.expert.domain.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.stereotype.Repository;

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
                .leftJoin(todo.user,user)
                .fetchJoin() //영속화 -> n+1 문제 해결
                .where(todo.id.eq(todoId))
                .fetchOne(); //결과 반환 역할
        return Optional.ofNullable(result);
    }
}
