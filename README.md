# 볼링 게임 점수판
## 진행 방법
* 볼링 게임 점수판 요구사항을 파악한다.
* 요구사항에 대한 구현을 완료한 후 자신의 github 아이디에 해당하는 브랜치에 Pull Request(이하 PR)를 통해 코드 리뷰 요청을 한다.
* 코드 리뷰 피드백에 대한 개선 작업을 하고 다시 PUSH한다.
* 모든 피드백을 완료하면 다음 단계를 도전하고 앞의 과정을 반복한다.

## 온라인 코드 리뷰 과정
* [텍스트와 이미지로 살펴보는 온라인 코드 리뷰 과정](https://github.com/next-step/nextstep-docs/tree/master/codereview)

# 1단계 - 질문 삭제하기 기능 리팩토링
## 질문 삭제하기 요구사항 분리
### 삭제 방법
- 질문 데이터를 완전히 삭제하는 것이 아닌 데이터의 상태를 삭테 상태(deleted - boolean type)로 변경한다.
- 질문 삭제 시 답변도 삭제해야 하며, 답변의 삭제 상태도 변경해야 한다.
### 삭제 가능 케이스  
- 로그인 사용자와 질문한 사람이 같은 경우 삭제가 가능하다.
- 답변이 없는 경우 삭제가 가능하다.
- 질문자와 답변 글의 모든 답변자가 같은 경우 삭제 가능하다.
### 삭제 불가 케이스
- 질문자와 답변자가 다르면 다르면 삭제 불가.
- 로그인 사용자와 질문자가 다르다.
### 히스토리  
- 질문과 답변의 삭제 이력을 DeleteHistory에 남긴다.

## 프로그래밍 요구사항
- qna.service.QnaService의 deleteQuestion()은 앞의 질문 삭제 기능을 구현한 코드이다.  
  이 메서드는 단위 테스트하기 어려운 코드와 단위 테스트 가능한 코드가 섞여 있따.
- 단위 테스트하기 어려운 코드와 단위 테스트 가능한 코드를 분리해 단위 테스트 가능한 코드에 대해 단위 테스트를 구현한다.  
```java
public class QnAService {
    public void deleteQuestion(User loginUser, long questionId) throws CannotDeleteException {
        Question question = findQuestionById(questionId);
        if (!question.isOwner(loginUser)) {
            throw new CannotDeleteException("질문을 삭제할 권한이 없습니다.");
        }

        List<Answer> answers = question.getAnswers();
        for (Answer answer : answers) {
            if (!answer.isOwner(loginUser)) {
                throw new CannotDeleteException("다른 사람이 쓴 답변이 있어 삭제할 수 없습니다.");
            }
        }

        List<DeleteHistory> deleteHistories = new ArrayList<>();
        question.setDeleted(true);
        deleteHistories.add(new DeleteHistory(ContentType.QUESTION, questionId, question.getWriter(), LocalDateTime.now()));
        for (Answer answer : answers) {
            answer.setDeleted(true);
            deleteHistories.add(new DeleteHistory(ContentType.ANSWER, answer.getId(), answer.getWriter(), LocalDateTime.now()));
        }
        deleteHistoryService.saveAll(deleteHistories);
    }
}
```
### 힌트1
- 객체의 상태 데이터를 꺼내지(get) 말고 메시지를 보낸다.
- 규칙 8 : 일급 콜렉션을 쓴다.
  - Question의 List를 일급 콜렉션으로 구현한다.
- 규칙 7 : 3개 이상의 인스턴스 변수를 가진 클래스를 쓰지 않는다.

### 힌트 2
- 테스트하기 쉬운 부분과 테스트하기 어려운 부분을 분리해 테스트 가능한 부분만 단위 테스트를 한다.

### ⚒ 리팩토링 할 목록
- Question 로직 도메인으로 위임  
- List<Answer> 로직 도메인으로 위임  
  → 일급 컬렉션 처리  
- List<DeleteHistory> 로직 도메인으로 위임  
  → 일급 컬렉션 처리  

위와 같이 로직을 도메인 클래스로 위임하면  
코드는 테스트할 수 있는 코드가 되고, 내부 로직에 의존하지 않고 변경에 유연한 코드가 된다.  
다른 곳에서 동일한 로직을 사용하더라도 잘못 사용할 위험도 줄어들게 된다.  

# 2단계 - 볼링 점수판(그리기)

---
# 📌 요구 사항

- 최종 목표는 볼링 점수를 계산하는 프로그램을 구현한다.
  **1단계 목표는 점수 계산을 제외한 볼링 게임 점수판을 구현하는 것이다.**
- 각 프레임이 스트라이크이면 “X”, 스페어이면 “9 | /”, 미스이면 “8 | 1” 과 같이 출력한다.
- 스트라이크 : 프레임의 첫번째 투구에서 모든 핀(10개)를 쓰러트린 상태
- 스페어 : 프레임의 두번째 투구에서 모든 핀(10개)를 쓰러트린 상태
- 미스 : 프레임의 두번째 투구에서도 모든 핀이 쓰러지지 않은 상태
- 거터 : 핀을 하나도 쓰러트리지 못한 상태. 거터는 “-”로 표시
- 10 프레임은 스트라이크이거나 스페어이면 더 투구할 수 있다.

## 실행 결과

```java
플레이어 이름은(3 english letters)?: PJS
| NAME |  01  |  02  |  03  |  04  |  05  |  06  |  07  |  08  |  09  |  10  |
|  PJS |      |      |      |      |      |      |      |      |      |      |

1프레임 투구 : 10
| NAME |  01  |  02  |  03  |  04  |  05  |  06  |  07  |  08  |  09  |  10  |
|  PJS |  X   |      |      |      |      |      |      |      |      |      |

2프레임 투구 : 8
| NAME |  01  |  02  |  03  |  04  |  05  |  06  |  07  |  08  |  09  |  10  |
|  PJS |  X   |  8   |      |      |      |      |      |      |      |      |

2프레임 투구 : 2
| NAME |  01  |  02  |  03  |  04  |  05  |  06  |  07  |  08  |  09  |  10  |
|  PJS |  X   |  8|/ |      |      |      |      |      |      |      |      |

3프레임 투구 :  7
| NAME |  01  |  02  |  03  |  04  |  05  |  06  |  07  |  08  |  09  |  10  |
|  PJS |  X   |  8|/ |  7   |      |      |      |      |      |      |      |

3프레임 투구 :  : 0
| NAME |  01  |  02  |  03  |  04  |  05  |  06  |  07  |  08  |  09  |  10  |
|  PJS |  X   |  8|/ |  7|- |      |      |      |      |      |      |      |

...
```

## 프로그래밍 요구 사항

### 객체지향 생활 체조 원칙

- 규칙 1: 한 메서드에 오직 한 단계의 들여쓰기만 한다.
- 규칙 2: else 예약어를 쓰지 않는다.
- 규칙 3: 모든 원시값과 문자열을 포장한다.
- 규칙 4: 한 줄에 점을 하나만 찍는다.
- 규칙 5: 줄여쓰지 않는다(축약 금지).
- 규칙 6: 모든 엔티티를 작게 유지한다.
- 규칙 7: 3개 이상의 인스턴스 변수를 가진 클래스를 쓰지 않는다.
- 규칙 8: 일급 콜렉션을 쓴다.
- 규칙 9: 게터/세터/프로퍼티를 쓰지 않는다.

## 힌트

- 객체 단위를 가장 작은 단위까지 극단적으로 분리하는 시도를 해본다.
- 1 ~ 9 프레임을 NormalFrame, 10 프레임을 FinalFrame과 같은 구조로 구현한 후 Frame을 추가해
  중복을 제거 한다.
- 다음 Frame을 현재 Frame 외부에서 생성하기 보다 현재 Frame에서 다음 Frame을 생성하는 방식으로
  구현해 보고, 어느 구현이 더 좋은지 검토해 본다.

## 요구 사항 분리 하기
### Player 클래스
- Player 한명 생성
- 이름은 1 ~ 3글자
- 영문만
- 대문자로 변환

### Pins 클래스
- 볼링 핀 한개 생성
- 쓰러트린 핀의 개수는 0이상 10이하이다.
- 스트라이크, 스페어, 미스, 거터 여부 확인

### ThrowingState 인터페이스
- `RunningState` : 한 프레임이 진행 중인 볼링 투구 상태
  - `Ready` : 한 프레임에서 투구를 던지기 전 준비 상태
  - `FirstBowl` : 한 프레임에서 첫 번재 투구를 던지는 상태
- `EndedState` : 한 프레임이 끝난 볼링 투구 상태
  - `Miss` : 한 프레임에서 더 이상 핀을 맞추지 못한 상태
  - `Spare` : 한 프레임에서 두 번째 투구에서 모든 핀을 맞춘 상태
  - `Strike` : 한 프레임에서 첫 번째 투구로 모든 핀을 맞춘 상태

### FrameIndex
- 진행 중인 Frame의 위치
- 1 ~ 10까지 가능하다.

### 각 프레임(Frame)
- `NormalFrame` : 1 ~ 9 프레임에서 최대 2개의 투구를 할 수 있는 프레임
  - 1 ~ 9 라운드의 현재 프레임 위치와 투구 상태 확인
  - 9라인드에서는 다음 라운드는 LastFrame(10프레임)을 생성한다.
- `LastFrame` : 10(마지막) 프레임
  - BasicFrame과 동일하고, 추가 사항은 추가 투구 1회가 있다.
  - 추가 투구는 앞 투구 2회에서 Spare or Strike가 있어야 한다.
  
### Frames
- 볼링에 필요한 10프레임을 구성하기 위한 일급 컬렉션

### BowlingGame
- `Player`와 `Frames`를 이용한 볼링 게임 구현

### BowlingController
- View와 Model을 분리하기 위한 컨트롤러

### InputView
- `Plyaer` 이름 입력
- 쓰러트린 핀 개수 입력

### OutputView
- 볼링 점수판 출력

## 피드백
### 정적 팩토리 메서드 사용 이유
- 상태 객체들의 정적 팩토리 메서드에서는 한 프레임의 첫번째 투구를 생성한다는 의도 전달을 위해 사용을 했다.  
  하지만 BowlingGame 클래스와 같이 create() 정적 팩토리 메서드는 아무 생각없이 사용한 것이다.  
  의도를 전달하지도 않고 아무런 의미가 없는 메서드이다. 무엇을 create 하는지도 알 수도 없고.. 생성자를 생성하는 것이라면  
  그냥 생성자를 사용하면 된다.