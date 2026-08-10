# AI Assistant 前端開發規範 (Angular 18 + PrimeNG)

本指南旨在為 `web-ui` 專案提供統一的開發標準，確保程式碼的可讀性、可維護性，並充分發揮 Angular 18 與 PrimeNG 框架的優勢。

## 1. 核心技術選型
*   **核心框架**：Angular 18 (全面採用 Standalone Components)
*   **UI 組件庫**：PrimeNG
*   **CSS 排版與工具**：PrimeFlex (CSS Utility classes)
*   **樣式預處理器**：SCSS
*   **狀態管理**：Angular Signals (處理同步狀態) 搭配 RxJS (處理非同步流)

## 2. 目錄結構與架構
為了因應未來功能的擴展，我們採用基於特性 (Feature-based) 的目錄劃分，並嚴格區分共用模組與核心服務：

```text
src/app/
├── core/               # 單例服務、攔截器 (Interceptors)、Auth Guard 等只能被實例化一次的物件
├── shared/             # 跨功能共用的 Dumb UI 元件、Pipes、Directives、PrimeNG 模組匯出
├── features/           # 依據業務功能劃分的模組 (例如: chat, knowledge-base, settings)
│   └── chat/           # 聊天室功能模組
│       ├── components/ # 該功能專屬的展示型元件
│       ├── pages/      # 路由綁定的 Smart 頁面元件
│       ├── services/   # 功能專屬的狀態與 API 服務
│       └── chat.routes.ts # 功能內部的子路由
├── layout/             # 系統整體佈局元件 (Sidebar, Header, Footer)
└── app.routes.ts       # 系統主路由設定
```

## 3. 元件設計模式 (Smart vs Dumb Components)
為了讓元件更容易測試與重複使用，嚴格遵循容器與展示元件的分離原則：

*   **Smart Components (頁面級容器)**：
    *   放置於 `pages/` 目錄。
    *   負責注入 Services、發送 API 請求、管理狀態。
    *   **不**負責排版與複雜的 UI 邏輯。
    *   透過 `[input]` 傳遞資料給 Dumb 元件，透過 `(output)` 監聽使用者行為。
*   **Dumb Components (展示型元件)**：
    *   放置於 `components/` 或 `shared/` 目錄。
    *   完全依賴 `@Input()` (或 Angular 18 的 `input()`) 接收資料。
    *   透過 `@Output()` 向外發射事件。
    *   內部不可注入任何與 API 或全域狀態相關的 Service，保持純粹的視覺呈現職責。

## 4. 樣式與 PrimeFlex 使用規範
為了維持一致的視覺體驗與高級科技感 (Dark Mode)：

1.  **優先使用 Utility Classes**：
    *   排版 (Flexbox, Grid)、間距 (Margin/Padding)、顏色、字體大小等，**必須優先使用 PrimeFlex 提供的 class** (如 `flex`, `justify-content-center`, `p-3`, `text-color`)。
2.  **避免濫用 Inline Style**：
    *   除了動態計算的寬度或高度，嚴禁在 HTML 寫死 `style="..."`。
3.  **SCSS 的適當時機**：
    *   只有當 PrimeFlex 無法滿足需求（例如：複雜的動畫效果 @keyframes、客製化的玻璃透視效果 Glassmorphism、或是要覆寫 PrimeNG 內部的深度樣式 `::ng-deep` 時），才寫進 `.scss` 檔案。
4.  **色彩計畫**：
    *   遵守 PrimeNG 的 Semantic Colors (例如 `var(--primary-color)`, `var(--surface-a)`)，這樣在切換亮暗主題時才不會破版。

## 5. 狀態管理 (Signals + RxJS)
Angular 18 大幅提升了 Signals 的重要性，請遵循以下模式：

*   **同步 UI 狀態**：使用 `signal()`、`computed()` 來管理 UI 的開關、目前選取的項目等。
*   **元件輸入**：全面改用 Angular 18 新的 Signal-based Inputs (`input()`) 來取代傳統的 `@Input()`。
*   **非同步資料流 (HTTP/WebSocket)**：保留 **RxJS** 來處理 API 請求、防抖 (Debounce) 以及取消請求等複雜操作，並透過 `toSignal()` 將 RxJS 的資料流轉換為 Signal 供 Template 訂閱，捨棄傳統的 `async` pipe。

## 6. 命名慣例 (Naming Conventions)
*   **檔案命名**：kebab-case (例如 `chat-window.component.ts`)。
*   **Class 命名**：PascalCase (例如 `ChatWindowComponent`)。
*   **Service 命名邏輯**：
    *   **結尾一律為 `Service`** (例如 `StorageService`)，不可使用 `Manager`、`Handler` 等模糊後綴。
    *   **UI / 狀態型**：以功能目的命名，例如 `LoadingService`、`ThemeService`。
    *   **資料 / API 型**：以業務領域實體加上 `Api`，或單純實體名稱命名，例如 `ChatApiService`、`UserProfileService`。
    *   **工具型**：以工具技術命名，例如 `StorageService`、`CryptoService`。
*   **變數與方法**：camelCase。
*   **Signal 變數**：不強制加綴，但必須清楚表達狀態，例如 `messages`。
*   **Observable 變數**：結尾加上 `$` 字號以利識別，例如 `messageStream$`。
