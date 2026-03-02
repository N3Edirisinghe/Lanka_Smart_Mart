# Lanka Smart Mart - Multi-Platform E-Commerce Solution

Lanka Smart Mart is a comprehensive, modern e-commerce platform designed to provide a seamless shopping experience for customers and efficient management tools for store owners. The project consists of a high-performance Android mobile application, a robust Web Admin dashboard, and a secondary backend service for specialized tasks.

---

## 📱 1. Mobile Application (Android)
The core shopping experience for customers, built with modern Android development practices.

### Key Features
*   **Authentication**: Secure login/signup via Email or **Google Sign-In**.
*   **Smart Search**: Real-time text search and **ML Kit-powered Barcode/Image Scanner**.
*   **Engagement**: Loyalty points system, "Favorites" wishlist, and personalized profiles.
*   **Ordering**: Seamless cart management, multi-address delivery setup, and real-time order tracking.
*   **UI/UX**: Dynamic theme switching (Dark/Light), glassmorphism elements, and premium animations.

### Tech Stack
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (MVVM Architecture)
*   **Backend**: Firebase (Firestore, Auth, Storage)
*   **Local Storage**: Room Database
*   **Dependency Injection**: Hilt

---

## 💻 2. Web Admin Dashboard
A centralized control panel for managing inventory, orders, and store operations.

### Key Features
*   **Inventory Management**: Full CRUD operations for products and categories.
*   **Order Fulfillment**: Live order monitoring and status update workflow (Processing → Shipped → Delivered).
*   **Analytics**: Overview of total revenue and order statistics.
*   **Responsive Design**: Built for desktop and tablet efficiency.

### Tech Stack
*   **Framework**: React (Vite)
*   **Styling**: Tailwind CSS & Framer Motion
*   **Backend**: Firebase JS SDK

---

## ⚙️ 3. Specialized Backend service
A specialized Node.js service handling automated business logic and external integrations.

### Key Features
*   **Transactional Emails**: Automatic order confirmations and status updates via **Nodemailer**.
*   **Payment Integration**: Secure transaction handling with **Stripe**.
*   **Admin Utilities**: Backend hooks for Firebase Admin operations.

### Tech Stack
*   **Environment**: Node.js (Express)
*   **Integrations**: Firebase Admin SDK, Stripe API, Nodemailer

---

## 🚀 Getting Started

### Prerequisites
*   Android Studio (Ladybug or newer)
*   Node.js (v18+)
*   Firebase Project Credentials (`google-services.json`)

### Installation
1.  **Clone the Repo**:
    ```bash
    git clone https://github.com/N3Edirisinghe/Lanka_Smart_Mart.git
    ```
2.  **Mobile App**: Open the root directory in Android Studio and sync Gradle.
3.  **Web Admin**:
    ```bash
    cd Lanka_Smart_Mart/web-admin
    npm install
    npm run dev
    ```
4.  **Backend**:
    ```bash
    cd Lanka_Smart_Mart/email-backend
    npm install
    # Set up .env with STRIPE_SECRET and EMAIL_CONFIG
    node index.js
    ```

---

## 👥 Development Team
*   **E.A.N.T. Edirisinghe (CIT-23-02-0021)**: Lead Developer & Architect
*   **Thrithwaka (CIT-23-02-0094)**: UI/UX & Core Features
*   **Binara Hansaka (CIT-23-02-0137)**: Feature Developer & QA
