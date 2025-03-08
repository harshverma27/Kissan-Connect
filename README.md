# 📌 Farmer-Consumer Marketplace App

## **📂 Project Overview**
This application directly connects **farmers** and **consumers**, eliminating intermediaries. Farmers can add, modify, delete products, track earnings, and manage orders. Consumers can browse products, search for items, place orders, and track order history.

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
✅ **AI Scanning** (Adding Product By Scanning Them) 
✅ **Photos** can be added for more simpler UI  
✅ **Dark Mode Support**

---
## **📜 Technologies Used**
- **Kotlin (Jetpack Compose)** → UI & Navigation
- **Firebase Authentication** → Secure Login/Signup
- **Firebase Firestore** → Real-time Database
- **Firebase Storage** → Image Uploading

---
## **🎉 Final Thoughts**
This project successfully implements a **real-time farmer-to-consumer marketplace** with Firebase **authentication, Firestore database, and cloud storage**. The UI is **responsive and intuitive**, supporting **multi-language** with expandable features!

🚀 **Developed for Electrothon 7.0 Hackathon!** 🎯

