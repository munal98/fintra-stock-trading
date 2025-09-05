# Fintra Stock Trading

## 📋 İçerik | Contents

- [Türkçe](#tr)
- [English](#en)

<a id="tr"></a>
## 🇹🇷 Türkçe

### 📖 Proje Hakkında

Fintra, modern ve kullanıcı dostu bir finans ve borsa yönetim platformudur. Hisse senedi alım-satımı, müşteri yönetimi, portföy takibi ve operasyonel işlemler için kapsamlı bir çözüm sunar.

### ✨ Özellikler

- **Hızlı Alım-Satım**: Kolay ve hızlı hisse senedi işlemleri
- **Müşteri Yönetimi**: Kapsamlı müşteri bilgileri ve portföy takibi
- **Çalışan Yönetimi**: Rol tabanlı erişim kontrolü (Admin, Analist, Trader)
- **Operasyonel İşlemler**: 
  - Hisse Senedi Transferleri
  - Para Transferleri
  - Gün Sonu İşlemleri
- **Raporlama**: Detaylı finansal raporlar ve analizler
- **Çoklu Dil Desteği**: Türkçe arayüz
- **Responsive Tasarım**: Tüm cihazlarda optimum kullanıcı deneyimi

### 🚀 Kurulum

#### Gereksinimler

- Node.js 20.x veya üzeri
- npm 10.x veya üzeri

#### Geliştirme Ortamı

```bash
# Repoyu klonlayın
git clone https://github.com/your-username/fintra.git
cd fintra/frontend

# Bağımlılıkları yükleyin
npm install

# Geliştirme sunucusunu başlatın (Turbopack ile)
npm run dev
```

Tarayıcınızda [http://localhost:3000](http://localhost:3000) adresini açarak uygulamayı görebilirsiniz.

### 🐳 Docker ile Dağıtım

Fintra, Docker kullanarak kolayca dağıtılabilir:

#### Windows için:

```bash
# Docker imajını oluşturun ve çalıştırın
docker-build.bat
```

#### Linux/Mac için:

```bash
# Çalıştırma izni verin
chmod +x docker-build.sh

# Docker imajını oluşturun ve çalıştırın
./docker-build.sh
```

#### Manuel Dağıtım:

```bash
# Docker imajını oluşturun
docker-compose build

# Uygulamayı başlatın
docker-compose up -d
```

Uygulama [http://localhost:3000](http://localhost:3000) adresinde çalışacaktır.

### 🧪 Test

```bash
# Linting kontrolü
npm run lint
```

### 📦 Derleme

```bash
# Üretim için derleme
npm run build

# Üretim sunucusunu başlatma
npm run start
```

---

<a id="en"></a>
## 🇬🇧 English

### 📖 About The Project

Fintra is a modern and user-friendly finance and stock exchange management platform. It provides a comprehensive solution for stock trading, customer management, portfolio tracking, and operational transactions.

### ✨ Features

- **Quick Buy-Sell**: Easy and fast stock trading operations
- **Customer Management**: Comprehensive customer information and portfolio tracking
- **Employee Management**: Role-based access control (Admin, Analyst, Trader)
- **Operational Transactions**: 
  - Stock Transfers
  - Wire Transfers
  - End of Day Operations
- **Reporting**: Detailed financial reports and analytics
- **Multi-language Support**: Turkish interface
- **Responsive Design**: Optimal user experience on all devices

### 🚀 Installation

#### Requirements

- Node.js 20.x or higher
- npm 10.x or higher

#### Development Environment

```bash
# Clone the repository
git clone https://github.com/your-username/fintra.git
cd fintra/frontend

# Install dependencies
npm install

# Start the development server (with Turbopack)
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser to see the application.

### 🐳 Deployment with Docker

Fintra can be easily deployed using Docker:

#### For Windows:

```bash
# Build and run Docker image
docker-build.bat
```

#### For Linux/Mac:

```bash
# Give execution permission
chmod +x docker-build.sh

# Build and run Docker image
./docker-build.sh
```

#### Manual Deployment:

```bash
# Build Docker image
docker-compose build

# Start the application
docker-compose up -d
```

The application will be running at [http://localhost:3000](http://localhost:3000).

### 🧪 Testing

```bash
# Run linting checks
npm run lint
```

### 📦 Building

```bash
# Build for production
npm run build

# Start production server
npm run start
```

## 🛠️ Built With

- [Next.js 15](https://nextjs.org/) - React framework
- [React 19](https://reactjs.org/) - JavaScript library
- [TypeScript](https://www.typescriptlang.org/) - Type-safe JavaScript
- [Tailwind CSS](https://tailwindcss.com/) - CSS framework
- [Radix UI](https://www.radix-ui.com/) - UI component primitives
- [Recharts](https://recharts.org/) - Charting library
- [i18next](https://www.i18next.com/) - Internationalization framework
- [Docker](https://www.docker.com/) - Containerization

## 📄 License

This project is proprietary and confidential.

## 📞 Contact

Project Link: [https://github.com/your-username/fintra](https://github.com/your-username/fintra)
