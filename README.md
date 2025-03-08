# 📌 Farmer-Consumer Marketplace App

## **📂 Project Overview**
This application is designed to connect **farmers** and **consumers** directly, eliminating intermediaries. Farmers can add, modify, delete products, track earnings, and manage orders. Consumers can browse products, search for items, place orders, and track order history.

---
## **🚀 Features Implemented**

### **👨‍🌾 Farmer Features:**
✅ **Add Products** (with name, price, and image upload)  
✅ **Modify & Delete Products**  
✅ **View Orders** (Accept or Reject Orders)  
✅ **Track Earnings** (Total earnings from accepted orders)  

### **🛍️ Consumer Features:**
✅ **Browse Products** (View all available products)  
✅ **Search Products** (Live filtering based on product name)  
✅ **Place Orders** (Request orders directly from product listings)  
✅ **View Order History** (See accepted, rejected, or pending orders)  

### **🔐 Authentication:**
✅ **User Registration & Login** (Firebase Authentication)  
✅ **Farmer & Consumer Account Separation**  
✅ **Stored Securely in Firebase Firestore**  

---
## **📁 Code Structure**

### **1️⃣ Authentication System**
- `AuthenticationScreen.kt`
- `signUpUser()` → Registers a new user (Farmer/Consumer) in Firebase
- `signInUser()` → Logs in users and redirects them to respective dashboards
- **Dropdown for selecting user type (Farmer/Consumer)**

### **2️⃣ Farmer Functionalities**
- `FarmerScreen.kt` → Dashboard with clickable options:
  - `AddProductScreen.kt`
  - `ModifyProductScreen.kt`
  - `ViewOrdersScreen.kt`
  - `TrackEarningsScreen.kt`
- **Firestore Integration for Products & Orders**

### **3️⃣ Consumer Functionalities**
- `ConsumerScreen.kt` → Dashboard with browsing & ordering options:
  - `BrowseProductsScreen.kt`
  - `SearchProductsScreen.kt`
  - `OrderHistoryScreen.kt`
- **Firestore Integration for Order Placement & History**

### **4️⃣ Firebase Firestore Database Rules**
✅ Farmers can **only** modify their own products  
✅ Consumers can **read** all products but **cannot modify** any  
✅ Orders can only be updated by **assigned farmers**  
✅ Consumers can **only view their own orders**

---
## **🌍 Multilingual Support**
✅ Implemented **string resources** (`strings.xml`) for **easy localization**
✅ Hindi (`values-hi/strings.xml`) added for full app translation
✅ Android auto-switches languages based on user settings

---
## **🎨 UI/UX Enhancements**
✅ **Clickable, shadowed, white background cards** for buttons  
✅ **BoxItem redesign** with smooth animations  
✅ **Dark mode-friendly text and icons**  
✅ **Loading indicators** for long tasks (image uploads, fetching data)  
✅ **Snackbar & Toast alerts** for successful operations

---
## **🔗 Next Steps / Possible Improvements**
✅ **Push Notifications** for order status updates  
✅ **Live Order Tracking** for consumers  
✅ **Admin Dashboard** for monitoring transactions  
✅ **Dark Mode Support**

---
## **📜 Technologies Used**
- **Kotlin (Jetpack Compose)** → UI & Navigation
- **Firebase Authentication** → Secure Login/Signup
- **Firebase Firestore** → Real-time Database
- **Firebase Storage** → Image Uploading

---
## **📂 File Breakdown**
📌 **MainActivity.kt** → Navigation and app entry point  
📌 **AuthenticationScreen.kt** → Sign In/Sign Up with Firebase  
📌 **FarmerScreen.kt** → Farmer's dashboard  
📌 **ConsumerScreen.kt** → Consumer's dashboard  
📌 **AddProductScreen.kt** → Form for adding products  
📌 **ModifyProductScreen.kt** → Edit/Delete existing products  
📌 **ViewOrdersScreen.kt** → Accept/Reject orders  
📌 **TrackEarningsScreen.kt** → Shows total earnings of a farmer  
📌 **strings.xml** → Multi-language support for UI  
📌 **Firebase Firestore Rules** → Restricts access to data  

---
## **🎉 Final Thoughts**
This project successfully implements a **real-time farmer-to-consumer marketplace** with Firebase **authentication, Firestore database, and cloud storage**. The UI is **responsive and intuitive**, supporting **multi-language** with expandable features!

🚀 **Developed for Electrothon 7.0 Hackathon!** 🎯

