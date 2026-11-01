# ShopEasy – Full Stack E-Commerce (Spring Boot)

A complete e-commerce app: **Spring Boot 3 + Spring Data JPA + H2** backend and a **vanilla HTML/CSS/JS** storefront served by the same app (one project, one command to run).

## Features
- 12 seeded products across Footwear, Clothing, Accessories, Electronics
- Every product has: **name, brand, category, price, sizes, colour, stock, rating, description and a matching image** (real photos via the download script)
- Search, filter by category / brand, sort by price / rating
- Size selection, cart (saved in browser), checkout
- Orders saved to DB; stock reduced on purchase; **price is always taken from the DB** (client can't tamper)
- **Sign up / Sign in / Sign out** pages (`/signup.html`, `/login.html`), passwords stored as BCrypt hashes
- Checkout requires an account; **My orders** page (`/orders.html`) shows each customer's order history
- Admin role for product management; REST API with full product CRUD
- H2 console for inspecting data

## Run
Requirements: **Java 17+** and **Maven 3.9+**

```bash
mvn spring-boot:run
```
Open http://localhost:8080

Other URLs: H2 console http://localhost:8080/h2-console (admin sign-in required; JDBC URL `jdbc:h2:file:./data/shopdb`, user `sa`, empty password).

Run tests: `mvn test` · Build jar: `mvn clean package` → `java -jar target/shopeasy-ecommerce-1.0.0.jar`

Docker: `docker build -t shopeasy . && docker run -p 8080:8080 shopeasy`

## Project structure
```
src/main/java/com/shopeasy
├── ShopEasyApplication.java
├── config/DataLoader.java          # seeds sample products + image paths
├── model/                          # Product, CustomerOrder, OrderItem (JPA entities)
├── repository/                     # Spring Data JPA repositories
├── service/                        # ProductService, OrderService (business rules)
├── controller/                     # REST controllers
└── dto/OrderRequest.java
src/main/resources
├── application.properties
└── static/                         # frontend
    ├── index.html, css/style.css, js/app.js
    └── images/                     # *.svg fallbacks + *.jpg photos (after running the script)
scripts/DownloadImages.java         # downloads real product photos
```

## Accounts & security
| Page | URL |
|---|---|
| Sign up | http://localhost:8080/signup.html |
| Sign in | http://localhost:8080/login.html |
| My orders | http://localhost:8080/orders.html |

- Anyone can browse; **placing an order needs an account**. The cart is kept in the browser, so after signing in you carry on where you left off.
- Built with Spring Security: BCrypt password hashing, HTTP-only session cookie with `SameSite=Lax`, session-fixation protection, minimum 8-character passwords.
- Roles: `USER` (default for sign-ups) and `ADMIN`. Only admins can create/update/delete products, list all orders, or open the H2 console.
- A default admin is created on first start (change it in `application.properties` **before deploying**):
  `admin@shopeasy.com` / `Admin@12345`
- Customers can only see their own orders.

Using the admin API with curl (log in once, reuse the cookie):
```bash
curl -c cookies.txt -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"email":"admin@shopeasy.com","password":"Admin@12345"}'
curl -b cookies.txt -X POST localhost:8080/api/products -H "Content-Type: application/json" -d '{...product json...}'
```

## REST API
| Method | URL | Description |
|---|---|---|
| POST | `/api/auth/signup` · `/api/auth/login` · `/api/auth/logout` | Create account / sign in / sign out |
| GET | `/api/auth/me` | Current user (`authenticated: false` if not signed in) |
| GET | `/api/products?q=&category=&brand=&sort=` | List / search (`sort`: `price_asc`, `price_desc`, `rating`) |
| GET | `/api/products/{id}` | Single product |
| GET | `/api/products/categories` · `/brands` | Filter values |
| POST | `/api/products` | Create product (admin) |
| PUT | `/api/products/{id}` | Update product (admin) |
| DELETE | `/api/products/{id}` | Delete product (admin) |
| POST | `/api/orders` | Place order (signed in) |
| GET | `/api/orders/my` | My orders (signed in) |
| GET | `/api/orders/{id}` | One order (owner or admin) |
| GET | `/api/orders` | All orders (admin) |

Example – place an order (after signing in, using the session cookie):
```bash
curl -b cookies.txt -X POST localhost:8080/api/orders -H "Content-Type: application/json" -d '{
  "customerName":"Ravi","email":"ravi@example.com","phone":"9999999999","address":"Guntur, AP",
  "items":[{"productId":1,"size":"9","quantity":1}]}'
```

## Realistic product photos
The app ships with illustrated SVG fallbacks, and a script downloads **real photos** for every product (shoe -> shoe photo, watch -> watch photo, ...) from [Pexels](https://www.pexels.com/api/) (free, no attribution required - see Pexels license).

```bash
# 1. get a free API key at https://www.pexels.com/api/  (takes ~1 minute)
export PEXELS_API_KEY=your_key_here          # Windows cmd: set PEXELS_API_KEY=your_key_here
# 2. from the project root:
java scripts/DownloadImages.java
# 3. restart the app, then hard refresh (Ctrl+F5)
```
- Photos are saved to `src/main/resources/static/images/*.jpg` - **commit them** so your GitHub repo contains the images.
- Not happy with a photo? Run `PICK=1 java scripts/DownloadImages.java` (or 2, 3, 4...) for the next search result, or edit the search phrases at the top of `scripts/DownloadImages.java`.
- To add your own photos, drop a file into `static/images/` and set the product's `imageUrl` (e.g. `/images/my-shoe.jpg`, or any full `https://` URL).
- If the `.jpg` is missing, the page automatically shows the SVG illustration instead.

## Switch to MySQL
Add the `mysql-connector-j` dependency to `pom.xml` and change `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/shopeasy
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```

## Push to GitHub
```bash
cd shopeasy-ecommerce
git init
git add .
git commit -m "Initial commit: Spring Boot e-commerce app"
git branch -M main
git remote add origin https://github.com/<your-username>/shopeasy-ecommerce.git
git push -u origin main
```
(Create the empty repo on GitHub first, with no README.)

## Ideas to extend
admin dashboard UI for products, password reset by email, login rate-limiting, JWT for a separate mobile/React client, payment gateway (Razorpay/Stripe), pagination, order history per user, product detail page.
