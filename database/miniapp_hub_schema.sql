-- AnamWallet 미니앱 허브 데이터베이스 스키마
-- PostgreSQL 14+ 권장

-- 확장 기능 활성화
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 스키마 생성
CREATE SCHEMA IF NOT EXISTS miniapp_hub;
SET search_path TO miniapp_hub, public;

-- =====================================================
-- 1. 개발자/퍼블리셔 관리
-- =====================================================

-- 개발자 계정 테이블
CREATE TABLE developers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    did VARCHAR(255) UNIQUE NOT NULL, -- DID (Decentralized ID)
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    company_name VARCHAR(200),
    website VARCHAR(500),
    description TEXT,
    logo_url VARCHAR(500),
    is_verified BOOLEAN DEFAULT FALSE,
    verification_date TIMESTAMP,
    api_key VARCHAR(255) UNIQUE DEFAULT encode(gen_random_bytes(32), 'hex'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'active' CHECK (status IN ('active', 'suspended', 'banned'))
);

-- =====================================================
-- 2. 미니앱 메타데이터
-- =====================================================

-- 미니앱 카테고리
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    name_ko VARCHAR(50) NOT NULL,
    icon VARCHAR(100),
    description TEXT,
    display_order INTEGER DEFAULT 0
);

-- 기본 카테고리 삽입
INSERT INTO categories (name, name_ko, icon, display_order) VALUES
('government', '정부/공공', 'account_balance', 1),
('finance', '금융', 'account_balance_wallet', 2),
('shopping', '쇼핑', 'shopping_cart', 3),
('lifestyle', '라이프스타일', 'style', 4),
('entertainment', '엔터테인먼트', 'movie', 5),
('education', '교육', 'school', 6),
('health', '건강', 'favorite', 7),
('utility', '유틸리티', 'build', 8);

-- 미니앱 기본 정보
CREATE TABLE app_modules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    app_id VARCHAR(255) UNIQUE NOT NULL, -- 예: kr.go.government24
    developer_id UUID NOT NULL REFERENCES developers(id),
    category_id INTEGER REFERENCES categories(id),
    
    -- 기본 정보
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    short_description VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    icon_url VARCHAR(500) NOT NULL,
    primary_color VARCHAR(7) DEFAULT '#000000', -- HEX color
    
    -- 버전 관리
    current_version VARCHAR(20) NOT NULL, -- 예: 1.0.0
    min_sdk_version VARCHAR(20) DEFAULT '1.0.0',
    
    -- 상태 관리
    status VARCHAR(50) DEFAULT 'draft' CHECK (status IN ('draft', 'review', 'approved', 'published', 'suspended', 'deprecated')),
    is_featured BOOLEAN DEFAULT FALSE,
    featured_order INTEGER,
    
    -- 통계
    download_count BIGINT DEFAULT 0,
    rating_average DECIMAL(2,1) DEFAULT 0.0,
    rating_count INTEGER DEFAULT 0,
    
    -- 타임스탬프
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,
    
    -- 검색 최적화
    search_vector tsvector
);

-- 전체 텍스트 검색을 위한 인덱스
CREATE INDEX idx_app_modules_search ON app_modules USING GIN(search_vector);

-- 검색 벡터 자동 업데이트 트리거
CREATE OR REPLACE FUNCTION update_search_vector() RETURNS trigger AS $$
BEGIN
    NEW.search_vector := 
        setweight(to_tsvector('simple', NEW.name), 'A') ||
        setweight(to_tsvector('simple', COALESCE(NEW.name_en, '')), 'A') ||
        setweight(to_tsvector('simple', NEW.short_description), 'B') ||
        setweight(to_tsvector('simple', NEW.description), 'C');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_app_modules_search_vector
    BEFORE INSERT OR UPDATE ON app_modules
    FOR EACH ROW EXECUTE FUNCTION update_search_vector();

-- =====================================================
-- 3. 버전 관리
-- =====================================================

-- 미니앱 버전 히스토리
CREATE TABLE app_versions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    app_module_id UUID NOT NULL REFERENCES app_modules(id) ON DELETE CASCADE,
    version VARCHAR(20) NOT NULL, -- 예: 1.0.0
    release_notes TEXT,
    
    -- 파일 정보
    package_url VARCHAR(500) NOT NULL, -- ZIP 파일 URL
    package_size BIGINT NOT NULL, -- bytes
    package_hash VARCHAR(64) NOT NULL, -- SHA-256
    signature VARCHAR(500), -- 디지털 서명
    
    -- 매니페스트 정보 (캐싱)
    manifest JSONB NOT NULL,
    
    -- 상태
    status VARCHAR(50) DEFAULT 'uploaded' CHECK (status IN ('uploaded', 'scanning', 'approved', 'rejected', 'published')),
    rejection_reason TEXT,
    
    -- 타임스탬프
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,
    
    UNIQUE(app_module_id, version)
);

-- =====================================================
-- 4. 권한 및 보안
-- =====================================================

-- 미니앱이 요청할 수 있는 권한 목록
CREATE TABLE app_permissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    app_module_id UUID NOT NULL REFERENCES app_modules(id) ON DELETE CASCADE,
    permission_type VARCHAR(100) NOT NULL, -- 예: vp_request, storage, camera
    
    -- VP 관련 권한일 경우
    vp_type VARCHAR(100), -- 예: authentication, resident_registration
    vp_issuer VARCHAR(255), -- 허용된 발급자
    
    -- JavaScript Bridge API 권한
    bridge_methods TEXT[], -- 허용된 메서드 목록 예: ['showToast', 'getSystemInfo']
    
    reason TEXT NOT NULL, -- 권한 필요 이유
    is_optional BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 5. 미니앱 리소스
-- =====================================================

-- 스크린샷
CREATE TABLE app_screenshots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    app_module_id UUID NOT NULL REFERENCES app_modules(id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    caption VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 태그 (검색 및 분류용)
CREATE TABLE tags (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE app_tags (
    app_module_id UUID REFERENCES app_modules(id) ON DELETE CASCADE,
    tag_id INTEGER REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (app_module_id, tag_id)
);

-- =====================================================
-- 6. 사용자 상호작용
-- =====================================================

-- 설치 기록
CREATE TABLE installations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_did VARCHAR(255) NOT NULL,
    app_module_id UUID NOT NULL REFERENCES app_modules(id),
    version VARCHAR(20) NOT NULL,
    
    installed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uninstalled_at TIMESTAMP,
    
    -- 사용 통계
    launch_count INTEGER DEFAULT 0,
    total_usage_time INTEGER DEFAULT 0, -- seconds
    
    UNIQUE(user_did, app_module_id)
);

-- 리뷰 및 평점
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    app_module_id UUID NOT NULL REFERENCES app_modules(id),
    user_did VARCHAR(255) NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(100),
    content TEXT,
    
    -- 개발자 답변
    developer_response TEXT,
    developer_response_at TIMESTAMP,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(app_module_id, user_did)
);

-- =====================================================
-- 7. 정부24 POC 미니앱
-- =====================================================

-- 정부24 미니앱 초기 데이터
INSERT INTO developers (did, name, email, company_name, is_verified) VALUES
('did:anam:gov:korea', '대한민국 정부', 'admin@korea.kr', '행정안전부', TRUE);

INSERT INTO app_modules (
    app_id, 
    developer_id, 
    category_id,
    name, 
    short_description, 
    description,
    icon_url,
    primary_color,
    current_version,
    status
) VALUES (
    'kr.go.government24',
    (SELECT id FROM developers WHERE did = 'did:anam:gov:korea'),
    (SELECT id FROM categories WHERE name = 'government'),
    '정부24',
    '대한민국 정부 민원 서비스',
    '주민등록등본, 가족관계증명서 등 각종 민원 서류를 간편하게 발급받을 수 있습니다. DID 기반 인증으로 안전하고 빠른 서비스를 제공합니다.',
    'https://hub.anamwallet.com/icons/government24.png',
    '#1976D2',
    '1.0.0',
    'published'
);

-- =====================================================
-- 8. 인덱스 및 성능 최적화
-- =====================================================

CREATE INDEX idx_app_modules_status ON app_modules(status);
CREATE INDEX idx_app_modules_category ON app_modules(category_id);
CREATE INDEX idx_app_modules_developer ON app_modules(developer_id);
CREATE INDEX idx_app_modules_featured ON app_modules(is_featured, featured_order);
CREATE INDEX idx_installations_user ON installations(user_did);
CREATE INDEX idx_app_versions_module ON app_versions(app_module_id);

-- =====================================================
-- 9. 뷰 (자주 사용하는 쿼리)
-- =====================================================

-- 공개된 미니앱 목록
CREATE VIEW published_apps AS
SELECT 
    am.*,
    d.name as developer_name,
    d.is_verified as developer_verified,
    c.name as category_name,
    c.name_ko as category_name_ko
FROM app_modules am
JOIN developers d ON am.developer_id = d.id
JOIN categories c ON am.category_id = c.id
WHERE am.status = 'published'
AND d.status = 'active';

-- 인기 미니앱 (다운로드 + 평점 기준)
CREATE VIEW popular_apps AS
SELECT 
    am.*,
    (am.download_count * 0.3 + am.rating_average * am.rating_count * 0.7) as popularity_score
FROM app_modules am
WHERE am.status = 'published'
ORDER BY popularity_score DESC;

-- =====================================================
-- 10. 함수 및 프로시저
-- =====================================================

-- 미니앱 설치 함수
CREATE OR REPLACE FUNCTION install_miniapp(
    p_user_did VARCHAR(255),
    p_app_id VARCHAR(255),
    p_version VARCHAR(20)
) RETURNS BOOLEAN AS $$
DECLARE
    v_app_module_id UUID;
BEGIN
    -- 앱 ID로 모듈 찾기
    SELECT id INTO v_app_module_id 
    FROM app_modules 
    WHERE app_id = p_app_id;
    
    -- 설치 기록 추가 또는 업데이트
    INSERT INTO installations (user_did, app_module_id, version)
    VALUES (p_user_did, v_app_module_id, p_version)
    ON CONFLICT (user_did, app_module_id) 
    DO UPDATE SET 
        version = p_version,
        installed_at = CURRENT_TIMESTAMP,
        uninstalled_at = NULL;
    
    -- 다운로드 수 증가
    UPDATE app_modules 
    SET download_count = download_count + 1
    WHERE id = v_app_module_id;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 11. 트리거
-- =====================================================

-- updated_at 자동 업데이트
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_app_modules_updated_at
    BEFORE UPDATE ON app_modules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER update_reviews_updated_at
    BEFORE UPDATE ON reviews
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();