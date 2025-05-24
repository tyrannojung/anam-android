package com.anam.wallet;

import com.anam.wallet.IMainAppService;
import android.view.Surface;

/**
 * 프론트 모듈 서비스 인터페이스
 * 메인앱에서 프론트 모듈 프로세스를 제어하기 위한 인터페이스
 */
interface IFrontModuleService {
    
    /**
     * 프론트 모듈 로드
     * @param apkPath APK 파일 경로
     * @param className 모듈 클래스명
     * @param moduleId 모듈 ID
     */
    void loadModule(String apkPath, String className, String moduleId);
    
    /**
     * 프론트 모듈 정지
     */
    void stopModule();
    
    /**
     * 메인앱 서비스 설정
     * @param service 메인앱 서비스
     */
    void setMainAppService(in IMainAppService service);
    
    /**
     * 프론트 모듈 UI Surface 생성 및 반환
     * @param width Surface 너비
     * @param height Surface 높이
     * @return Surface 객체
     */
    Surface createModuleSurface(int width, int height);
}