# 📱 WallSet - AI-Powered Wallpaper App

WallSet is an Android wallpaper application that allows users to discover, favorite, and set high-quality wallpapers. It also includes an **AI-powered semantic search** feature using the **Cohere API**, enabling users to search their favorites with natural language queries like *"orange car at sunset"*.

## 🚀 Features

- 🔍 **Search wallpapers** via keyword queries
- 💖 **Add/Remove from favorites**
- 🖼️ **Full-screen wallpaper preview**
- 📲 **Set wallpaper on home or lock screen**
- 🧠 **AI-based semantic search** on favorite wallpapers
- 🌙 **Dark/Light theme support**
- 🌐 **Multi-language support** (including Turkish)
- 📂 **SQLite database** used to persist favorites and embeddings

## 🧠 AI Semantic Search

WallSet integrates the **Cohere "embed-english-v3.0" model** to enable semantic search in the Favorites section. Descriptions of wallpapers are embedded and stored locally. When users search, the app compares the input with stored embeddings using cosine similarity.

- **Embedding provider:** [Cohere](https://cohere.com/)
- **Model:** `embed-english-v3.0`
- **Threshold:** `0.35` cosine similarity

## 🛠️ Tech Stack

- **Language:** Java
- **IDE:** Android Studio
- **Database:** SQLite
- **Libraries:**
  - [Retrofit](https://square.github.io/retrofit/)
  - [Glide](https://github.com/bumptech/glide)
  - [OkHttp](https://square.github.io/okhttp/)
  - [Cohere Java API](https://docs.cohere.com/)

## 📸 Screenshots

![WhatsApp Image 2025-05-22 at 21 56 45](https://github.com/user-attachments/assets/5a4b4613-60dd-49fd-9812-e0cec620d58b)
![WhatsApp Image 2025-05-22 at 21 56 45 (1)](https://github.com/user-attachments/assets/fa3db414-ae77-4f88-9cd6-cf2f95d969b2)
![WhatsApp Image 2025-05-22 at 21 56 45 (2)](https://github.com/user-attachments/assets/66a93d51-6f6d-4480-bab4-b6cd9a126f48)
![WhatsApp Image 2025-05-22 at 21 56 45 (3)](https://github.com/user-attachments/assets/f229cc96-d82f-48ad-8d71-25055afa0432)


## 🔒 Permissions

The app uses the following permissions:

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.SET_WALLPAPER"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

## 📦 Build & Run

1. Clone the repository
2. Open with Android Studio
3. Add your **Cohere API key** to `local.properties`:
   ```
   COHERE_API_KEY=your_cohere_api_key
   ```
4. Run the app on emulator or physical device

## 👨‍💻 Contributors

- **Mustafa Mansur YÖNÜGÜL** – [GitHub](https://github.com/your-username)
- *Project developed as part of the Mobile Programming Course at Duzce University.*

## 📄 License

This project is for educational purposes and not intended for commercial distribution.
