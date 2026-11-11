package com.back;

import com.back.domain.system.controller.SystemController;
import com.back.domain.wiseSaying.controller.WiseSayingController;
import com.back.domain.wiseSaying.repository.WiseSayingDbRepository;
import com.back.domain.wiseSaying.repository.WiseSayingRepository;
import com.back.domain.wiseSaying.service.WiseSayingService;

import java.util.Scanner;

public class AppContext {
    public static Scanner scanner;
    public static SystemController systemController;
    public static WiseSayingRepository wiseSayingRepository;
    public static WiseSayingService wiseSayingService;
    public static WiseSayingController wiseSayingController;

    public static void renew(Scanner _scanner) {
        scanner = _scanner;
        systemController = new SystemController();
        // 리포지터리 구현체만 갈아 끼우면 컨트롤러·서비스는 그대로 동작한다.
        // 파일 저장소로 되돌리려면 new WiseSayingFileRepository() 로 바꾸면 된다.
        wiseSayingRepository = new WiseSayingDbRepository();
        wiseSayingService = new WiseSayingService();
        wiseSayingController = new WiseSayingController();
    }

    // Scanner를 새로 생성하여 AppContext를 초기화하는 메서드
    // 필요한 의존성 세팅
    public static void renew() {
        renew(new Scanner(System.in));
    }

}
