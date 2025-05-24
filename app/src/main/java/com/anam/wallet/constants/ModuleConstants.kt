package com.anam.wallet.constants

/**
 * 프론트 모듈 관련 상수
 */
object ModuleConstants {
    
    /**
     * 프론트 모듈 구현 클래스명
     * 모든 프론트 모듈은 이 클래스명을 사용해야 함
     */
    const val FRONT_MODULE_IMPLEMENTATION_CLASS = "com.anam.wallet.apk.FrontModuleImpl"
    
    /**
     * 프론트 모듈 패키지명
     */
    const val FRONT_MODULE_PACKAGE = "com.anam.wallet.apk"
    
    /**
     * 프론트 모듈 인터페이스명
     */
    const val FRONT_MODULE_INTERFACE = "com.anam.wallet.core.IFrontModuleUI"
}