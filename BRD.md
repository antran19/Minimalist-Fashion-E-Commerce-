# BUSINESS REQUIREMENTS DOCUMENT

## U-Minimalist – Hệ thống Thương mại Điện tử Thời trang Tối giản

**Môn học:** HSF301 – Java Frameworks
**Tên đề tài:** U-Minimalist – Hệ thống Thương mại Điện tử Thời trang Tối giản
**Sinh viên thực hiện:** Nhóm 2
**Giảng viên hướng dẫn:** Nguyễn Ngọc Lâm
**Ngày gửi:** 16/05/2026
**Phiên bản tài liệu:** 1.0

---

# 1. Tổng quan dự án

## 1.1. Giới thiệu dự án

U-Minimalist là một hệ thống thương mại điện tử chuyên bán sản phẩm thời trang theo phong cách tối giản, lấy cảm hứng từ triết lý “LifeWear” của Uniqlo: đơn giản, tiện dụng, chất lượng và phù hợp với nhu cầu sử dụng hằng ngày.

Dự án tập trung xây dựng một website mua sắm quần áo trực tuyến cho phép khách hàng xem sản phẩm, lọc sản phẩm theo nhu cầu, chọn đúng biến thể như màu sắc và kích thước, thêm vào giỏ hàng, đặt hàng và theo dõi trạng thái đơn hàng.

Bên cạnh chức năng mua sắm cho khách hàng, hệ thống còn hỗ trợ quản trị viên quản lý danh mục, sản phẩm, biến thể sản phẩm, tồn kho, đơn hàng và doanh thu. Đây là phần quan trọng giúp dự án thể hiện rõ việc áp dụng Spring MVC, Hibernate ORM, Spring Data JPA và Spring Security trong môn HSF301.

---

# 2. Mục tiêu kinh doanh

## 2.1. Mục tiêu chính

Mục tiêu của dự án là xây dựng một nền tảng thương mại điện tử thời trang tối giản có khả năng:

* Hỗ trợ khách hàng mua sắm quần áo trực tuyến một cách nhanh chóng, dễ hiểu và thuận tiện.
* Cho phép khách hàng lựa chọn chính xác sản phẩm theo size, màu sắc và số lượng.
* Giúp quản trị viên quản lý sản phẩm, danh mục, biến thể và tồn kho một cách chi tiết.
* Hỗ trợ quản lý đơn hàng từ lúc khách đặt hàng đến khi đơn hàng hoàn tất.
* Cung cấp thống kê doanh thu cơ bản để hỗ trợ việc theo dõi tình hình kinh doanh.
* Đảm bảo hệ thống có phân quyền rõ ràng giữa Guest, Customer và Admin.

## 2.2. Mục tiêu học thuật

Vì đây là dự án thuộc môn HSF301 – Java Frameworks, hệ thống cần thể hiện được các kỹ năng sau:

* Xây dựng ứng dụng web bằng Java 17 và Spring MVC.
* Sử dụng Hibernate ORM và Spring Data JPA để thao tác với cơ sở dữ liệu.
* Thiết kế quan hệ dữ liệu phù hợp với mô hình thương mại điện tử.
* Áp dụng Spring Security để đăng nhập, đăng ký và phân quyền người dùng.
* Sử dụng Thymeleaf để render giao diện phía server.
* Tổ chức code theo mô hình Controller – Service – Repository – Entity.
* Kết nối hệ thống với Microsoft SQL Server.
* Thực hiện kiểm thử các chức năng chính.

---

# 3. Vấn đề kinh doanh cần giải quyết

## 3.1. Vấn đề của khách hàng

Trong quá trình mua quần áo online, khách hàng thường gặp các vấn đề:

* Không biết sản phẩm còn size hoặc màu mình muốn hay không.
* Khó tìm sản phẩm phù hợp do thiếu bộ lọc rõ ràng.
* Không kiểm tra được giá, số lượng và tổng tiền trong giỏ hàng một cách minh bạch.
* Không biết trạng thái đơn hàng sau khi đặt.
* Giao diện mua sắm phức tạp, nhiều thông tin gây rối mắt.

## 3.2. Vấn đề của quản trị viên

Đối với người quản lý cửa hàng, các vấn đề thường gặp là:

* Khó quản lý số lượng tồn kho theo từng size và màu sắc.
* Khó cập nhật trạng thái đơn hàng thủ công nếu không có hệ thống rõ ràng.
* Khó kiểm soát danh mục, sản phẩm và biến thể sản phẩm.
* Thiếu báo cáo doanh thu cơ bản để theo dõi hoạt động kinh doanh.
* Dữ liệu dễ bị sai lệch nếu không có quy tắc kiểm tra tồn kho khi khách đặt hàng.

## 3.3. Giải pháp đề xuất

U-Minimalist giải quyết các vấn đề trên bằng cách xây dựng một hệ thống thương mại điện tử có:

* Trang danh sách sản phẩm rõ ràng, dễ lọc và dễ tìm kiếm.
* Chức năng chọn biến thể sản phẩm theo size và màu.
* Giỏ hàng có tính tổng tiền tự động.
* Quy trình đặt hàng minh bạch.
* Quản lý tồn kho chi tiết theo từng biến thể.
* Quản lý đơn hàng theo từng trạng thái.
* Phân quyền người dùng bằng Spring Security.
* Giao diện đơn giản, responsive bằng Thymeleaf và Bootstrap 5.

---

# 4. Phạm vi dự án

## 4.1. Phạm vi bao gồm

Hệ thống sẽ bao gồm các phân hệ chính sau:

### Phân hệ Guest

Guest là khách chưa đăng nhập. Guest có thể:

* Xem trang chủ.
* Xem danh sách sản phẩm.
* Xem chi tiết sản phẩm.
* Tìm kiếm sản phẩm theo tên.
* Lọc sản phẩm theo danh mục, size, màu sắc và khoảng giá.
* Đăng ký tài khoản.
* Đăng nhập vào hệ thống.

### Phân hệ Customer

Customer là khách hàng đã đăng nhập. Customer có thể:

* Quản lý thông tin cá nhân.
* Thêm sản phẩm vào giỏ hàng.
* Cập nhật số lượng sản phẩm trong giỏ hàng.
* Xóa sản phẩm khỏi giỏ hàng.
* Đặt hàng.
* Chọn phương thức thanh toán.
* Xem lịch sử đơn hàng.
* Theo dõi trạng thái đơn hàng.
* Hủy đơn hàng trong điều kiện cho phép.

### Phân hệ Admin

Admin là quản trị viên hệ thống. Admin có thể:

* Quản lý danh mục sản phẩm.
* Quản lý sản phẩm.
* Quản lý biến thể sản phẩm theo size và màu sắc.
* Quản lý tồn kho.
* Quản lý tài khoản khách hàng.
* Quản lý đơn hàng.
* Cập nhật trạng thái đơn hàng.
* Xem thống kê doanh thu.
* Xem số lượng đơn hàng theo trạng thái.

## 4.2. Phạm vi không bao gồm

Trong phiên bản đầu tiên, hệ thống chưa bắt buộc bao gồm:

* Ứng dụng mobile riêng.
* Chat real-time giữa khách hàng và shop.
* Gợi ý sản phẩm bằng AI.
* Tích hợp vận chuyển thật với đơn vị giao hàng.
* Thanh toán thật ngoài môi trường production.
* Hệ thống đánh giá sản phẩm nâng cao.
* Quản lý nhiều chi nhánh kho hàng.
* Tích hợp email marketing tự động.

---

# 5. Các bên liên quan

| Bên liên quan         | Vai trò                   | Mối quan tâm                               |
| --------------------- | ------------------------- | ------------------------------------------ |
| Guest                 | Người dùng chưa đăng nhập | Xem và tìm kiếm sản phẩm                   |
| Customer              | Người mua hàng            | Mua hàng, quản lý giỏ hàng, theo dõi đơn   |
| Admin                 | Quản trị viên             | Quản lý sản phẩm, kho, đơn hàng, doanh thu |
| Giảng viên hướng dẫn  | Người đánh giá dự án      | Kiểm tra mức độ đáp ứng yêu cầu môn học    |
| Nhóm phát triển       | Sinh viên thực hiện       | Phân tích, thiết kế, lập trình, kiểm thử   |
| Chủ cửa hàng giả định | Người vận hành kinh doanh | Quản lý hoạt động bán hàng online          |

---

# 6. Actor của hệ thống

## 6.1. Guest

Guest là người dùng chưa đăng nhập vào hệ thống.

### Quyền của Guest

* Xem sản phẩm.
* Xem chi tiết sản phẩm.
* Tìm kiếm sản phẩm.
* Lọc sản phẩm.
* Đăng ký tài khoản.
* Đăng nhập.

### Giới hạn của Guest

* Không được thêm sản phẩm vào giỏ hàng.
* Không được đặt hàng.
* Không được xem lịch sử đơn hàng.
* Không được truy cập trang quản trị.

---

## 6.2. Customer

Customer là người dùng đã có tài khoản và đăng nhập thành công.

### Quyền của Customer

* Thêm sản phẩm vào giỏ hàng.
* Cập nhật giỏ hàng.
* Đặt hàng.
* Thanh toán mockup hoặc VNPay Sandbox.
* Xem lịch sử đơn hàng.
* Xem trạng thái đơn hàng.
* Cập nhật thông tin cá nhân.

### Giới hạn của Customer

* Không được truy cập trang Admin.
* Không được chỉnh sửa sản phẩm.
* Không được chỉnh sửa tồn kho.
* Không được thay đổi trạng thái đơn hàng ngoài phạm vi cho phép.

---

## 6.3. Admin

Admin là quản trị viên có quyền cao nhất trong hệ thống.

### Quyền của Admin

* Quản lý danh mục.
* Quản lý sản phẩm.
* Quản lý biến thể sản phẩm.
* Quản lý tồn kho.
* Quản lý đơn hàng.
* Quản lý khách hàng.
* Xem báo cáo doanh thu.
* Cập nhật trạng thái đơn hàng.

### Giới hạn của Admin

* Không can thiệp trực tiếp vào mật khẩu người dùng.
* Không xóa dữ liệu đơn hàng đã hoàn tất nếu hệ thống cần giữ lịch sử giao dịch.

---

# 7. Quy trình nghiệp vụ tổng quan

## 7.1. Quy trình mua hàng của Customer

1. Customer truy cập website U-Minimalist.
2. Customer xem danh sách sản phẩm.
3. Customer lọc sản phẩm theo danh mục, size, màu sắc hoặc giá.
4. Customer chọn một sản phẩm cụ thể.
5. Hệ thống hiển thị chi tiết sản phẩm và các biến thể hiện có.
6. Customer chọn màu sắc, kích thước và số lượng.
7. Hệ thống kiểm tra số lượng tồn kho.
8. Customer thêm sản phẩm vào giỏ hàng.
9. Customer vào trang giỏ hàng.
10. Customer kiểm tra lại sản phẩm, số lượng và tổng tiền.
11. Customer tiến hành đặt hàng.
12. Hệ thống yêu cầu nhập thông tin giao hàng.
13. Customer chọn phương thức thanh toán.
14. Hệ thống tạo đơn hàng.
15. Hệ thống trừ tồn kho tương ứng.
16. Customer theo dõi trạng thái đơn hàng trong tài khoản.

## 7.2. Quy trình quản lý sản phẩm của Admin

1. Admin đăng nhập vào hệ thống.
2. Admin truy cập trang quản trị.
3. Admin chọn chức năng quản lý sản phẩm.
4. Admin thêm mới hoặc cập nhật sản phẩm.
5. Admin nhập thông tin sản phẩm gồm tên, mô tả, giá, danh mục, hình ảnh.
6. Admin tạo các biến thể sản phẩm theo size và màu sắc.
7. Admin nhập số lượng tồn kho cho từng biến thể.
8. Hệ thống lưu dữ liệu vào cơ sở dữ liệu.
9. Sản phẩm được hiển thị trên trang khách hàng nếu đang ở trạng thái active.

## 7.3. Quy trình xử lý đơn hàng của Admin

1. Customer đặt hàng thành công.
2. Hệ thống tạo đơn hàng với trạng thái Pending.
3. Admin truy cập danh sách đơn hàng.
4. Admin xem chi tiết đơn hàng.
5. Admin xác nhận đơn hàng.
6. Hệ thống cập nhật trạng thái thành Confirmed.
7. Admin tiếp tục cập nhật trạng thái thành Shipping khi đơn được giao.
8. Khi giao thành công, Admin cập nhật trạng thái Completed.
9. Nếu đơn bị hủy, Admin cập nhật trạng thái Cancelled theo quy định.

---

# 8. Yêu cầu chức năng

# 8.1. Module xác thực và phân quyền

## BR-F01: Đăng ký tài khoản

### Mô tả

Hệ thống cho phép người dùng đăng ký tài khoản khách hàng mới.

### Đầu vào

* Họ và tên.
* Email.
* Số điện thoại.
* Mật khẩu.
* Xác nhận mật khẩu.

### Xử lý

* Kiểm tra email chưa tồn tại.
* Kiểm tra mật khẩu và xác nhận mật khẩu trùng nhau.
* Mã hóa mật khẩu trước khi lưu.
* Gán role mặc định là Customer.
* Lưu thông tin người dùng vào database.

### Đầu ra

* Tài khoản Customer được tạo thành công.
* Hiển thị thông báo đăng ký thành công.

### Acceptance Criteria

* Người dùng không thể đăng ký bằng email đã tồn tại.
* Mật khẩu phải được mã hóa trong database.
* Tài khoản mới mặc định có role Customer.
* Sau khi đăng ký thành công, người dùng có thể đăng nhập.

---

## BR-F02: Đăng nhập

### Mô tả

Hệ thống cho phép Guest đăng nhập bằng email và mật khẩu.

### Đầu vào

* Email.
* Mật khẩu.

### Xử lý

* Xác thực thông tin đăng nhập bằng Spring Security.
* Kiểm tra trạng thái tài khoản.
* Điều hướng người dùng theo role.

### Đầu ra

* Customer được chuyển về trang chủ hoặc trang tài khoản.
* Admin được chuyển vào trang Admin Dashboard.

### Acceptance Criteria

* Sai email hoặc mật khẩu thì hiển thị thông báo lỗi.
* Customer không được vào trang Admin.
* Admin đăng nhập thành công thì được vào trang quản trị.
* Người dùng chưa đăng nhập không được đặt hàng.

---

## BR-F03: Đăng xuất

### Mô tả

Hệ thống cho phép người dùng đăng xuất khỏi phiên làm việc.

### Acceptance Criteria

* Sau khi đăng xuất, người dùng trở về trạng thái Guest.
* Người dùng không thể truy cập các trang yêu cầu đăng nhập nếu chưa đăng nhập lại.

---

# 8.2. Module sản phẩm phía khách hàng

## BR-F04: Xem danh sách sản phẩm

### Mô tả

Guest và Customer có thể xem danh sách sản phẩm đang được bán trên hệ thống.

### Dữ liệu hiển thị

* Hình ảnh sản phẩm.
* Tên sản phẩm.
* Giá bán.
* Danh mục.
* Màu sắc có sẵn.
* Size có sẵn.
* Trạng thái còn hàng hoặc hết hàng.

### Acceptance Criteria

* Chỉ hiển thị sản phẩm đang active.
* Sản phẩm hết hàng vẫn có thể hiển thị nhưng phải có nhãn “Out of stock”.
* Danh sách sản phẩm có phân trang.

---

## BR-F05: Tìm kiếm sản phẩm

### Mô tả

Người dùng có thể tìm kiếm sản phẩm theo tên hoặc từ khóa.

### Acceptance Criteria

* Tìm kiếm không phân biệt chữ hoa chữ thường.
* Nếu không tìm thấy sản phẩm, hệ thống hiển thị thông báo phù hợp.
* Kết quả tìm kiếm chỉ bao gồm sản phẩm active.

---

## BR-F06: Lọc sản phẩm

### Mô tả

Người dùng có thể lọc sản phẩm theo các tiêu chí:

* Danh mục.
* Giới tính hoặc bộ sưu tập: Nam, Nữ, Trẻ em.
* Size.
* Màu sắc.
* Khoảng giá.
* Trạng thái còn hàng.

### Acceptance Criteria

* Người dùng có thể kết hợp nhiều bộ lọc cùng lúc.
* Kết quả lọc phải đúng với tiêu chí đã chọn.
* Người dùng có thể xóa bộ lọc để quay lại danh sách đầy đủ.

---

## BR-F07: Xem chi tiết sản phẩm

### Mô tả

Người dùng có thể xem thông tin chi tiết của một sản phẩm.

### Dữ liệu hiển thị

* Tên sản phẩm.
* Mô tả.
* Giá.
* Hình ảnh.
* Danh mục.
* Danh sách màu sắc.
* Danh sách size.
* Số lượng tồn kho theo biến thể.
* Sản phẩm liên quan.

### Acceptance Criteria

* Người dùng phải chọn size và màu trước khi thêm vào giỏ hàng.
* Nếu biến thể hết hàng, nút thêm vào giỏ phải bị vô hiệu hóa.
* Giá sản phẩm hiển thị rõ ràng.

---

# 8.3. Module giỏ hàng

## BR-F08: Thêm sản phẩm vào giỏ hàng

### Mô tả

Customer có thể thêm sản phẩm vào giỏ hàng sau khi chọn biến thể.

### Đầu vào

* Product Variant ID.
* Size.
* Màu sắc.
* Số lượng.

### Xử lý

* Kiểm tra người dùng đã đăng nhập.
* Kiểm tra biến thể có tồn tại.
* Kiểm tra số lượng tồn kho.
* Nếu sản phẩm đã có trong giỏ, cộng dồn số lượng.
* Nếu chưa có, thêm dòng mới vào giỏ hàng.

### Acceptance Criteria

* Không thể thêm sản phẩm nếu chưa chọn size hoặc màu.
* Không thể thêm số lượng lớn hơn tồn kho.
* Không thể thêm sản phẩm hết hàng.
* Giỏ hàng cập nhật đúng tổng số lượng.

---

## BR-F09: Xem giỏ hàng

### Mô tả

Customer có thể xem toàn bộ sản phẩm đã thêm vào giỏ.

### Dữ liệu hiển thị

* Tên sản phẩm.
* Hình ảnh.
* Size.
* Màu sắc.
* Đơn giá.
* Số lượng.
* Thành tiền.
* Tổng tiền.

### Acceptance Criteria

* Tổng tiền phải được tính chính xác.
* Mỗi dòng trong giỏ hàng tương ứng với một biến thể sản phẩm.
* Nếu sản phẩm đã hết hàng trong lúc khách xem giỏ, hệ thống phải cảnh báo.

---

## BR-F10: Cập nhật giỏ hàng

### Mô tả

Customer có thể tăng, giảm hoặc xóa sản phẩm khỏi giỏ hàng.

### Acceptance Criteria

* Số lượng không được nhỏ hơn 1.
* Số lượng không được vượt quá tồn kho.
* Khi xóa sản phẩm, tổng tiền phải cập nhật lại.
* Giỏ hàng rỗng phải hiển thị thông báo phù hợp.

---

# 8.4. Module đặt hàng và thanh toán

## BR-F11: Tạo đơn hàng

### Mô tả

Customer có thể tạo đơn hàng từ giỏ hàng.

### Đầu vào

* Danh sách sản phẩm trong giỏ.
* Họ tên người nhận.
* Số điện thoại.
* Địa chỉ giao hàng.
* Phương thức thanh toán.
* Ghi chú đơn hàng.

### Xử lý

* Kiểm tra giỏ hàng không rỗng.
* Kiểm tra tồn kho từng biến thể.
* Tạo Order.
* Tạo OrderDetail cho từng sản phẩm.
* Lưu giá sản phẩm tại thời điểm mua.
* Trừ tồn kho sau khi đơn hàng được tạo.
* Xóa giỏ hàng sau khi đặt hàng thành công.

### Đầu ra

* Đơn hàng mới được tạo.
* Trạng thái mặc định là Pending.
* Customer nhận thông báo đặt hàng thành công.

### Acceptance Criteria

* Không thể đặt hàng nếu giỏ hàng rỗng.
* Không thể đặt hàng nếu có sản phẩm vượt quá tồn kho.
* Mỗi OrderDetail phải lưu lại giá tại thời điểm mua.
* Sau khi đặt hàng, tồn kho phải giảm đúng số lượng.

---

## BR-F12: Thanh toán

### Mô tả

Hệ thống hỗ trợ thanh toán theo một trong hai hướng:

* Mockup Payment.
* VNPay Sandbox.

### Quy trình Mockup Payment

1. Customer chọn thanh toán.
2. Hệ thống hiển thị màn hình xác nhận thanh toán giả lập.
3. Customer xác nhận thanh toán.
4. Hệ thống cập nhật trạng thái thanh toán.

### Acceptance Criteria

* Đơn hàng có thể được tạo với trạng thái thanh toán Pending hoặc Paid.
* Nếu thanh toán thành công, payment status là Paid.
* Nếu thanh toán thất bại, hệ thống không hoàn tất đơn hàng hoặc giữ trạng thái Payment Failed.
* Hệ thống ghi nhận phương thức thanh toán.

---

## BR-F13: Xem lịch sử đơn hàng

### Mô tả

Customer có thể xem danh sách các đơn hàng đã đặt.

### Dữ liệu hiển thị

* Mã đơn hàng.
* Ngày đặt.
* Tổng tiền.
* Trạng thái đơn hàng.
* Trạng thái thanh toán.
* Nút xem chi tiết.

### Acceptance Criteria

* Customer chỉ xem được đơn hàng của chính mình.
* Đơn hàng mới nhất hiển thị trước.
* Có thể xem chi tiết từng đơn hàng.

---

## BR-F14: Xem chi tiết đơn hàng

### Mô tả

Customer có thể xem chi tiết một đơn hàng cụ thể.

### Dữ liệu hiển thị

* Thông tin người nhận.
* Địa chỉ giao hàng.
* Danh sách sản phẩm.
* Size, màu sắc, số lượng.
* Đơn giá tại thời điểm mua.
* Tổng tiền.
* Trạng thái đơn hàng.
* Trạng thái thanh toán.

### Acceptance Criteria

* Customer không thể xem đơn hàng của người khác.
* Dữ liệu sản phẩm trong đơn hàng không bị thay đổi dù Admin cập nhật giá sản phẩm sau đó.

---

## BR-F15: Hủy đơn hàng

### Mô tả

Customer có thể hủy đơn hàng khi đơn hàng vẫn ở trạng thái Pending.

### Xử lý

* Kiểm tra đơn hàng thuộc về Customer.
* Kiểm tra trạng thái đơn hàng.
* Cập nhật trạng thái thành Cancelled.
* Hoàn lại số lượng tồn kho.

### Acceptance Criteria

* Chỉ đơn hàng Pending mới được hủy bởi Customer.
* Đơn đã Confirmed, Shipping hoặc Completed không thể hủy bởi Customer.
* Khi hủy đơn, tồn kho phải được cộng lại chính xác.

---

# 8.5. Module quản trị danh mục

## BR-F16: Xem danh sách danh mục

### Mô tả

Admin có thể xem danh sách các danh mục sản phẩm.

### Acceptance Criteria

* Danh sách hiển thị tên danh mục, mô tả, trạng thái.
* Admin có thể tìm kiếm danh mục theo tên.

---

## BR-F17: Thêm danh mục

### Mô tả

Admin có thể thêm danh mục mới.

### Đầu vào

* Tên danh mục.
* Mô tả.
* Trạng thái.

### Acceptance Criteria

* Tên danh mục không được rỗng.
* Tên danh mục không được trùng.
* Danh mục mới có thể được gán cho sản phẩm.

---

## BR-F18: Cập nhật danh mục

### Mô tả

Admin có thể cập nhật thông tin danh mục.

### Acceptance Criteria

* Không được cập nhật thành tên trùng với danh mục khác.
* Sản phẩm thuộc danh mục đó vẫn được giữ nguyên quan hệ.

---

## BR-F19: Xóa hoặc vô hiệu hóa danh mục

### Mô tả

Admin có thể xóa mềm hoặc vô hiệu hóa danh mục.

### Acceptance Criteria

* Không nên xóa cứng danh mục nếu đang có sản phẩm liên kết.
* Danh mục inactive không hiển thị ở trang khách hàng.
* Dữ liệu lịch sử đơn hàng không bị ảnh hưởng.

---

# 8.6. Module quản trị sản phẩm

## BR-F20: Xem danh sách sản phẩm Admin

### Mô tả

Admin có thể xem toàn bộ sản phẩm, bao gồm cả sản phẩm active và inactive.

### Dữ liệu hiển thị

* Mã sản phẩm.
* Tên sản phẩm.
* Danh mục.
* Giá.
* Trạng thái.
* Tổng tồn kho.
* Ngày tạo.
* Ngày cập nhật.

### Acceptance Criteria

* Admin có thể tìm kiếm sản phẩm theo tên.
* Admin có thể lọc theo danh mục và trạng thái.
* Danh sách có phân trang.

---

## BR-F21: Thêm sản phẩm

### Mô tả

Admin có thể thêm sản phẩm mới.

### Đầu vào

* Tên sản phẩm.
* Mô tả.
* Giá bán.
* Danh mục.
* Hình ảnh.
* Trạng thái.

### Acceptance Criteria

* Tên sản phẩm không được rỗng.
* Giá sản phẩm phải lớn hơn 0.
* Sản phẩm phải thuộc một danh mục hợp lệ.
* Sản phẩm mới cần có ít nhất một biến thể để khách có thể mua.

---

## BR-F22: Cập nhật sản phẩm

### Mô tả

Admin có thể cập nhật thông tin sản phẩm.

### Acceptance Criteria

* Cập nhật giá sản phẩm không làm thay đổi giá trong các đơn hàng cũ.
* Cập nhật trạng thái inactive làm sản phẩm không hiển thị ở trang khách hàng.
* Không làm mất dữ liệu biến thể đang tồn tại.

---

## BR-F23: Vô hiệu hóa sản phẩm

### Mô tả

Admin có thể vô hiệu hóa sản phẩm thay vì xóa cứng.

### Acceptance Criteria

* Sản phẩm inactive không được hiển thị cho Guest và Customer.
* Sản phẩm inactive vẫn được giữ trong đơn hàng cũ.
* Admin vẫn có thể xem và kích hoạt lại sản phẩm.

---

# 8.7. Module biến thể sản phẩm và tồn kho

## BR-F24: Thêm biến thể sản phẩm

### Mô tả

Admin có thể thêm biến thể cho sản phẩm dựa trên size và màu sắc.

### Đầu vào

* Product ID.
* Size.
* Màu sắc.
* Số lượng tồn kho.
* SKU.
* Trạng thái.

### Ví dụ

Sản phẩm: Áo thun cổ tròn
Biến thể:

* Trắng - Size S - 20 chiếc.
* Trắng - Size M - 15 chiếc.
* Đen - Size L - 10 chiếc.

### Acceptance Criteria

* Một sản phẩm không được có hai biến thể trùng size và màu.
* Số lượng tồn kho không được âm.
* SKU không được trùng.
* Biến thể inactive không được chọn khi mua hàng.

---

## BR-F25: Cập nhật tồn kho

### Mô tả

Admin có thể cập nhật số lượng tồn kho cho từng biến thể.

### Acceptance Criteria

* Số lượng tồn kho phải là số nguyên không âm.
* Khi cập nhật tồn kho, hệ thống ghi nhận thời gian cập nhật.
* Khách hàng chỉ mua được số lượng nhỏ hơn hoặc bằng tồn kho hiện tại.

---

## BR-F26: Kiểm tra tồn kho khi đặt hàng

### Mô tả

Hệ thống phải kiểm tra tồn kho trước khi tạo đơn hàng.

### Acceptance Criteria

* Nếu tồn kho không đủ, hệ thống hiển thị thông báo lỗi.
* Nếu tồn kho đủ, hệ thống cho phép tạo đơn hàng.
* Sau khi tạo đơn hàng thành công, tồn kho giảm đúng số lượng đã mua.

---

# 8.8. Module quản lý đơn hàng Admin

## BR-F27: Xem danh sách đơn hàng

### Mô tả

Admin có thể xem toàn bộ đơn hàng trong hệ thống.

### Dữ liệu hiển thị

* Mã đơn hàng.
* Tên khách hàng.
* Ngày đặt.
* Tổng tiền.
* Trạng thái đơn hàng.
* Trạng thái thanh toán.

### Acceptance Criteria

* Admin có thể lọc đơn hàng theo trạng thái.
* Admin có thể tìm kiếm theo mã đơn hàng hoặc tên khách hàng.
* Đơn hàng mới nhất hiển thị trước.

---

## BR-F28: Xem chi tiết đơn hàng Admin

### Mô tả

Admin có thể xem chi tiết một đơn hàng.

### Acceptance Criteria

* Hiển thị đầy đủ thông tin khách hàng, sản phẩm, tổng tiền và địa chỉ giao hàng.
* Hiển thị rõ từng sản phẩm với size, màu sắc và số lượng.
* Hiển thị lịch sử trạng thái nếu có.

---

## BR-F29: Cập nhật trạng thái đơn hàng

### Mô tả

Admin có thể cập nhật trạng thái đơn hàng theo quy trình.

### Các trạng thái đơn hàng

* Pending.
* Confirmed.
* Shipping.
* Completed.
* Cancelled.

### Quy tắc chuyển trạng thái

* Pending → Confirmed.
* Confirmed → Shipping.
* Shipping → Completed.
* Pending → Cancelled.
* Confirmed → Cancelled.
* Completed không được chuyển về trạng thái khác.
* Cancelled không được chuyển về trạng thái khác.

### Acceptance Criteria

* Admin không được cập nhật trạng thái sai quy trình.
* Khi đơn hàng bị Cancelled, tồn kho được hoàn lại nếu trước đó đã bị trừ.
* Khi đơn hàng Completed, hệ thống ghi nhận vào doanh thu.

---

# 8.9. Module quản lý khách hàng

## BR-F30: Xem danh sách khách hàng

### Mô tả

Admin có thể xem danh sách tài khoản Customer.

### Dữ liệu hiển thị

* Tên khách hàng.
* Email.
* Số điện thoại.
* Trạng thái tài khoản.
* Ngày tạo tài khoản.

### Acceptance Criteria

* Admin có thể tìm kiếm khách hàng theo tên hoặc email.
* Admin có thể xem thông tin cơ bản của khách hàng.
* Admin không được xem mật khẩu người dùng.

---

## BR-F31: Khóa hoặc mở khóa tài khoản khách hàng

### Mô tả

Admin có thể vô hiệu hóa tài khoản khách hàng khi cần.

### Acceptance Criteria

* Tài khoản bị khóa không thể đăng nhập.
* Tài khoản bị khóa không bị xóa dữ liệu đơn hàng cũ.
* Admin có thể mở khóa lại tài khoản.

---

# 8.10. Module báo cáo và thống kê

## BR-F32: Thống kê doanh thu

### Mô tả

Admin có thể xem doanh thu theo ngày, tháng hoặc khoảng thời gian.

### Dữ liệu hiển thị

* Tổng doanh thu.
* Số đơn hàng hoàn tất.
* Số đơn hàng bị hủy.
* Sản phẩm bán chạy.
* Doanh thu theo thời gian.

### Acceptance Criteria

* Chỉ đơn hàng Completed mới được tính vào doanh thu.
* Đơn hàng Cancelled không được tính vào doanh thu.
* Dữ liệu thống kê phải khớp với dữ liệu đơn hàng.

---

## BR-F33: Dashboard Admin

### Mô tả

Admin Dashboard hiển thị tổng quan tình hình kinh doanh.

### Dữ liệu hiển thị

* Tổng số sản phẩm.
* Tổng số đơn hàng.
* Tổng số khách hàng.
* Tổng doanh thu.
* Số đơn Pending.
* Số đơn Completed.
* Sản phẩm sắp hết hàng.

### Acceptance Criteria

* Dashboard chỉ hiển thị cho Admin.
* Dữ liệu được lấy từ database.
* Sản phẩm sắp hết hàng là sản phẩm có tồn kho dưới ngưỡng cấu hình, ví dụ dưới 5 chiếc.

---

# 9. Yêu cầu phi chức năng

## 9.1. Bảo mật

* Hệ thống phải sử dụng Spring Security.
* Mật khẩu người dùng phải được mã hóa.
* Phân quyền rõ ràng giữa Guest, Customer và Admin.
* Người dùng chưa đăng nhập không được truy cập trang yêu cầu đăng nhập.
* Customer không được truy cập trang Admin.
* Hệ thống cần chống truy cập trái phép vào dữ liệu đơn hàng của người khác.

## 9.2. Hiệu năng

* Trang danh sách sản phẩm cần có phân trang.
* Các chức năng tìm kiếm và lọc sản phẩm phải phản hồi trong thời gian hợp lý.
* Truy vấn database cần tối ưu bằng Spring Data JPA.
* Không load toàn bộ sản phẩm nếu dữ liệu lớn.

## 9.3. Khả dụng

* Giao diện cần dễ sử dụng.
* Website cần responsive trên laptop, tablet và điện thoại.
* Các thông báo lỗi phải rõ ràng, dễ hiểu.
* Form nhập liệu cần có validation.

## 9.4. Khả năng bảo trì

* Code cần tổ chức theo mô hình Controller – Service – Repository – Entity.
* Tên class, method và biến cần rõ nghĩa.
* Logic nghiệp vụ nên đặt trong Service.
* Controller chỉ xử lý request và response.
* Cần tách riêng DTO, Entity và View Model nếu cần.

## 9.5. Tính toàn vẹn dữ liệu

* Không cho phép tồn kho âm.
* Không cho phép đơn hàng không có sản phẩm.
* Không cho phép OrderDetail không có Order.
* Không cho phép Product Variant không thuộc Product nào.
* Giá tại thời điểm mua phải được lưu trong OrderDetail.

---

# 10. Business Rules

## BR-R01: Quy tắc tài khoản

* Email là duy nhất trong hệ thống.
* Mỗi tài khoản có một role chính.
* Tài khoản Customer được tạo từ form đăng ký.
* Tài khoản Admin được tạo sẵn trong database hoặc do hệ thống khởi tạo.

## BR-R02: Quy tắc sản phẩm

* Mỗi sản phẩm phải thuộc một danh mục.
* Sản phẩm chỉ hiển thị cho khách hàng khi ở trạng thái active.
* Sản phẩm cần có ít nhất một biến thể để có thể được mua.
* Sản phẩm inactive không bị xóa khỏi đơn hàng cũ.

## BR-R03: Quy tắc biến thể

* Một biến thể được xác định bởi sản phẩm, màu sắc và size.
* Không được tạo hai biến thể trùng sản phẩm, size và màu.
* Số lượng tồn kho không được âm.
* SKU của biến thể phải là duy nhất.

## BR-R04: Quy tắc giỏ hàng

* Chỉ Customer mới có giỏ hàng.
* Mỗi dòng giỏ hàng tương ứng với một Product Variant.
* Số lượng trong giỏ không được vượt quá tồn kho.
* Khi sản phẩm trong giỏ bị hết hàng, hệ thống phải cảnh báo khách hàng.

## BR-R05: Quy tắc đặt hàng

* Customer phải đăng nhập mới được đặt hàng.
* Đơn hàng phải có ít nhất một sản phẩm.
* Mỗi đơn hàng phải có địa chỉ giao hàng.
* Khi đặt hàng thành công, hệ thống tạo Order và OrderDetail.
* Giá trong OrderDetail là giá tại thời điểm mua.

## BR-R06: Quy tắc tồn kho

* Tồn kho giảm khi đơn hàng được tạo thành công.
* Tồn kho được hoàn lại khi đơn hàng bị hủy.
* Không cho phép đặt hàng nếu tồn kho không đủ.
* Admin có thể cập nhật tồn kho thủ công.

## BR-R07: Quy tắc trạng thái đơn hàng

* Đơn hàng mới tạo có trạng thái Pending.
* Admin có thể xác nhận đơn hàng thành Confirmed.
* Đơn hàng Confirmed có thể chuyển sang Shipping.
* Đơn hàng Shipping có thể chuyển sang Completed.
* Đơn hàng Pending hoặc Confirmed có thể bị Cancelled.
* Đơn hàng Completed không được hủy.
* Đơn hàng Cancelled không được chuyển trạng thái khác.

## BR-R08: Quy tắc doanh thu

* Chỉ đơn Completed được tính vào doanh thu.
* Đơn Cancelled không được tính doanh thu.
* Doanh thu được tính dựa trên tổng tiền của đơn hàng.
* Doanh thu không bị ảnh hưởng khi Admin thay đổi giá sản phẩm sau này.

---

# 11. Thiết kế dữ liệu dự kiến

## 11.1. Các bảng chính

### User

Lưu thông tin người dùng.

Các trường dự kiến:

* id.
* fullName.
* email.
* password.
* phone.
* role.
* status.
* createdAt.
* updatedAt.

### Category

Lưu danh mục sản phẩm.

Các trường dự kiến:

* id.
* name.
* description.
* status.
* createdAt.
* updatedAt.

### Product

Lưu thông tin sản phẩm.

Các trường dự kiến:

* id.
* name.
* description.
* price.
* imageUrl.
* status.
* categoryId.
* createdAt.
* updatedAt.

### ProductVariant

Lưu biến thể sản phẩm theo size và màu.

Các trường dự kiến:

* id.
* productId.
* size.
* color.
* sku.
* stockQuantity.
* status.
* createdAt.
* updatedAt.

### Cart

Lưu giỏ hàng của Customer.

Các trường dự kiến:

* id.
* userId.
* createdAt.
* updatedAt.

### CartItem

Lưu từng sản phẩm trong giỏ hàng.

Các trường dự kiến:

* id.
* cartId.
* productVariantId.
* quantity.
* createdAt.
* updatedAt.

### Order

Lưu đơn hàng.

Các trường dự kiến:

* id.
* userId.
* receiverName.
* receiverPhone.
* shippingAddress.
* totalAmount.
* orderStatus.
* paymentStatus.
* paymentMethod.
* note.
* createdAt.
* updatedAt.

### OrderDetail

Lưu chi tiết đơn hàng.

Các trường dự kiến:

* id.
* orderId.
* productVariantId.
* productName.
* size.
* color.
* unitPrice.
* quantity.
* subtotal.

### Payment

Lưu thông tin thanh toán.

Các trường dự kiến:

* id.
* orderId.
* paymentMethod.
* amount.
* paymentStatus.
* transactionCode.
* paidAt.
* createdAt.

---

# 12. Quan hệ dữ liệu

## 12.1. Category – Product

Một Category có nhiều Product.

Quan hệ:

* Category: One-to-Many với Product.
* Product: Many-to-One với Category.

Ví dụ:

* Category “Men” có nhiều sản phẩm như áo thun, áo sơ mi, quần dài.

---

## 12.2. Product – ProductVariant

Một Product có nhiều ProductVariant.

Quan hệ:

* Product: One-to-Many với ProductVariant.
* ProductVariant: Many-to-One với Product.

Ví dụ:

Sản phẩm “Áo thun basic” có các biến thể:

* Trắng – Size S.
* Trắng – Size M.
* Đen – Size L.

---

## 12.3. User – Order

Một User có nhiều Order.

Quan hệ:

* User: One-to-Many với Order.
* Order: Many-to-One với User.

---

## 12.4. Order – OrderDetail

Một Order có nhiều OrderDetail.

Quan hệ:

* Order: One-to-Many với OrderDetail.
* OrderDetail: Many-to-One với Order.

---

## 12.5. ProductVariant – OrderDetail

Một ProductVariant có thể xuất hiện trong nhiều OrderDetail.

Quan hệ:

* ProductVariant: One-to-Many với OrderDetail.
* OrderDetail: Many-to-One với ProductVariant.

---

## 12.6. Cart – CartItem

Một Cart có nhiều CartItem.

Quan hệ:

* Cart: One-to-Many với CartItem.
* CartItem: Many-to-One với Cart.

---

# 13. User Stories

## 13.1. Guest

### US-G01

Là một Guest, tôi muốn xem danh sách sản phẩm để có thể tham khảo các mặt hàng đang bán.

### US-G02

Là một Guest, tôi muốn lọc sản phẩm theo size, màu và giá để tìm sản phẩm phù hợp nhanh hơn.

### US-G03

Là một Guest, tôi muốn đăng ký tài khoản để có thể mua hàng.

---

## 13.2. Customer

### US-C01

Là một Customer, tôi muốn thêm sản phẩm vào giỏ hàng sau khi chọn size và màu để mua đúng sản phẩm tôi cần.

### US-C02

Là một Customer, tôi muốn xem tổng tiền trong giỏ hàng để biết số tiền cần thanh toán.

### US-C03

Là một Customer, tôi muốn đặt hàng và nhập địa chỉ giao hàng để shop xử lý đơn của tôi.

### US-C04

Là một Customer, tôi muốn xem trạng thái đơn hàng để biết đơn đang được xử lý tới đâu.

### US-C05

Là một Customer, tôi muốn hủy đơn khi đơn chưa được xác nhận để thay đổi quyết định mua hàng.

---

## 13.3. Admin

### US-A01

Là một Admin, tôi muốn quản lý danh mục để phân loại sản phẩm rõ ràng.

### US-A02

Là một Admin, tôi muốn thêm và cập nhật sản phẩm để duy trì danh sách sản phẩm bán trên website.

### US-A03

Là một Admin, tôi muốn quản lý tồn kho theo từng size và màu để tránh bán vượt quá số lượng còn lại.

### US-A04

Là một Admin, tôi muốn cập nhật trạng thái đơn hàng để khách hàng biết tiến trình xử lý đơn.

### US-A05

Là một Admin, tôi muốn xem báo cáo doanh thu để theo dõi hiệu quả kinh doanh.

---

# 14. Màn hình dự kiến

## 14.1. Màn hình phía Guest và Customer

* Trang chủ.
* Trang danh sách sản phẩm.
* Trang chi tiết sản phẩm.
* Trang đăng nhập.
* Trang đăng ký.
* Trang giỏ hàng.
* Trang checkout.
* Trang lịch sử đơn hàng.
* Trang chi tiết đơn hàng.
* Trang hồ sơ cá nhân.

## 14.2. Màn hình phía Admin

* Admin Dashboard.
* Quản lý danh mục.
* Thêm / sửa danh mục.
* Quản lý sản phẩm.
* Thêm / sửa sản phẩm.
* Quản lý biến thể sản phẩm.
* Quản lý tồn kho.
* Quản lý đơn hàng.
* Chi tiết đơn hàng.
* Quản lý khách hàng.
* Báo cáo doanh thu.

---

# 15. Yêu cầu giao diện

## 15.1. Phong cách thiết kế

Giao diện của U-Minimalist cần theo phong cách tối giản, hiện đại và dễ sử dụng.

Định hướng thiết kế:

* Màu sắc nhẹ nhàng, ưu tiên trắng, đen, xám, be.
* Bố cục thoáng, ít chi tiết thừa.
* Nút bấm rõ ràng.
* Hình ảnh sản phẩm nổi bật.
* Font chữ dễ đọc.
* Trải nghiệm mua hàng đơn giản.

## 15.2. Responsive

Website cần hiển thị tốt trên:

* Desktop.
* Laptop.
* Tablet.
* Mobile.

## 15.3. Validation giao diện

Các form cần kiểm tra:

* Không để trống trường bắt buộc.
* Email đúng định dạng.
* Số điện thoại hợp lệ.
* Giá sản phẩm lớn hơn 0.
* Số lượng tồn kho không âm.
* Mật khẩu và xác nhận mật khẩu phải trùng nhau.

---

# 16. Công nghệ sử dụng

## 16.1. Backend

* Java 17.
* Spring MVC.
* Spring Security.
* Spring Data JPA.
* Hibernate ORM.

## 16.2. Database

* Microsoft SQL Server.

## 16.3. Frontend

* Thymeleaf.
* Bootstrap 5.
* HTML.
* CSS.
* JavaScript cơ bản.

## 16.4. Tooling

* Maven.
* IntelliJ IDEA hoặc Antigravity IDE.
* GitHub.
* SQL Server Management Studio.

---

# 17. Kiến trúc hệ thống

## 17.1. Mô hình kiến trúc

Hệ thống áp dụng mô hình MVC.

### Model

Bao gồm các Entity đại diện cho bảng trong database:

* User.
* Category.
* Product.
* ProductVariant.
* Cart.
* CartItem.
* Order.
* OrderDetail.
* Payment.

### View

Bao gồm các file giao diện Thymeleaf:

* home.html.
* product-list.html.
* product-detail.html.
* cart.html.
* checkout.html.
* order-history.html.
* admin-dashboard.html.
* admin-product.html.
* admin-order.html.

### Controller

Xử lý request từ người dùng và điều hướng đến View.

Ví dụ:

* AuthController.
* HomeController.
* ProductController.
* CartController.
* OrderController.
* AdminProductController.
* AdminOrderController.
* AdminDashboardController.

### Service

Chứa logic nghiệp vụ.

Ví dụ:

* UserService.
* ProductService.
* CartService.
* OrderService.
* InventoryService.
* PaymentService.
* ReportService.

### Repository

Làm việc với database thông qua Spring Data JPA.

Ví dụ:

* UserRepository.
* ProductRepository.
* CategoryRepository.
* ProductVariantRepository.
* CartRepository.
* OrderRepository.

---

# 18. Ràng buộc hệ thống

* Hệ thống phải chạy được trên Java 17.
* Hệ thống phải kết nối được Microsoft SQL Server.
* Hệ thống phải sử dụng Hibernate ORM.
* Hệ thống phải có phân quyền bằng Spring Security.
* Hệ thống phải có giao diện Thymeleaf.
* Hệ thống phải có ít nhất 3 role: Guest, Customer, Admin.
* Hệ thống phải có dữ liệu mẫu để demo.
* Hệ thống phải có CRUD cho Admin.
* Hệ thống phải có chức năng mua hàng cơ bản.

---

# 19. Giả định

* Dự án được xây dựng phục vụ mục tiêu học tập.
* Số lượng người dùng đồng thời không quá lớn.
* Thanh toán thật chưa bắt buộc trong phiên bản đầu.
* Dữ liệu sản phẩm có thể được nhập thủ công bởi Admin.
* Admin được tạo sẵn trong hệ thống.
* Hệ thống chỉ hỗ trợ một cửa hàng online, chưa hỗ trợ nhiều chi nhánh.

---

# 20. Rủi ro và phương án xử lý

| Rủi ro                        | Mức độ     | Ảnh hưởng                                     | Phương án xử lý                        |
| ----------------------------- | ---------- | --------------------------------------------- | -------------------------------------- |
| Thiết kế database sai quan hệ | Cao        | Khó phát triển chức năng giỏ hàng và đơn hàng | Thiết kế ERD kỹ trước khi code         |
| Quản lý biến thể phức tạp     | Cao        | Sai tồn kho, sai sản phẩm khi đặt hàng        | Tách Product và ProductVariant rõ ràng |
| Trừ tồn kho sai               | Cao        | Bán vượt số lượng còn lại                     | Kiểm tra tồn kho trước khi tạo đơn     |
| Phân quyền lỗi                | Cao        | Customer truy cập được trang Admin            | Cấu hình Spring Security chặt chẽ      |
| Giao diện chưa responsive     | Trung bình | Trải nghiệm người dùng kém                    | Dùng Bootstrap 5                       |
| Thanh toán VNPay khó tích hợp | Trung bình | Chậm tiến độ                                  | Có phương án Mockup Payment thay thế   |
| Thống kê doanh thu sai        | Trung bình | Báo cáo không chính xác                       | Chỉ tính đơn Completed                 |
| Thiếu thời gian kiểm thử      | Trung bình | Dễ phát sinh lỗi demo                         | Ưu tiên test chức năng chính           |

---

# 21. Tiêu chí nghiệm thu tổng thể

Dự án được xem là đạt yêu cầu khi đáp ứng các tiêu chí sau:

## 21.1. Về chức năng

* Guest có thể xem, tìm kiếm và lọc sản phẩm.
* Customer có thể đăng ký, đăng nhập, thêm vào giỏ hàng và đặt hàng.
* Customer có thể xem lịch sử và chi tiết đơn hàng.
* Admin có thể quản lý danh mục.
* Admin có thể quản lý sản phẩm.
* Admin có thể quản lý biến thể và tồn kho.
* Admin có thể quản lý đơn hàng.
* Admin có thể xem dashboard thống kê.
* Hệ thống có kiểm tra tồn kho khi đặt hàng.

## 21.2. Về bảo mật

* Người dùng đăng nhập bằng Spring Security.
* Mật khẩu được mã hóa.
* Customer không truy cập được trang Admin.
* Guest không đặt hàng được.
* Customer không xem được đơn hàng của người khác.

## 21.3. Về dữ liệu

* Dữ liệu sản phẩm, danh mục, đơn hàng được lưu trong SQL Server.
* Hibernate mapping đúng quan hệ.
* Không có tồn kho âm.
* OrderDetail lưu giá tại thời điểm mua.
* Đơn hàng có trạng thái rõ ràng.

## 21.4. Về giao diện

* Giao diện rõ ràng, dễ sử dụng.
* Website responsive.
* Các form có validation.
* Thông báo lỗi và thành công dễ hiểu.

## 21.5. Về kỹ thuật

* Dự án chạy được bằng Maven.
* Code được tổ chức rõ ràng theo MVC.
* Có Repository, Service, Controller.
* Có sử dụng Spring Data JPA.
* Có sử dụng Thymeleaf.
* Có dữ liệu mẫu để demo.

---

# 22. Timeline thực hiện

| Giai đoạn   | Thời gian | Công việc chính                                                 | Kết quả đầu ra                    |
| ----------- | --------- | --------------------------------------------------------------- | --------------------------------- |
| Giai đoạn 1 | Tuần 1–2  | Phân tích yêu cầu, thiết kế ERD, setup project                  | BRD, ERD, cấu trúc Spring MVC     |
| Giai đoạn 2 | Tuần 3–4  | Xây dựng CRUD Admin cho Category, Product, Variant              | Admin quản lý sản phẩm và tồn kho |
| Giai đoạn 3 | Tuần 5    | Xây dựng trang khách hàng, tìm kiếm, lọc sản phẩm               | Trang sản phẩm hoàn chỉnh         |
| Giai đoạn 4 | Tuần 6    | Xây dựng giỏ hàng, đặt hàng, Spring Security                    | Customer mua hàng được            |
| Giai đoạn 5 | Tuần 7    | Tích hợp thanh toán mockup hoặc VNPay Sandbox, quản lý đơn hàng | Quy trình order hoàn chỉnh        |
| Giai đoạn 6 | Tuần 8    | Dashboard, thống kê, kiểm thử, hoàn thiện tài liệu              | Dự án hoàn thiện để demo          |

---

# 23. Ưu tiên chức năng

## 23.1. Must Have

Các chức năng bắt buộc phải có:

* Đăng ký.
* Đăng nhập.
* Phân quyền.
* Xem sản phẩm.
* Tìm kiếm sản phẩm.
* Lọc sản phẩm.
* Xem chi tiết sản phẩm.
* Chọn size và màu.
* Thêm vào giỏ hàng.
* Đặt hàng.
* Quản lý sản phẩm.
* Quản lý danh mục.
* Quản lý biến thể.
* Quản lý tồn kho.
* Quản lý đơn hàng.

## 23.2. Should Have

Các chức năng nên có:

* Dashboard Admin.
* Thống kê doanh thu.
* Hủy đơn hàng.
* Quản lý khách hàng.
* Sản phẩm liên quan.
* Phân trang sản phẩm.

## 23.3. Could Have

Các chức năng có thể thêm nếu còn thời gian:

* VNPay Sandbox.
* Đánh giá sản phẩm.
* Mã giảm giá.
* Wishlist.
* Gửi email xác nhận đơn hàng.

## 23.4. Won’t Have trong phiên bản đầu

Các chức năng chưa làm trong phiên bản đầu:

* Mobile app.
* Chat real-time.
* AI recommendation.
* Quản lý nhiều kho.
* Tích hợp giao hàng thật.
* Thanh toán production.

---

# 24. Kết luận

U-Minimalist là một hệ thống thương mại điện tử thời trang tối giản, phù hợp với yêu cầu của môn HSF301 vì dự án có đầy đủ các thành phần quan trọng của một ứng dụng Java Web sử dụng Spring MVC, Hibernate, Spring Data JPA, Spring Security và Thymeleaf.

Dự án không chỉ tập trung vào giao diện mua sắm mà còn giải quyết bài toán nghiệp vụ quan trọng của thương mại điện tử thời trang: quản lý biến thể sản phẩm theo size và màu sắc, kiểm soát tồn kho, xử lý giỏ hàng, tạo đơn hàng và quản lý trạng thái đơn hàng.

Với phạm vi chức năng rõ ràng, kiến trúc phù hợp và cơ sở dữ liệu có nhiều quan hệ thực tế, U-Minimalist có thể được sử dụng làm một đề tài hoàn chỉnh để triển khai, demo và đánh giá trong môn Java Frameworks.
