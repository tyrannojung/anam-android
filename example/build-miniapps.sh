#!/bin/bash

# MiniApp 빌드 스크립트
# example 폴더의 미니앱들을 ZIP으로 압축하고 assets 폴더로 복사

echo "🔨 Building MiniApps..."

# 스크립트 위치 기준으로 프로젝트 루트 찾기
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
EXAMPLE_DIR="$PROJECT_ROOT/example"
ASSETS_DIR="$PROJECT_ROOT/app/src/main/assets/miniapps"

# assets/miniapps 폴더가 없으면 생성
mkdir -p "$ASSETS_DIR"

# 기존 ZIP 파일들 삭제
echo "🗑️  Cleaning old builds..."
rm -f "$ASSETS_DIR"/*.zip

# 1. Apps 빌드
echo "📦 Building Apps..."
cd "$EXAMPLE_DIR/apps"
for app in */; do
    if [ -d "$app" ]; then
        app_name="${app%/}"
        echo "  - Building $app_name..."
        
        # manifest.json에서 버전 읽기 (있으면)
        version="1.0.0"
        if [ -f "$app_name/manifest.json" ]; then
            # jq가 있으면 사용, 없으면 기본값
            if command -v jq &> /dev/null; then
                version=$(jq -r '.version // "1.0.0"' "$app_name/manifest.json")
            fi
        fi
        
        # test.html과 mock 파일들은 제외하고 압축
        cd "$app_name"
        zip -r "../${app_name}_${version}.zip" . \
            -x "test.html" \
            -x "mock-*.js" \
            -x "*.DS_Store" \
            -x ".git/*" \
            -x "node_modules/*"
        cd ..
        
        # manifest.json에서 app_id 읽기 (app_id 또는 appId 사용)
        app_id="$app_name"
        if [ -f "$app_name/manifest.json" ]; then
            if command -v jq &> /dev/null; then
                app_id=$(jq -r '.app_id // .appId // "'$app_name'"' "$app_name/manifest.json")
            fi
        fi
        
        # assets 폴더로 복사 (appId 사용)
        cp "${app_name}_${version}.zip" "$ASSETS_DIR/${app_id}_${version}.zip"
        
        # 임시 ZIP 파일 삭제
        rm "${app_name}_${version}.zip"
    fi
done

# 2. Blockchains 빌드
echo "⛓️  Building Blockchains..."
cd "$EXAMPLE_DIR/blockchains"
for blockchain in */; do
    if [ -d "$blockchain" ]; then
        blockchain_name="${blockchain%/}"
        echo "  - Building $blockchain_name..."
        
        # manifest.json에서 버전 읽기
        version="1.0.0"
        if [ -f "$blockchain_name/manifest.json" ]; then
            if command -v jq &> /dev/null; then
                version=$(jq -r '.version // "1.0.0"' "$blockchain_name/manifest.json")
            fi
        fi
        
        # 앱 ID 형식으로 변환 (ethereum → com.anam.ethereum)
        app_id="com.anam.${blockchain_name}"
        
        # test.html과 mock 파일들은 제외하고 압축
        cd "$blockchain_name"
        zip -r "../${app_id}_${version}.zip" . \
            -x "test.html" \
            -x "mock-*.js" \
            -x "*.DS_Store" \
            -x ".git/*" \
            -x "node_modules/*"
        cd ..
        
        # assets 폴더로 복사
        cp "${app_id}_${version}.zip" "$ASSETS_DIR/"
        
        # 임시 ZIP 파일 삭제
        rm "${app_id}_${version}.zip"
    fi
done

echo "✅ MiniApps build completed!"
echo ""
echo "📁 Built files in: $ASSETS_DIR"
ls -la "$ASSETS_DIR"/*.zip 2>/dev/null | awk '{print "  - " $9}'
echo ""
echo "💡 To update the app, run:"
echo "  ./gradlew assembleDebug"