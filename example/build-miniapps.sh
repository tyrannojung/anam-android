#!/bin/bash

# MiniApp 빌드 스크립트
# example 폴더의 미니앱들을 ZIP으로 압축하고 assets 폴더로 복사

echo "🔨 Building MiniApps..."

# 프로젝트 루트 경로
PROJECT_ROOT="/Users/dawoon/Projects/anam/Refactoring/anam-android"
EXAMPLE_DIR="$PROJECT_ROOT/example"
ASSETS_DIR="$PROJECT_ROOT/app/src/main/assets"

# 1. Apps 빌드
echo "📦 Building Apps..."
cd "$EXAMPLE_DIR/apps"
for app in */; do
    if [ -d "$app" ]; then
        app_name="${app%/}"
        echo "  - Building $app_name..."
        
        # test.html과 mock-anam.js는 제외하고 압축
        cd "$app_name"
        zip -r "../${app_name}_v1.0.0.zip" . -x "test.html" -x "mock-anam.js" -x "*.DS_Store"
        cd ..
        
        # assets 폴더로 복사
        cp "${app_name}_v1.0.0.zip" "$ASSETS_DIR/apps/"
        
        # 임시 ZIP 파일 삭제
        rm "${app_name}_v1.0.0.zip"
    fi
done

# 2. Blockchains 빌드
echo "⛓️  Building Blockchains..."
cd "$EXAMPLE_DIR/blockchains"
for blockchain in */; do
    if [ -d "$blockchain" ]; then
        blockchain_name="${blockchain%/}"
        echo "  - Building $blockchain_name..."
        
        # test.html과 mock-anam.js는 제외하고 압축
        cd "$blockchain_name"
        zip -r "../${blockchain_name}.zip" . -x "test.html" -x "mock-anam.js" -x "*.DS_Store"
        cd ..
        
        # assets 폴더로 복사
        cp "${blockchain_name}.zip" "$ASSETS_DIR/blockchains/"
        
        # 임시 ZIP 파일 삭제
        rm "${blockchain_name}.zip"
    fi
done

echo "✅ MiniApps build completed!"
echo ""
echo "📁 Built files:"
echo "  - Apps: $ASSETS_DIR/apps/"
echo "  - Blockchains: $ASSETS_DIR/blockchains/"
echo ""
echo "🧪 For local testing, open:"
echo "  - $EXAMPLE_DIR/apps/government24/test.html"
echo "  - $EXAMPLE_DIR/blockchains/ethereum/test.html"