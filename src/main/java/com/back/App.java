package com.back;

import com.back.domain.system.controller.SystemController;
import com.back.domain.wiseSaying.controller.WiseSayingController;
import com.back.global.rq.Rq;

import java.util.Scanner;

public class App {
    private final Scanner scanner;

    public App() {
        this.scanner = AppContext.scanner;
    }

    public void run() {
        System.out.println("== 명언 앱 ==");

        WiseSayingController wiseSayingController = AppContext.wiseSayingController;
        SystemController systemController = AppContext.systemController;

        while (true) {
            System.out.println("명령) ");
            String cmd = scanner.nextLine();

            Rq rq = new Rq(cmd);

            if (rq.getActionName().equals("종료")) {
                systemController.actionExit();
                return;
            }

            // 명령 하나가 실패해도(예: DB 오류) 세션은 끝내지 않고, 오류 한 줄만 보여 준 뒤 다음 명령을 받는다.
            try {
                switch (rq.getActionName()) {
                    case "등록" -> wiseSayingController.actionWrite();
                    case "목록" -> wiseSayingController.actionList(rq);
                    case "삭제" -> wiseSayingController.actionDelete(rq);
                    case "수정" -> wiseSayingController.actionModify(rq);
                    case "빌드" -> wiseSayingController.actionBuild();
                }
            } catch (RuntimeException e) {
                System.out.println("오류: " + describe(e));
            }
        }
    }

    // 가장 안쪽 원인의 메시지를 한 줄로 만든다.
    private static String describe(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null) root = root.getCause();

        String message = root.getMessage();
        if (message == null || message.isBlank()) message = root.getClass().getSimpleName();

        return message.lines().findFirst().orElse(message);
    }
}
