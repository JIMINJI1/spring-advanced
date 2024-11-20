# 🛠️SPRING PLUS
- 요구사항에 따라 코드 리팩토링 하기

## 1. @Transactional의 이해
- 요구사항
  -  API(/todos) 호출 시, 아래와 같은 에러 발생 코드가 정상적으로 동작 하도록 코드 수정하기
- 에러로그 원문
```
   jakarta.servlet.ServletException: Request processing failed: org.springframework.orm.jpa.JpaSystemException: could not execute statement [Connection is read-only. Queries leading to data modification are not allowed] [insert into todos (contents,created_at,modified_at,title,user_id,weather) values (?,?,?,?,?,?)]
```
- 원인
  - Connection is read-only 오류는 @Transactional(readOnly = true)가 적용된 서비스 메서드를 호출했기 때문
  해당 메서드가 읽기 전용임을 나타내며, 읽기 전용으로 설정된 메서드에서 데이터를 수정하려고 하면 위와 같은 예외가 발생

- 해결방법
   - Controller에서 해당 API에 @Transactional 어노테이션을 추가

<br>

## 2. JWT의 이해

- 요구사항  
   - User 테이블에 nickname 컬럼 추가, nickname은 중복 가능, 프론트엔드 개발자가 JWT에서 유저의 닉네임 필요로 함

- 해결방법 
   - User Entity에 nickname 컬럼 추가, 회원가입시 nickname도 입력, 토큰 생성시 nickname도 포함

<br>

## 3. AOP의 이해
- 요구사항
  - UserAdminController 클래스의 changeUserRole() 메소드가 실행 전 동작해야 함

- 해결방법
   - @After 어노테이션 -> @Before 변경하였습니다.
   - UserController.getUser-> UserAdminController.changeUserRole  변경

<br>

## 4. 컨트롤러 테스트의 이해
- 요구사항
  - TodoControllerTest의 todo_단건_조회_시_todo가_존재하지_않아_예외가_발생한다() 테스트가 실패
  테스트가 정상적으로 수행되어 통과할 수 있도록 테스트 코드를 수정

- 해결방법
  - 에러코드 Expected: 200인데 Actual: 400이라 badRequest로 변경

<br>

## 5.  JPA의 이해
- 요구사항
  - 할 일 검색 시 `weather` 조건으로도 검색, `weather` 조건은 있을 수도 있고, 없을 수도 있음
  - 할 일 검색 시 수정일 기준으로 기간 검색이 가능, 기간의 시작과 끝 조건은 있을 수도 있고, 없을 수도 있음
   - JPQL을 사용

- 구현 
  - JPQL을 사용하여 weather 조건과 수정일 기준으로 기간 검색이 가능하도록 동적 쿼리를 구현
<br>

## 6.  JPA Cascade
- 요구사항
   - 할 일을 새로 저장할 시, 할 일을 생성한 유저는 담당자로 자동 등록
   - JPA의 cascade 기능을 활용해 할 일을 생성한 유저가 담당자로 등록될 수 있게

- 해결방법
   - cascade = CascadeType.PERSIST를 추가하여 담당자 자동 등록 될 수 있게 함

<br>

## 7.  N+1
- 요구사항
   - CommentController 클래스의 getComments() API를 호출할 때 N+1 문제가 발생, 해당 문제가 발생하지 않도록 코드를 수정

- 원인
   -  연관된 엔티티를 조회할 때 발생하는 쿼리 실행 방식 때문

- 해결방법
   - JOIN FETCH 사용 
   - 연관된 엔티티들을 즉시 로딩(Eager Loading) 하기 때문에, N+1 문제를 한 번의 쿼리로 해결

<br>

## 8.  QueryDSL
- 요구사항
   -  findByIdWithUser 를 QueryDSL로 변경
   - N+1 문제가 발생하지 않도록 유의

- 구현
   - QueryDSL을 사용하여 findByIdWithUser 메서드를 구현, JOIN FETCH를 활용하여 N+1 문제를 방지

<br>

## 9.  Spring Security
- 요구사항
   -  기존 Filter와 Argument Resolver를 사용하던 코드들을 Spring Security로 변경
   -  접근 권한 및 유저 권한 기능은 그대로 유지
    - 권한은 Spring Security의 기능을 사용
    - 토큰 기반 인증 방식은 유지, JWT는 그대로 사용

- 구현
   - 기존의 Filter는 JwtAuthenticationFilter, JwtAuthorizationFilter로 대체
   - Argument Resolver는 @AuthenticationPrincipal 어노테이션을 사용
   - 사용자 정보는 UserDetails 인터페이스를 구현한 UserDetailsImpl 클래스로
<br>

## 10.  QueryDSL 을 사용하여 검색 기능 만들기

- 요구사항
    - 검색 키워드로 일정의 제목을 검색, 부분적으로 일치해도 검색이 가능
    - 일정의 생성일 범위로 검색, 일정을 생성일 최신순으로 정렬
    - 담당자의 닉네임으로도 검색 가능, 닉네임은 부분적으로 일치해도 검색이 가능
    - 일정 제목, 해당 일정의 담당자 수,해당 일정의 총 댓글 개수를 포함해서 검색 결과를 반환
    - 검색 결과는 페이징 처리되어 반환

- 구현
  - BooleanBuilder를 사용하여 검색 조건을 구성, Projections를 사용하여 검색 결과를 반환
