# Lịch Sử Cập Nhật Dự Án (Project Change History Log)

---

## [2026-07-21] Cập nhật Chức năng Quản lý User, Category, Product, Variant (CRUD Đầy Đủ) & Tối ưu Admin Dashboard

### 1. Quản lý Người dùng & Tài khoản (User & Account CRUD + Validation + Protection)
- **Service Layer (`AdminCatalogService.java`)**:
  - Tiêm phụ thuộc `PasswordEncoder` để mã hóa mật khẩu người dùng theo chuẩn BCrypt.
  - **Chức năng Thêm mới (`createUser`)**:
    - Validate Họ tên (không rỗng, <= 120 ký tự).
    - Validate Email (chuẩn format regex, kiểm tra duy nhất trong DB bằng `userRepository.findByEmail`).
    - Validate Số điện thoại (chuẩn regex `8-20` chữ số/ký tự hợp lệ).
    - Validate Mật khẩu (độ dài `8-72` ký tự, mã hóa BCrypt).
    - Validate Vai trò (chỉ chấp nhận `ADMIN` hoặc `CUSTOMER`).
  - **Chức năng Cập nhật (`updateUser`)**:
    - Cho phép sửa Họ tên, Email (có kiểm tra trùng lặp ngoại trừ ID bản thân), SĐT, Vai trò, Trạng thái Kích hoạt.
    - Đổi mật khẩu mới là tùy chọn (để trống giữ nguyên mật khẩu cũ).
  - **Chức năng Xóa (`deleteUser`)**:
    - Bắt ngoại lệ ràng buộc cơ sở dữ liệu (`DataIntegrityViolationException`) khi người dùng đã có đơn hàng/địa chỉ liên quan, đưa ra thông báo hướng dẫn Admin Vô hiệu hóa (Disable) thay vì xóa làm hỏng dữ liệu.
  - **Cơ chế Tự bảo vệ Tài khoản (Safeguards)**:
    - Ngăn Admin đang đăng nhập tự Khóa (Disable), tự Hạ quyền (Demote xuống CUSTOMER) hoặc tự Xóa (Delete) tài khoản của chính mình.

- **Controller Layer (`AdminController.java`)**:
  - Thêm các Endpoint tiếp nhận yêu cầu từ Admin:
    - `@PostMapping("/admin/users/create")`
    - `@PostMapping("/admin/users/{id}/edit")`
    - `@PostMapping("/admin/users/{id}/delete")`
  - Bắt các ngoại lệ `IllegalArgumentException` và trả về Flash Attributes (`adminError` / `adminMessage`).

- **Giao diện Admin (`dashboard.html`)**:
  - Thêm nút **`+ Add Account`** mở Modal tạo tài khoản mới.
  - Cập nhật cột **Action** trong bảng danh sách User gồm: `Edit`, `Disable/Enable`, và `Delete` (có hộp thoại xác nhận).
  - Thêm 2 Modal Bootstrap 5: `#addUserModal` và `#editUserModal` đồng bộ style tối giản.
  - Thêm hàm JavaScript `openEditUserModal(...)` tự động nạp dữ liệu lên Modal chỉnh sửa.

---

### 2. Quản lý Danh mục, Sản phẩm & Biến thể (Category, Product & Variant CRUD + Validation)
- **Data Repository Layer**:
  - Thêm `CategoryRepository.findByNameIgnoreCase(name)` kiểm tra trùng tên danh mục.
  - Thêm `ProductVariantRepository.findBySkuIgnoreCase(sku)` kiểm tra trùng mã SKU biến thể toàn hệ thống.
- **Service Layer (`AdminCatalogService.java`)**:
  - **Danh mục (Category)**:
    - `createCategory`: Validate tên danh mục không rỗng, max 120 ký tự, kiểm tra trùng tên.
    - `updateCategory`: Cho phép sửa Tên & Mô tả danh mục, kiểm tra trùng tên với danh mục khác.
    - `deleteCategory`: Kiểm tra nếu danh mục đang chứa sản phẩm thì không cho xóa (báo lỗi rõ ràng yêu cầu chuyển/xóa sản phẩm trước).
  - **Sản phẩm (Product)**:
    - `createProduct` / `updateProduct`: Validate tên sản phẩm, giá gốc (`0 < price < 1,000,000,000`), kiểm tra loại sản phẩm và danh mục hợp lệ.
    - `deleteProduct`: Xóa sản phẩm, tự động bắt lỗi `DataIntegrityViolationException` nếu sản phẩm đã phát sinh đơn hàng/giỏ hàng và thông báo người dùng ẩn (Disable) sản phẩm thay vì xóa.
  - **Biến thể (Product Variant)**:
    - `createVariant` / `updateVariant`: Validate Màu, Size, Mã SKU (bắt buộc duy nhất trong toàn hệ thống, max 80 ký tự), Tồn kho (`>= 0`).
    - `deleteVariant`: Xóa biến thể sản phẩm, bảo vệ dữ liệu đơn hàng cũ.
- **Controller Layer (`AdminController.java`)**:
  - Bổ sung đầy đủ các endpoint:
    - `@PostMapping("/admin/categories/{id}/edit")`
    - `@PostMapping("/admin/categories/{id}/delete")`
    - `@PostMapping("/admin/products/{id}/delete")`
    - `@PostMapping("/admin/variants/{id}/edit")`
    - `@PostMapping("/admin/variants/{id}/delete")`
- **Giao diện Admin (`dashboard.html`)**:
  - Thêm nút **`Edit`** và **`Delete`** cho từng Danh mục trong bảng Category.
  - Thêm nút **`Delete`** trên từng thẻ sản phẩm trong tab Products & Inventory.
  - Thêm nút **`Edit`** và **`Delete`** cho từng dòng Biến thể (Variant) trong bảng kho chi tiết.
  - Thêm 2 Modal Bootstrap 5 mới: `#editCategoryModal` và `#editVariantModal`.
  - Viết các hàm JS: `openEditCategoryModal(...)`, `openEditVariantModal(...)`.

---

### 3. Tối ưu Trải nghiệm Cuộn trang Admin (Scroll Position Retention)
- **Vấn đề đã khắc phục**: Trước đây khi người dùng cuộn xuống giữa trang và ấn nút thao tác (ví dụ: Disable user, Toggle product, Update stock, ...), sau khi gửi form và chuyển hướng lại trang thì trang web bị cuộn ngược lên đầu (`y = 0`).
- **Giải pháp triển khai**:
  - Thêm sự kiện `beforeunload` lưu vị trí cuộn `window.scrollY` hiện tại vào `sessionStorage`.
  - Thêm xử lý trong `DOMContentLoaded`: sau khi nạp trang và chọn tab theo URL Hash (`#users`, `#catalog`, `#categories`, `#orders`), tự động khôi phục lại vị trí cuộn `window.scrollTo` chính xác như trước khi bấm nút.

---

### 4. Chuẩn hóa Thiết kế & Sắp xếp Nút Hành động Admin (UI Design & Alignment Optimization)
- **Vấn đề đã khắc phục**: Các nút bấm và nhãn trạng thái (`Edit`, `+ Variant`, `ACTIVE`, `Hide`, `Delete`) bị lệch kích thước (nút to, nút nhỏ, border/height không đồng nhất) và màu sắc chưa đẹp mắt.
- **Giải pháp triển khai**:
  - **Đồng bộ Kích thước**: Xây dựng hệ thống Class CSS chuẩn `.admin-action-btn` và `.admin-status-badge` đảm bảo tất cả nút và badge có cùng chiều cao 32px, bo góc 6px, padding và font-size đồng nhất.
  - **Tối ưu Bố cục & Thứ tự**: Đặt nhãn Trạng thái (`ACTIVE` / `HIDDEN`) lên vị trí đầu tiên của dải nút, theo sau là các nút thao tác theo thứ tự logic: `[STATUS] | [+ Variant] | [Edit] | [Hide/Show] | [Delete]`.
  - **Màu sắc Hài hòa**:
    - Nhãn `ACTIVE`: Nền xanh lục dịu `#ecfdf5`, chữ xanh `#059669`, viền `#a7f3d0`.
    - Nhãn `HIDDEN`: Nền đỏ nhạt `#fef2f2`, chữ đỏ `#dc2626`, viền `#fecaca`.
    - Nút `+ Variant`: Nút tối màu nổi bật (`#18181b`).
    - Nút `Edit` & `Hide`: Nút viền xám đen tối giản.
    - Nút `Delete`: Nút viền đỏ tinh tế (`#dc2626`).

---

### 5. Khắc phục Tự động Mở Bảng Biến thể Kho Chi Tiết (Inventory Details Retention)
- **Vấn đề đã khắc phục**: Khi người dùng nhấn nút "Show inventory details" để xem/sửa các dòng kho biến thể (thay đổi stock `Save`, `Edit`, `Hide`, `Delete` biến thể), trang web load lại làm ẩn bảng kho chi tiết, bắt người dùng phải bấm "Show inventory details" lại từ đầu.
- **Giải pháp triển khai**:
  - Thêm hàm `saveOpenVariantTables()` ghi nhận danh sách ID các sản phẩm đang được mở bảng kho chi tiết vào `sessionStorage`.
  - Thêm cơ chế tự động mở lại (`DOMContentLoaded`): Khi trang nạp lại, script đọc `sessionStorage` và tự động hiển thị bảng kho chi tiết (`variants-table-container`) của đúng sản phẩm đó, đồng thời chuyển nút thành `"Hide inventory details"` mà không cần thao tác lại thủ công.

---

### 6. Cải tiến Xử lý Lỗi Form Modal Trực Tiếp & Giữ Nguyên Modal (Inline Modal Validation & Error Preservation)
- **Vấn đề đã khắc phục**: Trước đây khi người dùng nhập sai thông tin trên Modal (ví dụ: nhập số điện thoại sai định dạng, mật khẩu ngắn hơn 8 ký tự, giá <= 0), form gửi đi lỗi làm đóng Modal và xóa sạch toàn bộ thông tin đã nhập, bắt người dùng mở lại và nhập lại từ đầu.
- **Giải pháp triển khai**:
  - **Validate Client-Side tức tính tại Modal Form (`setupModalValidation`)**:
    - Chặn gửi form nếu thông tin nhập sai: SĐT (`8-20` chữ số), Email format, Mật khẩu (`>= 8` ký tự), Giá gốc (`> 0`), Tồn kho (`>= 0`).
    - Hiển thị thông báo lỗi dòng màu đỏ (`.invalid-feedback`) ngay bên dưới đúng ô input sai.
    - Tự động focus vào ô nhập sai, **giữ nguyên Modal đang mở và 100% dữ liệu đã nhập**.
  - **Tự động mở lại Modal khi gặp lỗi Server (`AdminController` + `adminErrorModal`)**:
    - Nếu gặp lỗi phía Server/DB (như trùng Email, trùng SKU, trùng Tên danh mục), Controller truyền thuộc tính flash `adminErrorModal`.
    - Khi trang nạp xong, script tự động hiển thị lại đúng Modal đó và chèn khung cảnh báo lỗi đỏ ngay đầu Modal để người dùng điều chỉnh trực tiếp mà không bị mất dấu.

---

### 7. Tự động Ẩn Thông báo Sau 10 Giây (Auto-Dismiss Alert Messages)
- **Vấn đề đã khắc phục**: Khung thông báo thành công hoặc lỗi hiển thị cố định ở đầu danh sách làm chiếm không gian giao diện nếu người dùng không chuyển trang.
- **Giải pháp triển khai**:
  - Viết hàm `setupAutoDismissAlerts()`: Các khung thông báo (`.cart-alert`, `.alert-dismissible`) sẽ **tự động mờ dần và biến mất mượt mà sau 10 giây (10,000ms)**.
  - Cho phép người dùng nhấp trực tiếp vào khung thông báo để đóng ngay lập tức nếu không muốn chờ 10 giây.

---

### 8. Tái Cấu Trúc Bố Cục Admin Dashboard & Nâng Cấp Tab Overview (Dashboard Restructuring & Overview Enhancement)
- **Vấn đề đã khắc phục**: Thanh nút Tab Navigation nằm dưới dải 6 ô số liệu làm rối tầm mắt; đồng thời khi chuyển qua các tab quản lý danh mục/sản phẩm/user thì 6 ô số liệu vẫn nằm ở trên làm chiếm không gian hiển thị bảng.
- **Giải pháp triển khai**:
  - **Chuyển Thanh Tab Navigation lên trên**: Đưa `.admin-tabs` lên vị trí ngay dưới Header để chuyển đổi tab nhanh chóng.
  - **Chuyển 6 ô số liệu thống kê vào trong Tab Overview**: Khi chuyển sang các tab khác, dải ô số liệu tự động ẩn đi để nhường toàn bộ diện tích cho các bảng quản lý.
  - **Nâng cấp nội dung Tab Overview thành Trung tâm Điều hành Executive Dashboard**:
    - Thêm **Dải phím tắt thao tác nhanh (Quick Actions Bar)**: Nút `+ Add Product`, `+ Add Category`, `+ Add Account`, `View Storefront`.
    - Thêm **Bảng Đơn hàng gần đây (Recent Orders)**: Giới hạn hiển thị **Top 5 đơn hàng mới nhất phát sinh** kèm trạng thái và nút "View All" chuyển nhanh sang Tab Orders.
    - Thêm **Bảng Cảnh báo hàng tồn kho thấp (Urgent Low Stock Alert)**: Giới hạn hiển thị **Top 5 biến thể SKU bị thiếu kho trầm trọng nhất (`stockQuantity ≤ 5`)** trên toàn bộ hệ thống sản phẩm, sắp xếp theo số lượng tồn tăng dần để ưu tiên xử lý gấp.

---

### 9. Khống chế Chiều cao & Cố định Tiêu đề Bảng Kho (Inventory Details Sticky Scroll)
- Bảng biến thể SKU bên trong thẻ sản phẩm được giới hạn chiều cao tối đa `max-height: 360px` kèm thanh cuộn mượt và **tiêu đề bảng cố định (Sticky Header)** giúp việc tra cứu kho của 1 sản phẩm cực kỳ bao quát và chuyên nghiệp.

---

### 10. Chuyển Đổi Hệ Thống Phân Trang Chuẩn Backend Server-Side (Spring Data JPA Pageable System)
- **Vấn đề đã khắc phục**: Thay thế toàn bộ cơ chế phân trang đệm đè ở Frontend (vốn vẫn phải nạp toàn bộ danh sách từ DB về RAM) bằng chuẩn phân trang **Server-Side Backend (Spring Data JPA `Pageable`)**.
- **Giải pháp triển khai**:
  - **Repository Layer**: Thêm các truy vấn trả về `Page<T>` với tham số `Pageable` trong `ProductRepository`, `UserRepository`, `CategoryRepository`.
  - **Service & Controller Layer (`AdminCatalogService`, `OrderService`, `AdminController`)**:
    - Tiếp nhận các tham số `catalogPage`, `userPage`, `orderPage`, `categoryPage` cùng từ khóa tìm kiếm/bộ lọc từ URL GET parameters.
    - Sử dụng `PageRequest.of(page, size)` thực thi truy vấn SQL chuẩn `LIMIT` & `OFFSET`, **chỉ lấy đúng số lượng dữ liệu cần thiết từ Database** (5 sản phẩm/trang, 10 user/trang, 10 đơn/trang, 10 danh mục/trang).
  - **View Layer (`dashboard.html`)**:
    - Xóa bỏ hoàn toàn code JavaScript đệm phân trang đè ở Client.
    - Sử dụng liên kết phân trang Server-side Thymeleaf (`th:href="@{'/admin/dashboard#tab'(page=...)}"`) và Form tìm kiếm GET truyền tham số chuẩn REST, vừa tối ưu tuyệt đối bộ nhớ RAM Server vừa đảm bảo chuyển tab không nháy màn hình.
    - Khắc phục cú pháp Thymeleaf URL Expression đối với Fragment Hash (đặt chuỗi URL `#tab` trong ngoặc đơn `@{'...#tab'(params)}`) giúp Thymeleaf render mượt mà 100%.

---

### 11. Cải Tiến Tìm Kiếm Category, Lọc Low Stock Catalog & Điểm Nhảy Tồn Kho (Category Search, Low-Stock Filter & Inventory Jump)
- **Thêm Tìm kiếm cho Tab Danh mục (Category Search)**:
  - Bổ sung Form tìm kiếm GET `<form th:action="@{'/admin/dashboard#categories'}" method="get">` tại `#tab-categories`.
  - Kết nối với `categoryRepository.searchCategories(...)` cho phép tìm kiếm nhanh danh mục theo Tên hoặc Slug.
- **Thêm Bộ Lọc Low Stock cho Tab Sản Phẩm & Tồn Kho (Catalog Filter)**:
  - Thêm tùy chọn `Low Stock Products` vào Dropdown lọc của `#tab-catalog`.
  - Bổ sung truy vấn `ProductRepository.findByLowStockPaged(Pageable)` tự động lọc các sản phẩm chứa ít nhất 1 biến thể SKU có tồn kho `≤ 5`.
- **Trỏ Trực Tiếp & Tự Động Mở Chi Tiết Kho Khi Bấm Hàng Thiếu Ở Overview**:
  - Mỗi dòng SKU trong bảng **Low Stock Items** ở tab Overview nay có thể **click trực tiếp** (`cursor: pointer`).
  - Viết hàm JavaScript `jumpToProductAndExpand(productId)`: Khi Admin bấm vào SKU thiếu hàng ở Overview, hệ thống sẽ tự động ghi nhớ ID sản phẩm, chuyển hướng sang Tab Catalog, tự động mở rộng bảng kho chi tiết (`variants-table-container`) và cuộn mượt (`scrollIntoView`) đến đúng thẻ sản phẩm đó để Admin chỉnh sửa tồn kho tức thì.

---

### 12. Tối Ưu Bố Cục Thanh Menu Điều Hướng Header (Header Navbar Navigation Reordering)
- **Cấu trúc Menu Mới theo Chuẩn Trải Nghiệm Người Dùng**:
  - Đưa **Cart (Giỏ hàng)** vào vị trí phía trong (sau Search, trước Account).
  - Đưa nút **Account (Tài khoản / Sign in)** ra vị trí bên phải ngoài cùng khi chưa đăng nhập.
  - Khi **Đăng nhập thành công (`isAuthenticated()`)**: Đưa nút **Sign out (Đăng xuất)** ra vị trí ngoài cùng góc phải màn hình (`[Cart] -> [Account/Admin] -> [Sign out]`).
- **Đồng bộ Toàn Bộ Giao Diện**: Áp dụng thứ tự navbar mới cho toàn bộ 11 template HTML (`home.html`, `products.html`, `product-detail.html`, `cart.html`, `checkout.html`, `checkout-success.html`, `login.html`, `register.html`, `account.html`, `order-detail.html`, `admin/dashboard.html`).

---

## [2026-07-21] Refactor Toàn Bộ Hệ Thống Template — Trích Xuất Fragment Dùng Chung (Header/Navbar/Footer/Scripts)

### 1. Tạo Thư Mục & 4 Fragment Dùng Chung
- **Thư mục mới**: `src/main/resources/templates/fragments/`
- **`fragments/head.html`** — Thẻ `<head>` dùng chung:
  - Meta charset, viewport, title (tham số động `${title}`), description (tham số động `${description}`).
  - Favicon SVG inline (logo U-Minimalist).
  - Bootstrap CSS + `landing.css`.
- **`fragments/navbar.html`** — Thanh điều hướng dùng chung:
  - Tham số: `activePage` (trang hiện tại để set `active`), `headerClass` (CSS class cho `<header>`), `dataAnimate` (animation cho home page), `toggler` (có/không nút hamburger mobile), `collapseShow` (luôn hiển thị menu không), `showSearch` (có/không link Search).
  - Tự động set class `active` và `aria-current="page"` theo `activePage`.
  - Tự động ẩn/hiện link Checkout (chỉ trên trang checkout).
  - Hỗ trợ `sec:authorize` cho 3 trạng thái: anonymous, customer, admin.
- **`fragments/footer.html`** — Chân trang dùng chung:
  - 4 cột: Shop, Support, Account, Updates.
  - Tham số `showNewsletter`: `true` cho home (có form đăng ký email), `false` cho các trang khác.
- **`fragments/scripts.html`** — Script JS dùng chung:
  - Bootstrap JS luôn có.
  - Tham số `full`: `true` cho home & products (GSAP + ScrollTrigger + landing.js), `false` cho các trang khác.

### 2. Cập Nhật Toàn Bộ 11 Template Sử Dụng Fragment
Mỗi template được viết lại để gọi fragment thay vì copy-paste:

| Template | Navbar call | Footer | Scripts |
|----------|------------|:---:|:---:|
| `home.html` | `navbar('home', 'site-header', 'nav', true, false, true)` | ✅ (newsletter) | full |
| `products.html` | `navbar('products', 'site-header catalog-header is-scrolled', '', true, false, true)` | ✅ | full |
| `product-detail.html` | `navbar('', 'site-header catalog-header is-scrolled', '', true, false, true)` | ✅ | basic + inline rating |
| `cart.html` | `navbar('cart', 'site-header catalog-header is-scrolled', '', true, false, true)` | ✅ | basic |
| `checkout.html` | `navbar('checkout', 'site-header catalog-header is-scrolled', '', false, true, false)` | ✅ | basic |
| `checkout-success.html` | `navbar('', 'site-header catalog-header is-scrolled', '', false, true, false)` | ✅ | basic |
| `login.html` | `navbar('login', 'site-header is-scrolled', '', true, false, true)` | ✅ | basic |
| `register.html` | `navbar('register', 'site-header is-scrolled', '', true, false, true)` | ✅ | basic |
| `account.html` | `navbar('account', 'site-header catalog-header is-scrolled', '', false, true, false)` | ✅ | basic + toggleEdit |
| `order-detail.html` | `navbar('account', 'site-header catalog-header is-scrolled', '', false, true, false)` | ✅ | basic |
| `admin/dashboard.html` | `navbar('admin', 'site-header is-scrolled', '', true, false, true)` | ✅ | basic + admin JS |

### 3. Nâng Cấp Nút Sign Out
- **Di chuyển** Sign Out từ trong `<ul class="navbar-nav mx-auto">` ra ngoài, dùng `ms-lg-auto` đẩy sang **góc phải** navbar.
- **Style nổi bật**: Viền đỏ `#dc3545`, chữ đỏ, font đậm. Hover: nền đỏ, chữ trắng.
- **Icon** logout SVG (mũi tên Bootstrap) bên cạnh chữ "Sign out".
- Áp dụng **toàn bộ 11 trang** chỉ bằng 1 lần sửa `fragments/navbar.html`.

### 4. Đồng Bộ Footer Cho Tất Cả Các Trang
- **Trước refactor**: 7/11 trang thiếu footer (products, product-detail, cart, checkout, checkout-success, account, order-detail).
- **Sau refactor**: 11/11 trang có footer đồng nhất, chỉ bằng 1 dòng `th:replace="~{fragments/footer :: footer(false)}"`.

### 5. Lợi Ích
- **Sửa 1 lần → áp dụng 11 trang**: Thay đổi navbar, footer, favicon, CSS, script chỉ cần sửa 1 file fragment.
- **Giảm trùng lặp**: ~60 dòng copy-paste được loại bỏ trên mỗi template.
- **Tổng code giảm**: `admin/dashboard.html` từ 1680 → 1600 dòng. Tổng toàn bộ template ~3180 dòng (đã bao gồm fragment).
- **Build sạch, HTTP 200 tất cả trang**.

---

## [2026-07-21] Tối Ưu Cấu Trúc Admin Dashboard — Tách CSS & Chia Nhỏ Modals

### 1. Chuyển CSS Admin Từ Inline `<style>` Sang File Tĩnh
- **Trước**: CSS admin nhúng trong `<style>` tag 350 dòng của `admin/fragments/admin-css.html`.
- **Sau**: File `static/css/admin.css` (404 dòng) — được browser cache, không render lại mỗi lần load trang.
- Dashboard gọi qua `<link rel="stylesheet" th:href="@{/css/admin.css}">`.

### 2. Chia Modals Thành 4 File Theo Domain
- **Trước**: 1 file `admin/fragments/modals.html` 311 dòng chứa 8 modal lẫn lộn.
- **Sau**: Thư mục `admin/modals/` với 4 file riêng biệt:
  | File | Dòng | Nội dung |
  |------|------|----------|
  | `category-modals.html` | 60 | Add/Edit Category |
  | `product-modals.html` | 93 | Add/Edit Product |
  | `variant-modals.html` | 77 | Add/Edit Variant |
  | `user-modals.html` | 99 | Add/Edit User |
- Muốn sửa modal nào → mở đúng file đó, không phải scroll tìm trong 311 dòng.

### 3. Kết Quả Cuối Cùng
```
templates/admin/
├── dashboard.html          ← 949 dòng (shell chính: 5 tab + JS)
└── modals/
    ├── category-modals.html
    ├── product-modals.html
    ├── variant-modals.html
    └── user-modals.html

static/css/
├── landing.css
└── admin.css               ← 404 dòng CSS thuần
```
- **Dashboard từ 1680 → 949 dòng (-44%)** sau toàn bộ đợt refactor.
- CSS tĩnh browser cache được, tăng perf.
