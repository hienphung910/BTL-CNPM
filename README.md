# CodeHub

CodeHub là ứng dụng chấm bài lập trình tự động viết bằng Java, giao diện dùng Java Swing. Người dùng nộp mã nguồn, hệ thống biên dịch, chạy trên bộ test rồi trả kết quả AC / WA / TLE / MLE / RE / CE cho từng test.

Đây là bài tập lớn môn Công nghệ phần mềm, Học viện Công nghệ Bưu chính Viễn thông (PTIT).

## Chức năng

- Đăng ký, đăng nhập, xem thông tin tài khoản, đăng xuất. Có 3 vai trò là Sinh viên, Giảng viên và Quản trị viên.
- Hai lớp học là Nhập môn (300 bài) và Lộ trình VNOI (300 bài), có theo dõi tiến độ học.
- Chấm bài bằng 4 ngôn ngữ: Java, C++, Python, JavaScript.
- Xem lịch sử nộp bài và bảng xếp hạng.
- Quản trị viên tạo bài tập và sắp xếp lộ trình ngay trên giao diện.

## Công nghệ

Java 11, Java Swing và MySQL 8 (kết nối bằng JDBC). Chương trình chỉ dùng thư viện chuẩn của JDK, thư viện ngoài duy nhất là MySQL Connector/J. Nếu máy không có MySQL thì chạy với `--no-db`, khi đó dữ liệu lưu trong bộ nhớ.

## Cấu trúc mã nguồn

Mã nguồn chia theo mô hình MVC, mỗi tầng một package:

| Tầng | Thư mục | Chứa gì |
|---|---|---|
| Model | `src/com/ptit/oj/model/` | Người dùng, bài tập, bài nộp, kết quả chấm |
| View | `src/com/ptit/oj/view/` | Các cửa sổ Swing |
| Controller | `src/com/ptit/oj/controller/` | Các lớp xử lí nghiệp vụ, `judge/` là bộ chấm |
| DAO | `src/com/ptit/oj/dao/` | Đọc ghi MySQL, bộ nhớ và thư mục `data/` |

Bốn module trong báo cáo nằm ở các tệp sau:

| Module | `model/` | `dao/` | `controller/` | `view/` |
|---|---|---|---|---|
| 1. Quản lý tài khoản và phân quyền | `User`, `Student`, `Teacher`, `Admin`, `Credentials` | `UserRepository` và hai bản cài đặt | `AuthService`, `PasswordService`, `SessionService` | `account/`, `HomeView` |
| 2. Quản lý bài tập và lộ trình học | `Problem`, `TestCase`, `Topic`, `ProblemDraft` | `ProblemLoader`, `ProblemWriter`, `TopicLoader` | `ProblemAdminService` | `problem/` |
| 3. Nộp bài và chấm tự động | `Submission`, `JudgeResult`, `TestCaseResult`, `Verdict` | | `judge/` | `judge/` |
| 4. Xem lịch sử nộp bài và bảng xếp hạng | | `SubmissionRepository` và hai bản cài đặt | `ScoreboardService` | `history/` |

`JudgeService` dùng chung cho cả bốn module.

## Cách chạy

Cần JDK 11 trở lên. Muốn chấm C++, Python, JavaScript thì cài thêm g++, Python 3, Node.js.

```bash
git clone https://github.com/hienphung910/BTL-CNPM.git
cd BTL-CNPM
```

Biên dịch vào thư mục `out/` (lệnh cho PowerShell):

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java src).FullName
```

Chạy không cần MySQL:

```bash
java "-Dfile.encoding=UTF-8" -cp out com.ptit.oj.Main --no-db
```

Phải chạy từ thư mục gốc của project vì đề bài đọc từ `data/`. Ở chế độ `--no-db`, tài khoản và lịch sử nộp bài sẽ mất khi tắt chương trình.

Lần chạy đầu chương trình tạo 5 tài khoản mẫu: `admin` (quản trị viên), `gvthanh` (giảng viên), `hiennm`, `lananh`, `tuanpv` (sinh viên). Cả năm tài khoản dùng chung một mật khẩu mẫu, chương trình in mật khẩu này ra terminal.

### Chạy với MySQL

1. Sửa mật khẩu `THAY_MAT_KHAU_NAY` trong `database/mysql/01_create_database.sql` rồi chạy file này:

   ```bash
   mysql -u root -p < database/mysql/01_create_database.sql
   ```

2. Chép `.env.example` thành `.env` và điền `OJ_DB_URL`, `OJ_DB_USER`, `OJ_DB_PASSWORD`.

3. Tải MySQL Connector/J vào thư mục `lib/`:

   ```bash
   mvn -q dependency:copy-dependencies -DoutputDirectory=lib -DincludeScope=runtime
   ```

4. Biên dịch như trên rồi chạy:

   ```bash
   java "-Dfile.encoding=UTF-8" -cp "out;lib/*" com.ptit.oj.Main
   ```

Các bảng được tạo tự động khi chương trình khởi động.

## Hạn chế

- Chưa có sandbox, code nộp lên chạy bằng quyền của người dùng đang mở chương trình.
- Trong lúc chấm, cửa sổ Làm bài phải đứng chờ đến khi chấm xong.
- Chưa sửa được đề, chưa xoá bài và chưa đổi mật khẩu qua giao diện.

## Thành viên

hienphung910, longnd1303, tm079, tienkhoi235
