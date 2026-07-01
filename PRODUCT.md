# PRODUCT.md

# U-Minimalist – Hệ thống Thương mại Điện tử Thời trang Tối giản

**Môn học:** HSF301 – Java Frameworks  
**Tên sản phẩm:** U-Minimalist  
**Loại sản phẩm:** Website thương mại điện tử thời trang  
**Nhóm thực hiện:** Nhóm 2  
**Giảng viên hướng dẫn:** Nguyễn Ngọc Lâm  
**Ngày gửi:** 16/05/2026  
**Phiên bản:** 1.0  

---

## 1. Tổng quan sản phẩm

U-Minimalist là một website thương mại điện tử chuyên bán quần áo theo phong cách tối giản. Sản phẩm lấy cảm hứng từ triết lý “LifeWear” của Uniqlo: đơn giản, chất lượng, dễ mặc và tập trung vào trải nghiệm người dùng.

Hệ thống cho phép khách hàng xem sản phẩm, tìm kiếm, lọc theo danh mục, size, màu sắc, khoảng giá, chọn đúng biến thể sản phẩm, thêm vào giỏ hàng, đặt hàng và theo dõi trạng thái đơn hàng.

Ngoài chức năng mua sắm, hệ thống còn cung cấp trang quản trị cho Admin để quản lý danh mục, sản phẩm, biến thể sản phẩm, tồn kho, đơn hàng, khách hàng và thống kê doanh thu.

---

## 2. Mục tiêu sản phẩm

### 2.1. Mục tiêu chính

- Xây dựng website mua sắm quần áo online đơn giản, dễ sử dụng.
- Hỗ trợ khách hàng tìm kiếm và lọc sản phẩm nhanh chóng.
- Cho phép khách hàng chọn chính xác sản phẩm theo size và màu sắc.
- Quản lý giỏ hàng, checkout và tạo đơn hàng.
- Theo dõi lịch sử và trạng thái đơn hàng.
- Hỗ trợ Admin quản lý sản phẩm, danh mục, biến thể và tồn kho.
- Hỗ trợ Admin xử lý đơn hàng và xem thống kê doanh thu.
- Phân quyền rõ ràng giữa Guest, Customer và Admin.

### 2.2. Mục tiêu kỹ thuật

- Sử dụng Java 17.
- Sử dụng Spring MVC làm framework chính.
- Sử dụng Hibernate ORM và Spring Data JPA để thao tác database.
- Sử dụng Microsoft SQL Server làm hệ quản trị cơ sở dữ liệu.
- Sử dụng Spring Security để xác thực và phân quyền.
- Sử dụng Thymeleaf để render giao diện.
- Sử dụng Bootstrap 5 để xây dựng giao diện responsive.
- Tổ chức code theo mô hình MVC: Controller, Service, Repository, Entity.

---

## 3. Vấn đề cần giải quyết

### 3.1. Vấn đề của khách hàng

Khách hàng khi mua quần áo online thường gặp các vấn đề:

- Không biết sản phẩm còn size hoặc màu mong muốn hay không.
- Khó lọc sản phẩm theo nhu cầu cá nhân.
- Không rõ tổng tiền trong giỏ hàng.
- Không theo dõi được trạng thái đơn hàng.
- Giao diện mua hàng phức tạp, nhiều bước không cần thiết.

### 3.2. Vấn đề của Admin

Admin khi quản lý cửa hàng online thường gặp các vấn đề:

- Khó quản lý tồn kho theo từng size và màu.
- Khó cập nhật sản phẩm nhanh chóng.
- Khó kiểm soát đơn hàng mới, đơn đang xử lý và đơn đã hoàn tất.
- Khó theo dõi doanh thu.
- Có nguy cơ bán vượt tồn kho nếu không kiểm tra số lượng khi khách đặt hàng.

### 3.3. Giải pháp của U-Minimalist

U-Minimalist giải quyết các vấn đề trên bằng cách cung cấp:

- Bộ lọc sản phẩm theo danh mục, size, màu sắc và giá.
- Chức năng quản lý biến thể sản phẩm.
- Giỏ hàng rõ ràng, tính tổng tiền tự động.
- Quy trình checkout đơn giản.
- Quản lý tồn kho chi tiết theo từng biến thể.
- Quản lý đơn hàng theo trạng thái.
- Dashboard thống kê cho Admin.
- Phân quyền bằng Spring Security.

---

## 4. Người dùng mục tiêu

## 4.1. Guest

Guest là người dùng chưa đăng nhập.

### Nhu cầu chính

- Xem sản phẩm.
- Tìm kiếm sản phẩm.
- Lọc sản phẩm.
- Xem chi tiết sản phẩm.
- Đăng ký hoặc đăng nhập để mua hàng.

### Quyền truy cập

- Xem trang chủ.
- Xem danh sách sản phẩm.
- Xem chi tiết sản phẩm.
- Tìm kiếm và lọc sản phẩm.
- Đăng ký tài khoản.
- Đăng nhập.

### Giới hạn

- Không được thêm sản phẩm vào giỏ hàng.
- Không được đặt hàng.
- Không được xem lịch sử đơn hàng.
- Không được truy cập trang Admin.

---

## 4.2. Customer

Customer là người dùng đã đăng nhập.

### Nhu cầu chính

- Chọn sản phẩm đúng size và màu.
- Thêm sản phẩm vào giỏ hàng.
- Quản lý giỏ hàng.
- Đặt hàng.
- Theo dõi trạng thái đơn hàng.
- Xem lịch sử đơn hàng.
- Quản lý thông tin cá nhân.

### Quyền truy cập

- Toàn bộ chức năng của Guest.
- Thêm sản phẩm vào giỏ hàng.
- Cập nhật giỏ hàng.
- Checkout.
- Tạo đơn hàng.
- Xem lịch sử đơn hàng.
- Xem chi tiết đơn hàng.
- Hủy đơn hàng khi còn ở trạng thái cho phép.
- Cập nhật hồ sơ cá nhân.

### Giới hạn

- Không được truy cập trang Admin.
- Không được sửa sản phẩm.
- Không được sửa tồn kho.
- Không được xem đơn hàng của khách hàng khác.

---

## 4.3. Admin

Admin là người quản trị hệ thống.

### Nhu cầu chính

- Quản lý danh mục sản phẩm.
- Quản lý sản phẩm.
- Quản lý biến thể sản phẩm.
- Quản lý tồn kho.
- Quản lý đơn hàng.
- Quản lý khách hàng.
- Xem dashboard và báo cáo doanh thu.

### Quyền truy cập

- Truy cập Admin Dashboard.
- CRUD danh mục.
- CRUD sản phẩm.
- CRUD biến thể sản phẩm.
- Cập nhật tồn kho.
- Quản lý đơn hàng.
- Cập nhật trạng thái đơn hàng.
- Quản lý khách hàng.
- Xem thống kê doanh thu.

---

## 5. Phạm vi sản phẩm

### 5.1. Chức năng trong phạm vi

#### Nhóm chức năng xác thực

- Đăng ký tài khoản.
- Đăng nhập.
- Đăng xuất.
- Phân quyền theo role.

#### Nhóm chức năng khách hàng

- Xem trang chủ.
- Xem danh sách sản phẩm.
- Tìm kiếm sản phẩm.
- Lọc sản phẩm.
- Xem chi tiết sản phẩm.
- Chọn size và màu sắc.
- Thêm vào giỏ hàng.
- Cập nhật giỏ hàng.
- Đặt hàng.
- Thanh toán mockup hoặc VNPay Sandbox.
- Xem lịch sử đơn hàng.
- Xem chi tiết đơn hàng.
- Hủy đơn hàng khi còn ở trạng thái cho phép.
- Quản lý thông tin cá nhân.

#### Nhóm chức năng Admin

- Quản lý danh mục.
- Quản lý sản phẩm.
- Quản lý biến thể.
- Quản lý tồn kho.
- Quản lý đơn hàng.
- Cập nhật trạng thái đơn hàng.
- Quản lý khách hàng.
- Xem dashboard.
- Xem thống kê doanh thu.

### 5.2. Chức năng ngoài phạm vi phiên bản đầu

- Ứng dụng mobile.
- Chat trực tuyến.
- AI gợi ý sản phẩm.
- Tích hợp đơn vị vận chuyển thật.
- Thanh toán production thật.
- Quản lý nhiều kho hàng.
- Mã giảm giá phức tạp.
- Email marketing tự động.
- Tích hợp hệ thống CRM.

---

## 6. Tech Stack

| Thành phần | Công nghệ |
|---|---|
| Backend | Java 17, Spring MVC |
| ORM | Hibernate ORM |
| Data Access | Spring Data JPA |
| Database | Microsoft SQL Server |
| Security | Spring Security |
| Frontend Template | Thymeleaf |
| UI Framework | Bootstrap 5 |
| Build Tool | Maven |
| IDE | IntelliJ IDEA hoặc Antigravity IDE |
| Version Control | Git, GitHub |

---

## 7. Luồng sử dụng chính

## 7.1. Luồng Guest xem và tìm sản phẩm

1. Guest truy cập website.
2. Hệ thống hiển thị trang chủ.
3. Guest chọn danh mục hoặc vào trang sản phẩm.
4. Guest tìm kiếm hoặc lọc sản phẩm.
5. Hệ thống hiển thị danh sách sản phẩm phù hợp.
6. Guest chọn một sản phẩm để xem chi tiết.
7. Nếu muốn mua hàng, Guest cần đăng nhập hoặc đăng ký.

---

## 7.2. Luồng Customer mua hàng

1. Customer đăng nhập.
2. Customer xem danh sách sản phẩm.
3. Customer chọn sản phẩm.
4. Customer chọn màu sắc.
5. Customer chọn size.
6. Customer chọn số lượng.
7. Hệ thống kiểm tra tồn kho.
8. Customer thêm sản phẩm vào giỏ hàng.
9. Customer vào giỏ hàng.
10. Customer kiểm tra sản phẩm và tổng tiền.
11. Customer tiến hành checkout.
12. Customer nhập thông tin giao hàng.
13. Customer chọn phương thức thanh toán.
14. Hệ thống tạo đơn hàng.
15. Hệ thống trừ tồn kho.
16. Giỏ hàng được làm trống.
17. Customer xem đơn hàng trong lịch sử đơn hàng.

---

## 7.3. Luồng Admin quản lý sản phẩm

1. Admin đăng nhập.
2. Admin vào trang quản trị.
3. Admin mở trang quản lý sản phẩm.
4. Admin thêm sản phẩm mới hoặc chỉnh sửa sản phẩm cũ.
5. Admin nhập thông tin sản phẩm.
6. Admin tạo biến thể theo size và màu.
7. Admin nhập số lượng tồn kho cho từng biến thể.
8. Hệ thống lưu sản phẩm.
9. Sản phẩm active được hiển thị cho người dùng.

---

## 7.4. Luồng Admin xử lý đơn hàng

1. Customer đặt hàng thành công.
2. Đơn hàng được tạo với trạng thái `Pending`.
3. Admin vào trang quản lý đơn hàng.
4. Admin xem chi tiết đơn hàng.
5. Admin xác nhận đơn hàng.
6. Hệ thống chuyển trạng thái sang `Confirmed`.
7. Admin cập nhật trạng thái `Shipping` khi giao hàng.
8. Admin cập nhật trạng thái `Completed` khi giao thành công.
9. Doanh thu được ghi nhận khi đơn hàng `Completed`.

---

## 8. Danh sách chức năng chi tiết

# 8.1. Authentication Module

## PRD-F01: Đăng ký tài khoản

### Mô tả

Cho phép Guest tạo tài khoản Customer mới.

### Actor

Guest.

### Đầu vào

- Họ tên.
- Email.
- Số điện thoại.
- Mật khẩu.
- Xác nhận mật khẩu.

### Xử lý

- Kiểm tra email không được trùng.
- Kiểm tra mật khẩu và xác nhận mật khẩu phải giống nhau.
- Mã hóa mật khẩu trước khi lưu.
- Gán role mặc định là `CUSTOMER`.
- Lưu thông tin người dùng vào database.

### Kết quả

- Tài khoản được tạo thành công.
- Người dùng có thể đăng nhập bằng tài khoản vừa tạo.

### Acceptance Criteria

- Hệ thống báo lỗi nếu email đã tồn tại.
- Hệ thống báo lỗi nếu mật khẩu xác nhận không khớp.
- Tài khoản mới có role `CUSTOMER`.
- Mật khẩu không được lưu dạng plain text.

---

## PRD-F02: Đăng nhập

### Mô tả

Cho phép Guest đăng nhập vào hệ thống.

### Actor

Guest, Customer, Admin.

### Đầu vào

- Email.
- Mật khẩu.

### Xử lý

- Xác thực thông tin bằng Spring Security.
- Kiểm tra trạng thái tài khoản.
- Điều hướng theo role.

### Kết quả

- Customer được chuyển đến trang chủ hoặc trang tài khoản.
- Admin được chuyển đến Admin Dashboard.

### Acceptance Criteria

- Sai email hoặc mật khẩu thì hiển thị thông báo lỗi.
- Tài khoản bị khóa không được đăng nhập.
- Customer không thể truy cập trang Admin.
- Admin có thể truy cập trang quản trị.

---

## PRD-F03: Đăng xuất

### Mô tả

Cho phép người dùng thoát khỏi phiên đăng nhập.

### Actor

Customer, Admin.

### Acceptance Criteria

- Sau khi đăng xuất, người dùng quay về trạng thái Guest.
- Người dùng không thể truy cập trang cần đăng nhập sau khi đã đăng xuất.

---

# 8.2. Product Browsing Module

## PRD-F04: Trang chủ

### Mô tả

Trang chủ hiển thị thông tin thương hiệu, banner, danh mục nổi bật và sản phẩm nổi bật.

### Actor

Guest, Customer.

### Thành phần chính

- Banner giới thiệu.
- Danh mục Nam, Nữ, Trẻ em.
- Sản phẩm mới.
- Sản phẩm bán chạy.
- Thanh điều hướng.
- Nút đăng nhập, đăng ký hoặc tài khoản cá nhân.

### Acceptance Criteria

- Trang chủ hiển thị được danh sách sản phẩm nổi bật.
- Người dùng có thể chuyển đến trang danh sách sản phẩm.
- Giao diện responsive trên desktop và mobile.

---

## PRD-F05: Danh sách sản phẩm

### Mô tả

Hiển thị tất cả sản phẩm đang active.

### Actor

Guest, Customer.

### Dữ liệu hiển thị

- Hình ảnh sản phẩm.
- Tên sản phẩm.
- Giá.
- Danh mục.
- Màu có sẵn.
- Size có sẵn.
- Trạng thái còn hàng.

### Acceptance Criteria

- Chỉ sản phẩm active được hiển thị.
- Có phân trang.
- Sản phẩm hết hàng cần hiển thị nhãn `Out of stock`.
- Người dùng có thể click vào sản phẩm để xem chi tiết.

---

## PRD-F06: Tìm kiếm sản phẩm

### Mô tả

Cho phép người dùng tìm kiếm sản phẩm theo tên hoặc từ khóa.

### Actor

Guest, Customer.

### Acceptance Criteria

- Tìm kiếm không phân biệt chữ hoa chữ thường.
- Kết quả chỉ bao gồm sản phẩm active.
- Nếu không có kết quả, hiển thị thông báo `Không tìm thấy sản phẩm phù hợp`.

---

## PRD-F07: Lọc sản phẩm

### Mô tả

Cho phép người dùng lọc sản phẩm theo nhiều tiêu chí.

### Bộ lọc

- Danh mục.
- Bộ sưu tập: Nam, Nữ, Trẻ em.
- Size.
- Màu sắc.
- Khoảng giá.
- Trạng thái còn hàng.

### Acceptance Criteria

- Người dùng có thể chọn nhiều bộ lọc cùng lúc.
- Kết quả trả về đúng với bộ lọc.
- Người dùng có thể xóa bộ lọc.
- Khi không có kết quả, hệ thống hiển thị thông báo phù hợp.

---

## PRD-F08: Chi tiết sản phẩm

### Mô tả

Hiển thị thông tin chi tiết của một sản phẩm.

### Actor

Guest, Customer.

### Dữ liệu hiển thị

- Tên sản phẩm.
- Hình ảnh.
- Mô tả.
- Giá.
- Danh mục.
- Màu sắc.
- Size.
- Số lượng còn lại theo biến thể.
- Sản phẩm liên quan.

### Acceptance Criteria

- Người dùng phải chọn màu và size trước khi thêm vào giỏ.
- Nếu biến thể hết hàng, không cho thêm vào giỏ.
- Nếu chưa chọn size hoặc màu, hệ thống hiển thị cảnh báo.
- Giá hiển thị rõ ràng.

---

# 8.3. Cart Module

## PRD-F09: Thêm sản phẩm vào giỏ hàng

### Mô tả

Customer có thể thêm sản phẩm vào giỏ hàng sau khi chọn biến thể.

### Actor

Customer.

### Đầu vào

- Product Variant.
- Số lượng.

### Xử lý

- Kiểm tra người dùng đã đăng nhập.
- Kiểm tra Product Variant tồn tại.
- Kiểm tra số lượng thêm vào không vượt quá tồn kho.
- Nếu sản phẩm đã có trong giỏ, cộng dồn số lượng.
- Nếu chưa có, tạo CartItem mới.

### Acceptance Criteria

- Guest bấm thêm vào giỏ thì được yêu cầu đăng nhập.
- Customer không thể thêm sản phẩm hết hàng.
- Nếu sản phẩm đã có trong giỏ, hệ thống cộng dồn số lượng.
- Tổng số lượng trong giỏ được cập nhật đúng.

---

## PRD-F10: Xem giỏ hàng

### Mô tả

Customer có thể xem các sản phẩm đã thêm vào giỏ.

### Actor

Customer.

### Dữ liệu hiển thị

- Hình ảnh sản phẩm.
- Tên sản phẩm.
- Size.
- Màu sắc.
- Đơn giá.
- Số lượng.
- Thành tiền.
- Tổng tiền.

### Acceptance Criteria

- Tổng tiền được tính chính xác.
- Mỗi dòng giỏ hàng đại diện cho một biến thể sản phẩm.
- Nếu giỏ hàng rỗng, hiển thị thông báo `Giỏ hàng của bạn đang trống`.

---

## PRD-F11: Cập nhật giỏ hàng

### Mô tả

Customer có thể tăng, giảm hoặc xóa sản phẩm trong giỏ hàng.

### Actor

Customer.

### Acceptance Criteria

- Số lượng không được nhỏ hơn 1.
- Số lượng không được vượt quá tồn kho.
- Khi thay đổi số lượng, tổng tiền cập nhật đúng.
- Khi xóa sản phẩm, sản phẩm không còn trong giỏ.

---

# 8.4. Checkout and Order Module

## PRD-F12: Checkout

### Mô tả

Customer nhập thông tin giao hàng và xác nhận đặt hàng.

### Actor

Customer.

### Đầu vào

- Họ tên người nhận.
- Số điện thoại.
- Địa chỉ giao hàng.
- Ghi chú.
- Phương thức thanh toán.

### Acceptance Criteria

- Không thể checkout nếu giỏ hàng rỗng.
- Các trường bắt buộc không được để trống.
- Số điện thoại phải hợp lệ.
- Hệ thống kiểm tra tồn kho trước khi tạo đơn.

---

## PRD-F13: Tạo đơn hàng

### Mô tả

Hệ thống tạo đơn hàng từ giỏ hàng của Customer.

### Actor

Customer.

### Xử lý

- Lấy danh sách sản phẩm trong giỏ.
- Kiểm tra tồn kho.
- Tạo Order.
- Tạo OrderDetail.
- Lưu giá sản phẩm tại thời điểm mua.
- Trừ tồn kho.
- Xóa giỏ hàng sau khi tạo đơn thành công.

### Acceptance Criteria

- Đơn hàng mới có trạng thái `Pending`.
- OrderDetail phải lưu `productName`, `size`, `color`, `unitPrice`, `quantity`.
- Tồn kho giảm đúng theo số lượng đã mua.
- Giỏ hàng được làm trống sau khi đặt hàng thành công.

---

## PRD-F14: Thanh toán

### Mô tả

Hệ thống hỗ trợ thanh toán giả lập hoặc VNPay Sandbox.

### Phương thức thanh toán

- Cash on Delivery.
- Mockup Payment.
- VNPay Sandbox nếu nhóm có thời gian tích hợp.

### Acceptance Criteria

- Hệ thống lưu `paymentMethod`.
- Hệ thống lưu `paymentStatus`.
- Nếu thanh toán thành công, `paymentStatus` là `Paid`.
- Nếu thanh toán thất bại, `paymentStatus` là `Failed` hoặc `Pending`.
- Đơn hàng phải có thông tin thanh toán rõ ràng.

---

## PRD-F15: Lịch sử đơn hàng

### Mô tả

Customer có thể xem các đơn hàng đã đặt.

### Actor

Customer.

### Dữ liệu hiển thị

- Mã đơn hàng.
- Ngày đặt.
- Tổng tiền.
- Trạng thái đơn hàng.
- Trạng thái thanh toán.
- Nút xem chi tiết.

### Acceptance Criteria

- Customer chỉ xem được đơn hàng của mình.
- Đơn hàng mới nhất hiển thị trước.
- Có thể click để xem chi tiết.

---

## PRD-F16: Chi tiết đơn hàng

### Mô tả

Customer có thể xem thông tin chi tiết của từng đơn hàng.

### Actor

Customer.

### Dữ liệu hiển thị

- Mã đơn hàng.
- Thông tin người nhận.
- Địa chỉ giao hàng.
- Danh sách sản phẩm.
- Size, màu, số lượng.
- Đơn giá.
- Tổng tiền.
- Trạng thái đơn hàng.
- Trạng thái thanh toán.

### Acceptance Criteria

- Customer không xem được đơn hàng của người khác.
- Giá trong đơn hàng không thay đổi khi Admin chỉnh giá sản phẩm sau này.
- Dữ liệu hiển thị đúng với OrderDetail.

---

## PRD-F17: Hủy đơn hàng

### Mô tả

Customer có thể hủy đơn hàng nếu đơn còn ở trạng thái `Pending`.

### Actor

Customer.

### Acceptance Criteria

- Chỉ đơn `Pending` mới được Customer hủy.
- Khi hủy đơn, trạng thái chuyển thành `Cancelled`.
- Tồn kho được hoàn lại.
- Đơn `Confirmed`, `Shipping`, `Completed` không được Customer hủy.

---

# 8.5. Admin Category Module

## PRD-F18: Quản lý danh mục

### Mô tả

Admin có thể xem, thêm, sửa và vô hiệu hóa danh mục.

### Actor

Admin.

### Chức năng

- Xem danh sách danh mục.
- Thêm danh mục.
- Cập nhật danh mục.
- Vô hiệu hóa danh mục.

### Acceptance Criteria

- Tên danh mục không được trùng.
- Danh mục đang có sản phẩm không nên xóa cứng.
- Danh mục inactive không hiển thị cho khách hàng.
- Admin vẫn xem được danh mục inactive.

---

# 8.6. Admin Product Module

## PRD-F19: Quản lý sản phẩm

### Mô tả

Admin có thể quản lý toàn bộ sản phẩm trong hệ thống.

### Actor

Admin.

### Chức năng

- Xem danh sách sản phẩm.
- Thêm sản phẩm.
- Cập nhật sản phẩm.
- Vô hiệu hóa sản phẩm.
- Tìm kiếm sản phẩm.
- Lọc sản phẩm theo danh mục và trạng thái.

### Dữ liệu sản phẩm

- Tên sản phẩm.
- Mô tả.
- Giá.
- Hình ảnh.
- Danh mục.
- Trạng thái.

### Acceptance Criteria

- Giá sản phẩm phải lớn hơn 0.
- Sản phẩm phải thuộc một danh mục hợp lệ.
- Sản phẩm inactive không hiển thị cho khách hàng.
- Cập nhật giá sản phẩm không ảnh hưởng đến đơn hàng cũ.

---

# 8.7. Admin Variant and Inventory Module

## PRD-F20: Quản lý biến thể sản phẩm

### Mô tả

Admin có thể tạo và quản lý các biến thể của sản phẩm theo size và màu sắc.

### Actor

Admin.

### Dữ liệu biến thể

- Product.
- Size.
- Color.
- SKU.
- Stock quantity.
- Status.

### Acceptance Criteria

- Không được tạo hai biến thể trùng Product, Size và Color.
- SKU không được trùng.
- Stock quantity không được âm.
- Biến thể inactive không cho khách hàng chọn mua.

---

## PRD-F21: Quản lý tồn kho

### Mô tả

Admin có thể cập nhật số lượng tồn kho cho từng biến thể.

### Actor

Admin.

### Acceptance Criteria

- Tồn kho phải là số nguyên không âm.
- Hệ thống phải hiển thị sản phẩm sắp hết hàng.
- Khi Customer đặt hàng, tồn kho giảm.
- Khi đơn hàng bị hủy, tồn kho được hoàn lại.

---

# 8.8. Admin Order Module

## PRD-F22: Quản lý đơn hàng

### Mô tả

Admin có thể xem và xử lý đơn hàng.

### Actor

Admin.

### Chức năng

- Xem danh sách đơn hàng.
- Tìm kiếm đơn hàng.
- Lọc đơn hàng theo trạng thái.
- Xem chi tiết đơn hàng.
- Cập nhật trạng thái đơn hàng.

### Trạng thái đơn hàng

- `Pending`.
- `Confirmed`.
- `Shipping`.
- `Completed`.
- `Cancelled`.

### Acceptance Criteria

- Đơn hàng mới tạo có trạng thái `Pending`.
- Admin được chuyển `Pending` sang `Confirmed`.
- Admin được chuyển `Confirmed` sang `Shipping`.
- Admin được chuyển `Shipping` sang `Completed`.
- Đơn `Completed` không được chuyển trạng thái khác.
- Đơn `Cancelled` không được chuyển trạng thái khác.

---

# 8.9. Admin Customer Module

## PRD-F23: Quản lý khách hàng

### Mô tả

Admin có thể xem danh sách tài khoản Customer và khóa hoặc mở khóa tài khoản.

### Actor

Admin.

### Dữ liệu hiển thị

- Họ tên.
- Email.
- Số điện thoại.
- Trạng thái tài khoản.
- Ngày tạo.

### Acceptance Criteria

- Admin không được xem mật khẩu người dùng.
- Tài khoản bị khóa không thể đăng nhập.
- Dữ liệu đơn hàng cũ của khách hàng vẫn được giữ lại.
- Admin có thể mở khóa tài khoản.

---

# 8.10. Admin Dashboard and Report Module

## PRD-F24: Dashboard Admin

### Mô tả

Dashboard hiển thị tổng quan hoạt động kinh doanh.

### Actor

Admin.

### Dữ liệu hiển thị

- Tổng số sản phẩm.
- Tổng số khách hàng.
- Tổng số đơn hàng.
- Tổng doanh thu.
- Số đơn Pending.
- Số đơn Completed.
- Số đơn Cancelled.
- Sản phẩm sắp hết hàng.

### Acceptance Criteria

- Chỉ Admin được truy cập Dashboard.
- Dữ liệu lấy từ database.
- Doanh thu chỉ tính đơn Completed.
- Sản phẩm sắp hết hàng là sản phẩm có tồn kho dưới 5.

---

## PRD-F25: Báo cáo doanh thu

### Mô tả

Admin có thể xem doanh thu theo ngày, tháng hoặc khoảng thời gian.

### Actor

Admin.

### Acceptance Criteria

- Chỉ đơn Completed được tính vào doanh thu.
- Đơn Cancelled không tính doanh thu.
- Báo cáo hiển thị tổng tiền chính xác.
- Có thể xem số lượng đơn hàng hoàn tất trong khoảng thời gian.

---

## 9. Yêu cầu giao diện người dùng

## 9.1. Nguyên tắc thiết kế

Giao diện cần theo phong cách tối giản, hiện đại và dễ sử dụng.

Yêu cầu:

- Màu sắc nhẹ nhàng.
- Bố cục rõ ràng.
- Hạn chế chi tiết thừa.
- Ưu tiên hình ảnh sản phẩm.
- Nút bấm dễ nhìn.
- Form nhập liệu dễ hiểu.
- Thông báo lỗi rõ ràng.

---

## 9.2. Giao diện phía khách hàng

Các màn hình cần có:

- Home Page.
- Product List Page.
- Product Detail Page.
- Login Page.
- Register Page.
- Cart Page.
- Checkout Page.
- Order History Page.
- Order Detail Page.
- Profile Page.

---

## 9.3. Giao diện phía Admin

Các màn hình cần có:

- Admin Dashboard.
- Category Management Page.
- Product Management Page.
- Product Form Page.
- Variant Management Page.
- Inventory Management Page.
- Order Management Page.
- Order Detail Page.
- Customer Management Page.
- Revenue Report Page.

---

## 10. Yêu cầu dữ liệu

## 10.1. Entity chính

Hệ thống cần có các entity chính sau:

- User.
- Category.
- Product.
- ProductVariant.
- Cart.
- CartItem.
- Order.
- OrderDetail.
- Payment.

---

## 10.2. Mô tả entity

### User

Lưu thông tin người dùng.

Trường dự kiến:

- id.
- fullName.
- email.
- password.
- phone.
- role.
- status.
- createdAt.
- updatedAt.

### Category

Lưu danh mục sản phẩm.

Trường dự kiến:

- id.
- name.
- description.
- status.
- createdAt.
- updatedAt.

### Product

Lưu thông tin sản phẩm.

Trường dự kiến:

- id.
- name.
- description.
- price.
- imageUrl.
- status.
- categoryId.
- createdAt.
- updatedAt.

### ProductVariant

Lưu biến thể sản phẩm theo size và màu.

Trường dự kiến:

- id.
- productId.
- size.
- color.
- sku.
- stockQuantity.
- status.
- createdAt.
- updatedAt.

### Cart

Lưu giỏ hàng của Customer.

Trường dự kiến:

- id.
- userId.
- createdAt.
- updatedAt.

### CartItem

Lưu từng sản phẩm trong giỏ hàng.

Trường dự kiến:

- id.
- cartId.
- productVariantId.
- quantity.
- createdAt.
- updatedAt.

### Order

Lưu đơn hàng.

Trường dự kiến:

- id.
- userId.
- receiverName.
- receiverPhone.
- shippingAddress.
- totalAmount.
- orderStatus.
- paymentStatus.
- paymentMethod.
- note.
- createdAt.
- updatedAt.

### OrderDetail

Lưu chi tiết đơn hàng.

Trường dự kiến:

- id.
- orderId.
- productVariantId.
- productName.
- size.
- color.
- unitPrice.
- quantity.
- subtotal.

### Payment

Lưu thông tin thanh toán.

Trường dự kiến:

- id.
- orderId.
- paymentMethod.
- amount.
- paymentStatus.
- transactionCode.
- paidAt.
- createdAt.

---

## 10.3. Quan hệ dữ liệu chính

- Một Category có nhiều Product.
- Một Product có nhiều ProductVariant.
- Một User có nhiều Order.
- Một Cart có nhiều CartItem.
- Một Order có nhiều OrderDetail.
- Một ProductVariant có thể xuất hiện trong nhiều CartItem.
- Một ProductVariant có thể xuất hiện trong nhiều OrderDetail.
- Một Order có thể có một Payment.

---

## 11. Business Rules

## 11.1. Product Rules

- Sản phẩm phải thuộc một danh mục.
- Sản phẩm phải có giá lớn hơn 0.
- Sản phẩm inactive không hiển thị cho khách hàng.
- Sản phẩm cần có ít nhất một biến thể active để có thể mua.

---

## 11.2. Variant Rules

- Một biến thể được xác định bằng Product, Size và Color.
- Không được trùng biến thể trong cùng một sản phẩm.
- SKU phải là duy nhất.
- Tồn kho không được âm.

---

## 11.3. Cart Rules

- Chỉ Customer mới có giỏ hàng.
- Mỗi CartItem tương ứng với một ProductVariant.
- Số lượng trong CartItem không được vượt quá tồn kho.
- Nếu thêm cùng một biến thể, hệ thống cộng dồn số lượng.

---

## 11.4. Order Rules

- Customer phải đăng nhập mới được đặt hàng.
- Đơn hàng phải có ít nhất một sản phẩm.
- Đơn hàng mới tạo có trạng thái `Pending`.
- Giá trong OrderDetail là giá tại thời điểm mua.
- Không được chỉnh sửa OrderDetail sau khi đơn hoàn tất.

---

## 11.5. Inventory Rules

- Tồn kho giảm khi đặt hàng thành công.
- Tồn kho tăng lại khi đơn bị hủy.
- Không cho phép đặt hàng nếu tồn kho không đủ.
- Admin có thể cập nhật tồn kho thủ công.

---

## 11.6. Revenue Rules

- Chỉ đơn `Completed` được tính doanh thu.
- Đơn `Cancelled` không tính doanh thu.
- Doanh thu dựa trên `totalAmount` của Order.
- Việc cập nhật giá sản phẩm không làm thay đổi doanh thu quá khứ.

---

## 12. Yêu cầu bảo mật

## 12.1. Authentication

- Người dùng phải đăng nhập bằng email và mật khẩu.
- Mật khẩu phải được mã hóa.
- Phiên đăng nhập được quản lý bởi Spring Security.

---

## 12.2. Authorization

- Guest chỉ xem được các trang công khai.
- Customer được truy cập giỏ hàng, checkout, lịch sử đơn hàng.
- Admin được truy cập trang quản trị.
- Customer không được truy cập URL của Admin.
- Customer không được xem đơn hàng của Customer khác.

---

## 12.3. Validation

- Email đúng định dạng.
- Số điện thoại hợp lệ.
- Giá sản phẩm lớn hơn 0.
- Tồn kho không âm.
- Số lượng đặt hàng không vượt tồn kho.
- Không để trống các trường bắt buộc.

---

## 13. Yêu cầu phi chức năng

## 13.1. Performance

- Trang sản phẩm cần có phân trang.
- Tìm kiếm và lọc sản phẩm cần phản hồi trong thời gian hợp lý.
- Không load toàn bộ dữ liệu lớn cùng lúc.
- Các truy vấn thống kê cần tối ưu để tránh chậm dashboard.

---

## 13.2. Reliability

- Hệ thống không được tạo đơn hàng nếu tồn kho không đủ.
- Hệ thống không được để tồn kho âm.
- Hệ thống phải lưu đúng OrderDetail.
- Hệ thống cần giữ lịch sử đơn hàng kể cả khi sản phẩm bị inactive.

---

## 13.3. Usability

- Giao diện dễ dùng.
- Người dùng mới có thể mua hàng mà không cần hướng dẫn nhiều.
- Form nhập liệu có thông báo lỗi rõ ràng.
- Các nút chính như Add to Cart, Checkout, Confirm cần dễ thấy.

---

## 13.4. Maintainability

- Code chia theo Controller, Service, Repository, Entity.
- Business logic nằm trong Service.
- Controller không chứa logic phức tạp.
- Entity mapping rõ ràng.
- Tên class và method dễ hiểu.

---

## 13.5. Compatibility

- Hệ thống chạy trên Java 17.
- Hệ thống tương thích với Microsoft SQL Server.
- Giao diện hiển thị tốt trên Chrome, Edge và Firefox.
- Giao diện responsive trên desktop và mobile.

---

## 14. MVP – Minimum Viable Product

## 14.1. MVP bắt buộc

### Guest

- Xem trang chủ.
- Xem danh sách sản phẩm.
- Tìm kiếm sản phẩm.
- Lọc sản phẩm.
- Xem chi tiết sản phẩm.
- Đăng ký.
- Đăng nhập.

### Customer

- Thêm sản phẩm vào giỏ hàng.
- Xem giỏ hàng.
- Cập nhật giỏ hàng.
- Checkout.
- Tạo đơn hàng.
- Xem lịch sử đơn hàng.
- Xem chi tiết đơn hàng.

### Admin

- CRUD danh mục.
- CRUD sản phẩm.
- CRUD biến thể.
- Cập nhật tồn kho.
- Xem đơn hàng.
- Cập nhật trạng thái đơn hàng.

---

## 14.2. Chức năng sau MVP

Nếu còn thời gian, nhóm có thể thêm:

- Dashboard thống kê.
- Báo cáo doanh thu.
- VNPay Sandbox.
- Hủy đơn hàng.
- Quản lý khách hàng.
- Sản phẩm liên quan.
- Sản phẩm bán chạy.
- Wishlist.
- Đánh giá sản phẩm.

---

## 15. Product Backlog

| Mã | Chức năng | Độ ưu tiên | Actor |
|---|---|---|---|
| PB-01 | Đăng ký tài khoản | Must Have | Guest |
| PB-02 | Đăng nhập / Đăng xuất | Must Have | Guest, Customer, Admin |
| PB-03 | Phân quyền role | Must Have | System |
| PB-04 | Xem danh sách sản phẩm | Must Have | Guest, Customer |
| PB-05 | Tìm kiếm sản phẩm | Must Have | Guest, Customer |
| PB-06 | Lọc sản phẩm | Must Have | Guest, Customer |
| PB-07 | Xem chi tiết sản phẩm | Must Have | Guest, Customer |
| PB-08 | Chọn size và màu | Must Have | Customer |
| PB-09 | Thêm vào giỏ hàng | Must Have | Customer |
| PB-10 | Cập nhật giỏ hàng | Must Have | Customer |
| PB-11 | Checkout | Must Have | Customer |
| PB-12 | Tạo đơn hàng | Must Have | Customer |
| PB-13 | Xem lịch sử đơn hàng | Must Have | Customer |
| PB-14 | CRUD danh mục | Must Have | Admin |
| PB-15 | CRUD sản phẩm | Must Have | Admin |
| PB-16 | CRUD biến thể | Must Have | Admin |
| PB-17 | Cập nhật tồn kho | Must Have | Admin |
| PB-18 | Quản lý đơn hàng | Must Have | Admin |
| PB-19 | Dashboard Admin | Should Have | Admin |
| PB-20 | Báo cáo doanh thu | Should Have | Admin |
| PB-21 | Quản lý khách hàng | Should Have | Admin |
| PB-22 | VNPay Sandbox | Could Have | Customer |
| PB-23 | Wishlist | Could Have | Customer |
| PB-24 | Đánh giá sản phẩm | Could Have | Customer |

---

## 16. Roadmap phát triển

## 16.1. Giai đoạn 1: Setup và thiết kế

**Thời gian:** Tuần 1–2.

### Công việc

- Phân tích yêu cầu.
- Thiết kế ERD.
- Thiết kế giao diện cơ bản.
- Setup project Spring MVC.
- Cấu hình SQL Server.
- Tạo cấu trúc package.
- Tạo entity và repository cơ bản.

### Kết quả

- Project chạy được.
- Database kết nối thành công.
- Có entity chính.
- Có ERD.

---

## 16.2. Giai đoạn 2: Admin CRUD

**Thời gian:** Tuần 3–4.

### Công việc

- CRUD Category.
- CRUD Product.
- CRUD ProductVariant.
- Cập nhật tồn kho.
- Upload hoặc lưu URL ảnh sản phẩm.

### Kết quả

- Admin có thể quản lý sản phẩm.
- Admin có thể tạo biến thể theo size và màu.
- Admin có thể cập nhật tồn kho.

---

## 16.3. Giai đoạn 3: Customer UI và Product Browsing

**Thời gian:** Tuần 5.

### Công việc

- Trang chủ.
- Trang danh sách sản phẩm.
- Tìm kiếm sản phẩm.
- Lọc sản phẩm.
- Trang chi tiết sản phẩm.

### Kết quả

- Guest và Customer xem được sản phẩm.
- Người dùng tìm và lọc được sản phẩm.

---

## 16.4. Giai đoạn 4: Security, Cart và Checkout

**Thời gian:** Tuần 6.

### Công việc

- Đăng ký.
- Đăng nhập.
- Phân quyền.
- Giỏ hàng.
- Checkout.
- Tạo đơn hàng.
- Kiểm tra tồn kho.

### Kết quả

- Customer có thể mua hàng.
- Admin và Customer được phân quyền rõ ràng.

---

## 16.5. Giai đoạn 5: Order Management và Payment

**Thời gian:** Tuần 7.

### Công việc

- Admin quản lý đơn hàng.
- Cập nhật trạng thái đơn hàng.
- Customer xem lịch sử đơn hàng.
- Thanh toán mockup hoặc VNPay Sandbox.

### Kết quả

- Quy trình đặt hàng hoàn chỉnh.
- Đơn hàng có trạng thái rõ ràng.

---

## 16.6. Giai đoạn 6: Dashboard, Testing và Finalize

**Thời gian:** Tuần 8.

### Công việc

- Dashboard Admin.
- Báo cáo doanh thu.
- Kiểm thử chức năng.
- Sửa lỗi.
- Hoàn thiện tài liệu.
- Chuẩn bị demo.

### Kết quả

- Sản phẩm hoàn chỉnh.
- Có tài liệu và dữ liệu mẫu để demo.

---

## 17. Test Scenarios chính

## TS-01: Đăng ký tài khoản

### Steps

1. Guest mở trang đăng ký.
2. Guest nhập thông tin hợp lệ.
3. Guest bấm đăng ký.

### Expected Result

- Hệ thống tạo tài khoản Customer.
- Người dùng có thể đăng nhập bằng tài khoản mới.

---

## TS-02: Đăng nhập sai mật khẩu

### Steps

1. User mở trang đăng nhập.
2. User nhập email đúng nhưng mật khẩu sai.
3. User bấm đăng nhập.

### Expected Result

- Hệ thống báo lỗi.
- User không được đăng nhập.

---

## TS-03: Customer thêm sản phẩm vào giỏ

### Steps

1. Customer đăng nhập.
2. Customer chọn sản phẩm.
3. Customer chọn size và màu.
4. Customer nhập số lượng hợp lệ.
5. Customer bấm thêm vào giỏ.

### Expected Result

- Hệ thống thêm sản phẩm vào giỏ thành công.
- Tổng số lượng giỏ hàng được cập nhật.

---

## TS-04: Customer đặt hàng

### Steps

1. Customer có sản phẩm trong giỏ.
2. Customer vào trang checkout.
3. Customer nhập thông tin giao hàng.
4. Customer xác nhận đặt hàng.

### Expected Result

- Hệ thống tạo đơn hàng.
- Hệ thống trừ tồn kho.
- Giỏ hàng được làm trống.
- Đơn hàng có trạng thái `Pending`.

---

## TS-05: Đặt hàng vượt tồn kho

### Steps

1. Customer chọn sản phẩm.
2. Customer nhập số lượng lớn hơn tồn kho.
3. Customer bấm thêm vào giỏ hoặc đặt hàng.

### Expected Result

- Hệ thống không cho đặt hàng.
- Hệ thống hiển thị thông báo lỗi.

---

## TS-06: Admin tạo sản phẩm

### Steps

1. Admin đăng nhập.
2. Admin vào trang quản lý sản phẩm.
3. Admin nhập thông tin sản phẩm hợp lệ.
4. Admin lưu sản phẩm.

### Expected Result

- Hệ thống lưu sản phẩm thành công.
- Sản phẩm có thể được gán biến thể.

---

## TS-07: Admin tạo biến thể trùng

### Steps

1. Admin chọn một sản phẩm.
2. Admin tạo biến thể có cùng Product, Size và Color với biến thể đã tồn tại.

### Expected Result

- Hệ thống báo lỗi.
- Biến thể không được tạo.

---

## TS-08: Admin cập nhật trạng thái đơn hàng

### Steps

1. Admin mở trang quản lý đơn hàng.
2. Admin chọn đơn hàng trạng thái `Pending`.
3. Admin chuyển trạng thái sang `Confirmed`.

### Expected Result

- Hệ thống cập nhật thành công.
- Customer xem được trạng thái mới.

---

## TS-09: Customer truy cập trang Admin

### Steps

1. Customer đăng nhập.
2. Customer nhập URL trang Admin.

### Expected Result

- Hệ thống từ chối truy cập.

---

## TS-10: Doanh thu chỉ tính đơn Completed

### Steps

1. Hệ thống có một đơn `Completed`.
2. Hệ thống có một đơn `Cancelled`.
3. Admin mở dashboard doanh thu.

### Expected Result

- Dashboard chỉ tính doanh thu từ đơn `Completed`.

---

## 18. Ràng buộc kỹ thuật

- Project sử dụng Java 17.
- Framework chính là Spring MVC.
- ORM sử dụng Hibernate.
- Repository sử dụng Spring Data JPA.
- Database sử dụng Microsoft SQL Server.
- Security sử dụng Spring Security.
- View sử dụng Thymeleaf.
- Giao diện sử dụng Bootstrap 5.
- Build tool sử dụng Maven.
- Source code quản lý bằng GitHub.

---

## 19. Tiêu chí hoàn thành sản phẩm

Sản phẩm được xem là hoàn thành khi:

- Chạy được trên môi trường local.
- Kết nối được SQL Server.
- Có dữ liệu mẫu để demo.
- Guest xem và tìm kiếm sản phẩm được.
- Customer đăng ký, đăng nhập, thêm giỏ hàng và đặt hàng được.
- Admin quản lý được danh mục, sản phẩm, biến thể và đơn hàng.
- Hệ thống kiểm tra tồn kho chính xác.
- Hệ thống phân quyền đúng.
- Giao diện hiển thị tốt trên desktop và mobile.
- Có tài liệu BRD, PRD, ERD và hướng dẫn chạy project.
- Nhóm có thể demo luồng mua hàng hoàn chỉnh.

---

## 20. Kết luận

U-Minimalist là một sản phẩm thương mại điện tử thời trang tối giản, tập trung vào hai nhóm người dùng chính là Customer và Admin.

Customer cần trải nghiệm mua hàng rõ ràng, nhanh chóng và chính xác khi chọn sản phẩm theo size và màu. Admin cần công cụ quản lý sản phẩm, biến thể, tồn kho và đơn hàng một cách hiệu quả.

Tài liệu PRODUCT.md này đóng vai trò là tài liệu yêu cầu sản phẩm chính, giúp nhóm phát triển hiểu rõ mục tiêu, phạm vi, chức năng, luồng nghiệp vụ, yêu cầu dữ liệu, business rules và tiêu chí hoàn thành của dự án.
