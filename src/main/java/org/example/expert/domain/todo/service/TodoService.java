package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;

    @Transactional
    public TodoSaveResponse saveTodo(User user, TodoSaveRequest todoSaveRequest) {
        // User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail())
        );
    }

    public Page<TodoResponse> getTodos(String weather, String startDate, String endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        //Page<Todo> todos = todoRepository.findAllByOrderByModifiedAtDesc(pageable);

        //날짜 타입 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime start = (startDate != null) ? LocalDate.parse(startDate, formatter).atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? LocalDate.parse(endDate, formatter).atTime(LocalTime.MAX) : null;

        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 늦을 수 없습니다.");
        }

        if (start != null && end == null && end.isBefore(start)) {
            throw new IllegalArgumentException("종료 날짜는 시작 날짜보다 빠를 수 없습니다.");
        }


        Page<Todo> todos = todoRepository.findTodosByConditions(weather, start, end, pageable);

        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    public TodoResponse getTodo(long todoId) {
        Todo todo = todoRepository.findByIdWithUser(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

        User user = todo.getUser();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(user.getId(), user.getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }

    //도전 레벨3 10 검색기능
    public Page<TodoSearchResponse> getTodoSearch(String title, String startDate, String endDate, String nickname, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        //날짜 타입 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime start = (startDate != null) ? LocalDate.parse(startDate, formatter).atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? LocalDate.parse(endDate, formatter).atTime(LocalTime.MAX) : null;

        return todoRepository.searchTodo(title, start, end, nickname, pageable);
    }

}
