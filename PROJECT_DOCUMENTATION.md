# VisualMoney: Project Documentation & Technical Proposal

## 1. Project Overview
VisualMoney is a professional-grade portfolio management and wealth tracking application built with **Kotlin Multiplatform (KMP)**. It provides investors with a unified dashboard to track Stocks, Cryptocurrencies, ETFs, and Commodities across global markets. The application aims to replace fragmented spreadsheets with high-fidelity visualizations, real-time data, and pro-grade risk analytics.

---

## 2. Feature & Functionality

### 2.1 Core Features (Free)
*   **Unified Asset Tracking**: Add and manage a diverse range of assets (Equities, Crypto, Precious Metals, etc.) with real-time price updates.
*   **Intuitive Home Dashboard**: Instant view of total balance, 1-day gains, and a list of top market movers.
*   **Interactive Charts**: Professional sparklines and historical charts (1D, 1W, 1M, 6M, 1Y, 5Y) for every asset.
*   **Market News**: Real-time news feed tailored to the user's portfolio holdings with direct redirection to original sources.
*   **Global Search**: Integrated search powered by the Financial Modeling Prep (FMP) API to quickly find and add ticker symbols.

### 2.2 Premium Features
*   **Portfolio Pulse (Risk Analysis)**:
    *   **Risk Score**: A proprietary calculation of portfolio volatility.
    *   **Sharpe Ratio**: Evaluation of risk-adjusted returns compared to a risk-free benchmark.
    *   **Diversification Metrics**: Real-time calculation of exposure by Sector, Geography, and Asset Class using donut chart visualizations.
*   **Dividend Intelligence**:
    *   **Projected Annual Income**: Automated estimation of yearly dividend payouts.
    *   **Dividend Calendar**: A dedicated interface to track upcoming payment dates, amounts, and ex-dividend dates.
*   **Strategic Insights**:
    *   **Concentration Alerts**: Automated warnings for over-exposure to specific assets or sectors.
    *   **Rebalancing Suggestions**: AI-driven tips to optimize portfolio health based on user-defined risk profiles.

---

## 3. Technical Architecture

### 3.1 Technology Stack
*   **Language**: 100% Kotlin (Common, Android, iOS).
*   **Framework**: Compose Multiplatform (sharing UI code across platforms).
*   **Dependency Injection**: **Koin** for modern, lightweight, and thread-safe DI.
*   **Local Persistence**: **Room (KMP)** for structured relational data (Portfolio, Reminders, Cached Quotes).
*   **Networking**: **Ktor Client** for asynchronous, multiplatform network requests.
*   **Monetization**: **RevenueCat (KMP)** for seamless cross-platform subscription management.
*   **Configuration**: **BuildKonfig** for secure, variant-aware API key management (FMP, Logo.dev, RevenueCat).

### 3.2 Data Architecture
VisualMoney follows the **Clean Architecture** and **MVVM** patterns:
1.  **Data Layer**: `FmpDataSource` handles raw API interaction, while `FinancialRepository` manages the coordination between the network and the `AppDatabase`.
2.  **Model Layer**: Pure Kotlin data classes (POJOs) representing domain entities like `AssetHolding`, `Dividend`, and `PortfolioMetrics`.
3.  **ViewModel Layer**: Reactively collects data from the repository using **Kotlin Flows** and exposes UI state to the screens.
4.  **UI Layer**: Declarative UI built with Jetpack Compose, ensuring high performance and a premium look-and-feel.

### 3.3 Security & Configuration
The project uses a secure `local.properties` based secret management system. API keys are extracted at build time and injected into the `BuildKonfig` object, differentiating between `debug` (using sandbox/test keys) and `release` (using production keys) environments.

---

## 4. Development & Deployment
The project is configured for professional deployment:
*   **Namespace**: `com.visualmoney.app`.
*   **Platform Support**: Native Android App and Native iOS App (via Framework embedding).
*   **Asset Branding**: Integrated with **Logo.dev** for high-quality institutional-grade asset logos.
